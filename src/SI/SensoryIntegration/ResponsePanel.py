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
                                 
    def _init_sizers(self):
        self.boxSizer1 = wx.BoxSizer(orient=wx.VERTICAL)

        self.flexGridSizer1 = wx.FlexGridSizer(cols=3, hgap=5, rows=0, vgap=5)
        self.flexGridSizer1.SetMinSize(wx.Size(100, 41))

        self._init_coll_boxSizer1_Items(self.boxSizer1)
        self._init_coll_flexGridSizer1_Items(self.flexGridSizer1)
        self.flexGridSizer1.AddGrowableCol(1)

        self.SetSizer(self.boxSizer1)


    def _init_coll_flexGridSizer1_Items(self, parent):
        parent.Add(self.staticText1, 0, border=0, flag=wx.ALIGN_RIGHT)
        parent.Add(self.slider1, 0, border=0, flag=wx.EXPAND)
        parent.Add(self.staticText2, 0, border=0, flag=0)
        parent.Add(self.staticText3, 0, border=0, flag=0)
        parent.Add(self.slider2, 0, border=0, flag=wx.EXPAND)
        parent.Add(self.staticText4, 0, border=0, flag=0)

    def _init_coll_boxSizer1_Items(self, parent):
        parent.Add(self.flexGridSizer1, 0, border=0, flag=wx.EXPAND)
        parent.Add(wx.Size(8, 18), border=0, flag=wx.EXPAND)
        parent.Add(self.button1, 0, border=0,
              flag=wx.ALIGN_CENTER_HORIZONTAL)

    def _init_ctrls(self):
#        self.SetClientSize(wx.Size(611, 128))
#        self.SetBestFittingSize(wx.Size(387, 128))
        self.SetAutoLayout(True)

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

        self.button1 = wx.Button(label=u'OK',
              parent=self, pos=wx.Point(268, 59),
              size=wx.Size(75, 20), style=0)

        self.Bind(wx.EVT_BUTTON, lambda event: self.recordResponse(event))

        self._init_sizers()                                 

    def setTrial(self, trial):
        self.trial = trial
        
    def getTrial(self):
        return self.trial

    def recordResponse(self, event):
        self.Enable(False)
        self.trial.recordResponse(length=self.slider1.GetValue(), agreement=self.slider2.GetValue())
        self.responseRecordedCallback()
        
    def show(self):
        self.slider1.SetValue(50)
        self.slider2.SetValue(50)

        # self.Show(True)
        self.Enable(True)
        
    def reset(self):
        self.slider1.SetValue(50)
        self.slider2.SetValue(50)
        self.Enable(False)
        
        
    """
    Hides agreement slider and text (it is irrelvant in A or V block)
    """    
    def hideAgreement (self):
        self.slider2.Show(False)
        self.staticText3.Show(False)
        self.staticText4.Show(False) 
        self.slider2.SetValue(50)  

    """
     Shows agreement slider and text (needed only in AV block)
    """   
    def showAgreement (self):
        self.slider2.Show(True)
        self.staticText3.Show(True)
        self.staticText4.Show(True) 
        self.slider2.SetValue(50)      
        
