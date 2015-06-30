

Example properties files:
  * [Example rhythm.properties](GeneralRhythmPropertiesFile.md)
  * [Example si.properties](GeneralSIPropertiesFile.md)
  * [Example toj.properties](GeneralTOJPropertiesFile.md)


---

Properties files match a key to a value to allow per experiment configuration of parameters.

They follow the form:
`key= value`

To break up long values, escape to the next line by putting a backslash (\) at the end of a line.
Example:
```
    key= this is a very long\
    string value that had to be split
```

Different experiments try to read the values for different keys. Extraneous key value pairs are ignored.

---

# General Properties #
## Experiment logging ##
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **experimentID** | integer               | Use for storing/labeling data about a specific experiment number |
| **subExperimentID** | integer               | Use for storing/labeling data about a specific experiment number |
| **debug**      | boolean               | Turn on to show debug console window |

## Trial Blocks Setup ##
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **blockSetRepetitions** | positive integer      | Number of times blocks should be presented. Blocks sets are sets of [no. of offsetDegrees] trials. A set of blocks covers every baseIOI with and without tapping, so the number of blocks in a full set is [no. baseIOIs]`*`2. Each set of blocks will be randomized independently, unless specified. |
| **metablocks** | non-negative integer  | Metablocks are groups of blocks, possibly repeated |
| **randomizeBlocks** | boolean               | Randomizes the order of blocks within different metablocks |
| **randomizeTrials** | boolean               | Randomizes the order of trials within different blocks |

## Trial Timings ##
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **trialDelay** | non-negative integer  | Number of milliseconds to wait until proceeding to the next trial |
| **preStimulusSilence** | non-negative integer  | Number of milliseconds to wait before playback of stimulus. |
| **postStimulusSilence** | non-negative integer  | Number of milliseconds to wait after playback of stimulus. |

## Volume Settings ##
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **playbackGain** | float in range [0.0, 1.0] | Playback volume as a percentage of maximum |

