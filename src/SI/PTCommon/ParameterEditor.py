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
# $Id: ParameterEditor.py 180 2006-09-05 19:07:00Z sfitch $
#

__version__ = '$LastChangedRevision: 180 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'


from Formatter import *
from ObjectAttrValidator2 import *
from VisionEgg import *
from VisionEgg.Core import *
from threading import Thread
import VisionEgg.ParameterTypes as vtype
import wx
import wx.lib.scrolledpanel as scrolled
from WxStimulus import *

class ParameterEditor(scrolled.ScrolledPanel):
    def __init__(self, object, parent=None, id=-1):
        scrolled.ScrolledPanel.__init__(self, parent, id, style=wx.TAB_TRAVERSAL|wx.SUNKEN_BORDER)
        assert(isinstance(object, ClassWithParameters))
            
        self.object = object
        
        self.entries = {}
        
        self.sizer=wx.FlexGridSizer(cols=2, vgap=10, hgap=5)
        self.sizer.AddGrowableCol(1)
        self._createWidgets()
        
        self.SetSizer(self.sizer)
        self.SetupScrolling()
        
        self.InitDialog()
        
        self.sizer.Fit(self)

        
    def _onChange(self, event):
        source = event.GetEventObject()
        val = source.GetValidator()

        if val.Validate(source):
            val.TransferFromWindow()
        else:
            val.TransferToWindow()
                        
    def _createWidgets(self):
        """Inspect the parameters of assigned object and create editor."""
        self.SetTitle('Editing ' + str(self.object))
        self.defaults = dict()
        params = self.object.parameters_and_defaults
        if hasattr(self.object, 'parameter_order'):
            keys = self.object.parameter_order
        else:
            keys = params.keys()
            keys.sort()
            
        for key in keys:
            if params.has_key(key):
                value = params[key]
                label = key
                if len(value) > 2:
                    label = value[2]
                self._addField(key, label+":", value[1])
                # save off current value for reset button
                self.defaults[key] = getattr(self.object.parameters, key, None)
            else:
                # Create a section label for non key strings
                txt = wx.StaticText(self, label=key)
                fnt = txt.GetFont()
                fnt.SetWeight(wx.BOLD)
                txt.SetFont(fnt)
                self.sizer.Add(txt, 0, wx.ALIGN_RIGHT)
                self.sizer.Add(wx.StaticLine(self, style=wx.LI_HORIZONTAL), 0, wx.EXPAND)
                
        self.sizer.Add(wx.StaticText(self)) # spacer
        btn = wx.Button(self, label='Reset to defaults')
        btn.Bind(wx.EVT_BUTTON, self._reset)
        self.sizer.Add(btn)
            
    def _addField(self, attrName, label, type):
        wgt = None
        formatter = self._formatterForType(type)
        if formatter is None: 
            if type is vtype.Boolean:
                wgt = wx.CheckBox(self)
                wgt.SetValidator(
                    ObjectAttrCheckBoxValidator(self.object.parameters, attrName))
                wgt.Bind(wx.EVT_CHECKBOX, self._onChange)
        else:
            wgt = wx.TextCtrl(self, style=wx.TE_PROCESS_ENTER)
            wgt.SetValidator(
                ObjectAttrTextValidator(self.object.parameters, attrName, formatter,
                                        validationCB=self._validationCB))
            wgt.Bind(wx.EVT_KILL_FOCUS, self._onChange)
            wgt.Bind(wx.EVT_TEXT_ENTER, self._onChange)
            
        if not wgt is None:
            wgt.SetToolTipString(attrName)
            txt = wx.StaticText(self, label=label)
            txt.SetToolTipString(attrName)
            self.sizer.Add(txt, 0, wx.ALIGN_RIGHT)
            self.sizer.Add(wgt, 0, wx.EXPAND)            
        
    def _formatterForType(self, type):
        formatter = None
        if type is vtype.Real:
            formatter = FloatFormatter()
        elif type is vtype.Integer:
            formatter = IntFormatter()
        elif type is vtype.UnsignedInteger:
            formatter = UIntFormatter()
        elif type is vtype.String:
            formatter = StringFormatter()
        elif isinstance(type, vtype.Sequence):
            size = int(type.__class__.__name__[-1])
            subFormatter = self._formatterForType(type.item_type)
            return None
            return TupleFormatter(size, subFormatter)
            
        return formatter
        
    def _reset(self, event):
        params = self.object.parameters_and_defaults
        for key in params.keys():
            # get the default value and set it.
            if self.defaults.has_key(key):
                value = self.defaults[key]
                setattr(self.object.parameters, key, value)
        self.InitDialog()
      
    def _validationCB(self, obj, attrName, val, flRequired, flValid):
        if not flValid:
            pass
        
class ParameterEditorFrame(wx.Frame):
    def __init__(self, object, parent=None, id=-1):
        wx.Frame.__init__(self, parent, id)
        
        self.sizer = wx.FlexGridSizer(rows=1, cols=1)
        self.sizer.AddGrowableCol(0)
        self.sizer.AddGrowableRow(0)
        self.SetSizer(self.sizer)
        
        self.editor = ParameterEditor(object, parent=self)
        self.sizer.Add(self.editor, flag=wx.EXPAND)
        
        s = self.GetSize()
        
        self.SetSize((s[0]+100,min(s[1], 700)))
        
        import sys
        self.Bind(wx.EVT_CLOSE, lambda event: sys.exit(0))
                
    def InitDialog(self):
        wx.Frame.InitDialog(self)
        self.editor.InitDialog()

if __name__ == '__main__':
    class Foo(ClassWithParameters):
        parameters_and_defaults = {
            'position' : ((320, 240), vtype.Sequence2(vtype.Real)), 
            'numCols' : (15, vtype.UnsignedInteger), 
            'numRows' : (15, vtype.UnsignedInteger), 
            'dotSize' : (10, vtype.UnsignedInteger), 
            'scale' : (50, vtype.Real), 
            'angle' : (0, vtype.Real), 
            'gamma' : (85, vtype.Real), 
            'aspectRatio' : (1, vtype.Real), 
            'apertureSize' : (300, vtype.Real), 
            'perturb' : (False, vtype.Boolean), 
            'lighting' : (False, vtype.Boolean), 
            'lightPos' : ((100, 100, -100), vtype.Sequence3(vtype.Real)), 
            'foreground' : ((0, 0, 0, 1), vtype.Sequence4(vtype.Real)), 
            'background' : ((0.3, 0.3, 0.3, 1), vtype.Sequence4(vtype.Real))
        }    
            
        def __init__(self, **kw):
            ClassWithParameters.__init__(self, **kw)
            
        def __str__(self):
            return 'Foo'
            
    f = Foo()
    
    app = wx.PySimpleApp()
    frame = ParameterEditorFrame(f)
    frame.Show(True)
    
    looper = WxStimulus()
    while True:
        looper.draw()
        time.sleep(0.01)
        
    sys.exit(0)

