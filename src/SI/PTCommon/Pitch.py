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
# $Id: Pitch.py 69 2006-03-28 01:49:29Z sfitch $
#

import os
import time
from Formatter import *

    
RT12_2 = 2**(1.0/12)
REF_PITCH = ('A', 4)
REF_FREQ = 440.0
NOTE_NAMES = ['C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#', 'A', 'A#', 'B']
OCTAVES = (1, 2, 3, 4, 5, 6)



class Pitch(object):

    """
    Class representing a value in **pitch format.
    See http://dactyl.som.ohio-state.edu/Humdrum/guide04.html
    """
    def __init__(self, note, octave):
        if note not in NOTE_NAMES:
            raise Error("Invalid note specifier: " + str(note))
        if octave not in OCTAVES:
            raise Error("Invalid octave specifier: " + str(note))
        
        self.note = note
        self.octave = octave
 
        
    def toSemisFromRef(self):
        return self.distance(Pitch(*REF_PITCH))
        
    def toFrequency(self):
        semis = toSemisFromRef()
        return REF_FREQ * RT12_2**semis

    def noteIndex(self):
        return NOTE_NAMES.index(self.note)
        
    def distance(self, pitch):
        """ 
        Compute the distance from this to the given pitch in semitones
        """
        # First determine how many octaves
        octs = self.octave - pitch.octave  
        #print 'octaves ', octs

        # Now difference in semitones
        semis = self.noteIndex() - pitch.noteIndex()
        #print 'semis ', semis
        semis = semis + octs * 12.0
        #print 'total ', semis        
        return semis
        
    def midpoint(self, pitch):
        semis = self.distance(pitch)
        half = round(semis/2)
        print self, pitch, semis
        
        semis = self.toSemisFromRef() - half
        octaves =  round(semis/12)
        semis = int(semis % 12)
        
        ref = Pitch(*REF_PITCH)
        
        indexDiff = ref.noteIndex() - semis
        if(indexDiff < 0):
            octaves = octaves - 1
            semis = semis + 12
        elif indexDiff >= 12:
            octaves = octaves + 1
            semis = semis - 12

        oct = int(ref.octave - octaves)
        note = NOTE_NAMES[semis]
        raise Exception("not working yet")
        return Pitch(note, oct)
        
        
    def __str__(self):
        return "('" + str(self.note) + "', " + str(self.octave) + ")"
        
    def __repr__(self):
        return "Pitch" + self
        
    def __getitem__(self, index):
        if index == 0:
            return self.note
        elif index == 1:
            return self.octave
        else:
            raise IndexError("Index out of range")
            

    
# Placeholder
class PitchFormatter( Formatter ):
    """
    Text formatter for Pitch objects
    """
    __metaclass__ = FormatterMeta

            
import wx
class PitchEditor(wx.Panel):

    def __init__(self, parent):
        wx.Panel.__init__(self, parent)
        self.sizer = wx.FlexGridSizer(cols=2)
        self.SetSizer(self.sizer)

        self.notes = wx.Choice(self, choices=NOTE_NAMES)
        self.sizer.Add(self.notes)
        self.octaves = wx.Choice(self, choices=OCTAVES)
        self.sizer.Add(self.octaves)
           
    def SetValue(self, value):
        # Value comes in as a string
        value = eval(value)
        note = value[0]
        octave = value[1]
        
        try:
            self.notes.SetStringSelection(note)
            self.octaves.SetStringSelection(str(octave))
        except:
            print value, note, octave
            raise

    def GetValue(self):
        return Pitch(self.notes.GetStringSelection(), self.octaves.GetSelection() + 1)
        
        
RTCMIX_COMMAND = "PYCMIX"
RTCMIX_SETUP = """
from rtcmix import *
print_off()
rtsetparams(44100, 2)
load("WAVETABLE")
waveform = maketable("wave", 1000, "sine")           
"""

RTCMIX_PLAY = """
# start time, duration, amplitude, frequency, pan, reference
WAVETABLE(%f, %f, 20000, %f, 0.5, waveform)
"""

class RTcmixPitchPlayer(object):
    def __init__(self):
        path = os.getenv('PATH')
        if path is None:
            path = os.getenv('path')
        if path is None:
            path = os.defpath
        
        dirs = path.split(os.pathsep)

        # Modify path for our use.
        usrlocal = "/usr/local/bin"
        if usrlocal not in dirs:
            dirs.append(usrlocal)
        
        self.command = None
        for d in dirs:
            curr = os.path.join(d, RTCMIX_COMMAND)
            if os.path.isfile(curr):
                self.command = curr
                break;
        
        if self.command is None:
            raise os.error("Could not find RTcmix command '%s'" % RTCMIX_COMMAND)
 
        self.totalTime = None
        self.rtcmix = None
        self.rtcmixout = None
         
    def prepare(self):
        if self.rtcmix != None:
            os.close(self.rtcmix.fileno())
            self.rtcmix = None
        if self.rtcmixout != None:
            os.close(self.rtcmixout.fileno())
            self.rtcmixout = None
            
        (self.rtcmix, self.rtcmixout) = os.popen2(self.command)
        
        # print(RTCMIX_SETUP)
        self.rtcmix.writelines(RTCMIX_SETUP)
        self.rtcmix.flush()
        self.totalTime = 0;

    def add(self, pitch, duration):
        if self.totalTime is None:
            self.prepare()

        # print(RTCMIX_PLAY % (self.totalTime, duration, pitch.toFrequency()))
        self.rtcmix.writelines(RTCMIX_PLAY % (self.totalTime, duration, pitch.toFrequency()))
        self.totalTime = self.totalTime + duration

    def go(self):
        # Tell RTCmix to go by closing the input file descriptor
        self.rtcmix.flush()
        os.close(self.rtcmix.fileno())
        time.sleep(self.totalTime)
        self.totalTime = None
        
if __name__ == "__main__":
#    p = Pitch('A', 4)
#    print p, str(p.toFrequency())
#    p = Pitch('C', 4)
#    print p, str(p.toFrequency())
      
#    for o in OCTAVES:
#        for n in NOTE_NAMES:
#            p = Pitch(n, o)
#            print p.note + str(p.octave) + "\t" + str(p.toFrequency())
            
    player = RTcmixPitchPlayer()

    player.add(Pitch('A', 4), .5)
    player.add(Pitch('C', 5), .5)
    player.add(Pitch('E', 5), .5)
    player.add(Pitch('A', 5), .5)
    player.go()
    
    print 'Done'
    