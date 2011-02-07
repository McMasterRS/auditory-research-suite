#
# Copyright (C) 2005-2006 University of Virginia
# Supported by grants to the University of Virginia from the National Eye Institute 
# and the National Institute of Deafness and Communicative Disorders.
# PI: Prof. Michael Kubovy <kubovy@virginia.edu>
# Author: Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>
#         Michael Schutz <schutz@virginia.edu>
#
# Distributed under the terms of the GNU Lesser General Public License
# (LGPL). See LICENSE.TXT that came with this file.
#
# $Id: TrialData.py 416 2008-02-01 22:08:20Z mschutz $
#

__version__ = '$LastChangedRevision: 416 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>, Michael Schutz <schutz@virginia.edu>'

import csv
import os
from random import *
import wx

from AudioClip import *

def notifyError(title, message):
    dlg = wx.MessageDialog(None, 
          title, message,
          wx.OK | wx.ICON_EXCLAMATION | wx.STAY_ON_TOP)
    dlg.ShowModal()
    dlg.Destroy()    
            
            
class Trial(object):
    """
    Class/Structure for encapsulating trial configuration
    dataFile: file containing animation points 
    aspectRatio: (XXX Mike: how is this different from the one in SISession?)
    sndFile: audio file
    sndSilence: duration in sound file before tone commences (in sections)
    sndDelay: number of seconds to delay playing sound
    vis: ?
    aud: ?
    pointLimit: if set, the maximum number of points to render
    pitchVisualCode: ?
    window: if set, a 2-tuple representing the (start, end) time of the
                animation "window" to render.
    """
    
    
    def __init__(self, dataFile, experimentType, aspectRatio, sndFile, 
                  sndSilence, sndDelay, pitch, vis, aud, 
                  pointLimit=None, pitchVisualCode=None, window=None, vidFile=None):
                      
        object.__init__(self)
        
        if dataFile is None and sndFile is None and vidFile is None:
            raise ValueError("All stimulus sources (animation, sound, video) are set to None for " + pitch + vis + aud)
        
        self.dataFile = dataFile
        self.experimentType = experimentType
        self.sndFile = sndFile
        self.aspectRatio = aspectRatio
        self.sndSilence = sndSilence
        self.sndDelay = sndDelay
        self.window = window
        if window is not None and len(window) != 2:
            raise TypeError("\"window\" attribute expected to be a 2-tuple or None")
        self.vidFile = vidFile    # should only be set if dataFile is not
        self.aud = aud            # redundant but simplifies analysis
        self.vis = vis            # redundant but simplifies analysis
        self.pitch = pitch        # redundant but simplifies analysis
        if (pitchVisualCode==None):    # if unspecified just use 'x'
            self.pitchVisual="x"    
        else: self.pitchVisual=pitchVisualCode  # otherwise record actual code
        self.pitchVisualCode=pitchVisualCode
        
        self.pointLimit = pointLimit
        self.index = -1
        self.frames = []
        self.aniOffset = 0
        self.sndOffset = 0       
        self.sndPlayed = False       
        self.lengthResponse = None
        self.agreementResponse = None     
        self.orderResponse = None
        self.confResponse = None    
        self.totalDuration = 0
        self.audio = None

        if dataFile is not None:
            if(dataFile.endswith(".txt")): 
                try:
                    self._parse(dataFile)
                    self._findMinFrame()
                except IOError, e:
                    print e
                    notifyError("File missing",
                                "Could not find file " + dataFile + ".")
                    return
                try:
                    if sndFile is not None:
                        self.audio = PyGameAudioClip(sndFile)

                except IOError, e:
                    print 'Error loading ', sndFile, ': ', e
                    notifyError("File missing",
                                "Could not load file " + sndFile + ".")
                        
                                     
    def _findMinFrame(self):
        # Index of frame with minimum y value in first coord.
        self.minPosFrame = -1
        minPos = 1e100
        for i, f in enumerate(self.frames):
            pos = f.coords[0][1]
            if pos < minPos:
                minPos = pos
                self.minPosFrame = i
                
        # print "Min frame: " + str(self.minPosFrame) + ", " + str(self.frames[self.minPosFrame].t)
        
        # Compute the offsets used to determine when to start playing the sound
        # and how much to delay the animation.

        minFrameTime = self.frames[self.minPosFrame].t
        #print "minFrameTime: " , minFrameTime, "sndSilence: ", self.sndSilence

        # Handle difference between impact point and sound initiation
        self.aniOffset = self.sndSilence - minFrameTime

        # Set delay
        self.sndOffset = self.sndDelay
            
        # Convert negative offsets into positive ones by swapping offsets
        if self.aniOffset < 0:
            self.sndOffset = self.sndOffset - self.aniOffset
            self.aniOffset = 0
        
        if self.sndOffset < 0:
            self.aniOffset = self.aniOffset - self.sndOffset
            self.sndOffset = 0

        # print "self.aniOffsset ", self.aniOffset, "self.sndOffset ", self.sndOffset

        # Total duration of animation and sound, including offsets
        self.totalDuration = self.frames[-1].t + self.sndOffset + self.aniOffset

    def isVideo(self):
        return self.vidFile != None

    def getValues(self):
        """
        Get the trial state as a list of values.
        """
        if (self.pitchVisualCode!=None):
            self.pitchVisualValue=self.pitchVisualCode
        else: self.pitchVisualValue="."
        
        
        displayType=self.getDisplayType()
        if self.experimentType == 'SI':
            return [
                self.pitch,
                self.aud,
                self.pitchVisualValue,
                self.vis,
                self.sndDelay,
                self.lengthResponse,
                self.agreementResponse,
                self.dataFile,
                self.sndFile,
                self.sndSilence,
                self.pointLimit,
                self.vidFile,
                displayType
            ]        
        else:
            return [
                self.pitch,
                self.aud,
                self.pitchVisualValue,
                self.vis,
                self.sndDelay,
                self.orderResponse,
                self.confResponse,
                self.dataFile,
                self.sndFile,
                self.sndSilence,
                self.pointLimit,
                self.vidFile,
                displayType
            ]
     
    def getDisplayType(self): 
        audio="<NONE>"
        video="<NONE>"
        visual="<NONE>"
        
        if (self.vidFile!=None):
            video=self.vidFile
            
        if (self.dataFile!=None):
            visual=self.dataFile
            
        if (self.sndFile!=None):
            audio=self.sndFile
            
        displayType="undefined" 
        
        # guard agains case where file is not defined    
        if ((self.vidFile!=None) & (self.dataFile==None) & (self.sndFile==None)):
            displayType="video"
        if ((self.vidFile==None) & (self.dataFile!=None) & (self.sndFile!=None)):
            displayType="animation"
        
        #print ("video=" + video + "\n audio=" + audio + "\n visual=" + visual)
        return displayType
    
    def prettyValues(self):
        if self.pitchVisualCode==None:
            retStr=(self.pitch + self.vis + self.aud)
        else: retStr=("V:" + self.pitchVisual + self.vis + ", A:" + self.pitch + self.aud)
        return (retStr + " (pts=" + str(self.pointLimit) + ", off: " + str(self.sndDelay) + ", " + "disp: " + self.getDisplayType() + ")" ) # " d(" + str(self.sndDelay) + ")"
    
    def getTrialHeader(self):
        """ 
        Get the list of strings naming the values returned by the values() method.
        """
        if self.experimentType == 'SI':
            return [
                #'trialNumber',
                #'metaBlock',
                #'block',
                'pitch',
                'aud',
                'pitchVisual',
                'vis',
                'delay',
                'duration',
                'agreement',
                'audFile',
                'visFile',
                'sndSilence',
                'numPoints',
                'vidFile',
                'displayType',
            ]   
        else:
            return [
                #'trialNumber',
                #'metaBlock',
                #'block',
                'pitch',
                'aud',
                'pitchVisual',
                'vis',
                'delay',
                'dotFirst',
                'confidence',
                'audFile',
                'visFile',
                'sndSilence',
                'numPoints',
                'vidFile',
                'displayType',
            ] 
            
        
    def _parse(self, filename):
        f = file(filename, 'rU')

        # See: http://www.python.org/doc/2.3.2/lib/csv-fmt-params.html
        reader = csv.reader(f, dialect="excel-tab")
    
        # The first row is the file header
        headerVals = reader.next()
    
        for row in reader:
            if len(row) < 3: continue
            vals = []
            for v in row:
                try:
                    vals.append(float(v))
                except ValueError:
                    # Getting here means we got to the pit/vis markers at the end
                    break
            if len(vals) == 0: continue
            
            
            lum = 1
            # This is a hack to support reading old animation files as well
            # as new ones that have had a luminance specifier tacked onto the
            # end of the line.
            if len(row) % 2 == 0:
                try:
                    lum = float(row[-1])
                except ValueError:
                    pass
                
            frame = Frame(vals[0], vals[1:], lum)
            
            self.frames.append(frame)

        f.close()
        self.index = 1
        
        if len(self.frames) < 1:
            raise IOError("No data values parsed in '" + filename + "'")
        
    def _interpolate(self, t, f1, f2):
        alpha = (t - f1.t)/(f2.t - f1.t)
        
        # clamp to better ensure we represent the true values at each frame
