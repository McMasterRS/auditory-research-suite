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
# $Id:Animator.py 12 2005-11-21 14:59:18Z sfitch $
#

"""
Animation Stimulus
"""

__version__ = '$LastChangedRevision:12 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

from OpenGL.GL import *
from OpenGL.GLUT import *
from OpenGL.GLU import *
from TrialData import *
import PTCommon.GLUtils
import time

glBlack = (0, 0, 0, 1)
glWhite = (1, 1, 1, 1)

class SensoryIntegrationAnimator(object):
    
    def __init__(self, finishedCallback=None, diskSize=0.1, connectTheDots=True):
        object.__init__(self)
        
        self.finishedCallback = finishedCallback
        self.frame = None
        self.diskSize = diskSize
        self.connectTheDots = connectTheDots
        self.trial = None
        self.aspectRatio = 1
        self.renderer = Renderer(self.diskSize)
        self.canvas = None
        
    def setTrial(self, trial): 
        self.trial = trial
        self.aspectRatio = trial.aspectRatio
        self.frame = None
        
    def go(self):
        if self.canvas is None:
            raise Exception("No canvas to start animation")
        self.startTime = time.time()
        self.canvas.AnimateStop() # Sanity check?
        self.canvas.AnimateStart()

    def init(self, canvas):
        if not isinstance(canvas, PTCommon.GLUtils.PT3DCanvas):
            raise Error("Canvas must be type PT3DCanvas")
        
        self.canvas = canvas
        glClearColor(*glBlack)
        glMatrixMode(GL_MODELVIEW)
        glLoadIdentity()

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)                
        glEnable(GL_LINE_SMOOTH)

    def resize(self, width, height):
        self.size = (width, height)
        glMatrixMode(GL_PROJECTION)
        gluOrtho2D(0, width, 0, height)
        glMatrixMode(GL_MODELVIEW)

    def draw(self):
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT)
        
        if self.trial is not None:
            elapsedTime = time.time() - self.startTime
            #print "Animator.draw()", self.startTime, elapsedTime
            if self.trial.isDone(elapsedTime):
                self.canvas.AnimateStop()
                if self.finishedCallback is not None:
                    self.finishedCallback()
                return
            else:
                frame = self.trial.getFrameState(elapsedTime)
                self.renderer.frame = frame
            
                self.renderer.screenSize = self.size
                self.renderer.aspectRatio = self.aspectRatio
                self.renderer.connectTheDots = self.connectTheDots
                self.renderer.draw()

class Renderer(object):
    def __init__(self, diskSize):
        self.screenSize = (10, 10)
        self.diskSize = diskSize
        self.aspectRatio = 1
        self.init = False
        self.connectTheDots = True
                                 
    def _initDisk(self):
        quad = gluNewQuadric()
        gluQuadricDrawStyle(quad, GLU_FILL)
                
        # Construct representative disk
        self.diskList = glGenLists(1)
        glNewList(self.diskList, GL_COMPILE)
        gluDisk(quad, 0, self.diskSize, 50, 1)        
        glEndList()  
        gluDeleteQuadric(quad)
        self.init = True

    def _scale(self):
        self.xScale = self.screenSize[0]/10
        self.yScale = self.xScale / self.aspectRatio
        glScale(self.xScale, self.yScale, 0)
                
    def draw(self):
        if self.frame is None:
            return
        
        if not self.init:
            self._initDisk()
        
        glLineWidth(1.0);

        # Clear the modeview matrix
        glMatrixMode(GL_MODELVIEW)
        glLoadIdentity()

        self._scale()
        lum = self.frame[1]
        # print self.frame[2], lum

        glColor(lum, lum, lum)
      
        # Draw the current object positions.
        # Joint connectors
        if self.connectTheDots:
            glBegin(GL_LINE_STRIP)
            for pt in self.frame[0]:
                glVertex(pt[0], pt[1], 0)
            glEnd()
        
        # Joint positions
        for pt in self.frame[0]:
            glPushMatrix()
            glTranslate(pt[0], pt[1], 0)
            glScale(1/self.aspectRatio, 1, 1);
            glCallList(self.diskList)            
            glPopMatrix()
            
        