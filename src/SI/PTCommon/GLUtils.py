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
# $Id: GLUtils.py 128 2006-07-04 12:19:29Z sfitch $
#

__version__ = '$LastChangedRevision: 128 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

from OpenGL.GL import *
from OpenGL.GLU import *
import wx
import wx.glcanvas
from math import *
import sys

class PT3DCanvas(wx.glcanvas.GLCanvas):
    """Convenience specialization of the GLCanvas running under wxWidgets"""
    def __init__(self, parent, renderDelegate):
        wx.glcanvas.GLCanvas.__init__(self, parent, -1)
        self.renderDelegate = renderDelegate
        self.init = False
        self.timer = None

        self.Bind(wx.EVT_ERASE_BACKGROUND, self.OnEraseBackground)
        self.Bind(wx.EVT_SIZE, self.OnSize)
        self.Bind(wx.EVT_PAINT, self.OnPaint)
        self.Bind(wx.EVT_TIMER, self.OnAnimate)
        
    def OnEraseBackground(self, event):
        pass # Do nothing, to avoid flashing on MSW.

    def OnSize(self, event):
        size = self.size = self.GetClientSize()
        if self.GetContext():
            self.SetCurrent()
            glViewport(0, 0, size.width, size.height)
        self.renderDelegate.resize(size.width, size.height)
        event.Skip()

    def OnPaint(self, event):
        dc = wx.PaintDC(self)
        self.SetCurrent()
        if not self.init:
            self.InitGL()
            self.init = True
        self.renderDelegate.draw()
        glFlush()
        self.SwapBuffers()

    def InitGL(self):
        self.renderDelegate.init(self)
        
    def AnimateStart(self):
        # start whatever timer we need to fire opengl redraw events.
        if self.timer is not None:
            if self.timer.IsRunning():
                self.timer.Stop()
        else:
            self.timer = wx.Timer(self)
            
        self.timer.Start(1, False);
        
    def AnimateStop(self):
        if self.timer is not None:
            self.timer.Stop()
        
    def OnAnimate(self, evt):
        self.SetCurrent()
        self.renderDelegate.draw()
        glFlush()
        self.SwapBuffers()        
        
        
    
class Circle:
    """Utility class for rendering circle approximations in OpenGL."""
    def __init__(self, radius=1, divisions=100, z=0):
        self.radius = radius
        self.divisions = divisions
        self.z = z
        
    def draw(self):
        inc = 2.0*pi/self.divisions
        
        glBegin(GL_LINE_LOOP)
        for i in xrange(self.divisions):
            angle = i * inc
            glVertex3f(self.radius * cos(angle), self.radius * sin(angle), self.z)
            
        glEnd()
        

def _quitOnESCCallback(event):
    import pygame
    if event.key == pygame.constants.K_ESCAPE:
         sys.exit()    
         
def createDefaultCallbacks():
    import pygame
    callbacks = [
       (pygame.constants.KEYDOWN, lambda event: _quitOnESCCallback(event)),
       (pygame.constants.QUIT, lambda event: sys.exit(0))
    ] 
    return callbacks
    

class ViewState(object):
    """
    Class for capturing the current modelview, projection, and viewport state
    """
    def __init__(self):
        self._viewport = glGetInteger(GL_VIEWPORT)
        self._mvmatrix = glGetDouble(GL_MODELVIEW_MATRIX)
        self._projmatrix = glGetDouble(GL_PROJECTION_MATRIX)
    
    def unProject(self, x, y, z):
        """
        Convert the given point in screen coordinates into world coordinates.
        z is the depth value, usually in [0, 1]
        """
        return gluUnProject(x, y, z, self._mvmatrix, self._projmatrix, self._viewport)

    def project(self, x, y, z): 
        """
        Project the point in world coordinates into screen coordinates.
        """
        return gluProject(x, y, z, self._mvmatrix, self._projmatrix, self._viewport)



