#!/usr/bin/env python
# Sensory Integration Experiment
#
# Copyright (C) 2005-2006 University of Virginia
# Supported by grants to the University of Virginia from the National Eye Institute 
# and the National Institute of Deafness and Communicative Disorders.
# PI: Prof. Michael Kubovy <kubovy@virginia.edu>
# Author: Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>
# Author: Michael Schutz <schutz@virginia.edu>
#
# Distributed under the terms of the GNU Lesser General Public License
# (LGPL). See LICENSE.TXT that came with this file.
#
# $Id: SensoryIntegrationMain.py 411 2008-01-23 22:58:33Z mschutz $
#

"""
Sensory Integration Main Routine
"""

__version__ = '$LastChangedRevision: 411 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

from SensoryIntegration.TrialData import *
from SensoryIntegration.SISetupGUI import *
from SensoryIntegration.SISessionData import *
from SensoryIntegration.ExperimentPanel import *
from PTCommon.TrialLogger import *
from SensoryIntegration.ExperimentData import *
import csv, time
import wx

app = wx.PySimpleApp()

# 0) Pilot/ replication of original movie findings 3 reps, 3 pitch=(e,d,g), aud = (l,s,p), vis=(l,s)
# 1) Upped number of reps to 6 (also plugged headphones in!) 
# 3) 
# 4) crossed all video with all audio, but only 1 rep each
# 5) this worked OK, so now upping to 3 reps and including pitch level "a"
# 6) using both dynamic and static dots
# 7) dynamic, 4 points, offsets
# 8) some sine waves, static dot,  moving dot.,  Note: as hack, sine waves labeled "e_sine" "d_sine" "g_sine" for now
# 9) Running .dv and .avi videos (original and static sine wave sounds) Paid subjects - summer '06 
# Note:  not sure if 9 ever actually ran anything
# 10) same as experiment 8 but with shorter sine waves.  Still using e_sine as hack.  
# 11) Video vs. animation (4 points, 1 point)
# 12) Timbre I: marimba, sine, voice
# 13) Timbre II: 600, 400, and 220 ms sine waves, vocal samples (3 lengths) and guitar (1 length) with undamped marimba sounds
# 14) Timbre III: trumpet, violin, voice, clairnet, marimba, piano
# 15) TimberIV: trumpet,trumpetS, violin violinS, voice, voiceS, clairnet, clairnetS, marimba, marimbaD, piano, pianoS
# 16) <not run>
# 17) Timbre V: trumpetS, violinS, voiceS, clairnetS, marimba, piano
# 18) inverted/ through bar gestures
# 19) Timbre VI: 
# 20) Freeze on strike
# 21) Altered speed (pitch level e with slow, regular, fast versions of long and short.  Undamped edg tones
# 22) Swapped files (split gesture, mix and match "before" and "during" components
# 23) Like 20 (Freeze on strike) but including original gesture as well
# 24) Timbre videos
# 25) Timbre VII: attempt to match sine with marimba (290, 380, 600)
# 26) Time, Velocity, Distance manipulation for James's DMP
# 27) Percussive and gradual onsets with matched percussive offsets
# 28) Same as 27 but without sustain on gradual onsets
# 29) Timbre videos but with horn instead of trumpet.  Intended for paid subjects
# UPDATED _x File to add space for aAlone stimuli
# 30) Expanded version of 27 with flat enveloped endpoints (e.g. really long, really short) 
# 31)  Sine wave vs. marimba tone (660, 390, 300 ms - 1 ms onset, 5 offset)
# 32)  Static dot vs. moving dot (staticLong4/staticShort4, el, es) 
# 33)  same as 32 but with static lengths of 500 and 1500 to match Walker & Scott (1981) 
#  Next: Sine wave vs. marimba tone (620, 390, 300 ms - 1 ms onset, 5 offset)
# Flat vs. Perc envelope (400, 635, 1075 perc envelop vs. e,d,g marimba)

# 34) Text (re-run with paid)
# 35) Moving single dot with flat vs. perc enveloped sine waves (620, 390, 300 flat), 400, 625, 1075 perc)
# 36) Moving single dot with flat vs. perc enveloped sine waves (640, 440, 300 flat), 400, 625, 1075 perc)
# 37) Moving single dot with flat vs. perc enveloped sine waves (640, 425, 300 flat), 400, 625, 1075 perc)
# 38) Regular vs. reverse audio (3 lengths @ 220 Hz, Static visual)
# 39) Negative dot (1 vs 2 seconds) and (positive) static dot
# 40) James DMP (const velocity vs. const accel vs. original)
# 41) Attempt II for Exp 34 (Text).  First version didn't include original videos.  Original vs. Text, A + AV
# 42) 37 again but with 3 types of vis long and short (e, d, g) rather than just one (e) 2 reps
# 43) original marimba, perc envelope pure tone, binary envelope pure tone
# 44) Misc impact sounds in addition to marimba
# 45) Misc impact sounds along with horn and voice to show differences between perc and non-perc
# 46) Timbre quasi-redo.  Using clarinet, voice, french horn, marimba, piano sounds.  Hope to get nice clear results for future talks
# 47) Splash 
# 48) Short freeze-on-strike (0, 100, 200, 300 ms) AV, A, V
# 49) Event timing - long and short events with changes of direction (determine the time of ___ driving effect)
# 50) Fadeout 
# 51) Freeze on strike with both shorter and original values
# 52) Freeeze on strike with controlled 100ms spacings
# 53) Expanding/ contracting ball with different
# 54) Dissertation 1 - flat vs. perc redo
# 55) Dissertation 3 - Splash with offsets
# 56) Dissertation 1b- shorter flat sounds
# 57) Dissertation 2 - splash vs. marimba vs. flat;  impact vs. through the bar
# 58) Dissertation 1c- in between flat sounds (572, 355, 228)
# 59) Dissertation 2b - splash vs. marimba vs. flat;  impact vs. through the bar with splashM, and splashS; 320 588 flat, and marimbaE marimbaD sounds
# 60) Dissertation 4 - aud influence on vis
# 61) Dissertation 2c - splash vs. marimba vs. flat;  impact vs. through the bar with 3 splash; 228, 320 588 flat, and 3 marimba sounds
# 62) Dissertation 2.1a -  marimba vs. flat;  impact vs. through the bar with 3 splash; 228, 320 588 flat, and 3 marimba sounds
# 63) Dissertation 2.1b -  marimba vs. clarinet vs. splash;  impact vs. through the bar with 2 splash (L, S); 2 clarinet, and 2 marimba (E, D) sounds