#        if alpha < .333:
#            alpha = 0
#        elif alpha < .666:
#            alpha = 0.5
#        else:
#            alpha = 1
            
        coords = []
        
        num = min(len(f1.coords), len(f2.coords))

        # Limit points if requested
        if(self.pointLimit is not None):
            num = min(num, self.pointLimit)

        for i in range(num):
            v00 = f1.coords[i][0]
            v01 = f1.coords[i][1]
            v10 = f2.coords[i][0]
            v11 = f2.coords[i][1]
            coords.append(v00 + alpha * (v10 - v00))
            coords.append(v01 + alpha * (v11 - v01))
        
        lum = f1.luminance + alpha * (f2.luminance - f1.luminance) 
        return Frame(t, coords, lum)

    def chkSnd(self, t):
        # Might be "None" if load error.
        if self.audio is not None:
            if t >= self.sndOffset and not self.sndPlayed:
                # print "t = " + str(t) + " playing: " + str(self.sndFile)
                self.audio.play()
                self.sndPlayed = True
                
    def isDone(self, t):
        if self.totalDuration > 0:
            return t > self.totalDuration and self.audio != None and not self.audio.isPlaying()
        else:
            return self.audio != None and not self.audio.isPlaying()
       
    def getFrameState(self, t):
        """
        Compute the joint positions and luminance values at the given time.
        Returns a 3-tuple containing a coordinate pair and luminance value, and time:
        ((x,y), lum, t) where lum->[0.0,1.0], or None if time value outside range
        """
        # XXX hack! Get this out of here. Sound should be handled via a hook
        # or some other abstraction.
        self.chkSnd(t)

        t = t - self.aniOffset

        if len(self.frames) == 0: 
            # If sound playback is occurring then we return an offscreen point
            # Otherwise, we return None which triggers the animator to stop the 
            # trial.
            if self.audio is not None and self.audio.isPlaying():
                return [(-100,-100)]
            else:
                return None
            
        # Sanity check
        if self.index >= len(self.frames):
            raise IndexError("index: " + str(self.index) + " len: " + str(len(self.frames)))
            
        # Check to see if we are inside the visual window. 
        if self.window != None:
            if t < self.window[0]:
                t = self.window[0]
            elif t > self.window[1]:
                if t < self.totalDuration:
                    t = self.window[1]
                else:
                    self.index = 1
                    return None
                
        # Find the frames that are on either side of the current time value.
        while self.frames[self.index].t <= t: 
            self.index += 1
            if self.index >= (len(self.frames) - 1): 
                self.index = 1
                return None

        # Now that we have fount the two frames on either side of the current time
        # create a new frame that interpolates the values between the data points.
        f = self._interpolate(t, self.frames[self.index-1], self.frames[self.index])

        # If interpolation worked return data, otherwise None
        if f is not None:
            return (f.coords, f.luminance, t)
        else:
            return None
        
    def recordSIResponse(self, length, agreement):
        # XXX hack to reset sound played flag. Need to move audio playback 
        # and management outside of Trial class
        self.sndPlayed = False
        self.lengthResponse = length
        self.agreementResponse = agreement
        
    def recordTOJResponse(self, dotIsFirst, confLevel):
        # XXX hack to reset sound played flag. Need to move audio playback 
        # and management outside of Trial class
        self.sndPlayed = False
        self.orderResponse = dotIsFirst
        self.confResponse = confLevel
        
    def __str__(self):
        retval = ""
        for f in self.frames:
            retval += str(f) + '\n'
        return retval

    def getDisplayString (self):
        retval = str(self.dataFile) + ", "
        retval = retval + str(self.sndFile) + ", "
        retval = retval + str(self.sndSilence) + ", "
        retval = retval + str(self.sndDelay) + ", "
        retval = retval + str(self.vidFile)
        
        return  retval
        
