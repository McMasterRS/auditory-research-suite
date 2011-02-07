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
# $Id: SISessionData.py 334 2007-04-04 19:26:22Z sfitch $
#

__version__ = '$LastChangedRevision: 334 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

from PTCommon.SessionData import *

class SISession(Session):
    """
    Class/Structure for encapsulating the session configuration
    """
    
    def __init__(self, name = 'SensoryIntegration', version = __version__.split()[1], subject = 1, session = 1): 
        Session.__init__(self, name, version, subject)
        
        self.sequenceFile = self.name + 'SequenceData.txt'        

        self.ra = 1
        self.screenWidth = 720
        self.screenHeight = 480
        self.aspectRatio = self.screenWidth/float(self.screenHeight)
        self.diskSize = 0.1
        self.warmup = True
        self.includeVideo = False
        self.connectTheDots = True
        
    def getValues(self):
        """
        Get the session state as a list of values.
        """
        return [self.ra, self.aspectRatio, self.diskSize, self.includeVideo, self.connectTheDots] + Session.getValues(self)

        
    def getSessionHeader(self):
        """ 
        Get the list of strings describing the values returned by the Session.values() method.
        """
        return [ 'ra', 'aspectRatio' , 'diskSize', 'includeVideo', 'connectTheDots'] + Session.getSessionHeader(self)
        
    def __setstate__(self, state):
        """ Support updates to older persistence files"""
        if 'diskSize' not in state:
            state['diskSize'] = 0.1
        if 'ra' not in state:
            state['ra'] = 1
        if 'warmup' not in state:
            state['warmup'] = True
        if 'includeVideo' not in state:
            state['includeVideo'] = False
        if 'connectTheDots' not in state:
            state['connectTheDots'] = True
        self.__dict__.update(state)
        
