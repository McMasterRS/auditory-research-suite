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
# $Id: TrialLogger.py 207 2006-10-03 16:37:25Z sfitch $
#

__version__ = '$LastChangedRevision: 207 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

import csv
import os
import os.path
import time

class TrialLogger:
    """
    Class responsible for managing the data file and logging each trial to it.

    """
    def __init__(self, session, dataFileName, experimentVersion=None, autoNumberTrials=False):
        """
        Constructor.
        Arguments:
            session: The session configuration object. 
                     Requires the methods "getSessionHeader()" and "getValues()", both
                     returning lists of values to log.
                 
            dataFileName: The file to write/log to
    
            autoNumberTrials: (default: False) if True, keep track of the number of
                              trials written, prefixing each line with this number.
        """
        if autoNumberTrials:
            self.trialNumber=0
        else:
            self.trialNumber=None
            
        self.session = session
        self.experimentVersion = experimentVersion
        
        
        
        (targetFileBaseName, ext) = os.path.splitext(dataFileName)
        if ext is None:
            ext = ".txt"
            
        self.targetFileName = targetFileBaseName + ext
        self.tmpFileName = targetFileBaseName + "-" + time.strftime('%Y%m%d%H%M%S') + ext
        
        self.dataFile = file(self.tmpFileName, 'a')
        self.dataFileWriter = csv.writer(self.dataFile, delimiter='\t')

        
        # If we  created the file, mark as needing a header line for reference
        self.newFile = not os.path.exists(self.targetFileName)
        self.header = None

#        print ("path is: " + os.path.dirname(self.targetFileName), " header needed=" + str(self.newFile)) 
        
    def log(self, trial, metaBlockNumber=None, blockNumber=None):
        """
        Log the given trial object.
        Arguments:
            trial: The trial object to log. Requires the methods "getTrialHeader()" 
                   and "getValues(), both returning lists of values to log.
            metaBlockNumber: (default: None) if set, value will be prepended to the
                             trial's log
            blockNumber: (default: None) if set, value will be prepended to the 
                             trial's log.
        """
        if self.trialNumber != None:
            self.trialNumber=self.trialNumber+1
        
        if self.newFile and self.header is None:
            sh = self.session.getSessionHeader()
            th = trial.getTrialHeader()
            self.header = th + sh + ["timeStamp"]
            if self.experimentVersion != None:
                self.header.insert(0, 'expVersion')
            if blockNumber != None:
                self.header.insert(0, 'block')
            if metaBlockNumber != None:
                self.header.insert(0, 'metaBlock')
            if self.trialNumber != None:
                self.header.insert(0, 'trial')            
            
        tv = trial.getValues()
        sv = self.session.getValues()
        timeStamp=str( time.ctime(time.time()))
        
        retval = tv + sv + [timeStamp]
        
        if self.experimentVersion != None:
            retval.insert(0, self.experimentVersion)
        if blockNumber != None:
            retval.insert(0, blockNumber)
        if metaBlockNumber != None:
            retval.insert(0, metaBlockNumber)
        if self.trialNumber != None:
            retval.insert(0, self.trialNumber)
        
        self.dataFileWriter.writerow(retval)
        self.dataFile.flush()
        

    def closeLog(self):
        self.dataFile.close();

        target = file(self.targetFileName, 'a')
        targetWriter = csv.writer(target, delimiter='\t')
                        
        #print ("mb=" + str(metaBlockNumber) + ", block = " + str(blockNumber))
        if self.newFile:
            targetWriter.writerow(self.header)
            target.flush() 
            
        # Copy contents of temp file to destination file.
        tmp = file(self.tmpFileName, 'r')
        for l in tmp.xreadlines():
            target.write(l)
             
        tmp.close()
        target.close()
         
             