class Frame(object):
    """Represents a single animation "frame" or placement of all points"""
    def __init__(self, t, coords, luminance=1.0):        
        object.__init__(self)
        self.t = t
        self.luminance = luminance
        
        self.coords = [(coords[i], coords[i+1]) for i in range(0, len(coords), 2) ]

    def __str__(self):
        return str(self.t) + '\t' + str(self.coords)
        

def setupTrialData(session):        
    """
    Generate a list of TrialData instances from the data contained in the given file
    @return Array of Trial objects.
    """
    sequenceFile = os.path.join(session.workingDir, session.sequenceFile)
    # Make sure the sequence input file exists
    exists = os.path.exists(sequenceFile)
    if not exists:
        dlg = wx.MessageDialog(None, 
           "File missing", 
           "Could not find file " + sequenceFile + ".",
            wx.OK | wx.ICON_EXCLAMATION | wx.STAY_ON_TOP)
        dlg.ShowModal()
        dlg.Destroy()
        import sys
        sys.exit(1)

    f = file(sequenceFile, 'rU')
    sequenceFileReader = csv.reader(f, dialect="excel-tab")
    
    # The first row is the file header
    headerVals = sequenceFileReader.next()
    
    retval = []
    for row in sequenceFileReader:
        if len(row) < 3: continue
        # Check for comment character.
        if row[0][0] == '#': continue
        data = os.path.join(session.workingDir, row[0])
        wav = os.path.join(session.workingDir, row[1])
        silence = float(row[2])
        delay = float(row[3])
        retval.append(Trial(data, session.experimentType, session.aspectRatio, wav, silence, delay, None, None, None))
        
    f.close()
    return retval
 
#def lookupSilence (pitch, aud):
def lookupSilence (soundFile):
    """
    Looks up length of introSilence based on observed file value in wav file
    """ 
    silenceTable = {
        "_x.wav" : 0,
        "a_l.wav" : 1.441,
        "a_n.wav" : 1.407,
        "a_p.wav" : 1.437,
        "a_s.wav" : 1.437,
        "d_l.wav" : 1.364,
        "d_n.wav" : 1.39,
        "d_p.wav" : 1.393,
        "d_s.wav" : 1.34,
        "e_l.wav" : 1.291,
        "e_n.wav" : 1.3,
        "e_p.wav" : 1.3, 
        "e_s.wav" : 1.332,
        "g_l.wav" : 1.064,
        "g_n.wav" : 1.064,
        "g_p.wav" : 1.066,
        "g_s.wav" : 1.064,
        "conga.wav" : 0,
    }
    
    retval = 0
    if soundFile is not None:
        basename = os.path.basename(soundFile)
        if silenceTable.has_key(basename):
            retval = silenceTable[basename]
    
    #a={"l":1.441, "n":1.407, "s":1.437, "p":1.437}
    #d={"l":1.364, "n":1.39, "s":1.34, "p":1.393}
    #e={"l":1.291, "n":1.3, "s":1.332, "p":1.3}
    #g={"l":1.064, "n":1.064, "s":1.064, "p":1.066}
    #silenceTable={"e":e, "a":a, "d":d, "g":g}
    
    #if aud =="x":
    #    retval=2.0
    #else: retval=silenceTable[pitch][aud]

    return retval

# checks if pitch/aud combo refer to original file (e.g. marimba note from original experiment)
def isOriginalFile (pitch, aud):
    isOriginal=False
    if ((pitch == "a") or (pitch == "d") or (pitch=="e") or (pitch=="g")):
         if (aud=="s") or (aud=="l") or (aud=="n") or (aud=="p") or (aud=="x"):
            isOriginal=True
    #print ("pitch=" + str(pitch) + ", aud=" + str(aud) + ", isOriginal=" + str(isOriginal))
    return isOriginal
        

