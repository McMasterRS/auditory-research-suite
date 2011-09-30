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
# $Id: Response.py 71 2006-04-05 20:14:09Z sfitch $
#

"""
Container for presenting trial stimulus.
"""

__version__ = '$LastChangedRevision: 71 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

import wx
import wx.media
from Animator import *
import PTCommon.GLUtils as GLUtils

class StimulusPanel(wx.Panel):
    def __init__(self, parent, session, trialEndCallback, **kw):
        wx.Panel.__init__(self, parent, -1, **kw)    
        self.SetBackgroundColour(wx.BLACK)
        
        self.trialEndCallback = trialEndCallback
        
        self.sizer = wx.BoxSizer(wx.VERTICAL)
        self.SetSizer(self.sizer)

        self.ani = AnimationPanel(self, session, trialEndCallback) 
        self.sizer.Add(self.ani, 0, wx.EXPAND)
        self.vid = VideoPanel(self, session, self.swapStimPanel, self.onVidFinished)
        self.sizer.Add(self.vid, 0, wx.EXPAND)
        
        self.vid.Show(False)
        
    def setTrial(self, trial):
        self.trial = trial

    def show(self):
        isVid = self.trial.isVideo()

        self.swapStimPanel()

        if isVid:
            self.vid.play(self.trial.vidFile)
        else:
            self.ani.setTrial(self.trial)
            self.ani.go()
            
    def onVidFinished(self):
        self.trialEndCallback()

    def swapStimPanel(self):
        isVid = self.trial.isVideo()
        self.vid.Show(isVid)
        self.ani.Show(not isVid)
        self.GetParent().Refresh()
        

class AnimationPanel(wx.Panel):
    def __init__(self, parent, session, finishedCallback):
        wx.Panel.__init__(self, parent, -1)
        
        self.sizer = wx.BoxSizer(orient=wx.VERTICAL)
        self.SetSizer(self.sizer)
        
        self.animator = SensoryIntegrationAnimator(
           finishedCallback, session.diskSize, session.connectTheDots)
        self.canvas = GLUtils.PT3DCanvas(self, self.animator)
        self.canvas.SetMinSize((session.screenWidth, session.screenHeight))
        self.sizer.Add(self.canvas, 0, wx.EXPAND)

    def setTrial(self, trial):
        self.animator.setTrial(trial);

    def go(self):
        self.animator.go()

class VideoPanel(wx.Panel):
    def __init__(self, parent, session, prepareCallback, finishedCallback):
        wx.Panel.__init__(self, parent, -1, style=wx.NO_BORDER)
        self.SetBackgroundColour(wx.BLACK)
        try:
            self.mc = wx.media.MediaCtrl(self, style=wx.NO_BORDER)
        except NotImplementedError:
            self.Destroy()
            raise
        
        self.mc.SetBackgroundColour(wx.BLACK)
        self.mc.SetMinSize((session.screenWidth, session.screenHeight))
        self.finishedCallback = finishedCallback
        self.prepareCallback = prepareCallback
        
        self.sizer = wx.BoxSizer(orient=wx.VERTICAL)
        self.SetSizer(self.sizer)
        
        self.sizer.Add(self.mc)
        
        self.Bind(wx.media.EVT_MEDIA_LOADED, self.onMediaLoaded)
        self.Bind(wx.media.EVT_MEDIA_FINISHED, self.onMediaFinished)

    def play(self, path):
        if not self.mc.Load(path):
            wx.MessageBox("Unable to load %s: Unsupported format?" % path, "ERROR", wx.ICON_ERROR | wx.OK)
        
    def onMediaLoaded(self, evt):
        self.prepareCallback()
        import time
        time.sleep(1)
        self.mc.Play()
        
    def onMediaFinished(self, evt):
        self.finishedCallback()
        
        