## Warmup Settings ##
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **numWarmupTrials** | non-negative integer  | Number of warmup trials to run before actual experiment trials |
| **warmupDelayText** | string (html allowed) | Text to display during the [trial delay time](PropertiesFiles#Trial_Timings.md) before a warmup trial |

## Feedback Settings ##
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **allowFeedback** | boolean               | If feedback is false, then the following text properties are ignored. WARNING: Sensory Integration does not currently determine correctness of a response, so this should be false for SI experiments! |
| **accuracyCorrectText** | string (html allowed) | Text to display when a response is correct |
| **accuracyIncorrectText** | string (html allowed) | Text to display when a response is incorrect |
| **resultsFormatText** | string (html allowed) |  Format string for displaying result percentages. Example: `<html><small>%d of %d correct (%d%%)`|

## Visual Settings ##
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **statusOrientation** | _verticalTop, verticalBottom, horizontalLeft, horizontalRight, suppressed_ | Use one of these 5 values to place the location of the status box. Defaults to _horizontalLeft_ if unspecified. _suppressed_ blocks the status box from being shown. |
| **firstDelayText** | string (html allowed) | Text to display during the trial delay time before the first recorded trial. |
| **trialDelayText** | string (html allowed) | Text to display during the trial delay time for all other trials. |
| **introScreenTitle** | string (html allowed) | Title for introduction screen. |
| **introScreenText** | string (html allowed) | Welcome and general instructions. |
| **preWarmupTitle** | string (html allowed) | Title for pre-warmup screen. |
| **preWarmupText** | string (html allowed) | Warmup trials general instructions. |
| **warmupScreenTrialTitle** | string (html allowed) | Title for warmup trial screen. |
| **warmupScreenTrialText** | string (html allowed) | Warmup trial instructions. |
| **preTrialTitle** | string (html allowed) | Title for pre-trial screen. |
| **preTrialText** | string (html allowed) | Trial general instructions. |
| **testScreenTrialTitle** | string (html allowed) | Title for trial screen. |
| **testScreenTrialText** | string (html allowed) | Trial instructions. |
| **completionTitle** | string (html allowed) | Title for completion screen. |
| **completionText** | string (html allowed) | Completion and thanks for participation. |



---

# Rhythm Specific Properties #

| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **speedMode**  | boolean               | Debug setting. Flag to divide IOIs by 10 for testing experiment quickly. |
| **randomizeAcrossRepetitions** | boolean               | true indicates that all trials within a block will be randomized, even across repetitions. Specialization for rhythm, see [general block setup](PropertiesFiles#Trial_Blocks_Setup.md) |
| **middleC**    | Pitch value           | Set the Middle-C value to make instrument specification easier with different SoundBanks, as each use a different representation. Value is normally labeled either "C3", "C4", or "C5". This defaults to C4. Octave values from 5 below to 5 above are allowed around this Middle C, as per MIDI specification. For example, if set to C4, this allows notes in octaves from -1 to 9. |
| **usePercussionChannel** | boolean               | If this is set to true, rhythm will be generated using the Percussion channel instruments.  The soundbank being used will need to have percussion instruments, which will be mapped by Notes/(Pitches) instead of instrument numbers. |
| **instrumentNumber** | int [0,127]           | General midi bank number to select for playback instrument. [0..127]. If usePercussionChannel is false, this sets the instrument to use. Otherwise (when usePercussionChannel is true) this sets the drum-kit to use, which is normally a value in the set [0,8,16,24,32,40,48,56], but can vary based on the Soundbank being used. |

## Trial Specification ##
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **trialSpecificationStyle** | _HighLowPitches, PatternWithNotes_ | Specify which style of pattern input to use for building trial note sequences. Defaults to PatternWithNotes |
| **baseIOIs**   | comma delimited list of integers | List of integers specifying the inter-onset time between notes in milliseconds. This is basically a psychologists way of discussing tempo. An IOI of 1000 means that notes are spaced 1 second apart, and would correspond to a musical tempo marking of 60 beats per minute. KEY EXPERIMENT VARIABLE |
| **offsetDegrees** | comma delimited list of floats | List representing fraction of baseIOI that probe tone should be offset [0-1] So for example, a 0.1 for BaseIOI=600 means trials at 600+/-60 (540 and 660). KEY EXPERIMENT VARIABLE |
| **probeDetuneAmounts** | comma delimited list of integers | List of probe detune offsets, which pitchbend the probe by cents (hundredths of semitones). Can take any values, allowing arbitrary detuning to the extremes allowed by MIDI. Leaving empty or setting to just '0' will effectively cause the experiment to run as before the inclusion of probe detunes. KEY EXPERIMENT VARIABLE |

WARNING: offsetDegrees and probeDetuneAmounts should not both have multiple values. This would create experiments where both variables were varied, while we can only test one type of varying parameter. See more important notes at [Question Labeling](PropertiesFiles#Question_Labeling.md).

#### For HighLowPitches specification ####
| **highPitch** | Pitch value | Note value played on the stressed beat. |
|:--------------|:------------|:----------------------------------------|
| **lowPitch**  | Pitch value | Note value played on non-stressed beats. |
| **playbackMeasures** | non-negative integer | Number of measures played               |
| **beatsPerMeasure** | non-negative integer | Number of beats per measures (first beat is the stressed one) |
| **silenceMultiplier** | non-negative integer | Number of IOI units of silence after sounded measures (not including offset) |

#### For PatternWithNotes specification ####
**Pitch** is given as a Note value

**Velocity** is an integer in [0,127] (defaults to 64)

**Duration** is a percentage of the baseIOI that the note should play. Float value (defaults to 1.0). This allows for arbitrary timings by changing the [baseIOI](PropertiesFiles#Trial_Specification.md).

| **trialNotePattern** | String Pattern consisting of [pst`_*`] | Specified with the characters 'p' (primary), 's' (secondary), 't' (tertiary), '`_`' (silence), '`*`' (probe). Capitalization and spaces are ignored for readability. Example: `Psts Psts Psts P_ _ _ *` |
|:---------------------|:---------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **primaryPitch**     | Pitch value                            |                                                                                                                                                                                                         |
| **primaryVelocity**  | int [0,127]                            |                                                                                                                                                                                                         |
| **primaryDuration**  | float                                  |                                                                                                                                                                                                         |
| **secondaryPitch**   | Pitch value                            |                                                                                                                                                                                                         |
| **secondaryVelocity** | int [0,127]                            |                                                                                                                                                                                                         |
| **secondaryDuration** | float                                  |                                                                                                                                                                                                         |
| **tertiaryPitch**    | Pitch value                            |                                                                                                                                                                                                         |
| **tertiaryVelocity** | int [0,127]                            |                                                                                                                                                                                                         |
| **tertiaryDuration** | float                                  |                                                                                                                                                                                                         |
| **probePitch**       | Pitch value                            | If unspecified, will default to the same as the primary pitch.                                                                                                                                          |
| **probeVelocity**    | int [0,127]                            |                                                                                                                                                                                                         |
| **probeDuration**    | float                                  |                                                                                                                                                                                                         |
| **silenceDuration**  | float                                  |                                                                                                                                                                                                         |

## Question Labeling ##
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **experimentTestingType** | _OffsetTiming, ProbeDetune_ | Indicates what type of testing this experiment will run. If value is "_OffsetTiming_", will check subject responses against offset timing. (Make sure to only have zero or one probeDetuneAmounts value when testing timing.) If value is "_ProbeDetune_", will check subject responses against probe detune. (Make sure to only have zero or one offsetDegrees value when testing detune.) No current support for recording both types simultaneously. |
| **question.label** | string                | This labels the question part of user response. Change based on experiment, e.g. "Accurate timing?" or "Accurate tuning?" |
| **answerPositive.label** | string                | Suggested value: "Yes" |
| **answerNegative.label** | string                | Suggested value: "No" |
| **answerPositive.hotkey** | string                | Suggested value: 'y' |
| **answerNegative.hotkey** | string                | Suggested value: 'n' |

Hotkeys must all be unique, or erroneous behavior will occur. Only first character from string will be used for hotkey.


## Confidence Labeling ##

Property keys for confidence must be of the form: "confidence.{integer}" where the integer is >= confidenceMin Values may not be skipped, although order does not matter.

Example:
```
confidenceMin=-2
confidenceOrderHighToLow=true
confidence.-2=Very unconfident
confidence.-1=Somewhat unconfident
confidence.1=Somewhat confident
confidence.0=Unsure
confidence.2=Very confident
confidence.-2.hotkey=	1
confidence.-1.hotkey=	2
confidence.0.hotkey=	3
confidence.1.hotkey=	4
confidence.2.hotkey=	5
```

| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **confidenceMin** | integer               | The minimum (numerically) confidence value |
| **confidenceOrderHighToLow** | boolean               | Indicates the numeric order (descending) in which the confidence levels will appear: true indicates high-to-low, false indicates low-to-high. |
| **confidence.{integer}** | string                | label for confidence level {integer} |
| **confidence.{integer}.hotkey** | string                | hotkey for confidence level {integer} |

## In Trial User Update Labels ##
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **withTapText** | string (html allowed) | Displays for trials that the user should tap along with. |
| **withoutTapText** | string (html allowed) | Displays for trials that the user should _not_ tap along with. |
| **enterResponseText** | string (html allowed) | Displays after a trial to request user response. |

## User Tap Responses ##
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **computerKeyInput** | boolean               | Allow user taps via computer keyboard |
| **recordNoteOff** | boolean               | Record MIDI note-off events |
| **recordActualTapNote** | boolean               | Whether to record the actual tap note (if valid and available, computer keyboard note is generated) or the tap note override. Defaults to true. |
| **recordActualTapVelocity** | boolean               | Whether to log/record the actual tap velocity (if valid and available) or the tap velocity override. Defaults to true. |
| **suppressionWindow** | integer               | Number of milliseconds within which to ignore repeat note-on events |
| **subjectTapSound** | boolean               | Play a feedback sound for subject taps |
| **subjectTapVelocity** | int [0,127]           | Value to override the subject's tap velocity, or -1 to use the actual (or default, for computer key presses) tap velocity |
| **subjectTapNote** | Pitch value           | Note for subject tap sounds |
| **subjectTapGain** | float [0.0, 1.0]      | Gain level for subject tap sounds DURING TAP TRIALS as a percentage of maximum [0.0, 1.0] |
| **subjectNoTapGain** | float [0.0, 1.0]      | Gain level for subject tap sounds DURING NON-TAP TRIALS as a percentage of maximum [0.0, 1.0]. Absence of this property or a negative value will default to the value of subjectTapGain. |
| **subjectTapInstrumentNumber** | int [0,127]           | General midi bank number for subject tap sound |


---

# SI and TOJ Properties #
| **fullScreen** | boolean | Run the experiment in fullscreen mode. Not applicable in demo mode. |
|:---------------|:--------|:--------------------------------------------------------------------|

Screen width and height - used to set the animation and/or video window size. Values that are too large will not have enough room to render

| **screenWidth** | non-negative integer | Suggested value: 640 |
|:----------------|:---------------------|:---------------------|
| **screenHeight** | non-negative integer | Suggested value: 480 |

## Audio/Visual Block Inclusion ##
Block types to include, as well as single-block structure and randomization parameters.

```
include[type]Block: true indicates to include this type of block in the experiment. If false, the next two properties would be ignored. 
single[type]Block: true indicates that all trials of this type, from repetitions, should be forced into a single block (within each metablock). 
single[type]FullRandom: true indicates that all trials within a block will be randomized, even across repetitions.
```

| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **includeAudioBlock** | boolean               | Audio only block  |
| **singleAudioBlock** | boolean               |                   |
| **singleAudioFullRandom** | boolean               |                   |
| **includeAnimationBlock** | boolean               | Animation only block |
| **singleAnimationBlock** | boolean               |                   |
| **singleAnimationFullRandom** | boolean               |                   |
| **includeVideoBlock** | boolean               | Video block       |
| **singleVideoBlock** | boolean               |                   |
| **singleVideoFullRandom** | boolean               |                   |
| **includeAudioAnimationBlock** | boolean               | Audio and Animation block |
| **singleAudioAnimationBlock** | boolean               |                   |
| **singleAudioAnimationFullRandom** | boolean               |                   |

## Animation and Audio Options ##
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **animationPointAspect** | float                 | Animation point aspect scales point locations according to the given relationship between x and y values (animationPointAspect=x/y). If greater than zero, this value overrides "pointAspectRatio" in animation files. |
| **animationPointSize** | float                 | Base radius for animation points - sizes in the animation files are relative based on this value, which does not scale linearly. |
| **numAnimationPoints** | non-negative integer  | Maximum number of animation points/dots included - more than one value here creates sets of trials for each case. |
| **connectDots** | boolean               | Indicator for whether the points/dots in the animation should have lines connecting them. |
| **soundOffsets** | comma delimited list of integers | List representing milliseconds that tone should be offset. KEY EXPERIMENT VARIABLE |

## Parameters and File Specification ##

This specification allows for arbitrary file naming by defining parameters and file formats based on those parameters. Then all values of those parameters are combined in all possible ways to get the files to load.

Parameters in the file format string are specified as `'${PARAM_NAME}'` Files are then constructed by replacing every occurrence of `${PARAM_NAME}` with all possible values extracted from the entry `'PARAM_NAME=[value], [value], [value], ...'`

For all entries, capitalization matters!

Most media will only need one file extension to be specified.  In the case of multiple, they will be checked IN ORDER for the existence of the desired media file.  (So, 'dnn.avi' would be used, if it exists, instead of 'dnn.dv', even if 'dnn.dv' also exists.)

| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **videoParams** | comma delimited single-word strings | Parameters listed here will be searched for in the properties file and used to build file names using the file format in the **videoFileFormat** property. See [Parameter Settings](PropertiesFiles#Parameter_Settings.md). |
| **videoFileFormat** | single string using above parameters | Example: `${videoParamOne}${videoParamTwo}` would create possible filenames that are concatenations of videoParamOne values and videoParamTwo values. |
| **videoFileExtensions** | comma delimited list of single-word strings | List of possible video file extensions. See above note about extension ordering. |
| **videoFileSubDirectory** | string                | Folder name inside data directory (chosen during setup) to search inside of for video files. |

| **animationParams** | comma delimited single-word strings | Parameters listed here will be searched for in the properties file and used to build file names using the file format in the **animationFileFormat** property. See [Parameter Settings](PropertiesFiles#Parameter_Settings.md). |
|:--------------------|:------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **animationFileFormat** | single string using above parameters | Example: `${animationParamOne}${animationParamTwo}` would create possible filenames that are concatenations of animationParamOne values and animationParamTwo values.                                                           |
| **animationFileExtensions** | comma delimited list of single-word strings | List of possible animation file extensions. See above note about extension ordering.                                                                                                                                            |
| **animationFileSubDirectory** | string                              | Folder name inside data directory (chosen during setup) to search inside of for animation files.                                                                                                                                |

| **audioParams** | comma delimited single-word strings | Parameters listed here will be searched for in the properties file and used to build file names using the file format in the **audioFileFormat** property. See [Parameter Settings](PropertiesFiles#Parameter_Settings.md). |
|:----------------|:------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **audioFileFormat** | single string using above parameters | Example: `${audioParamOne}${audioParamTwo}` would create possible filenames that are concatenations of audioParamOne values and audioParamTwo values.                                                                       |
| **audioFileExtensions** | comma delimited list of single-word strings | List of possible audio file extensions. See above note about extension ordering.                                                                                                                                            |
| **audioFileSubDirectory** | string                              | Folder name inside data directory (chosen during setup) to search inside of for audio files.                                                                                                                                |

| **synchronizeParameters** | boolean | Indicate if parameters appearing in more than one file type should be synchronized if those types both appear in the same trial (audio and animation).  That is, should parameter types be combined independently (synchronizeParameters=false) for two different media sources.  Default is 'true'.  Does not apply to the Demo GUI. |
|:--------------------------|:--------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|

### Parameter Settings ###

These are properties that are referenced by listing a property in **videoParams**, **animationParams**, or **audioParams**.
| **{parameter}** | comma delimited list of values | Possible values for this parameter. |
|:----------------|:-------------------------------|:------------------------------------|
| **{parameter}.label** | string                         | Main label for this type of parameter |
| **{parameter}.labels** | comma delimited list of strings | Labels for each of the parameter values. For spaces, use '\u00A0'.|

File specification example:

Say we have a directory called 'video' which contains several video files that we want to load:
```
/dataDirectory
  |- /video
     |- 240_awesome.wav
     |- 240_boring.wav
     |- 240_cool.wav
     |- 360_awesome.wav
     |- 360_boring.wav
     |- 360_cool.wav
```
To load these files, we can set the video parameters as follows:
```
# (The params values are ones we choose, so we can refer to them later.)
videoParams=timing, coolness 

# (Here we use the above defined parameters to build a filename string that will match the files we want to load.)
# (Notice that the underscore '_' was added between the parameter values, and parameter names are surrounded with the construct:  ${paramName}.)
videoFileFormat= ${timing}_${coolness}

# (Now we specify the file extension types to match .wav files (Do not add any '.' here))
videoFileExtensions=wav

# (and specify the directory within the data directory to find these files)
videoFileSubDirectory=video
```

Now we just have to give the possible values for our chosen parameters:
```
# possible values for timing are:
timing= 240, 360
timing.label= Timing: 
timing.labels= 240Label, 360Label

# possible values for coolness are: (notice the use of '\u00A0' to put spaces in the coolness labels)
coolness= awesome, boring, cool
coolness.label= Coolness
coolness.labels= Awesome\u00A0Label, Boring\u00A0Label, Cool\u00A0Label
```

Now the loading code will search for the following six files in the video directory:
  * 240\_awesome.wav
  * 240\_boring.wav
  * 240\_cool.wav
  * 360\_awesome.wav
  * 360\_boring.wav
  * 360\_cool.wav

And it will also apply the matching labels in data output.

This file specification is very general and allows for many styles of filename, as long as the format is uniform for the specific experiment.


---

## SI Specific Properties ##

### Slider Response Controls ###

Long answer labels may mess up the experiment GUI layout.

| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **showTickMarks** | boolean               | Set to show the tick marks on response sliders. |
| **sliderLength** | integer               | Set the pixel length of response sliders. Minimum of 50 px, maximum of 2000 px (for large screens). Default value is 550 pixels. Exceeding the limits will default to this value. For large values, the experiment window might need to be resized. |
| **durationLow** | string                | Low end label for duration slider |
| **durationHigh** | string                | High end label for duration slider |
| **agreementLow** | string                | Low end label for agreement slider |
| **agreementHigh** | string                | High end label for agreement slider |
| **durationMin** | integer               | minimum end of allowed range for duration slider |
| **durationMax** | integer               | maximum end of allowed range for duration slider  |
| **agreementMin** | integer               | minimum end of allowed range for agreement slider |
| **agreementMax** | integer               | maximum end of allowed range for agreement slider  |


---


## TOJ Specific Properties ##
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **audioPollWait** | integer               | Time in milliseconds to wait between audio playback completion polling. Note: polling only applies after the expected audio duration has already elapsed but the audio is still playing. |
| **audioCallAhead** | float [0.0,1.0]       | Gain level for playback as a percentage of maximum [0.0, 1.0] |

Note: the following two values should likely be the same . . .
| **animationFrameAdvance** | integer | How much time the renderer should look ahead into the future to determine the frame that it is currently rendering.  This value cannot be greater than the monitor refresh period (milliseconds). Negative values indicate looking into the past for frames. |
|:--------------------------|:--------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **renderCallAhead**       | integer | How far ahead the frame render should be called.  This value cannot be greater than the monitor refresh period (milliseconds). Negative values indicate a delay.                                                                                             |


### Question Labeling ###

**WARNING: Internal code makes assumption that the positive label is corresponded to the Dot response. Changing the experiment meaning of these values _will_ lead to erroneous output data.**
| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **question.label** | string                | Suggested value for TOJ: "Which came first?" |
| **answerPositive.label** | string                | Suggested value: "Dot" |
| **answerNegative.label** | string                | Suggested value: "Tone" |
| **answerPositive.hotkey** | string                | Suggested value: 'd' |
| **answerNegative.hotkey** | string                | Suggested value: 't' |

Hotkeys must all be unique, or erroneous behavior will occur. Only first character from string will be used for hotkey.


### Confidence Labeling ###

Property keys for confidence must be of the form: "confidence.{integer}" where the integer is >= confidenceMin Values may not be skipped, although order does not matter.

Example:
```
confidenceMin=-2
confidenceOrderHighToLow=true
confidence.-2=Very unconfident
confidence.-1=Somewhat unconfident
confidence.1=Somewhat confident
confidence.0=Unsure
confidence.2=Very confident
confidence.-2.hotkey=	1
confidence.-1.hotkey=	2
confidence.0.hotkey=	3
confidence.1.hotkey=	4
confidence.2.hotkey=	5
```

| _**Property**_ | _**Possible Values**_ | _**Usage Notes**_ |
|:---------------|:----------------------|:------------------|
| **confidenceMin** | integer               | The minimum (numerically) confidence value |
| **confidenceOrderHighToLow** | boolean               | Indicates the numeric order (descending) in which the confidence levels will appear: true indicates high-to-low, false indicates low-to-high. |
| **confidence.{integer}** | string                | label for confidence level {integer} |
| **confidence.{integer}.hotkey** | string                | hotkey for confidence level {integer} |