def constructDataFileNames(session, pitch, vis, aud, pitchVisualCode=None):
    workingDir = "."
    if(session is not None):
        workingDir = session.workingDir
        
    basePath=workingDir + "/"
    audBase= basePath+ "aud/"
    visBase= basePath + "vis/"
    vidBase= basePath + "video/"
    # FILE NAMES
    audSuffix=[".wav"]  
    visSuffix=["_.txt"]    
    vidSuffixes=[".avi",".dv"] #,".mp4"]
    videoPathExtensions=["original","timbre"] # ["fadeout","text","timbre","original"]  #["offset","sinemix","staticsine","fadeout"] #original
    visPathExtensions=[None,"NoHorizontalEquated","horizontalPath","RateVisual","staticDots"] #,"noHorizontal","eventTime","shortFreeze","Fadeout","freezeAtStrike100"]
    audPathExtensions=[None, "newSounds","newSounds","flatPercExp","harmonics","whitenoiseTones","reverseAudio","matchedPeak"] #,"decayTest","forTimbreVideos","reverseAudio","impactSounds"]
    # file names path extensions pathExt
    # sometimes the pitch level of the gesture differes from the pitch level of the audio
    # this operation sets them to be the same if the visual pitch level isn't specified
    if (pitchVisualCode==None):
        pitchVis=pitch
    else: pitchVis=pitchVisualCode
    
    # AUDIO file
    audFileName="" 
    if (aud != "x"):
        audFileName= pitch 
    audFileName=audFileName + "_" + aud
    audFile=findFile(audBase, audPathExtensions, audFileName, audSuffix, pitch, vis, aud, "AUD")
    
    # VISUAL definition
    #visFileName=visBase
    if (vis != "x"):
        # no pitch info when presenting blank screen
        visFileName= pitchVis
    else: visFileName=""   # no pitch info when vis=="x"
    
    
    visFileName=visFileName  + vis  
    #visFile=visFile + vis + visSuffix
    visFile=findFile(visBase, visPathExtensions, visFileName, visSuffix, pitch, vis, aud, "VIS")
    if (visFile==None):
        visFile="<None>"
    #print ("vis file name is " + visFile + "\n")
    
    
    vidFileName= pitch + vis + aud
    vidFile=findFile(vidBase, videoPathExtensions, vidFileName,vidSuffixes, pitch, vis, aud, "VID")   

    return (audFile, visFile, vidFile)

# checks all possible path extensions and file types searching for a given file name
# if found, full path to file is returned, else None is returned
def findFile(aPath, pathExtensions,fileName,fileTypes, pitch, vis, aud, seekingType=None):
    if (seekingType==None):
        seekingType="?"
    #if (seekingType=="VIS"):
    #    print ("seeking vis file.  aPath=" + str() + "pathExtensions=" + str(pathExtensions) + ", fileName=" + str(fileName) + ", fileTypes=" + str(fileTypes) + ", pva=" + pitch + vis + aud + "\n")
    retFile=None
    potentialFile=None
    for fileType in fileTypes:
        if (pathExtensions!=None):
            for pathExtension in pathExtensions:
                potentialFile=checkForFile(aPath, pathExtension,fileName, fileType)
                if (potentialFile!=None):
                        retFile=potentialFile    # search should be terminated, but how?
                else: ("file not found for extension " + str(pathExtension))
        else: retFile=checkForFile(aPath, None, fileName, fileType)
    if (retFile==None):
        print ("Unable to find file '" + fileName + "' of type " + seekingType + " for pitch=" + pitch + ", vis=" + vis + ", aud=" + aud + ", path=" + aPath + ", extensions=" + str(pathExtensions))
    else: 
        pass
#        print (str(retFile) + " chosen when pitch=" + pitch + ", vis=" + vis + ", aud=" + aud + ", path=" + aPath )   
    return retFile
    
def checkForFile(aPath, pathExtension, fileName, fileType):
    retFile=None
    fileCheck = aPath
    if (pathExtension!=None):
        fileCheck=fileCheck + pathExtension + "/"  
    fileCheck=fileCheck+ fileName + fileType
    # print ("checking for hypo file: " + str(fileCheck) + " . . .")
    # if file exists, then return this
    if (os.path.exists(fileCheck)):
        retFile = fileCheck 
    return retFile
    
def generateTrial (pitch, vis, aud, delay, session, numPoints=None, pitchVisualCode=None, window=None, asVideo=False):
    #print ("entering generateTrial for (" + str(pitch) + str(vis) + str(aud) + ", asVideo=" + str(asVideo) +")")
    """
    pitchVisual - used when a and v pitches aren't identical.  
                  If not specified, pitch parameter functions for both
    """

    (audFile, visFile, vidFile) = constructDataFileNames(session, pitch, vis, aud, pitchVisualCode)
    
    # introSilence = lookupSilence(pitch,aud)             
    introSilence = lookupSilence(audFile)
    
    # Determine the time window around which to show animation. 
    # Set as a 2-tuple representing the upper and lower limits
    # Set to "None" to use all data points in input file
    window = None
    
    # Mike, hack this as desired...
    # windowWidth=.5
    #window = (introSilence-windowWidth, introSilence+windowWidth)
    # window=(0,5)
    #window = (introSilence-0.2, introSilence+0.2)
    
    if (asVideo):
        audFile=None
        visFile=None
        if vidFile is None:
##            notifyError("Required video file can't be found.",
##                        "WARNING: skipping trial \"" + pitch + vis + aud + '"')
            return None
    else: 
        vidFile=None
        if audFile is None and visFile is None:
##            notifyError("Neither animation nor sound file can be found.",
##                        "WARNING: skipping trial " + pitch + vis + aud)
            return None
            
    #print ("asVideo=" + str(asVideo))
#    print ("Treating as video = " + str(asVideo) + ", visFile=" + str(visFile) + ", audFile=" + str(audFile) + ", vidFile=" + vidFile)
    
    retTrial = Trial(visFile,
                     session.experimentType,
                     session.aspectRatio,
                     audFile,
                     introSilence,
                     delay, 
                     pitch, 
                     vis, 
                     aud, 
                     numPoints,
                     pitchVisualCode,
                     window,
                     vidFile)

    # print ("generated trial for " + pitch + vis + aud)

    return retTrial
         
def smartSetup(session):    
    avBlock = Block()
    aBlock = Block()
    vBlock = Block()


