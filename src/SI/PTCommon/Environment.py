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
# $Id: Environment.py 14 2005-11-22 19:09:10Z sfitch $
#

__version__ = '$LastChangedRevision: 14 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

import csv
import os
import sys

def getUserHomeDirectory():
    if sys.platform == 'win32':
        home = os.environ['USERPROFILE']
        home = os.path.join(home,'My Documents')     
    else:
        home = os.environ['HOME']
        
    return home
    
def getUserPreferencesDirectory():
    if sys.platform == 'win32':
        prop = os.environ['USERPROFILE']
    elif sys.platform == 'darwin':
        prop = os.environ['HOME']
        prop = os.path.join(prop, 'Library')
        prop = os.path.join(prop, 'Preferences')
    else:
        prop = os.environ['HOME']
        
    return prop
    
