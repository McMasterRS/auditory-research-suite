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
# $Id: Instructions.py 223 2006-10-19 13:29:00Z sfitch $
#

"""
Support for displaying textual messages and waiting for user to press a key.
"""

__version__ = '$LastChangedRevision: 223 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

from VisionEgg.Core import *
from pygame.locals import *
import VisionEgg.Text
import string
from OpenGL.GL import *

class Instructions:
    """ 
    Convenience class for rendering multi-line text and waiting for
    The user to press a key (the any-key key)
    """
    
    def __init__(self, screen, text=None, fileName=None, margin=(50,50)):

        lines = []
        
        if not fileName is None:
            try:
                f = open(fileName, 'r')
                lines.extend(f.readlines());
                f.close()
            except:
                lines.append('Instructions file "' + fileName + '" not found.')
        elif not text is None:
            lines.extend(string.split(text, '\n'))

        stimuli=[]
        y = screen.size[1] - margin[1]
        for l in lines:
            text = VisionEgg.Text.Text(text=l.strip())
            size = text.parameters.size
            y -= size[1]
            text.set(position=(margin[0], y))
            stimuli.append(text)
            
        text = VisionEgg.Text.Text(
            position=(margin[0], y-50),
            font_size=20,
            text="Press any key to continue...")
        text.font.set_italic(True) # This doesn't work :-(
        stimuli.append(text)

        callbacks = [
           (KEYDOWN, self.cont),
           (MOUSEBUTTONDOWN, self.cont),
           (QUIT, lambda event: sys.exit())
        ]

        self.view = Viewport(screen=screen, size=screen.size, stimuli=stimuli)
#                              projection=OrthographicProjection())
        self.pres = Presentation(viewports=[self.view],
                                 handle_event_callbacks=callbacks,
                                 collect_timing_info=False,
                                 warn_mean_fps_threshold=10000)

    
    def show(self):
        glMatrixMode(GL_MODELVIEW)
        glPushMatrix()
        glMatrixMode(GL_PROJECTION)
        glPushMatrix()
        glPushAttrib(GL_ENABLE_BIT|GL_TEXTURE_BIT)
        
        self.pres.set(go_duration=('forever',))
        self.pres.go()
        
        
        glPopAttrib()
        glMatrixMode(GL_PROJECTION)
        glPopMatrix()
        glMatrixMode(GL_MODELVIEW)
        glPopMatrix()        
        
        
    def cont(self, event):
        self.pres.set(go_duration=(0, 'frames'))
        
        