#    visTypes=["l","s"]
#    avAudTypes=["l","p",".75static","1.5static"]
#    #avAudTypes=[".75static","1.5static"]
#    #avAudTypes=["n-18","n-6","n-3"]
#    #avAudTypes=[".75Static","1.5Static","sMarimba","pMarimba","l","p"]
#    pitchTypes=["e","d","g"]
#    pitchVisualTypes=[None]
#    numPoints=[4] 
#    numReps=1
#    offsetTypes=[0]
#    delay=0.00
#    videoTypes=[True]
#    aAloneAudTypes=avAudTypes
#    #avVideoPathExtension="offset"
#    includeVblock=False
#    includeAblock=True
#    includeAVblock=True
#    

    
    # next experiment
    #visTypes=["l","s","staticLong2","staticShort2"]
    #avAudTypes=["l","p","sine"]
    
    # v18visTypes=["longRegular","shortRegular","longThrough","shortThrough", "longRegularInverted","shortRegularInverted","longThroughInverted","shortThroughInverted"]
    # 50) visTypes=["FadeL2","FadeS2","static500","static1500"] 
    # v 51
#    visTypes=["elFreezeAtStrike1b","esFreezeAtStrike1","elFreezeAtStrike2b","esFreezeAtStrike2",
#              "shortFreeze200","longFreeze200","shortFreeze100","longFreeze100",
#              "el","es"] 
#   
    #v52
    #visTypes=["el","es"]
    # v53 visTypes=["ContL","ContS","ExpL","ExpS"]
    
     #v21
    #visTypes=["elFast","elRegular","elSlow","esFast","esRegular","esSlow"]
    
    
    
    #v22
    #visTypes=["ls","sl","l","s"]
    
    #v23
    # visTypes=["elFreezeAtStrike1","elFreezeAtStrike2","elFreezeAtStrike3",
    #          "esFreezeAtStrike1","esFreezeAtStrike2","esFreezeAtStrike3", 
    #          "el","es"]
    
    #visTypes=["L","M","S"]  # 26
    #visTypes=["static500","static1500","el","es"]
    #visTypes=["longAccel","shortAccel","longVelocity","shortVelocity","longOriginal","shortOriginal"]
    #visTypes=["l","s"]
    #v17 avAudTypes=["marimba","voiceS","clarinetS","violinS","trumpetS","piano","whitenoise150"]
    # v 19
    #avAudTypes=["marimbaE","marimbaD","voiceS","clarinetS","trumpetS","piano",
    #                                  "voiceP","clarinetP","trumpetP","pianoS",
    #                                  "whitenoise150","whitenoise400"]
    # avAudTypes=["marimbaE","marimbaD","marimbaG"]
    
    # final timbre video experiment!!
    #avAudTypes=["ClarinetL","MarimbaL","PianoL","HornL","VoiceL","WhitenoiseL",
    #            "ClarinetS","MarimbaS","PianoS","HornS","VoiceS","WhitenoiseS"] # for v29 
    
    #avAudTypes=["marimbaE","marimbaD","marimbaG"] 
    
    #avAudTypes=["pText","nText"] 
    #avAudTypes=["n","p","nText","pText"] #,"1300"] #,"440Perc_400","440Perc_850","440Perc_1300"]
                #"220-Long-Backwards","220-Medium-Backwards","220-Short-Backwards"] #26
    #avAudTypes=["Gradual-Long","Gradual-Medium","Gradual-Short",
    #            "Perc-Long","Perc-Medium","Perc-Short",
    #            "Flat-100", "Flat-1400"] 
    #46 avAudTypes=["marimbaD","marimbaE","piano","pianoS","voiceS","voiceP","hornP","hornS","clarinetP","clarinetS"]  
    
    # for V52
    #avAudTypes=["marimbaE","marimbaD","marimbaG"] #,"tweakedSplashM","tweakedSplashS","tweakedSplashL"]  
    # v53avAudTypes=["BagL","BagS","MarimL","MarimS","VoiceL","VoiceS","ViolinL","ViolinS"]
    
    #avAudTypes=["marimbaD","marimbaG","hornS","voice","bongo","cowbell","hihat","timbale","woodtap","piano"]  
    #avAudTypes=["l","p","LinearL","LinearS","PercussionL","PercussionS","Natural","Damped","StaticL", "StaticS"]
    #avAudTypes=["l","LinearL","PercussionL","Natural","Damped","StaticL"]
    #avAudTypes=["l2","n2","p2","s2"]
    #avAudTypes=["l","p"]
    # pitchTypes=["220Perc","440Perc"] #,"e","g","a"]    # ""
    #pitchTypes=["440Perc","220Perc"] #,"e","g","a"]    # ""
    
    # for v52
    #pitchVisualTypes=["0","100","200","300","400","500","600"] #["short","long"] #["TimeDistance","TimeVelocity","VelocityDistance"]#,"d","g"]   #None
    
    # 1) Flat Perc experiment (54 trials - 3*12 + 16)
#    visTypes=["el","es"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms","440Perc-0400ms",   "440Flat-0588ms","440Flat-0390ms","440Flat-0276ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False
#    includeAblock=True
#    includeAVblock=True

    # 1b)  Flat Perc experiment with shorter flat values (54 trials - 3*12 + 16)
#    visTypes=["el","es"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms","440Perc-0400ms",   "440Flat-0555ms","440Flat-0320ms","440Flat-0180ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False
#    includeAblock=True
#    includeAVblock=True

    # 1c)  Flat Perc experiment with shorter flat values (54 trials - 3*12 + 16)
#    visTypes=["el","es"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms","440Perc-0400ms",   "440Flat-0572ms","440Flat-0355ms","440Flat-0228ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False
#    includeAblock=True
#    includeAVblock=True
    
 #   1.1a) Simiarl to 1c, but with harmonic (rather than pure) tones       
#    visTypes=["el","es"]
#    avAudTypes=["100hz(10)Flat-228ms","100hz(10)Flat-355ms", "100hz(10)Flat-572ms",
#                "100hz(10)Perc-400ms", "100hz(10)Perc-675ms", "100hz(10)Perc-1075ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False 
#    includeAblock=True
#    includeAVblock=True
    
    
    # 2) SPLASH, Marimba, Flat sounds (102 trials - 3*24 + 18 + 12)
