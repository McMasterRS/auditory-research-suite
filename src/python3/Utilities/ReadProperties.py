from Utilities.PropertiesParser import PropertiesParser

# Read the file and extract a dict with all the data
def readPropertiesFile(p):

    dat = {}

    f = open(p, 'r')

    # Convert lines into a dict
    for line in f:
        
        # Strip blank lines
        if line.strip() == "":
            continue
        # Strip comments
        if line[0] == "#":
            continue

        splitLine = line.strip().split("=", 1)
        # If theres an error, return the line that caused the issue
        if len(splitLine) != 2:
            continue
            #return False, line

        dat[splitLine[0]] = splitLine[1]

    return True, dat

# Extract the useful information from a .properties file
def readTojProperties(p):
    e, raw = readPropertiesFile(p)

    # If e returns false, return with the line that caused an error
    if not e:
        return False, "Unable to parse the properties file at '{0}'. Please make sure that the file matches the new formatting requirements".format(raw)

    typeDict = {
        # Experiment Data
        "experimentID"      : "int",
        "subExperimentID"   : "int",

        # Flags
        "debug"             : "bool",
        "fullScreen"        : "bool",

        # Experiment Details
        # TODO

        # Visual Options
        "screenWidth" : "int",
        "screenHeight" : "int",

        # Animation Options
        "animationPointAspect" : "float",
        "animationPointSize" : "float",
        "numAnimationPoints" : "int",
        "connectDots" : "bool",
        "animationFrameAdvance" : "int",
        "renderCallAhead" : "int",

        # Audio Options
        # TODO

        # Parameters and file specification
        "videoParams" : "array",
        "videoFileFormat" : "str",
        "videoFileExtensions" : "array",
        "videoFileSubDirectory" : "str",
        "animationParams" : "array",
        "animationFileFormat" : "str",
        "animationFileExtensions" : "array",
        "animationFileSubDirectory" : "str",
        "audioParams" : "array",
        "audioFileFormat" : "str",
        "audioFileExtensions" : "array",
        "audioFileSubDirectory" : "str",
        "synchronizeParameters" : "bool",

        # Parameter set definitions
        # TODO

        # Answer Labels
        # TODO

        # Response confidence levels

        # Silence durations

        # General text displays
        "duringTrialText" : "str",
        "enterResponseText" : "str",

        # Subject feedback options
        "allowFeedback" : "bool",
        "accuracyCorrectText" : "str",
        "accuracyIncorrectText" : "str",
        "resultsFormatText" : "str",

        # Experiment Stages
        "trialDelay" : "int",
        "numWarmupTrials" : "int",
        "warmupDelayText" : "str",
        "firstDelayText" : "str",
        "trialDelayText" : "str",

        # Intro screen
        "introScreenTitle" : "str",
        "introScreenText" : "str",

        # Warmup prep screen
        "preWarmupTitle" : "str",
        "preWarmupText" : "str",
        
        # Warmup mode stimulus/response screen
        "warmupScreenTrialTitle" : "str",
        "warmupScreenTrialText" : "str",

        # Pre-trial instruction screen
        "preTrialTitle" : "str",
        "preTrialText" : "str",

        # Stim/response screen
        "testScreenTrialTitle" : "str",
        "testScreenTrialText" : "str",

        # Response block orientation
        "statusOrientation" : "str",

        # Completion announcement screen
        "completionTitle" : "str",
        "completionText" : "str"

    }

    parser = PropertiesParser(raw)
    e, properties = parser.parseProperties(typeDict)

    return e, properties