#
# Copyright (C) 2005-2006 University of Virginia
# Supported by grants to the University of Virginia from the National Eye Institute 
# and the National Institute of Deafness and Communicative Disorders.
# PI: Prof. Michael Kubovy <kubovy@virginia.edu>
# Author: Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>
#
# Distributed under the terms of the GNU Lesser General Public License
# (LGPL). See LICENSE.TXT that came with this file.
#
# $Id: SessionSetupGUI.py 155 2006-08-21 14:10:47Z sfitch $
#

__version__ = '$LastChangedRevision: 155 $'
__author__ = 'Simeon H.K. Fitch <simeon.fitch@mseedsoft.com>'

import wx, sys
from PTCommon.Formatter import *
from PTCommon.ObjectAttrValidator2 import *
from PTCommon.Pitch import *

class SessionSetupGUI(wx.Dialog):
    """GUI Screen for accepting setup parameters."""
    def __init__(self, session, ctrls, parent=None, id=-1, title='Setup', 
                  pos=wx.DefaultPosition, size=None,
                  style=wx.DEFAULT_DIALOG_STYLE|wx.THICK_FRAME):
        wx.Dialog.__init__(self, parent, id=id, title=title, pos=pos, size=size, style=style)
        self.session = session
        self._createWidgets(ctrls)
        
        self.Bind(wx.EVT_CLOSE, self._onCloseWindow)
        
        if(size is None):
            self.sizer.Fit(self)        
        
    def _onCloseWindow(self, event):
        import sys
        sys.exit(0)
        
    def _createWidgets(self, ctrls):
        self.sizer=wx.GridBagSizer(vgap=4, hgap=5)
        self.sizer.SetFlexibleDirection(wx.HORIZONTAL)
        self.SetSizer(self.sizer)
        self.SetAutoLayout(1)
     
        self.sizer.AddGrowableCol(1)
        
        for i, d in enumerate(ctrls):
            self._addField(i, *d)

        l = len(ctrls)
        self.sizer.AddGrowableRow(l)
        
        self.addExtraWidgets();

        self.ok = wx.Button(self, wx.ID_OK, label='OK')
        self.ok.SetDefault()
        # Filler
        self.sizer.Layout()
        self.sizer.Add(self.ok, pos=(self.sizer.GetRows()+1, 0), span=(1, 2), flag=wx.ALIGN_CENTER)

        
    def addExtraWidgets(self):
        """
            Add widgets after the input fields. Designed to be
            overridden by subclasses
        """
        pass
        
     
    def _addField(self, i, attrName, label, formatter):
        self.sizer.Add(wx.StaticText(self, label=label), (i, 0), flag=wx.ALIGN_RIGHT)
        
        validator = None
        if isinstance(formatter, FilePathFormatter):
            wgt = _FileBrowser(self)
        elif isinstance(formatter, BooleanFormatter):
            wgt = wx.CheckBox(self)
            validator = ObjectAttrCheckBoxValidator(self.session, attrName, formatter,
                                                    validationCB=self._validationCB)
        elif isinstance(formatter, PitchFormatter):
            wgt = PitchEditor(self)
        else:
            wgt = wx.TextCtrl(self)
            
        if validator is None:
            validator =  ObjectAttrTextValidator(self.session, attrName, formatter,
                                                 validationCB=self._validationCB)
        wgt.SetValidator(validator)
        self.sizer.Add(wgt, (i, 1), flag=wx.EXPAND)
        
    def _validationCB(self, obj, attrName, val, flRequired, flValid):
        if not flValid:
            dlg = wx.MessageDialog(self, 'The value "' + val + "' is not valid.",
                                    'Incorrect value...', wx.OK)
            dlg.ShowModal()
            dlg.Destroy()

class _FileBrowser(wx.Panel):
    def __init__(self, parent):
        wx.Panel.__init__(self, parent)
        self.sizer = wx.FlexGridSizer(cols=2)
        self.SetSizer(self.sizer)
        self.sizer.SetFlexibleDirection(wx.HORIZONTAL)
        self.sizer.AddGrowableCol(0)
        
        self.fileText = wx.TextCtrl(self) 
        self.sizer.Add(self.fileText, 1, wx.EXPAND)
        btn = wx.Button(self, label="Browse...")
        self.sizer.Add(btn)
        btn.Bind(wx.EVT_BUTTON, self.browse)
                
    def browse(self, evt): 
        dlg = wx.DirDialog(self, defaultPath=self.fileText.GetValue(), style=wx.DD_NEW_DIR_BUTTON)
        if dlg.ShowModal() == wx.ID_OK:
            self.fileText.SetValue(dlg.GetPath())
        dlg.Destroy()
           
    def SetValue(self, value):
        self.fileText.SetValue(value)

    def GetValue(self):
        return self.fileText.GetValue()


           