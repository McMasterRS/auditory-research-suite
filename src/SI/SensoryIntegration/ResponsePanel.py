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
# $Id: ResponsePanel.py 150 2006-08-15 16:36:03Z sfitch $
#

"""
Response screen for Sensory Integration experiment.
"""

__version__ = '$LastChangedRevision: 150 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

import wx

class ResponsePanel(wx.Panel):
    
    def __init__(self, parent, session, responseRecordedCallback, **kw):
        wx.Panel.__init__(self, parent, -1, style=wx.SUNKEN_BORDER, **kw)

        self.session = session
        self.responseRecordedCallback = responseRecordedCallback
        
        self._init_ctrls()
        
        self.boxSizer1.AddSpacer((10,10))

        self.Fit()
        self.Enable(False)
        
    def SetFocus(self):
        super(ResponsePanel, self).SetFocus()
        self.focusPanel.SetFocus()
                                 
    def _init_sizers(self):
        if self.isExperimentSI():
            self.boxSizer1 = wx.BoxSizer(orient=wx.VERTICAL)
            self.flexGridSizer1 = wx.FlexGridSizer(cols=3, hgap=5, rows=0, vgap=5)
        else:
            self.boxSizer1 = wx.BoxSizer(orient=wx.HORIZONTAL)
            self.flexGridSizer1 = wx.FlexGridSizer(cols=5, hgap=5, rows=0, vgap=5)
            
        self.flexGridSizer1.SetMinSize(wx.Size(100, 41))
        self._init_coll_boxSizer1_Items(self.boxSizer1)
        self._init_coll_flexGridSizer1_Items(self.flexGridSizer1)
        self.flexGridSizer1.AddGrowableCol(1)

        self.SetSizer(self.boxSizer1)


    def _init_coll_flexGridSizer1_Items(self, parent):
        if self.isExperimentSI():
            parent.Add(self.staticText1, 0, border=0, flag=wx.ALIGN_RIGHT)
            parent.Add(self.slider1, 0, border=0, flag=wx.EXPAND)
            parent.Add(self.staticText2, 0, border=0, flag=0)
            parent.Add(self.staticText3, 0, border=0, flag=0)
            parent.Add(self.slider2, 0, border=0, flag=wx.EXPAND)
            parent.Add(self.staticText4, 0, border=0, flag=0)
        else:
            parent.AddSpacer((20,10))
            parent.Add(self.first, 0, border=0, flag=wx.ALIGN_LEFT)
            parent.AddSpacer((20,10))
            parent.Add(self.conf, 0, border=0, flag=wx.ALIGN_RIGHT)
            parent.AddSpacer((20,10))

    def _init_coll_boxSizer1_Items(self, parent):
        if self.isExperimentSI():
            parent.Add(self.flexGridSizer1, 0, border=0, flag=wx.EXPAND)
            parent.Add(wx.Size(8, 18), border=0, flag=wx.EXPAND)
            parent.Add(self.button1, 0, border=0,
                      flag=wx.ALIGN_CENTER_HORIZONTAL)
        else:
            parent.Add(self.flexGridSizer1, 0, border=0, flag=wx.ALIGN_CENTER_HORIZONTAL)
            parent.Add(wx.Size(8, 18), border=0, flag=wx.EXPAND)
            parent.Add(self.button1, 0, border=0,
                      flag=wx.ALIGN_CENTER_VERTICAL)
            
        # Need focus panel regardless of mode
        parent.Add(self.focusPanel, 0, border=0, flag=0)

    def _init_ctrls(self):