# 66) Dissertation 2.1c -  2 kinds of splash with horizontal lines (serving as a visual control condition)

# 66) Dissertation 2.1d -  3 kinds of splash (plus voice (2) and horn (1) with revised through bar (stopping)

# 67) Dissertation 1.1a - repeat 56 but with harmonic tones (flat and perc, same lengths as in 56)
# 68) Dissertation 5a -  comparing offsets with dynamic and static
# 69) Dissertation 5b - trying to roughly equate lengths of static and dynamic dots
# 70) Dissertation 5c - trying to roughly equate lengths of static and dynamic dots, part 2
# 71) Exploartion - Replicating JEP offset experiment (plus/minus 400, 700 ms offset) with perc tones and d, e gestures  TYPO!! (-.7-.4)
# 72) Exploartion - Replicating JEP offset experiment (plus/minus 400, 700 ms offset) with perc tones and d, e gestures FIXED (-.7, -.4)
# 73) Dissertation 1.3 - repeating 67 (56) with whitenoise (flat and perc, same lengths as in 67)
# 74) Dissertation 1.4 - perc vs. reverse perc envelopes - 400, 675, 1075ms.  Using lengths from 67 and 73, since this seems better
# 75) Dissertation 1.4b - perc vs. reverse perc envelopes from 38 (why are they bettered matched?!)
# 76) Dissertation 1.4c - Perc vs. Reverse - trying to get lengths equated.  Using standard 1075, 675, 400 perc, and trying 750, 390, and 250 reverse
# 77) Dissertation  - repeat 67 but use forward and reverse percussive harmonic tones (Perc: 1075, 675, 400; Reverse: 730, 360, 225)
# 78) Dissertation 1.4c -Same as 76 but with Perc vs. Reverse - trying to get lengths equated.  Using standard 1075, 675, 400 perc, and trying 750, 390, and 250 reverse
# 79) Dissertation 4b - aud influence on vis (this time with matched pre-impact information).  From gesture for pitch E
# 80) Dissertation 4c - aud influence on vis (this time with matched pre-impact information). From gesture for pitch D
# 81) future work - attempting to equate post-impact gesture with static dot (needed for later work on static vs. moving dots)
# 82) future work - continue 81 but with longer dots (original were all too short)
# 83) future work - continue 82 but with longer dots (short static dots were fine, but long were still shorter than gesture)
# 84) Dissertation 1.4d - Perc vs. Reverse - trying to get lengths equated.  Using standard 1075, 675, 400 perc, and trying 580, 300, and 125 reverse (440 Hz)
# 85) Dissertation 1.4d - Perc vs. Reverse - trying to get lengths equated.  Using standard 1075, 675, 400 perc, and trying 580, 300, and 125 reverse (220 Hz)
# 86) Dissertation 1 - still trying for perfect match between perc 1075, 675, 400, and flat 564, 337, 204
# 87) Diss 5 (!?!) - repeat 79 (aud influence on vis for pitch level E) with OFFSETS
# 88) future work - continue 83 but with static lengths of   2500, 2750, 3000  and 3250, 3500, 3750
# 89) future work - matching amplitude of onsets between flat and perc

# -----------
# 90) Sample for autism  project
# Define the global environment

session = SISession(version = __version__.split()[1])
session = session.load()

session.experimentVersion=90       # to be appended to file name
session.experimentSubversion=1  # for use in verifying which experiment is running
session.blockNumber=99          # assume starting witih warmup block
session.metaBlockNumber=1      # default for now, should be handled more elegantly
##trialList = None       # Populated below
##blockSequence = None   # ditto
session.shuffleTrials = True   # switch to control randomizing trials
session. shuffleBlocks = True   # switch to control randomizing blocks


# Show setup GUI
s = SISetupGUI(session)
s.CenterOnScreen()
s.ShowModal()
# for now, ignore warmup block
session.warmup = s.getWarmup()
session.demo = s.getDemoMode()
s.Destroy()

# Record settings as defaults for next time.
session.dump()

(base, ext) = os.path.splitext(session.dataFile)
session.dataFile=base + "-" + str(session.experimentVersion) + ext

# Put the output into its own folder for clarity.
outputDir = os.path.join(session.workingDir,"Output")
if not os.access(outputDir, os.F_OK):
    os.mkdir(outputDir)

dataFileName = os.path.join(outputDir, session.dataFile)


session.experiment = None
ui = None

# Handle setup difference between demo and experiment mode.
if not session.demo:
    session.experiment = Experiment(session)    
    # Initialize data file
    logger = TrialLogger(session, dataFileName, session.experimentVersion, True)
    ui = ExperimentFrame(session, logger)
    ui.queueExperiment() 
else:
    from SensoryIntegration.DemoGUI import *
    ui = ExperimentFrame(session, None)
    editor = DemoGUIFrame(session, ui)
    editor.Show(True);
    
    
ui.Show()  
app.MainLoop()
sys.exit(0)


    

