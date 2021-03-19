import os
import math
import csv
from Utilities.GetPath import *
from Experiments.ExperimentState import ExperimentState
from _version import __version__, __versionDate__


class SIState(ExperimentState):
    def __init__(self, parent, data):
        super(SIState, self).__init__(parent, data)

    # Set the responses that the subject provides
    def setResponseValues(self, response, conf):

        self.currentTrialData["subjDurationValue"] = response
        self.currentTrialData["subjDurationResponse"] = self.getResponseText("duration", response)
        self.currentTrialData["subjAgreementValue"] = conf
        self.currentTrialData["subjAgreementResponse"] = self.getResponseText("agreement", conf)
        self.trials.append(self.currentTrialData)

        if (self.data.properties["debug"]):
            print("Duration Value: " + str(response))
            print("Duration Response: " + str(self.getResponseText("duration", response)))
            print("Confidence Value: " + str(conf))
            print("Confidence Response: " + str(self.getResponseText("agreement", conf)))
            print("")

    def getResponseText(self, type, val):
        mid = math.floor((self.data.properties["{0}Max".format(type)] - self.data.properties["{0}Min".format(type)]) / 2.0)
        text = "Neutral"
        if val < mid:
            text = self.data.properties["{0}Low".format(type)]
        elif val > mid:
            text = self.data.properties["{0}High".format(type)]

        return text

    # Update the state of the experiment
    def updateState(self):

        if (self.data.properties["debug"]):
            print("Current State: " + self.state)

        # This just helps make this entire function more readable
        currentState = self.state

        # Update the interface first
        self.parent.updateState(currentState)

        # Then update the experiment based on the current state

        # Not yet started
        if (currentState == "IDLE"):
            self.state = "WARMUP_DELAY"
            self.updateState()

        # Pre-warmup trial delay
        elif (currentState == "WARMUP_DELAY"):
            self.state = "WARMUP_TRIAL"
            self.stateTimer.start(self.data.properties["preStimulusSilence"])
            self.currentTrial = -1
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
                                "subjDurationResponse", "subjDurationValue",
                                "subjAgreementResponse", "subjAgreementValue"
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
                    visFileName = "N/A"
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
                    trial["subjDurationResponse"],
                    trial["subjDurationValue"],
                    trial["subjAgreementResponse"],
                    trial["subjAgreementValue"]
                ])
