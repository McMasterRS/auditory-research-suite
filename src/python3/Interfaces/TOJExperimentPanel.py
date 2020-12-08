import os, datetime, itertools, random
from PyQt5 import QtWidgets, QtGui, uic, QtCore
from Utilities.GetPath import *

from Interfaces.ExperimentSetup import ExperimentSetup
from Visualizer.Visualizer import Visualizer

EXP_BUILD = 0
EXP_BUILD_DATE = 0

class TOJExperimentPanel(QtWidgets.QWidget):
    def __init__(self):
        super(QtWidgets.QWidget, self).__init__()

        self.setup = ExperimentSetup(self)
        self.config = {"exp_build" : EXP_BUILD, "exp_build_date" : EXP_BUILD_DATE}

        self.data = []
        self.currentPage = 0

        self.state = "IDLE"

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
    
    def startExperiment(self, data):

        self.data = data
        self.setupUI()
        self.genBlocks()
    
    ###########################################
    #                    UI                   #
    ###########################################

    # setup UI settings that arent pre-defined
    def setupUI(self):
        
        # Setup UI
        uic.loadUi(getGui('TOJExperiment.ui'), self)

        # Plot
        self.vis = Visualizer(self.plot, self, self.data.properties["animationPointSize"], self.data.properties["playbackGain"], self.data.properties["connectDots"])
        self.vis.data.defaultOffset = self.data.properties["audioCallAhead"]

        # Navigation buttons
        self.btNext.clicked.connect(lambda: self.movePage(1))
        self.btPrev.clicked.connect(lambda: self.movePage(-1))

        # Question box + radio buttons
        self.gbQuestion.setTitle(self.data.properties["question.label"])
        self.rbAnswerPositive.setText(self.data.properties["answerPositive.label"])
        self.rbAnswerNegative.setText(self.data.properties["answerNegative.label"])
        # Hotkeys for question answers
        QtWidgets.QShortcut(self.data.properties["answerPositive.hotkey"].strip(), self).activated.connect(lambda: self.rbAnswerNegative.toggleRadioButton(not self.rbAnswerPositive))
        QtWidgets.QShortcut(self.data.properties["answerNegative.hotkey"].strip(), self).activated.connect(lambda: self.rbAnswerNegative.toggleRadioButton(not self.rbAnswerNegative))

        # Array of the confidence radio buttons for convenience later
        self.confidenceButtons = [self.rbConf1, self.rbConf2, self.rbConf3, self.rbConf4, self.rbConf5]
        # Set confidence button labels and hotkeys
        confMin = self.data.properties["confidenceMin"]
        for i, button in enumerate(self.confidenceButtons):
            button.setText(self.data.properties["confidence." + str(i + confMin)])
            QtWidgets.QShortcut(self.data.properties["confidence." + str(i + confMin) + ".hotkey"].strip(), self).activated.connect(lambda arg = button: arg.setChecked(not arg.isChecked()))

        # Populate the text boxes with data
        self.setText("introScreen", "preWarmup", "warmupScreenTrial", "completion")
        self.show()

    # Fills the text boxes on the pages
    def setText(self, introName, prepName, trialName, completeName):
        self.lbIntroTitle.setText(self.data.properties[introName + "Title"])
        self.tbIntroText.setText(self.data.properties[introName + "Text"])
        self.lbPrepTitle.setText(self.data.properties[prepName + "Title"])
        self.tbPrepText.setText(self.data.properties[prepName + "Text"])
        self.lbTestTitle.setText(self.data.properties[trialName + "Title"])
        self.lbTestText.setText(self.data.properties[trialName + "Text"])
        self.lbCompleteTitle.setText(self.data.properties[completeName + "Title"])
        self.tbCompleteText.setText(self.data.properties[completeName + "Text"])

    # Toggle the button enabled states
    def setButtons(self, state):
        self.btNext.setEnabled(state)
        self.btPrev.setEnabled(state)

    # Move between the instruction pages
    def movePage(self, dir):
        # Moving between text
        if self.state == "IDLE":
            self.currentPage += dir
            self.swPages.setCurrentIndex(self.currentPage)

            self.setButtons(True)
            if self.currentPage == 0:
                self.btPrev.setEnabled(False)
            elif self.currentPage == 2:
                self.resetTrialUI()
                self.updateState()

        # Moving to info after warmup trials
        elif self.state == "TRIAL_FIRST_DELAY":
            if self.currentPage != 1:
                self.currentPage = 1
            else:
                self.currentPage = 2
                self.resetTrialUI()
                self.updateState()

            self.swPages.setCurrentIndex(self.currentPage)

        # Moving between trials
        else:
            self.getResponse()
    
    # Reset the UI for a new trial run
    def resetTrialUI(self):
        self.setButtons(False)

        self.rbAnswerPositive.setAutoExclusive(False)
        self.rbAnswerPositive.setChecked(False)
        self.rbAnswerPositive.setAutoExclusive(True)

        self.rbAnswerNegative.setAutoExclusive(False)
        self.rbAnswerNegative.setChecked(False)
        self.rbAnswerNegative.setAutoExclusive(True)

        for rb in self.confidenceButtons:
            rb.setAutoExclusive(False)
            rb.setChecked(False)
            rb.setAutoExclusive(True)

        self.gbQuestion.setEnabled(False)
        self.howConfident.setEnabled(False)

    ###########################################
    #               Experiment                #
    ###########################################

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
            
    def getResponse(self):
            # Make sure a response is selected
            if self.rbAnswerPositive.isChecked():
                self.currentTrial["subjResponse"] = self.rbAnswerPositive.text()
            elif self.rbAnswerNegative.isChecked():
                self.currentTrial["subjResponse"] = self.rbAnswerNegative.text()
            else:
                msgbox = QtWidgets.QMessageBox.critical(self, "Error", "Please select a response")
                return

            # Make sure a confidence is selected
            for i, button in enumerate(self.confidenceButtons):
                if button.isChecked():
                    # Set the confidence level, append the trial and reset
                    self.currentTrial["confidence"] = self.data.properties["confidenceMin"] + i
                    self.trials.append(self.currentTrial)
                    self.resetTrialUI()
                    self.updateState()
                    return

            msgbox = QtWidgets.QMessageBox.critical(self, "Error", "Please select a confidence level")
            return
    
    # Start a new trial
    def newTrial(self):

        self.currentBlockTrial += 1
        self.totalTrialCount += 1
        self.currentMetablockTrial += 1

        # if we've finished the current block, move to the next one
        if self.currentBlockTrial > len(self.blocks[self.currentBlock]["data"]):
            self.currentBlock += 1
            self.currentBlockTrial = 0
            self.blocks[self.currentBlock]["reps"] += 1
            
            # If we've finished the current metablock, repeat if needed but otherwise move to next metablock
            if self.currentBlock > len(self.blocks):
                
                self.currentRepetition += 1
                if self.currentRepetition >= self.data.properties["blockSetRepetitions"]:
                    self.currentRepetition = 0
                    self.currentMetablockTrial = 0
                    self.currentMetablock += 1
                
                    # If we've finished all the metablocks, end the experiment
                    if self.currentMetablock >= self.data.properties["metablocks"]:

                        self.state = "FINISH"
                        self.updateState()
                        return

                # TODO - re-scramble all data in blocks

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
        self.vis.setData(currentData)

    # Update the state of the experiment
    def updateState(self):
        print(self.state)
        # Not yet started
        if (self.state == "IDLE"):
            self.state = "WARMUP_DELAY"
            self.updateState()

        # Pre-warmup trial delay
        elif (self.state == "WARMUP_DELAY"):
            self.lbStatus.setText(self.data.properties["warmupDelayText"])
            self.state = "WARMUP_TRIAL"    
            self.stateTimer.start(self.data.properties["preStimulusSilence"])
            self.currentBlockTrial = random.randint(0, len(self.blocks[self.currentBlock]["data"]) - 1)
            self.newTrial()
 
        # Warmup trial
        elif(self.state == "WARMUP_TRIAL"):
            self.lbStatus.setText(self.data.properties["duringTrialText"])
            self.state = "WARMUP_PAUSE"
            self.stateTimer.stop()
            self.vis.play()

        # Pause after trial
        elif(self.state == "WARMUP_PAUSE"):
            self.state = "WARMUP_RESPOND"
            self.stateTimer.start(self.data.properties["postStimulusSilence"])

        # Waiting for user response
        elif(self.state == "WARMUP_RESPOND"):
            self.vis.clearPlot()
            self.lbStatus.setText(self.data.properties["enterResponseText"])
            self.stateTimer.stop()
            self.btNext.setEnabled(True)
            self.gbQuestion.setEnabled(True)
            self.howConfident.setEnabled(True)
            
            # Start the real trials if the warmup is complete
            self.warmupTrialCount += 1
            if self.warmupTrialCount == self.data.properties["numWarmupTrials"]:
                self.setText("introScreen", "preTrial", "testScreenTrial", "completion")
                self.state = "TRIAL_FIRST_DELAY"
            else:
                self.state = "WARMUP_DELAY"

        # Sets text for first trial
        elif(self.state == "TRIAL_FIRST_DELAY"):
            self.lbStatus.setText(self.data.properties["firstDelayText"])
            self.state = "TRIAL"
            self.stateTimer.start(self.data.properties["preStimulusSilence"])
            self.currentBlockTrial = 0
            self.totalTrialCount = 0
            self.newTrial()

        # Trial delay
        elif(self.state == "TRIAL_DELAY"):
            self.lbStatus.setText(self.data.properties["trialDelayText"])
            self.state = "TRIAL"
            self.stateTimer.start(self.data.properties["preStimulusSilence"])
            self.newTrial()

        # Run trial
        elif(self.state == "TRIAL"):
            self.lbStatus.setText(self.data.properties["duringTrialText"])
            self.state = "PAUSE"
            self.stateTimer.stop()
            self.vis.play()

        # Pause after trial
        elif(self.state == "PAUSE"):
            self.state = "RESPOND"
            self.stateTimer.start(self.data.properties["postStimulusSilence"])

        # Waiting for user response
        elif(self.state == "RESPOND"):
            self.vis.clearPlot()
            self.lbStatus.setText(self.data.properties["enterResponseText"])
            self.stateTimer.stop()
            self.btNext.setEnabled(True)
            self.gbQuestion.setEnabled(True)
            self.howConfident.setEnabled(True)
            self.state = "TRIAL_DELAY"

        # Finish the experiment
        elif(self.state == "FINISH"):
            self.swPages.setCurrentIndex(3)
            for trial in self.trials:
                for key, value in trial.items():
                    print("{0}: {1}".format(key, value))
                print("")
   