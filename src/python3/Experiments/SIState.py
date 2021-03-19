from PyQt5 import QtGui, QtCore
import os
import datetime
import itertools
import random
import csv
from Utilities.GetPath import *
from Experiments.ExperimentState import ExperimentState
from _version import __version__, __versionDate__
import numpy as np


class SIState(ExperimentState):
    def __init__(self, parent, data):
        super(SIState, self).__init__(parent, data)

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
            self.currentBlockTrial = random.randint(0, len(self.blocks[self.currentBlock]["data"]) - 2)
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

    # Save the data to file
    def saveCSV(self):
        with open("./testFiles/test.csv", "w", newline="") as csvfile:
            csvWriter = csv.writer(csvfile, delimiter=',')
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
                    self.parent.vis.timings["animationStart"],
                    self.parent.vis.timings["animationDelay"],
                    self.parent.vis.timings["audioStart"],
                    self.parent.vis.timings["audioDelay"],
                    trial["confidence"],
                    trial["subjResponse"],
                    trial["responseCorrect"]
                ])
