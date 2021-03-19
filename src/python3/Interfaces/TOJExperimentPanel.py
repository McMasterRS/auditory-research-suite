import os
import operator
import datetime
import itertools
import random
from PyQt5 import QtWidgets, QtGui, uic, QtCore
from Utilities.GetPath import *

from Interfaces.ExperimentSetup import ExperimentSetup
from Visualizer.Visualizer import Visualizer
from Experiments.TOJState import TOJState


class TOJExperimentPanel(QtWidgets.QWidget):
    def __init__(self, parent):
        super(QtWidgets.QWidget, self).__init__()

        self.parent = parent

        self.state = []

        self.data = []
        self.currentPage = 0

    def startExperiment(self, data):

        self.data = data
        self.state = TOJState(self, data)
        self.setupUI()

    def getState(self):
        return(self.state.state)

    def closeEvent(self, *args, **kwargs):
        self.parent.close()

    ###########################################
    #                    UI                   #
    ###########################################

    # setup UI settings that arent pre-defined
    def setupUI(self):

        # Load UI file
        uic.loadUi(getGui('TOJExperiment.ui'), self)
        self.show()

        if self.data.properties["fullScreen"] == True:
            self.showFullScreen()
        else:
            # Have to move to the test page to get an accurate size
            self.swPages.setCurrentIndex(2)
            sizeDiff = [self.width() - self.plot.width(), self.height() - self.plot.height()]
            self.resize(sizeDiff[0] + self.data.properties["screenWidth"], sizeDiff[1] + self.data.properties["screenHeight"])
            #self.setFixedWidth(sizeDiff[0] + self.data.properties["screenWidth"])
            #self.setFixedHeight(sizeDiff[1] + self.data.properties["screenHeight"])
            self.swPages.setCurrentIndex(0)

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
            QtWidgets.QShortcut(self.data.properties["confidence." + str(i + confMin) + ".hotkey"].strip(), self).activated.connect(lambda arg=button: arg.setChecked(not arg.isChecked()))

        # Populate the text boxes with data
        self.setText("introScreen", "preWarmup", "warmupScreenTrial", "completion")

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
        if self.getState() == "IDLE":
            self.currentPage += dir
            self.swPages.setCurrentIndex(self.currentPage)

            self.setButtons(True)
            if self.currentPage == 0:
                self.btPrev.setEnabled(False)
            elif self.currentPage == 2:
                self.resetTrialUI()
                self.state.updateState()

        # Moving to info after warmup trials
        elif self.getState() == "TRIAL_FIRST_DELAY":
            if self.currentPage != 1:
                self.currentPage = 1
                self.setText("introScreen", "preTrial", "warmupScreenTrial", "completion")
            else:
                self.currentPage = 2
                self.resetTrialUI()
                self.state.updateState()

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

    def getResponse(self):
            # Make sure a response is selected
        if self.rbAnswerPositive.isChecked():
            response = self.rbAnswerPositive.text()
        elif self.rbAnswerNegative.isChecked():
            response = self.rbAnswerNegative.text()
        else:
            msgbox = QtWidgets.QMessageBox.critical(self, "Error", "Please select a response")
            return

        # Make sure a confidence is selected
        for i, button in enumerate(self.confidenceButtons):
            if button.isChecked():
                # Set the confidence level, append the trial and reset
                self.state.setResponseValues(response, self.data.properties["confidenceMin"] + i)
                self.resetTrialUI()
                self.state.updateState()
                return

        msgbox = QtWidgets.QMessageBox.critical(self, "Error", "Please select a confidence level")
        return

    # Update the interface based on the experiment state
    def updateState(self, state):

        # Pre-warmup trial delay
        if (state == "WARMUP_DELAY"):
            self.lbStatus.setText(self.data.properties["warmupDelayText"])

        # Warmup trial
        elif(state == "WARMUP_TRIAL"):
            self.lbStatus.setText(self.data.properties["duringTrialText"])
            self.vis.play()

        # Waiting for user response
        elif(state == "WARMUP_RESPOND"):
            self.vis.clearPlot()
            self.lbStatus.setText(self.data.properties["enterResponseText"])
            self.btNext.setEnabled(True)
            self.gbQuestion.setEnabled(True)
            self.howConfident.setEnabled(True)

        # Sets text for first trial
        elif(state == "TRIAL_FIRST_DELAY"):
            self.setText("introScreen", "preTrial", "testScreenTrial", "completion")
            self.lbStatus.setText(self.data.properties["firstDelayText"])

        # Trial delay
        elif(state == "TRIAL_DELAY"):
            self.lbStatus.setText(self.data.properties["trialDelayText"])

        # Run trial
        elif(state == "TRIAL"):
            self.lbStatus.setText(self.data.properties["duringTrialText"])
            self.vis.play()

        # Waiting for user response
        elif(state == "RESPOND"):
            self.vis.clearPlot()
            self.lbStatus.setText(self.data.properties["enterResponseText"])
            self.btNext.setEnabled(True)
            self.gbQuestion.setEnabled(True)
            self.howConfident.setEnabled(True)

        # Finish the experiment
        elif(state == "FINISH"):
            self.swPages.setCurrentIndex(3)
            for trial in self.state.trials:
                for key, value in trial.items():
                    print("{0}: {1}".format(key, value))
                print("")
