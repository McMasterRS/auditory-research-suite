import os
import csv
from Utilities.GetPath import *
from Experiments.ExperimentState import ExperimentState
from _version import __version__, __versionDate__
import numpy as np


class TOJState(ExperimentState):
    def __init__(self, parent, data):
        super(TOJState, self).__init__(parent, data)

    # Set the responses that the subject provides
    def setResponseValues(self, response, conf):
        self.currentTrialData["subjResponse"] = response
        self.currentTrialData["confidence"] = conf

        # Convert the response into a positive or negative offset
        if response == self.data.properties["answerPositive.label"]:
            answerDir = 1
        else:
            answerDir = -1

        # Compare the response with the actual offset
        if (np.sign(self.currentTrialData["audioOffset"]) == answerDir):
            self.currentTrialData["responseCorrect"] = True
        else:
            self.currentTrialData["responseCorrect"] = False

        self.trials.append(self.currentTrialData)

        if (self.data.properties["debug"]):
            print("Response: " + str(response))
            print("Confidence: " + str(conf))
            print("")

    # Update the state of the experiment
    def updateState(self):

        if (self.data.properties["debug"]):
            print("Current State: " + self.state)

        # This just helps make this entire function more readable
        currentState = self.state

        # Update the interface first
        self.parent.updateState(self.state)

        # Then update the experiment based on the current state

        # Not yet started
        if (currentState == "IDLE"):
            self.state = "WARMUP_DELAY"
            self.updateState()

        # Pre-warmup trial delay
        elif (currentState == "WARMUP_DELAY"):
            self.state = "WARMUP_TRIAL"
            self.stateTimer.start(self.data.properties["preStimulusSilence"])
            self.newTrial()

        # Warmup trial
        elif(currentState == "WARMUP_TRIAL"):
            self.state = "WARMUP_PAUSE"
            self.stateTimer.stop()

        # Pause after trial
        elif(currentState == "WARMUP_PAUSE"):
            self.state = "WARMUP_RESPOND"
            self.stateTimer.start(self.data.properties["postStimulusSilence"])

        # Waiting for user response
        elif(currentState == "WARMUP_RESPOND"):
            self.stateTimer.stop()

            # Start the real trials if the warmup is complete
            self.warmupTrialCount += 1
            if self.warmupTrialCount == self.data.properties["numWarmupTrials"]:
                self.state = "TRIAL_FIRST_DELAY"
            else:
                self.state = "WARMUP_DELAY"

        # Sets text for first trial
        elif(currentState == "TRIAL_FIRST_DELAY"):
            self.state = "TRIAL"
            self.stateTimer.start(self.data.properties["preStimulusSilence"])
            self.resetCounters()
            self.newTrial()

        # Trial delay
        elif(currentState == "TRIAL_DELAY"):
            self.state = "TRIAL"
            self.stateTimer.start(self.data.properties["preStimulusSilence"])
            self.newTrial()

        # Run trial
        elif(currentState == "TRIAL"):
            self.state = "PAUSE"
            self.stateTimer.stop()

        # Pause after trial
        elif(currentState == "PAUSE"):
            self.state = "RESPOND"
            self.stateTimer.start(self.data.properties["postStimulusSilence"])

        # Waiting for user response
        elif(currentState == "RESPOND"):
            self.stateTimer.stop()
            self.state = "TRIAL_DELAY"

        # Finish the experiment
        elif(currentState == "FINISH"):
            self.stateTimer.stop()
            self.saveCSV()

            if (self.data.properties["debug"]):
                for trial in self.trials:
                    for key, value in trial.items():
                        print("{0}: {1}".format(key, value))

                print("")

    # Save the data to file
    def saveCSV(self):
        filename = os.path.splitext(self.data.propFile)[0] + ".csv"
        with open(filename, "w", newline="") as csvfile:
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

                # Validate test the filenames and replace some values with N/A if set to None
                if trial["audioFile"] == None:
                    audioFileName = "NA"
                    trial["audioStart"] = "NA"
                    trial["audioDelay"] = "NA"
                    trial["audioOffset"] = 0
                else:
                    audioFileName = os.path.basename(trial["audioFile"])

                if trial["visFile"] == None:
                    visFileName = "NA"
                    trial["animationStart"] = "NA"
                    trial["animationDelay"] = "NA"
                    trial["numDots"] = 0
                else:
                    os.path.basename(trial["visFile"])

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
                    audioFileName,
                    visFileName,
                    trial["audioOffset"],
                    trial["numDots"],
                    trial["animationStart"],
                    trial["animationDelay"],
                    trial["audioStart"],
                    trial["audioDelay"],
                    trial["confidence"],
                    trial["subjResponse"],
                    trial["responseCorrect"]
                ])
