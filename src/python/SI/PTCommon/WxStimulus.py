#
# Copyright (C) 2005 University of Virginia
# Supported by grants to the University of Virginia from the National Eye Institute 
# and the National Institute of Deafness and Communicative Disorders.
# PI: Prof. Michael Kubovy <kubovy@virginia.edu>
# Author: Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>
#
# Distributed under the terms of the GNU Lesser General Public License
# (LGPL). See LICENSE.TXT that came with this file.
#
# $Id: WxStimulus.py 23 2005-12-07 14:37:19Z sfitch $
#

"""
Wrapper around the WX Windows Event loop allowing it to be embedded in the VisionEgg
event loop.
"""

__version__ = '$LastChangedRevision: 23 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

from VisionEgg.Core import Stimulus
import wx
import time

class WxStimulus(Stimulus):
    """
    Class for processing GUI events inside the Presentation event loop.
    """
    def __init__(self, checksPerCall=25, sleepPerCheck=0.01, **kw):
        Stimulus.__init__(self, **kw)
        self.checksPerCall = checksPerCall
        self.sleepPerCheck = sleepPerCheck
        
    def draw(self):
        # Process any GUI events
        # until there are no more waiting.
        wxApp = wx.GetApp()
        if wxApp is None:
            print "wxApp missing"
            return
        
        for i in range(self.checksPerCall):
            while wxApp.Pending():
                wxApp.Dispatch()

            # Send idle events to idle handlers.  
            wxApp.ProcessIdle()
            time.sleep(self.sleepPerCheck)

            