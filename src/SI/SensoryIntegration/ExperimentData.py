#
# Copyright (C) 2005-2006 University of Virginia
# Supported by grants to the University of Virginia from the National Eye Institute 
# and the National Institute of Deafness and Communicative Disorders.
# PI: Prof. Michael Kubovy <kubovy@virginia.edu>
# Author: Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>
#         Michael Schutz <schutz@virginia.edu>
#
# Distributed under the terms of the GNU Lesser General Public License
# (LGPL). See LICENSE.TXT that came with this file.
#
# $Id: ExperimentData.py 150 2006-08-15 16:36:03Z sfitch $
#

__version__ = '$LastChangedRevision: 150 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>, Michael Schutz <schutz@virginia.edu>'

import TrialData
from random import shuffle, sample

class Experiment(object):
    """
    Encapsulates the block and trial ordering definitions. Behaves somewhat like
    an iterator via the "nextStep()" method
    """
    def __init__(self, session):
        self.session = session
        
        self.steps = []  # This list contains the basic experiment block/message ordering.
        
        # Define the trials
        self.metablock = TrialData.smartSetup(session)

        # Add RA setup message.
        self.steps.append(DialogMessage("RAs please press enter to proceed. \n",
                          "This is experiment version " + 
                          str(session.experimentVersion) + "." + 
                          str(session.experimentSubversion)))

        # Add warmup data if requested.
        if self.session.warmup:
            self._populateWithWarmup()
            
        # Setup block ordering
        blockSequence=range(self.metablock.getNumBlocks())
        if self.session.shuffleBlocks:        
            shuffle(blockSequence)
            
        # Loop over the block sequence ordering, adding blocks and trials to step list
        for index in blockSequence:
            self._populateWithBlockTrials(self.metablock.getBlock(index),blockIntroMessage=True)
        
        # Add completion messate.
        self.steps.append(DialogMessage("Thank you for your time and attention.  \n"+
                                    "If you have any questions, feel "+
                                    "free to ask the research assistant",
                                    "You have now completed the experiment"))
        
    def stepGenerator(self):
        """
        Return a generator function for iterating over the step list.
        """
        for step in self.steps:
            yield step  # Note: "yield" passes control of to the caller, returning
                        # to this exact context when nextStep() is called again.
                        
    def _populateWithWarmup(self):
        """
        Populate the self.steps list with warmup data.
        """
        self.steps.append(DialogMessage("Responses to this section will not be analyzed. "+
                           "The purpose of the warmup period is to demonstrate the "+
                           "range of stimuli used in the actual experiment and give "+
                           "you a chance to practice using the response sliders. \n",
                          "You are about to begin the warmup period"))
 
        blockList=self.metablock.getBlocks()
        warmupBlock=blockList[0]   # for now, assume first block is warmup block
        self._populateWithBlockTrials(warmupBlock, limitSize=True)   # present the warmup block, assume it is block 0

        self.steps.append(DialogMessage("Please note that material in the actual experiment "+
                           "will be similar to what you just experienced \n"+
                           "in terms of both duration and level of agreement",
                           "You have completed the warmup period"))
                         
    def _populateWithBlockTrials(self, aBlock, limitSize=False, blockIntroMessage=True):
        """
        Populate the self.steps list with the given block, followed by all the
        trials in the block (in the order they will be presented.
        """
        enforcedMaxStimuli=15
        
        toJudge="AUDITORY"
        #toJudge="VISUAL"
        typeCode=aBlock.getBlockType()
        if typeCode=="A":
            typeMessage="auditory information alone.  Please judge the duration of the sounds."
        if typeCode=="V":
            typeMessage="visual information alone. Please judge the duration of the motions."
        if typeCode=="AV":
            typeMessage=("both auditory and visual information.  Please judge the duration of the " + toJudge + " information alone."
            + "\n \nAdditionally, you will need to rate the level of agreement between the sounds and the motions.")
                        
        fullMessage = "The following group of trials contains " + typeMessage
            
        # indicate which modality to respond to     
        if blockIntroMessage:
            self.steps.append(DialogMessage(fullMessage,"Duration ratings"))

        self.steps.append(aBlock)
        
        sequence = range(aBlock.getNumTrials())
        if self.session.shuffleTrials:
            shuffle(sequence)

        numStimuli=len(sequence)
        if (limitSize==True and numStimuli > enforcedMaxStimuli):
            showAll=False
        else: showAll=True
    
        if (showAll==False):
            sequence=sample(sequence, enforcedMaxStimuli) # select only 15 trials
        for i in (sequence):
            self.steps.append(aBlock.getTrial(i))
   
   
       
class DialogMessage(object):
    """
    The class encapsulates the step of showing a simple message to the user, and
    waiting for the user to click "OK".
    """
    def __init__(self, message, title):
        self.message = message
        self.title = title
        
    def show(self, parent):
        import wx
        newFont = wx.SystemSettings_GetFont(wx.SYS_DEFAULT_GUI_FONT)

        newFont.SetPointSize(25)
        
        dlg = wx.Dialog(parent, title=self.title, style=wx.DEFAULT_DIALOG_STYLE|wx.STAY_ON_TOP)

        sizer = wx.BoxSizer(wx.VERTICAL)
        dlg.SetSizer(sizer)
        
        
        message = wx.StaticText(dlg, label=self.message)
        message.SetFont(newFont)
        sizer.Add(message, 1, wx.EXPAND|wx.ALL, 10)
        
        ok = wx.Button(dlg, wx.ID_OK, "OK")
        ok.SetDefault()
        
        sizer.Add(ok, 0, wx.ALIGN_CENTER|wx.ALL, 10)
        
        dlg.Fit()
        
        size = dlg.GetSize()
        size.IncBy(100, 100)
        dlg.SetSize(size)

        dlg.Centre();
        
        ##wx.Font(50, wx.DEFAULT, wx.NORMAL, wx.NORMAL))
        
        dlg.ShowModal()        
        dlg.Destroy()
        
        
