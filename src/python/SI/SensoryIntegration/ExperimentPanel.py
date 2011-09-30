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
Upper level container for presenting the stimulus and the response recording area.
"""

__version__ = '$LastChangedRevision: 71 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

import wx
import wx.lib.newevent
import threading

import ExperimentData
import TrialData

from StimulusPanel import *
from ExperimentPanel import *
from ResponsePanel import *

(ExecuteFunctionEvent, EVT_EXECUTE_FUNCTION) = wx.lib.newevent.NewEvent()

class ExperimentPanel(wx.Panel):
    """
    Main container and primary manager for running experiment.
    """
    def __init__(self, parent, session, responseCallback, **kwds):
        wx.Panel.__init__(self, parent, -1, **kwds)
        
        self.sizer = wx.BoxSizer(orient=wx.VERTICAL)
        self.SetSizer(self.sizer)
        
        self.stimulus = StimulusPanel(self, session, self._onTrialEnd)
        self.sizer.Add(self.stimulus, 0, wx.EXPAND)
        
        self.response = ResponsePanel(self, session, responseCallback)
        self.sizer.Add(self.response, 0, wx.EXPAND)
        
    def setBlockType(self, blockType):
        print "--->new Block:", blockType
        if (blockType=="AV"):
            self.response.showAgreement()
        else: 
            self.response.hideAgreement()
    
    def setTrial(self, trial):
        print "--->trial started:", trial.prettyValues()
        self.response.reset()
        self.response.setTrial(trial)
        
        self.stimulus.setTrial(trial)
        self.stimulus.show()
        
    def getTrial(self):
        return self.response.getTrial()
        
    def _onTrialEnd(self):
        print "--->trial ended", self.response.trial.prettyValues()
        self.response.show()
        self.response.SetFocus()
        
class ExperimentFrame(wx.Frame):
    def __init__(self, session, logger, **kwds):
        wx.Frame.__init__(self, None, -1, 
                          style=wx.DEFAULT_FRAME_STYLE & ~ (wx.CLOSE_BOX),
                          **kwds)

        self.session = session
        self.logger = logger
        self.stepGenerator = None
        
        self.SetTitle(session.windowTitle)
        
        self.exp = ExperimentPanel(self, session, self._onResponseRecorded)

        sizer = wx.BoxSizer(wx.VERTICAL)
        sizer.Add(self.exp, 0, wx.EXPAND)
        
        self.SetAutoLayout(True)
        self.SetSizer(sizer)
        sizer.Fit(self)
        sizer.SetSizeHints(self)
        self.Layout()
                
        darea = wx.GetClientDisplayRect()
        wsize = self.GetSize();
        self.SetPosition((darea[0] + (darea[2] - wsize[0])/2, darea[1] + (darea[3] - wsize[1])/2))

        self.Bind(EVT_EXECUTE_FUNCTION, self._onExecuteFunction)

    def _onExecuteFunction(self, evt):
        """
        Handler for "ExecuteFunctionEvent" objets posted when code wants to execute
        some function in the event loop after the currently pending events.
        """
        if not hasattr(evt, "function"): return

        data = None
        
        if hasattr(evt, "data"):
            data = evt.data
            
        if data is not None:
            if isinstance(data, tuple):
                evt.function(*data)
            else:
                evt.function(data)
        else:
            evt.function()

    def _onResponseRecorded(self):
        trial = self.exp.getTrial()
        if self.logger is not None:
            self.logger.log(trial, self.session.metaBlockNumber, self.session.experiment.blockNumber)
        self._loadNextStep()
    
    def _loadNextStep(self):
        """
        Step dispatch function based on base type.
        """
        assert self.stepGenerator is not None
        
        step = None
        
        try:
            step = self.stepGenerator.next()
        except StopIteration:
            self.stepGenerator = None
            pass
            
        if isinstance(step, ExperimentData.DialogMessage):
            step.show(self)
            self._loadNextStep()
        elif isinstance(step, TrialData.Block):
            self.exp.setBlockType(step.getBlockType())
            self.session.experiment.blockNumber += 1
            self._loadNextStep()
        elif isinstance(step, TrialData.Trial):
            self.setTrial(step)
        elif callable(step):
            step()  # For future use
        elif step is None:
            self.logger.closeLog()  # Experiment has ended
        
    def setTrial(self, trial):
        print "\n->setTrial(" + trial.prettyValues() + ")"
        self.exp.setTrial(trial)
        
    def queueExperiment(self):
        assert self.session.experiment is not None
        
        self.session.experiment.blockNumber = 0
        self.stepGenerator = self.session.experiment.stepGenerator()
        evt = ExecuteFunctionEvent(function=self._loadNextStep, data=None)
        wx.PostEvent(self, evt)   