#    visTypes=["longRegular","shortRegular","longThrough","shortThrough"]
#    avAudTypes=["marimbaE","marimbaD","tweakedSplashM","tweakedSplashL","425msFlat-48K","640msFlat-48K"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[4]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=True
#    includeAVblock=True

   # 2b) SPLASH, Marimba, Flat sounds 102 trials- 3*24 + 3*6 = 3*4) 
#    visTypes=["longRegular","shortRegular","longThrough","shortThrough"]
#    avAudTypes=["marimbaE","marimbaD",
#                "tweakedSplashS", "tweakedSplashM",
#               "440Flat-0320ms","440Flat-0588ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[4]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=True
#    includeAVblock=True

  # 2c) SPLASH, Marimba, Flat sounds 98 trials- 2*36 + 2*9 + 2*4) 

#    visTypes=["longRegular","shortRegular","longThrough","shortThrough"]
#    avAudTypes=["marimbaE","marimbaD","marimbaG",
#                "tweakedSplashS", "tweakedSplashM","tweakedSplashL",
#               "440Flat-0320ms","440Flat-0588ms","440Flat-0228ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=2
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=True
#    includeAVblock=True

  # 2d) SPLASH, Marimba, Flat sounds 68 trials- 2*24 + 2*6 + 2*4) 

#    visTypes=["longRegular","shortRegular","longThrough","shortThrough"]
#    avAudTypes=["marimbaE","marimbaD","marimbaG",
#               "440Flat-0320ms","440Flat-0588ms","440Flat-0228ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=2
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=True
#    includeAVblock=True
#    
 # 2e) SPLASH, Marimba, Flat sounds 68 trials- 2*24 + 2*6 + 2*4) 

#    visTypes=["lThrough-Stop","sThrough-Stop","lImpact-Stop","sImpact-Stop"]
#    avAudTypes=["marimbaE","marimbaD","marimbaG","voiceP","voiceS","hornS","tweakedSplashL","splashRock","splashMud"]
#  
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=2
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=True
#    includeAVblock=True

# 2 ) Horizontal Line
#    visTypes=["lThrough-Stop","sThrough-Stop","lImpact-Stop","sImpact-Stop","horizontalLine2","horizontalLine"]
#    avAudTypes=["marimbaE","marimbaD","splashRock","splashMud"]
#  
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[2]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=2
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=True
#    includeAVblock=True



    # 3) SPLASH OFFSET (72 trials - 3*30 + 9 + 6)
#    visTypes=["longThrough","shortThrough"]
#    avAudTypes=["tweakedSplashS","tweakedSplashM","tweakedSplashL"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[4]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[-.7,-.4,0,.4,.7]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=True
#    includeAVblock=True

    # 4) STATIC vs. DYNAMIC  OFFSET EXPERIMENT (102 trials- 2*48 + 6)
#    visTypes=["el","static1500-1dot","el","es"]
#    avAudTypes=["marimbaE","marimbaD","marimbaG"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[4]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=2
#    offsetTypes=[-0.4, 0, 0.4, 0.7]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False
#    includeAblock=True
#    includeAVblock=True
    
    # 5) RATE A, RATE V (57 trials - 3*12 + 12 + 9)
    
    #  Future things to address - pre-impact motion needs to be matched; should have AV trials rating both A and V (currently only V))V)
    #    REMEMBER TO CHANGE INSTRUCTIONS SO THAT THEY TRY TO IGNORE AUDIO, RATHER THAN VISUAL
#    visTypes=["dl","ds","dn"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms",  "440Flat-0572ms","440Flat-0355ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=True
#    includeAVblock=True
    
    # NEW FOR SPRING 09
    # STILL NEED TO MATCH PRE-IMPACT MOTION
#        
#    visTypes=["dNormal","dShort","dLong"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms",  "440Flat-0572ms","440Flat-0355ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[-.5,0,.5]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=True
#    includeAVblock=True

#  First attempt at comparing static and offsets (68)
#    visTypes=["static900","static1400","el","es"]
#    ##avAudTypes=["440Perc-1075ms","440Perc-0675ms", "440Perc-0400ms"]
#    avAudTypes=["marimbaE","marimbaD", "marimbaG"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=2
#    offsetTypes=[-.5, 0, .5]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=True
#    includeAVblock=True
#  
       
   #  Trying to find rough equivelants of static and moving lengths 
#    visTypes=["staticDot2000","staticDot2250", "staticDot2500","staticDot3500","staticDot3750","staticDot4000","el","es"]
#    ##avAudTypes=["440Perc-1075ms","440Perc-0675ms", "440Perc-0400ms"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms", "440Perc-0400ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=False
#    includeAVblock=True
    
    #  Exp72 - replicating JEP offset experiment
#    #visTypes=["staticDot2250", "staticDot3750","el","es"]
#    visTypes=["el","es","dl","ds"]
#    ##avAudTypes=["440Perc-1075ms","440Perc-0675ms", "440Perc-0400ms"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms", "440Perc-0400ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=2
#    offsetTypes=[-.7,-.4,0,.4,.7]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False
#    includeAblock=False
#    includeAVblock=True
#    
    
    #delay=0.00


    # 1.3) Exp 73 - perc and flat envelopes with white noise
          
#    visTypes=["el","es"]
#    avAudTypes=["whitenoise-flat-228","whitenoise-flat-355", "whitenoise-flat-572",
#                "whitenoise-perc-400", "whitenoise-perc-675", "whitenoise-perc-1075"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False 
#    includeAblock=True
#    includeAVblock=True

#     1.4) Exp 74 - perc and reverse perc envelopes
          
#    visTypes=["el","es"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms", "440Perc-0400ms",
#                "440PercReverse-1075ms", "440PercReverse-0675ms", "440PercReverse-0400ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False 
#    includeAblock=True
#    includeAVblock=True

#     1.4b) Exp 75 - perc and reverse perc envelopes
              
