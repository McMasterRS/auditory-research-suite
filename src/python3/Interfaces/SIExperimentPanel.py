import os
import operator
import datetime
import itertools
import random
from PyQt5 import QtWidgets, QtGui, uic, QtCore
from Utilities.GetPath import *

from Interfaces.ExperimentSetup import ExperimentSetup
from Visualizer.Visualizer import Visualizer
from Experiments.SIState import SIState


class SIExperimentPanel(QtWidgets.QWidget):
    def __init__(self, parent):
        super(QtWidgets.QWidget, self).__init__()

        self.parent = parent

        self.state = []
        self.data = []
        self.currentPage = 0

        self.questionChanged = False
        self.agreementChanged = False

    def startExperiment(self, data):

        self.data = data
        self.state = SIState(self, data)
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

        # Setup UI
        uic.loadUi(getGui('SIExperiment.ui'), self)
        self.show()

        if self.data.properties["fullScreen"] == True:
            self.showFullScreen()
        else:
            self.swPages.setCurrentIndex(2)
            sizeDiff = [self.width() - self.plot.width(), self.height() - self.plot.height()]
            self.resize(sizeDiff[0] + self.data.properties["screenWidth"], sizeDiff[1] + self.data.properties["screenHeight"])
            self.swPages.setCurrentIndex(0)

        # Plot
        self.vis = Visualizer(self.plot, self, self.data.properties["animationPointSize"], self.data.properties["playbackGain"], self.data.properties["connectDots"])
        self.vis.data.defaultOffset = 0

        # Navigation buttons
        self.btNext.clicked.connect(lambda: self.movePage(1))
        self.btPrev.clicked.connect(lambda: self.movePage(-1))

        # Slider labels
        self.gbQuestion.setTitle(self.data.properties["duration.label"])
        self.lbDurationLow.setText(self.data.properties["durationLow"])
        self.lbDurationHigh.setText(self.data.properties["durationHigh"])
        self.howConfident.setTitle(self.data.properties["agreement.label"])
        self.lbAgreementLow.setText(self.data.properties["agreementLow"])
        self.lbAgreementHigh.setText(self.data.properties["agreementHigh"])

        # Slider values
        self.hsQuestion.setMinimum(self.data.properties["durationMin"])
        self.hsQuestion.setMaximum(self.data.properties["durationMax"])
        self.hsQuestion.setTickPosition(int((self.data.properties["durationMin"] + self.data.properties["durationMax"]) / 2.0))

        self.hsAgreement.setMinimum(self.data.properties["agreementMin"])
        self.hsAgreement.setMaximum(self.data.properties["agreementMax"])
        self.hsAgreement.setTickPosition(int((self.data.properties["agreementMin"] + self.data.properties["agreementMax"]) / 2.0))

        # Hook up the sliders to their functions
        self.hsQuestion.sliderReleased.connect(lambda: self.toggleSliders("question"))
        self.hsAgreement.sliderReleased.connect(lambda: self.toggleSliders("agreement"))

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
                self.setText("introScreen", "preTrial", "testScreenTrial", "completion")
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

        self.hsQuestion.setValue(int((self.data.properties["durationMin"] + self.data.properties["durationMax"]) / 2.0))
        self.hsAgreement.setValue(int((self.data.properties["agreementMin"] + self.data.properties["agreementMax"]) / 2.0))

        self.gbQuestion.setEnabled(False)
        self.howConfident.setEnabled(False)

        self.questionChanged = False
        self.agreementChanged = False

    # Toggle the bool that tracks if the sliders have been moved
    def toggleSliders(self, slider):
        if slider == "question":
            self.questionChanged = True

        if slider == "agreement":
            self.agreementChanged = True

    ###########################################
    #               Experiment                #
    ###########################################

    # Used in the warmup trials to ensure that an input has been selected
    def checkResponse(self):
        if not self.questionChanged:
            msgbox = QtWidgets.QMessageBox.critical(self, "Error", "Please select a duration")
            return False

        if not self.agreementChanged:
            msgbox = QtWidgets.QMessageBox.critical(self, "Error", "Please select an agreement level")
            return False

        return True

    # Actually extracts the response from the UI
    def getResponse(self):
        # Make sure a response is selected
        if self.questionChanged:
            response = self.hsQuestion.value()
        else:
            msgbox = QtWidgets.QMessageBox.critical(self, "Error", "Please select a duration")
            return

        # Make sure a confidence is selected
        if self.agreementChanged:
            # Set the agreement level, append the trial and reset
            self.state.setResponseValues(response, self.hsAgreement.value())
            self.resetTrialUI()
            self.state.updateState()
            return

        msgbox = QtWidgets.QMessageBox.critical(self, "Error", "Please select an agreement level")
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
