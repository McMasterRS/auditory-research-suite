import os, datetime, itertools
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
        self.currentTrialCount = 0
        self.currentBlock = 0
        self.currentMetablock = 0

        # For book keeping purposes
        self.currentMetablockTrial = 0
        self.currentMetablockBlock = 0
        self.currentBlockTrial = 0

        self.stateTimer = QtCore.QTimer()
        self.stateTimer.timeout.connect(self.updateState)
    
    def startExperiment(self, data):

        self.data = data
        self.setupUI()
        self.newTrial()
    
    ###########################################
    #                    UI                   #
    ###########################################

    # setup UI settings that arent pre-defined
    def setupUI(self):
        
        # Setup UI
        uic.loadUi(getGui('TOJExperiment.ui'), self)

        # Plot
        self.vis = Visualizer(self.plot, self)

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
        if self.state == "IDLE":
            self.currentPage += dir
            self.swPages.setCurrentIndex(self.currentPage)

            self.setButtons(True)
            if self.currentPage == 0:
                self.btPrev.setEnabled(False)
            elif self.currentPage == 2:
                self.resetTrialUI()
                self.updateState()
        else:
            self.updateState()
    
    # Reset the UI for a new trial run
    def resetTrialUI(self):
        self.setButtons(False)
        self.rbAnswerPositive.setChecked(False)
        self.rbAnswerNegative.setChecked(False)
        for rb in self.confidenceButtons:
            rb.setChecked(False)

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
        print(filenames)
        return filenames

    # generate blocks
    def genBlocks(self):
        if self.data.properties["includeAudioAnimationBlock"]:
            audioFilenames = self.genFilenames("audio")
            animationFilenames = self.genFilenames("animation")

            self.vis.setData(animationFilenames[0], self.data.properties["numAnimationPoints"], self.data.properties["animationPointSize"])
           

    # Start a new trial
    def newTrial(self):
        currentTrial = {"exp_id" : self.data.properties["experimentID"],
                        "sub_exp_id" : self.data.properties["subExperimentID"],
                        "exp_build" : self.config["exp_build"],
                        "exp_build_date" : self.config["exp_build_date"],
                        "ra_id" : self.data.RAID,
                        "subject" : self.data.subjID,
                        "session" : self.data.sessID,
                        "trial_num" : len(self.trials) + 1,
                        "block_num" : self.currentBlock + 1,
                        "metablock_num" : self.currentMetablock + 1,
                        "block_instance" : 0, # FIXME
                        "repetition_num" : 0, # FIXME
                        "trial_in_metablock" : self.currentMetablockTrial + 1,
                        "block_in_metablock" : self.currentMetablockBlock + 1,
                        "trial_in_block" : self.currentBlockTrial + 1,
                        "time_stamp" : datetime.datetime.now().strftime("%b %d, %Y; %I:%M:%S %p")
        }

        self.genBlocks()

    def updateState(self):
        # Not yet started
        if (self.state == "IDLE"):
            self.state = "WARMUP_DELAY"
            self.updateState()

        # Pre-warmup trial delay
        elif (self.state == "WARMUP_DELAY"):
            self.lbStatus.setText(self.data.properties["warmupDelayText"])
            self.state = "WARMUP_TRIAL"    
            self.stateTimer.start(self.data.properties["preStimulusSilence"])
 
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
            self.lbStatus.setText(self.data.properties["enterResponseText"])
            self.stateTimer.stop()
            self.btNext.setEnabled(True)
            
            # Start the real trials if the warmup is complete
            self.warmupTrialCount += 1
            if self.warmupTrialCount == self.data.properties["numWarmupTrials"]:
                self.state = "TRIAL_FIRST_DELAY"
            else:
                self.state = "WARMUP_DELAY"

        # Sets text for first trial
        elif(self.state == "TRIAL_FIRST_DELAY"):
            self.lbStatus.setText(self.data.properties["firstDelayText"])
            self.state = "TRIAL"
            self.stateTimer.start(self.data.properties["preStimulusSilence"])

        # Trial delay
        elif(self.state == "TRIAL_DELAY"):
            self.lbStatus.setText(self.data.properties["trialDelayText"])
            self.state = "TRIAL"
            self.stateTimer.start(self.data.properties["preStimulusSilence"])

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
            self.lbStatus.setText(self.data.properties["enterResponseText"])
            self.stateTimer.stop()
            self.btNext.setEnabled(True)
            self.state = "TRIAL_DELAY"
   