#    visTypes=["el","es"]
#    avAudTypes=["220-Long-Backwards","220-Medium-Backwards", "220-Short-Backwards",
#                "220-Long-Frontwards","220-Medium-Frontwards", "220-Short-Frontwards"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False 
#    includeAblock=True
#    includeAVblock=True
#    
#    

#     1.4c) Exp 76 - perc and reverse perc envelopes
#
#    visTypes=["el","es"]
#    avAudTypes=["220Hz-Forward-1075","220Hz-Forward-675", "220Hz-Forward-400",
#                "220Hz-Reverse-750","220Hz-Reverse-390", "220Hz-Reverse-250"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False 
#    includeAblock=True
#    includeAVblock=True

#     Dissertation backup) Exp 77 - perc and reverse perc envelopes harmonic
#    
#    visTypes=["el","es"]
#    avAudTypes=["100hz(10)Perc-730ms(reverse)","100hz(10)Perc-360ms(reverse)", "100hz(10)Perc-225ms(reverse)",
#                "100hz(10)Perc-400ms", "100hz(10)Perc-675ms", "100hz(10)Perc-1075ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False 
#    includeAblock=True
#    includeAVblock=True
#        

    # 78 - perc and reverse perc envelopes (repeat of 76 but with 440 Hz)
    
#    visTypes=["el","es"]
#    avAudTypes=["440Hz-Forward-1075","440Hz-Forward-0675", "440Hz-Forward-0400",
#                "440Hz-Reverse-0750","440Hz-Reverse-0390", "440Hz-Reverse-0250"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False 
#    includeAblock=True
#    includeAVblock=True

    # 79-80 RATE A, RATE V (57 trials - 3*12 + 12 + 9)
    # 79 used gestures from pitch level E, 80 from pitch level D 
    #  Future things to address - pre-impact motion needs to be matched; should have AV trials rating both A and V (currently only V))V)
    #    REMEMBER TO CHANGE INSTRUCTIONS SO THAT THEY TRY TO IGNORE AUDIO, RATHER THAN VISUAL
#    visTypes=["d-med-long","d-med-med","d-med-short"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms",  "440Flat-0572ms","440Flat-0355ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=True
#    includeAVblock=True
    
    # 81 static vis vs. post-impact motion (rating visual component)
#    visTypes=["staticDot0250","staticDot0500","staticDot0750", "staticDot1000","staticDot1500","staticDot2000","postImpact-E-long","postImpact-E-short"]
#    #avAudTypes=["440Perc-1075ms","440Perc-0675ms", "440Perc-0400ms"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=False
#    includeAVblock=True

    # 82 static vis vs. post-impact motion (rating visual component)

#    visTypes=["staticDot1000","staticDot1500","staticDot2000","staticDot2250","staticDot2500","staticDot3000","postImpact-E-long","postImpact-E-short"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=False
#    includeAVblock=True

#    # 83 static vis vs. post-impact motion (rating visual component)
#
#    visTypes=["staticDot2000","staticDot2250","staticDot2500","staticDot3500","staticDot3750","staticDot4000","postImpact-E-long","postImpact-E-short"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=False
#    includeAVblock=True
#    

    # 84 perc and reverse envelopes (440 hz)
#    
#    visTypes=["el","es"]
#    avAudTypes=["440Hz-Forward-1075","440Hz-Forward-0675", "440Hz-Forward-0400",
#                "440Hz-Reverse-0580","440Hz-Reverse-0300", "440Hz-Reverse-0125"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False 
#    includeAblock=True
#    includeAVblock=True

    # 85 perc and reverse envelopes (220 hz)
#
#    visTypes=["el","es"]
#    avAudTypes=["220Hz-Forward-1075","220Hz-Forward-675", "220Hz-Forward-400",
#                "220Hz-Reverse-0580","220Hz-Reverse-0300", "220Hz-Reverse-0125"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False 
#    includeAblock=True
#    includeAVblock=True
#    
#    

    # 86 perc and flat 

#    visTypes=["el","es"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms","440Perc-0400ms",   "440Flat-0564ms","440Flat-0337ms","440Flat-0204ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False
#    includeAblock=True
#    includeAVblock=True
#    
   # 87 Rate visual (matched pre-impact info) with offsets
            
#    visTypes=["e-med-long","e-med-med","e-med-short"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms",  "440Flat-0572ms","440Flat-0355ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=2 #3
#    offsetTypes=[-.7,-.4,0,.4, .7]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False
#    includeAblock=False
#    includeAVblock=True    
#    
#    

# 88 Static vs. moving dots (attempt 3 to get them matched)

#    visTypes=["staticDot2500","staticDot2750","staticDot3000","staticDot3250","staticDot3500","staticDot3750","postImpact-E-long","postImpact-E-short"]
#    avAudTypes=["440Perc-1075ms","440Perc-0675ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=True
#    includeAblock=False
#    includeAVblock=True
   
  # 89 Perc vs. flat with matched peak volumes 
