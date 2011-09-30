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
# $Id: SISetupGUI.py 334 2007-04-04 19:26:22Z sfitch $
#

__version__ = '$LastChangedRevision: 334 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

from PTCommon.Formatter import *
from PTCommon.SessionSetupGUI import *

class SISetupGUI(SessionSetupGUI):
    """GUI Screen for accepting setup parameters."""
    def __init__(self, session, parent=None, id=-1, title='Setup'):
                      
        ctrls = [
            ('ra', 'RA ID:', UIntFormatter()),
            ('subject', 'Subject number:', IntFormatter()),
            ('session', 'Session number:', UIntFormatter()),
            ('screenWidth', 'Screen width:', UIntFormatter()),
            ('screenHeight', 'Screen height:', UIntFormatter()),
            ('aspectRatio', 'Data set aspect ratio:', UFloatFormatter()),
            ('diskSize', 'Joint disk size:', UFloatFormatter()),
            ('workingDir', 'Data directory:', FilePathFormatter()),
            ('dataFile', 'Data file name:', StringFormatter()),
            ('includeVideo', "Include video stimuli:", BooleanFormatter()),
            ('connectTheDots', "Connect joints with lines:", BooleanFormatter())
        ]
                      
        SessionSetupGUI.__init__(self, session, ctrls, parent, id=id, title=title)
        
        # Pseudo auto size...
        self.Fit()
        size = self.GetSize()
        size[0] = max(600, size[0])
        self.SetSize(size)
        
    def addExtraWidgets(self):
        """
        Base class hook for appending additional widgets. (can probably go
        away once SessionSetupGUI supports boolean properties):
        """
        self.sizer.Layout()
        currRows = self.sizer.GetRows();
        
        self.sizer.Add(wx.StaticText(self, label="Warmup:"), flag=wx.ALIGN_RIGHT, pos=(currRows, 0))
        self.warmup = wx.CheckBox(self, label='')
        self.warmup.SetValue(True)
        self.sizer.Add(self.warmup, pos=(currRows, 1))
        currRows = currRows + 1
                
        # Filler
        self.sizer.Add(wx.StaticText(self), pos=(currRows, 0))
        currRows = currRows + 1

        self.demo = wx.CheckBox(self, label='Demo mode')
        self.sizer.Add(self.demo, pos=(currRows, 1))
        
    def getWarmup(self):
        return self.warmup.GetValue()
        
    def getDemoMode(self):
        return self.demo.GetValue()
        