#        self.SetClientSize(wx.Size(611, 128))
#        self.SetBestFittingSize(wx.Size(387, 128))
        self.SetAutoLayout(True)
        self.focusPanel = wx.Panel(self, -1, (0, 0), None) #holds focus for key events
            
        if self.isExperimentSI():
            self.staticText1 = wx.StaticText(
                  label=u'Short',  parent=self, pos=wx.Point(33,
                  0), size=wx.Size(71, 16), style=wx.ALIGN_RIGHT)
            self.staticText1.SetAutoLayout(False)
    
            self.slider1 = wx.Slider(maxValue=100,
                  minValue=0,  parent=self, point=wx.Point(71, 0),
                  size=wx.DefaultSize, style=wx.SL_HORIZONTAL | wx.SL_AUTOTICKS, value=50)
    
            self.staticText2 = wx.StaticText(
                  label=u'Long', parent=self, pos=wx.Point(496,
                  0), size=wx.Size(32, 16), style=0)
    
            self.staticText3 = wx.StaticText(
                  label=u'Low Agreement', parent=self,
                  pos=wx.Point(0, 23), size=wx.Size(104, 16), style=0)
    
            self.slider2 = wx.Slider(maxValue=100,
                  minValue=0, parent=self, point=wx.Point(99, 18),
                  size=wx.DefaultSize, style=wx.SL_HORIZONTAL | wx.SL_AUTOTICKS, value=50)
    
            self.staticText4 = wx.StaticText(
                  label=u'High Agreement', parent=self,
                  pos=wx.Point(496, 23), size=wx.Size(118, 16), style=0)
            
        else:
            self.ordered = ['Dot', 'Tone', '']
            self.confidence = ['Very confident', 'Somewhat confident', 'Neither confident nor unconfident',
                                    'Somewhat unconfident', 'Very unconfident', '']
            
            self.first = wx.RadioBox(self, label="Which came first?",
                                    choices=self.ordered, majorDimension=2, style=wx.RA_SPECIFY_COLS)
            self.first.SetSelection(len(self.ordered) - 1)
            self.first.ShowItem(len(self.ordered) - 1, False)
            self.conf = wx.RadioBox(self, label="How confident are you of this answer?",
                                    choices=self.confidence, majorDimension=1, style=wx.RA_SPECIFY_COLS)
            self.conf.SetSelection(len(self.confidence) - 1)
            self.conf.ShowItem(len(self.confidence) - 1, False)
        
        #Need these regardless of mode
        self.focusPanel.Bind(wx.EVT_KEY_DOWN, self.onKeyPress)
        self.focusPanel.Bind(wx.EVT_CHAR, self.onKeyPress)
        self.button1 = wx.Button(label=u'OK',
                  parent=self, pos=wx.Point(268, 59),
                  size=wx.Size(75, 20), style=0)
        self.Bind(wx.EVT_BUTTON, lambda event: self.recordResponse(event), self.button1)
        
        self.SetFocus()
        self._init_sizers()       
        
    def onKeyPress(self, event):
        keycode = event.GetKeyCode()
        # Allow return key if selections are made
        if keycode == wx.WXK_RETURN and self.responseReady():
            self.recordResponse(event)
        elif not self.isExperimentSI():
            if keycode == ord('D') or keycode == ord('d'):
                self.first.SetSelection(0)
            elif keycode == ord('T') or keycode == ord('t'):
                self.first.SetSelection(1)
            elif keycode == ord('1') or keycode == wx.WXK_NUMPAD1:
                self.conf.SetSelection(4)
            elif keycode == ord('2') or keycode == wx.WXK_NUMPAD2:
                self.conf.SetSelection(3)
            elif keycode == ord('3') or keycode == wx.WXK_NUMPAD3:
                self.conf.SetSelection(2)
            elif keycode == ord('4') or keycode == wx.WXK_NUMPAD4:
                self.conf.SetSelection(1)
            elif keycode == ord('5') or keycode == wx.WXK_NUMPAD5:
                self.conf.SetSelection(0)
            else:
                event.Skip()
        else:    
            event.Skip()
            
    def responseReady(self):
        if not self.isExperimentSI():
            return self.first.GetSelection() in range(len(self.ordered) - 1) and \
                    self.conf.GetSelection() in range(len(self.confidence) - 1)
        else:
            return True
        
    def isExperimentSI(self):
        return self.session.experimentType == 'SI'

    def setTrial(self, trial):
        self.trial = trial
        
    def getTrial(self):
        return self.trial

    def recordResponse(self, event):
        if (self.responseReady()):
            self.Enable(False)
            
            if self.isExperimentSI():
                self.trial.recordSIResponse(length=self.slider1.GetValue(), agreement=self.slider2.GetValue())
            else:
                dotFirst = self.first.GetSelection() == 0
                confLevel = (self.conf.GetSelection() - 5) * -1
                self.trial.recordTOJResponse(dotFirst, confLevel)
                
            self.SetFocus()
            self.responseRecordedCallback()
            
        else:
            event.Skip()
        
    def show(self):
        if self.isExperimentSI():
            self.slider1.SetValue(50)
            self.slider2.SetValue(50)
        else:
            self.first.SetSelection(len(self.ordered) - 1)
            self.conf.SetSelection(len(self.confidence) - 1)

        # self.Show(True)
        self.SetFocus()
        self.Enable(True)
        
    def reset(self):
        if self.isExperimentSI():
            self.slider1.SetValue(50)
            self.slider2.SetValue(50)
        else:
            self.first.SetSelection(len(self.ordered) - 1)
            self.conf.SetSelection(len(self.confidence) - 1)
        
        self.SetFocus()
        self.Enable(False)
        
        
    """
    Hides agreement slider and text (it is irrelvant in A or V block)
    """    
    def hideAgreement (self):
        if self.isExperimentSI():
            self.slider2.Show(False)
            self.staticText3.Show(False)
            self.staticText4.Show(False) 
            self.slider2.SetValue(50)  
        
        self.SetFocus()

    """
     Shows agreement slider and text (needed only in AV block)
    """   
    def showAgreement (self):
        if self.isExperimentSI():
            self.slider2.Show(True)
            self.staticText3.Show(True)
            self.staticText4.Show(True) 
            self.slider2.SetValue(50)      
        
        self.SetFocus()
        
