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
# $Id: AudioClip.py 158 2006-08-23 18:25:24Z sfitch $
#

__version__ = '$LastChangedRevision: 158 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>, Michael Schutz <schutz@virginia.edu>'

sndCache = {} # Cache of pre-loaded sounds

class AudioClip(object):
    """Abstraction around sound data and playback"""
    
    def __init__(self, sndFile):
        self.sndFile = sndFile
        self.sndData = self._initSound(sndFile)
        
    def _initSound(self, sndFile):
        """ 
            Do the work of getting sound data from the given file, and caching it
            for subsequent requests. 
        """
        if not hasattr(sndCache, sndFile):
            sndCache[sndFile] = self.loadSound(sndFile)
        return sndCache[sndFile];   
    
    def loadSound(self, sndFile):
        """
        Load the sound file and return the loaded data.
        """
        return None
    
    def play(self):
        pass

    def isPlaying(self):
        return False;
    
    def __str__(self):
        import os.path
        return os.path.basename(self.sndFile)
   
   
class PyGameAudioClip(AudioClip): 
    import pygame.mixer
    SAMPLE_RATE=48000
    pygame.mixer.init(SAMPLE_RATE)    
    def __init__(self, sndFile):
        AudioClip.__init__(self, sndFile)
    
    def loadSound(self, sndFile):    
        import pygame.mixer
        return pygame.mixer.Sound(sndFile) 

    def play(self):
        print "---->playing", self
        self.sndChannel = self.sndData.play()

    def isPlaying(self):
        return self.sndChannel != None and self.sndChannel.get_busy()
    
    
class WxSoundAudioClip(AudioClip):
    import wx
    def __init__(self, sndFile):
        AudioClip.__init__(self, sndFile)
  
    def loadSound(self, sndFile):
        retval = wx.Sound(sndFile)
        if not retval.IsOk():
            raise Exception("IsOk is not ok")           
        return retval
    
    def play(self):
        print "---->playing", self
        self.sndData.Play()

    def isPlaying(self):
        return false
        # TODO: figure out why IsPlaying() exists in some wxPython builds, but not others.
        #return self.sndData != None and self.sndData.IsPlaying()

class WxMediaAudioClip(AudioClip):
    def __init__(self, sndFile):
        AudioClip.__init__(self, sndFile)
        self.finished = False
        self.mc = None
        
    def _initSound(self, sndFile):
        import wx.media
        self.mc = wx.media.MediaCtrl(wx.GetApp().GetTopWindow())
        self.mc.Show(False)
        
        self.mc.Bind(wx.media.EVT_MEDIA_FINISHED, self._onMediaFinished)
        if not self.mc.Load(sndFile):
            raise IOError("Unable to load " + sndFile)
        
        return None
    
    def play(self):
        print "---->playing", self
        self.finished = False
        if self.mc is not None:
            self.mc.Play
            
    def isPlaying(self):
        return not self.finished

    def _onMediaFinished(self):
        self.finished = True