#    visTypes=["el","es"]
#    avAudTypes=["440Hz-Flat-228ms","440Hz-Flat-355ms", "440Hz-Flat-572ms",
#                "440Hz-Perc-400ms","440Hz-Perc-675ms", "440Hz-Perc-1075ms"]
#    pitchTypes=[""]
#    pitchVisualTypes=[None]
#    numPoints=[1]#,2,3,4] 
#    splitAudioAloneOver=1
#    numReps=3
#    offsetTypes=[0]    
#    videoTypes=[False]
#    aAloneAudTypes=avAudTypes 
#    includeVblock=False 
#    includeAblock=True
#    includeAVblock=True
#    
  # 89 Perc vs. flat with matched peak volumes 
    visTypes=["EL","ES"]
    #avAudTypes=["l","p"]
    avAudTypes=["ClarinetL","MarimbaL","PianoL","HornL","VoiceL","WhitenoiseL"]
             #   "ClarinetS","MarimbaS","PianoS","HornS","VoiceS","WhitenoiseS"] # for v29 
    #pitchTypes=["e","a","d"]
    pitchTypes=[""]
    pitchVisualTypes=[None]
    numPoints=[1]#,2,3,4] 
    splitAudioAloneOver=1
    numReps=1    
    offsetTypes=[0]    
    videoTypes=[True]
    aAloneAudTypes=avAudTypes 
    includeVblock=False
    includeAblock=False
    includeAVblock=True
    
        
 





    #    REMEMBER TO CHANGE INSTRUCTIONS BACK in ExperimentData.py
    
    
    
    #if session.includeVideo:
    #    videoTypes=[True, False]

    #aAloneAudTypes=["whiteNoiseL","whiteNoiseS"]
    
  # -----------------------------------------------------              
    mb = MetaBlock()
    reps=range(numReps)
    audioAloneBlockRange=range(splitAudioAloneOver)               
    # make AV blocks
    if  ((numReps % splitAudioAloneOver)!=0):
        print ("WARNING: audio alone trials split unevenly over blocks (" + 
               str(splitAudioAloneOver) + " blocks with " + str(numReps/splitAudioAloneOver) + " reps EACH)!!")
    
    if (includeAVblock):
        avBlock.setBlockType("AV") 
        for pitch in pitchTypes:        
            for aud in avAudTypes:
                for vis in visTypes:
                    for pitchVis in pitchVisualTypes:
                        for offset in offsetTypes:
                            for videoType in videoTypes:
                                if (videoType==True):     # ignore num points array if using video
                                    smartNumPoints=[1]
                                else: smartNumPoints=numPoints
                                
                                for points in smartNumPoints:
                                    avTrial=generateTrial(pitch, vis, aud, offset, session,points,pitchVis, asVideo=videoType)
                                    avBlock.addTrial(avTrial)
            
    # make A blocks
    if (includeAblock):
        aBlock.setBlockType("A") 
        for r in (range(numReps/splitAudioAloneOver)):
            for pitch in pitchTypes:        
                for aud in aAloneAudTypes:
                    aTrial=generateTrial(pitch, "x",aud , 0, session, asVideo=False)
                    aBlock.addTrial(aTrial) 

    # make V blocks
    if (includeVblock):
        vBlock.setBlockType("V")
        for r in reps:
            for pitch in pitchTypes:        
                for vis in visTypes:
                    for pitchVis in pitchVisualTypes:
                        for points in numPoints:
                            for videoType in videoTypes:
                                vTrial=generateTrial(pitch, vis, "x", 0, session,points, pitchVis, asVideo=videoType)
                                vBlock.addTrial(vTrial) 
    
    
   # If AV block is desired, print info and add to experiment
    if (includeAVblock):
        print (str(numReps) + " AV blocks:" + str(avBlock) + "\n")
        for r in reps:
            mb.addBlock(avBlock)

    # If A block is desired, print info and add to experiment
    if (includeAblock):
        print (str(splitAudioAloneOver) +" A blocks:" + str(aBlock) + "\n")
        for audioAloneBlocks in audioAloneBlockRange:
            mb.addBlock(aBlock)
        
    # If V block is desired, print info and add to experiment
    if (includeVblock):
        print ("V block:" + str(vBlock) + "\n")
        mb.addBlock(vBlock)
    print (str(mb.getNumTrials()) + " total trials (not counting warmup)")
    return mb
 

class Block(object):
    """
    A block is a container for a series of Trials with some (experimentally) meaningful relationship.
    For the SI study, trials will be blocked by conditions: AV (audio-visual), A (audio-alone), and V(video-alone)
    """
    blockType="test"

    def __init__(self):
        self.trials = []
        self.setBlockType("default")
    
    def addTrial(self, trial):
        if trial is not None:
            self.trials.append(trial)
        
    def getTrials(self):
        return self.trials

    def getTrial(self, i):
        t = self.trials[i]
        return t
    
    def getNumTrials(self):
        return len(self.trials)

    def __str__(self, reps=None):
        retval = self.getBlockType() +  " block (" + str(self.getNumTrials()) +")    \n   {\n"                   
        index=1
        if (reps==None):
            maxDisplay=len(self.trials)
        else: maxDisplay=reps
        print ("going to display first " + str(maxDisplay) + " trials")
        
        for t in self.trials:
            retval = retval + "        " +  str(index) + ") "
            retval = retval + t.prettyValues()
            retval = retval + "\n"
            index=index+1
        retval= retval + "   }"  
        return retval    
    
    def setBlockType(self,typeString):
        #print ("in setBlockType with string value of " + typeString)
        self.blockType=typeString
        
    def getBlockType(self):
        return self.blockType
    
    
    
class MetaBlock(object):
    """
    A meta block is a container of blocks.
    Metablocks are useful in ensuring an even distribution of stimuli across an experiment.  
    If we want to present 3 types of blocks (e.g. AV, A, and V) 5 times each in a truely random order
    we will likely end up with many "unfortunate" sequences (either extended runs of the same block type
    or an  unequal distrubtion of block types across the experiment)
    This problem is solved by placing the three block types within a MetaBlock, then repeating the MetaBlock 
    5 times over the course of the experiment which ensures each of the three block types will be presented
    at roughly equivalant points
    """
    
    def __init__(self):
        self.blocks = []
        
    def addBlock(self, block):
        self.blocks.append(block)
                 
    def getBlocks(self):
        return self.blocks

    def getBlock(self, i):
        t = self.blocks[i]
        return t
    
    def getNumBlocks(self):
        return len(self.blocks)
    
    def __str__(self):
        retval = "MetaBlock {\n"                   
        for b in self.blocks:
            retval = retval + "    "
            retval = retval + str(b)
            retval = retval + "\n"
        retval= retval + "}"  
        return retval                 

    def getNumTrials(self):
        totalTrials=0
        for block in self.blocks:
            totalTrials=totalTrials+block.getNumTrials()
        return totalTrials
    