from PyQt5 import QtGui, QtCore
import os, datetime, itertools, random, csv
from Utilities.GetPath import *
import numpy as np

EXP_BUILD = 0
EXP_BUILD_DATE = 0

class TOJState:
    def __init__(self, parent, data):

        self.config = {"exp_build" : EXP_BUILD, "exp_build_date" : EXP_BUILD_DATE}

        self.state = "IDLE"
        self.data = data
        self.parent = parent

        self.warmupTrialCount = 0

        self.trials = []
        self.currentTrial = []
        self.totalTrialCount = 0
        self.blocks = []
        self.currentBlock = 0
        self.totalBlockCount = 0
        self.currentMetablock = 0
        
        self.currentMetablockTrial = 0
        self.currentMetablockBlock = 0
        self.currentBlockTrial = 0
        self.currentRepetition = 0

        self.stateTimer = QtCore.QTimer()
        self.stateTimer.timeout.connect(self.updateState)

        self.genBlocks()

    # Generate filenames based on the format strings in properties
    def genFilenames(self, prefix):
         # Extract values for the parameters and convert any strings into single-item lists
        nameParams = [self.data.properties[param] for param in self.data.properties[prefix + "Params"]]
        nameParams = [[p] if isinstance(p, str) else p for p in nameParams]

        # Replace the string literal with numerical literals
        # e.g. "{var1}{var2}{var3}" is converted into "{0}{1}{2}"
        nameFormat = self.data.properties[prefix + "FileFormat"].replace("$", "")
        for i, param in enumerate(self.data.properties[prefix + "Params"]):
            nameFormat = nameFormat.replace(param, str(i))

        # Generate all combinations of the parameters
        paramCombinations = list(itertools.product(*nameParams))

        filenames = []
        # Apply params to filename
        for params in paramCombinations:
            # Loop through file extensions and use the first that is found to exist
            for ext in self.data.properties[prefix + "FileExtensions"]:
                filename, exists = mergePathsVerify([self.data.dataDir, self.data.properties[prefix + "FileSubDirectory"], nameFormat.format(*params) + "." + ext])
                if exists:
                    filenames.append(filename)
                    break
        return filenames

    # generate blocks
    def genBlocks(self):

        blockCount = 0

        # Load audio block properties
        if self.data.properties["includeAudioBlock"]:
            audioFilenames = self.genFilenames("audio")
            trialBlock = []

            for name in audioFilenames:
                for offset in self.data.properties["soundOffsets"]:
                    trial = {
                        "audioFile" : name,
                        "visFile" : None,
                        "audioOffset" : offset,
                        "numDots" : None
                    }

                    trialBlock.append(trial)

            blockDict = {"ID" : blockCount, "data" : trialBlock, "reps" : 1}

            self.blocks.append(blockDict)
            blockCount += 1

        # Load animation block properties
        if self.data.properties["includeAnimationBlock"]:
            animationFilenames = self.genFilenames("animation")
            trialBlock = []
            
            for name in animationFilenames:
                for points in self.data.properties["numAnimationPoints"]:
                    trial = {
                        "audioFile" : None,
                        "visFile" : name,
                        "audioOffset" : None,
                        "numDots" : points
                    }

                    trialBlock.append(trial)

            blockDict = {"ID" : blockCount, "data" : trialBlock, "reps" : 1}

            self.blocks.append(blockDict)
            blockCount += 1

        # Load anim/audio block properties
        if self.data.properties["includeAudioAnimationBlock"]:
            audioFilenames = self.genFilenames("audio")
            animationFilenames = self.genFilenames("animation")
            trialBlock = []

            for name in audioFilenames:
                for name2 in animationFilenames:
                    for offset in self.data.properties["soundOffsets"]:
                        for points in self.data.properties["numAnimationPoints"]:
                            trial = {
                                "audioFile" : name, 
                                "visFile" : name2,
                                "audioOffset" : offset,
                                "numDots" : points
                            }

                            trialBlock.append(trial)

            blockDict = {"ID" : blockCount, "data" : trialBlock, "reps" : 1}

            self.blocks.append(blockDict)

        # Randomize blocks
        if self.data.properties["randomizeBlocks"]:
            random.shuffle(self.blocks)

        # Randomize trials
        if self.data.properties["randomizeTrials"]:
            for block in self.blocks:
                random.shuffle(block["data"])

    # Set the responses that the subject provides
    def setResponseValues(self, response, conf):
        self.currentTrial["subjResponse"] = response
        self.currentTrial["confidence"] = conf
        self.trials.append(self.currentTrial)

        # Convert the response into a positive or negative offset
        if response == self.data.properties["answerPositive.label"]:
            answerDir = 1
        else:
            answerDir = -1

        # Compare the response with the actual offset
        if (np.sign(self.currentTrial["audioOffset"]) == answerDir):
            self.currentTrial["responseCorrect"] = True
        else:
            self.currentTrial["responseCorrect"] = False

        if (self.data.properties["debug"]):
            print("Response: " + str(response))
            print("Confidence: " + str(conf))
            print("")

    # Start a new trial
    def newTrial(self):

        self.currentBlockTrial += 1
        self.totalTrialCount += 1
        self.currentMetablockTrial += 1

        # if we've finished the current block, move to the next one
        if self.currentBlockTrial >= len(self.blocks[self.currentBlock]["data"]):
            self.currentBlock += 1
            self.currentBlockTrial = 0

            if (self.data.properties["debug"]):
                print("New block")
            
            # If we've finished the current metablock, repeat if needed but otherwise move to next metablock
            if self.currentBlock >= len(self.blocks):
                
                self.currentRepetition += 1

                if (self.data.properties["debug"]):
                    print("New Repetition")

                if self.currentRepetition >= self.data.properties["blockSetRepetitions"]:
                    self.currentRepetition = 0
                    self.currentMetablockTrial = 0
                    self.currentMetablock += 1

                    if (self.data.properties["debug"]):
                        print("New Metablock")
                
                    # If we've finished all the metablocks, end the experiment
                    if self.currentMetablock >= self.data.properties["metablocks"]:

                        self.state = "FINISH"

                        if (self.data.properties["debug"]):
                            print("Finishing")

                        self.updateState()
                        return

                # TODO - re-scramble all data in blocks

            # If not at end of blocks
            else:
                self.blocks[self.currentBlock]["reps"] += 1

        self.currentTrial = {"exp_id" : self.data.properties["experimentID"],
                            "sub_exp_id" : self.data.properties["subExperimentID"],
                            "exp_build" : self.config["exp_build"],
                            "exp_build_date" : self.config["exp_build_date"],
                            "ra_id" : self.data.RAID,
                            "subject" : self.data.subjID,
                            "session" : self.data.sessID,
                            "trial_num" : self.totalTrialCount + 1,
                            "block_num" : self.blocks[self.currentBlock]["ID"],
                            "metablock_num" : self.currentMetablock + 1,
                            "block_instance" : self.blocks[self.currentBlock]["reps"],
                            "repetition_num" : 0, # FIXME
                            "trial_in_metablock" : self.currentMetablockTrial + 1,
                            "block_in_metablock" : self.currentMetablockBlock + 1,
                            "trial_in_block" : self.currentBlockTrial + 1,
                            "time_stamp" : datetime.datetime.now().strftime("%b %d, %Y; %I:%M:%S %p")
        }

        currentData = self.blocks[self.currentBlock]["data"][self.currentBlockTrial]
        self.currentTrial.update(currentData)
        self.parent.vis.setData(currentData)

        if (self.data.properties["debug"]):
            print("")
            print("##########################################")
            print("")
            print("Total trials: " + str(self.totalTrialCount))
            print("Trial in block: " + str(self.currentBlockTrial))
            print("Current block: " + str(self.currentBlock))
            print("Current metablock: " + str(self.currentMetablock))
            print("")
            print("Vis file: " + str(currentData["visFile"]))
            print("Aud file: " + str(currentData["audioFile"]))
            print("Audio Offset: " + str(currentData["audioOffset"]))
            print("")

    # Update the state of the experiment
    def updateState(self):

        if (self.data.properties["debug"]):
            print("Current State: " + self.state)

        # Update the interface first
        self.parent.updateState(self.state)

        # Then update the experiment based on the current state

        # Not yet started
        if (self.state == "IDLE"):
            self.state = "WARMUP_DELAY"
            self.updateState()

        # Pre-warmup trial delay
        elif (self.state == "WARMUP_DELAY"):
            self.state = "WARMUP_TRIAL"    
            self.stateTimer.start(self.data.properties["preStimulusSilence"])
            self.currentBlockTrial = random.randint(0, len(self.blocks[self.currentBlock]["data"]) - 1)
            self.newTrial()
 
        # Warmup trial
        elif(self.state == "WARMUP_TRIAL"):
            self.state = "WARMUP_PAUSE"
            self.stateTimer.stop()

        # Pause after trial
        elif(self.state == "WARMUP_PAUSE"):
            self.state = "WARMUP_RESPOND"
            self.stateTimer.start(self.data.properties["postStimulusSilence"])

        # Waiting for user response
        elif(self.state == "WARMUP_RESPOND"):
            self.stateTimer.stop()
            
            # Start the real trials if the warmup is complete
            self.warmupTrialCount += 1
            if self.warmupTrialCount == self.data.properties["numWarmupTrials"]:
                self.state = "TRIAL_FIRST_DELAY"
            else:
                self.state = "WARMUP_DELAY"

        # Sets text for first trial
        elif(self.state == "TRIAL_FIRST_DELAY"):
            self.state = "TRIAL"
            self.stateTimer.start(self.data.properties["preStimulusSilence"])
            # Set to -1 because it iterates to 0 on the first run of newTrial
            self.currentBlockTrial = -1
            self.totalTrialCount = -1
            self.totalMetablockCount = -1
            self.newTrial()

        # Trial delay
        elif(self.state == "TRIAL_DELAY"):
            self.state = "TRIAL"
            self.stateTimer.start(self.data.properties["preStimulusSilence"])
            self.newTrial()

        # Run trial
        elif(self.state == "TRIAL"):
            self.state = "PAUSE"
            self.stateTimer.stop()

        # Pause after trial
        elif(self.state == "PAUSE"):
            self.state = "RESPOND"
            self.stateTimer.start(self.data.properties["postStimulusSilence"])

        # Waiting for user response
        elif(self.state == "RESPOND"):
            self.stateTimer.stop()
            self.state = "TRIAL_DELAY"

        # Finish the experiment
        elif(self.state == "FINISH"):
            self.stateTimer.stop()
            self.saveCSV()
            
            if (self.data.properties["debug"]):
                for trial in self.trials:
                    for key, value in trial.items():
                        print("{0}: {1}".format(key, value))

                print("")

    def saveCSV(self):
        with open("test.csv", "w", newline="") as csvfile:
            csvWriter = csv.writer(csvfile, delimiter = ',')
            # headers
            csvWriter.writerow(["exp_id", "sub_exp_id", "exp_build",
                                "exp_build_date", "ra_id", "subject",
                                "session", "trial_num", "block_num",
                                "metablock_num", "block_instance", 
                                "repetition_num", "trial_in_metablock",
                                "block_in_metablock", "trial_in_block",
                                "time_stamp", "audioFile", "visFile",
                                "audioOffset", "numDots", "animationStart",
                                "aniStrikeDelay", "audioStart", "audioToneDelay",
                                "confidence", "subjResponse", "responseCorrect"
                                ])
            # rows
            for trial in self.trials:
                csvWriter.writerow([
                    trial["exp_id"],
                    trial["sub_exp_id"],
                    trial["exp_build"],
                    trial["exp_build_date"],
                    trial["ra_id"],
                    trial["subject"],
                    trial["session"],
                    trial["trial_num"],
                    trial["block_num"],
                    trial["metablock_num"],
                    trial["block_instance"],
                    trial["repetition_num"],
                    trial["trial_in_metablock"],
                    trial["block_in_metablock"],
                    trial["trial_in_block"],
                    trial["time_stamp"],
                    os.path.basename(trial["audioFile"]),
                    os.path.basename(trial["visFile"]),
                    trial["audioOffset"],
                    trial["numDots"],
                    0,
                    0,
                    0,
                    0,
                    trial["confidence"],
                    trial["subjResponse"],
                    trial["responseCorrect"]
                ])
