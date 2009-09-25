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
# $Id: SessionData.py 34 2006-01-19 14:34:09Z sfitch $
#

__version__ = '$LastChangedRevision: 34 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

import csv
import os
from PTCommon.Environment import *
import cPickle as pickle

class Session:
    """
    Class/Structure for encapsulating the session configuration
    """
    
    def __init__(self, name, version = __version__.split()[1], subject = 1, session = 1): 
        self.name = name
        self.version = version
        self.subject = subject
        self.session = session
            
        self.workingDir = getUserHomeDirectory()
        
        self.dataFile = self.name + 'TrialData.txt'
        
    def getValues(self):
        """
        Get the session state as a list of values.
        """
        return [
            self.session, 
            self.subject, 
            self.version, 
            self.name
        ]

    def load(self):       
        """Unpickle self"""
        prefsDir = getUserPreferencesDirectory()
        prefsFile = os.path.join(prefsDir, self.name + '.pkl')
        if os.path.exists(prefsFile):
            f = file(prefsFile, 'r')
            try:
                self = pickle.load(f)
            except EOFError:
                pass 
            except ValueError:
                # Corrupt file
                try:
                    # Try to delete it if possible.
                    os.unlink(prefsFile)
                except:
                    pass
            f.close()
                
        return self
        
    def dump(self):
        """Pickle self"""
        prefsDir = getUserPreferencesDirectory()
        prefsFile = os.path.join(prefsDir, self.name + '.pkl')
        f = file(prefsFile, 'w')
        pickle.dump(self, f)
        f.close()
        
    def getSessionHeader(self):
        """ 
        Get the list of strings describing the values returned by the Session.values() method.
        """
        return [
            'session', 
            'subject', 
            'version', 
            'name'
        ]
