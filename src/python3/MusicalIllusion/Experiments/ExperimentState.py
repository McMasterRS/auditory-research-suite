from PyQt5 import QtCore, QtWidgets
import os
import csv
import datetime
import itertools
import random
from MusicalIllusion.Utilities.GetPath import *
from _version import __version__, __versionDate__


class ExperimentState:
    def __init__(self, parent, data):

        self.config = {"exp_build": __version__, "exp_build_date": __versionDate__}

        self.state = "IDLE"
        self.data = data
        self.parent = parent

        self.warmupTrialCount = 0
        self.blocks = []
        self.blockPrototypes = {}

        self.stateTimer = QtCore.QTimer()
        self.stateTimer.timeout.connect(self.updateState)

        self.resetCounters()
        self.genBlocks()

    def resetCounters(self):
        self.trials = []
        self.currentTrialData = {}

        self.totalTrialCount = -1
        self.currentTrial = -1
        self.currentBlock = 0
        self.totalBlockCount = 0
        self.currentMetablock = 0

        self.instanceCount = {"Audio": 0, "Animation": 0, "AudioAnimation": 0}
        self.repCount = {"Audio": 0, "Animation": 0, "AudioAnimation": 0}
        # Yes, I could just use fancy maths to make this obsolite, but this makes the code far more readable
        self.currentMetablockRepCount = {"Audio": 0, "Animation": 0, "AudioAnimation": 0}

        self.currentBlockTrial = -1
        self.currentMetablockTrial = -1
        self.currentMetablockBlock = 0

    # Generate filenames based on the format strings in properties
    def genFilenames(self, prefix):
        if self.data.properties["loadTrialsFromFolder"] == False:
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

            if len(filenames) == 0:
                msgbox = QtWidgets.QMessageBox.warning(self.parent, "Warning - No files detected for {0} block".format(prefix),
                                                       "Unable to locate any files for the {0} block. Please ensure that the correct data directory is selected and that the properties file contains the relevant file parameters".format(prefix))

            return filenames

        else:
            # Extract filenames by looping through all files in that folder
            dataDir = self.data.dataDir
            path = os.path.join(dataDir, self.data.properties[prefix + "FileSubDirectory"])
            filenames = []

            for filename in os.listdir(path):
                if os.path.splitext(filename)[1][1:] in self.data.properties["{0}FileExtensions".format(prefix)]:
                    filenames.append(os.path.join(path, filename))

            if len(filenames) == 0:
                msgbox = QtWidgets.QMessageBox.warning(self.parent, "Warning - No files detected for {0} block".format(prefix),
                                                       "Unable to locate any files for the {0} block. Please ensure that the correct data directory is selected".format(prefix))
            return filenames

    # Populate the prototype blocks array from the playlist file
    def genBlocksFromPlaylist(self, f):

        blockTrials = {"Audio": [], "Animation": [], "AudioAnimation": []}

        # Loop through all
        with open(f, mode="r", encoding="utf-8-sig") as csvFile:
            blockReader = csv.DictReader(csvFile, delimiter=",")
            for trial in blockReader:
                type = trial["type"]

                # Replace any of the blank rows with None
                trial = {k: None if v == "" else v for k, v in trial.items()}

                # Prefix all filenames with their directories
                if "Audio" in type:
                    trial["audioFile"], e1 = mergePathsVerify([self.data.dataDir, self.data.properties["audioFileSubDirectory"], trial["audioFile"]])
                    trial["audioOffset"] = int(trial["audioOffset"])
                    if not e1:
                        msgbox = QtWidgets.QMessageBox.warning(self.parent, "Warning - Cannot find file",
                                                               "File {0} cannot be found. Please ensure the correct data directory is selected".format(trial["audioFile"]))
                        continue
                if "Animation" in type:
                    trial["visFile"], e2 = mergePathsVerify([self.data.dataDir, self.data.properties["animationFileSubDirectory"], trial["visFile"]])
                    trial["numDots"] = int(trial["numDots"])
                    if not e2:
                        msgbox = QtWidgets.QMessageBox.warning(self.parent, "Warning - Cannot find file",
                                                               "File {0} cannot be found. Please ensure the correct data directory is selected".format(trial["visFile"]))
                        continue

                trial.pop("type")

                blockTrials[type].append(dict(trial))

                for key, block in blockTrials.items():
                    self.blockPrototypes[key] = {"type": key, "data": block, "single": self.data.properties["single{0}Block".format(key)]}

    # Builds a playlist file using the current block prototypes
    def buildPlaylist(self, f):

        rows = []

        # Extract the trials from each prototype
        for block in self.blockPrototypes:
            for trial in self.blockPrototypes[block]["data"]:
                t = trial.copy()

                # Add the trial type and trim the folder from the path
                t["type"] = block
                if t["audioFile"] is not None:
                    t["audioFile"] = os.path.basename(t["audioFile"])
                if t["visFile"] is not None:
                    t["visFile"] = os.path.basename(t["visFile"])

                rows.append(t)

        # Save the rows to CSV
        with open(f, mode="w", encoding="utf-8-sig", newline='') as csvFile:
            writer = csv.DictWriter(csvFile, ["type", "audioFile", "visFile", "audioOffset", "numDots"])
            writer.writeheader()
            writer.writerows(rows)

    # generate blocks
    def genBlocks(self):

        # Playlist settings
        playlistBuilder = False
        if self.data.properties["usePlaylist"]:
            f, exists = mergePathsVerify([self.data.dataDir, self.data.properties["playlistName"] + ".csv"])
            if exists:
                self.genBlocksFromPlaylist(f)
                self.generateBlockList()
                return
            else:
                playlistBuilder = True

        # Load audio block properties
        audioFilenames = self.genFilenames("audio")
        trialBlock = []

        # For every audio file
        for name in audioFilenames:
            # For every audio offset
            for offset in self.data.properties["soundOffsets"]:
                trial = {
                    "audioFile": name,
                    "visFile": None,
                    "audioOffset": offset,
                    "numDots": None
                }

                trialBlock.append(trial)

        self.blockPrototypes["Audio"] = {"type": "Audio", "data": trialBlock, "single": self.data.properties["singleAudioBlock"]}

        # Load animation block properties
        animationFilenames = self.genFilenames("animation")
        trialBlock = []

        # For every animation file
        for name in animationFilenames:
            # For every number of animation points
            for points in self.data.properties["numAnimationPoints"]:
                trial = {
                    "audioFile": None,
                    "visFile": name,
                    "audioOffset": None,
                    "numDots": points
                }

                trialBlock.append(trial)

        self.blockPrototypes["Animation"] = {"type": "Animation", "data": trialBlock, "single": self.data.properties["singleAnimationBlock"]}

        # Load anim/audio block properties
        audioFilenames = self.genFilenames("audio")
        animationFilenames = self.genFilenames("animation")
        trialBlock = []

        # For every audio file
        for name in audioFilenames:
            # For every animation file
            for name2 in animationFilenames:
                # For every sound offset
                for offset in self.data.properties["soundOffsets"]:
                    # For every number of animation points
                    for points in self.data.properties["numAnimationPoints"]:
                        trial = {
                            "audioFile": name,
                            "visFile": name2,
                            "audioOffset": offset,
                            "numDots": points
                        }

                        trialBlock.append(trial)

        self.blockPrototypes["AudioAnimation"] = {"type": "AudioAnimation", "data": trialBlock, "single": self.data.properties["singleAudioAnimationBlock"]}

        if playlistBuilder:
            self.buildPlaylist(f)

        self.generateBlockList()

    # Use the prototypes to generate a list of blocks
    def generateBlockList(self):

        if (self.data.properties["debug"]):
            print("Generating Block List")

        # Generate list of block types in use
        usedBlocks = []
        blockNames = ["Audio", "Animation", "AudioAnimation"]
        for block in blockNames:
            if self.data.properties["include{0}Block".format(block)]:
                # If its single, add it once, otherwise add as many as in blockSetRepetitions
                if self.data.properties["single{0}Block".format(block)]:
                    usedBlocks.append(block)
                else:
                    usedBlocks.extend([block]*self.data.properties["blockSetRepetitions"])

        # Use this list of block types to generate the actual block list
        for block in usedBlocks:
            self.blocks.append(self.blockPrototypes[block].copy())

        self.shuffleBlocks()

    def shuffleBlocks(self):
        if (self.data.properties["randomizeBlocks"]):
            random.shuffle(self.blocks)

        if (self.data.properties["randomizeTrials"]):
            for block in self.blocks:
                random.shuffle(block["data"])

    # Start a new trial
    def newTrial(self):

        self.currentTrial += 1
        self.totalTrialCount += 1

        self.currentBlockTrial += 1
        self.currentMetablockTrial += 1

        # if we've finished the current block, move to the next one
        if self.currentTrial == len(self.blocks[self.currentBlock]["data"]):

            # Always incriment the rep number and reset the current trial
            self.repCount[self.blocks[self.currentBlock]["type"]] += 1
            self.currentMetablockRepCount[self.blocks[self.currentBlock]["type"]] += 1
            self.currentTrial = 0

            newBlock = False

            # If we're in a single block
            if self.data.properties["single{0}Block".format(self.blocks[self.currentBlock]["type"])]:
                # If we have repetitions left
                if self.currentMetablockRepCount[self.blocks[self.currentBlock]["type"]] < self.data.properties["blockSetRepetitions"]:
                    # If FullRandom is enabled, randomize
                    if self.data.properties["single{0}FullRandom".format(self.blocks[self.currentBlock]["type"])]:
                        random.shuffle(self.blocks[self.currentBlock]["data"])

                    if (self.data.properties["debug"]):
                        print("Moving to next repetition of single block")

                # If we dont have repetitions left, move to the next block
                else:
                    newBlock = True

            # If we're not in a single block, move to the next block
            else:
                newBlock = True

            # Updates counters for the new block
            if newBlock:
                self.instanceCount[self.blocks[self.currentBlock]["type"]] += 1
                self.currentBlock += 1
                self.currentMetablockBlock += 1
                self.totalBlockCount += 1
                self.currentBlockTrial = 0

                if (self.data.properties["debug"]):
                    print("New block")

            # If we've finished the current metablock, move to the next
            if self.currentBlock == len(self.blocks):

                self.currentMetablockTrial = 0
                self.currentMetablockBlock = 0
                self.currentMetablockRepCount = {"Audio": 0, "Animation": 0, "AudioAnimation": 0}
                self.currentMetablock += 1

                # Shuffle the trials if required
                self.shuffleBlocks()

                if (self.data.properties["debug"]):
                    print("New Metablock")

                # If we've finished all the metablocks, end the experiment
                if self.currentMetablock == self.data.properties["metablocks"]:

                    self.state = "FINISH"

                    if (self.data.properties["debug"]):
                        print("Finishing")

                    self.updateState()
                    return

        self.currentTrialData = {"exp_id": self.data.properties["experimentID"],
                                 "sub_exp_id": self.data.properties["subExperimentID"],
                                 "exp_build": self.config["exp_build"],
                                 "exp_build_date": self.config["exp_build_date"],
                                 "ra_id": self.data.RAID,
                                 "subject": self.data.subjID,
                                 "session": self.data.sessID,
                                 "trial_num": self.totalTrialCount + 1,
                                 "block_num": self.totalBlockCount + 1,
                                 "metablock_num": self.currentMetablock + 1,
                                 "block_instance": self.instanceCount[self.blocks[self.currentBlock]["type"]] + 1,
                                 "repetition_num": self.repCount[self.blocks[self.currentBlock]["type"]] + 1,
                                 "trial_in_metablock": self.currentMetablockTrial + 1,
                                 "block_in_metablock": self.currentMetablockBlock + 1,
                                 "trial_in_block": self.currentBlockTrial + 1,
                                 "time_stamp": datetime.datetime.now().strftime("%b %d, %Y; %I:%M:%S %p")
                                 }

        currentData = self.blocks[self.currentBlock]["data"][self.currentTrial]
        self.currentTrialData.update(currentData)
        self.parent.vis.setData(currentData)

        if (self.data.properties["debug"]):
            print("")
            print("##########################################")
            print("")
            print("Total trials: " + str(self.totalTrialCount+1))
            print("Trial in block: " + str(self.currentBlockTrial+1))
            print("Current block: " + str(self.currentBlock+1))
            print("Current metablock: " + str(self.currentMetablock+1))
            print("Block Instance: " + str(self.instanceCount[self.blocks[self.currentBlock]["type"]] + 1))
            print("Repetition Number: " + str(self.repCount[self.blocks[self.currentBlock]["type"]] + 1))
            print("")
            print("Vis file: " + str(currentData["visFile"]))
            print("Aud file: " + str(currentData["audioFile"]))
            print("Audio Offset: " + str(currentData["audioOffset"]))
            print("")

    # Set the current trial's responses
    def setResponseValues(self, response, conf):
        print("Warning: Using default setResponseValues")
        return

    # Update the experiment state
    def updateState(self):
        print("Warning: Using default updateState")
        return

    # Save the experiment data to file
    def saveCSV(self):
        print("Warning: Using default saveCSV")
        return
