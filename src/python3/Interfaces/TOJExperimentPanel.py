import os, datetime
from PyQt5 import QtWidgets, QtGui, uic, QtCore
from Utilities.GetPath import *

from Interfaces.ExperimentSetup import ExperimentSetup

EXP_BUILD = 0
EXP_BUILD_DATE = 0

class TOJExperimentPanel(QtWidgets.QWidget):
    def __init__(self):
        super(QtWidgets.QWidget, self).__init__()

        self.setup = ExperimentSetup(self)
        self.config = {"exp_build" : EXP_BUILD, "exp_build_date" : EXP_BUILD_DATE}

        self.data = []
        self.currentPage = 0

        self.trials = []
        self.currentTrial = []
        self.currentBlock = 0
        self.currentMetablock = 0

        # For book keeping purposes
        self.currentMetablockTrial = 0
        self.currentMetablockBlock = 0
        self.currentBlockTrial = 0
    
    def startExperiment(self, data):

        self.data = data
        
        # Setup UI
        uic.loadUi(getGui('TOJExperiment.ui'), self)
        self.btNext.clicked.connect(lambda: self.movePage(1))
        self.btPrev.clicked.connect(lambda: self.movePage(-1))
        # Array of the confidence radio buttons for convenience later
        self.confidenceButtons = [self.rbConf1, self.rbConf2, self.rbConf3, self.rbConf4, self.rbConf5]
        # Populate the text boxes with data
        self.setText("introScreen", "preWarmup", "warmupScreenTrial")
        self.newTrial()
        self.show()

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


    # Fills the text boxes on the pages
    def setText(self, introName, prepName, trialName):
        self.lbIntroTitle.setText(self.data.properties[introName + "Title"])
        self.tbIntroText.setText(self.data.properties[introName + "Text"])
        self.lbPrepTitle.setText(self.data.properties[prepName + "Title"])
        self.tbPrepText.setText(self.data.properties[prepName + "Text"])
        self.lbTestTitle.setText(self.data.properties[trialName + "Title"])
        self.lbTestText.setText(self.data.properties[trialName + "Text"])

    # Toggle the button enabled states
    def setButtons(self, state):
        self.btNext.setEnabled(state)
        self.btPrev.setEnabled(state)

    # Move between the instruction pages
    def movePage(self, dir):

        self.currentPage += dir
        self.swPages.setCurrentIndex(self.currentPage)

        self.setButtons(True)
        if self.currentPage == 0:
            self.btPrev.setEnabled(False)
        elif self.currentPage == 2:
            self.resetTrial()
            
    # Reset the UI for a new trial run
    def resetTrial(self):
        self.setButtons(False)
        self.rbDot.setChecked(False)
        self.rbTone.setChecked(False)
        for rb in self.confidenceButtons:
            rb.setChecked(False)
