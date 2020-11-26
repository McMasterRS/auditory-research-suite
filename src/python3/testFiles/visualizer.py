from PyQt5 import QtWidgets, QtGui, uic, QtCore, QtMultimedia
import pyqtgraph as pg
import sys, asyncio
from quamash import QSelectorEventLoop
import numpy as np
import os
import json
import datetime
from ast import literal_eval

SHAPE_DICT = {"dot" : "o", "square" : "s", "diamond" : "d", "cross" : "+", "" : "o", "-" : "o"}
SHAPE_DICT_REVERSE = {"o" : "Dot", "s" : "Square", "d" : "Diamond", "+" : "Cross"}
DEFAULT_SIZE = 10
SIZE_SCALE = 10

NUM_JOINTS = 4

class TestPlot(QtWidgets.QWidget):
    def __init__(self):
        QtWidgets.QWidget.__init__(self)
        
        uic.loadUi('ControlPanel.ui', self)
        self.show()

        self.playing = True
        self.muted = False
        self.sound = QtMultimedia.QSoundEffect()
        
        self.noteList = {}
        self.pathList = {}
        self.colours = {}
        self.defaultPen = [255,255,255]

        self.times = []
        
        self.frame = 0

        self.timer = QtCore.QTimer()
        self.timer.timeout.connect(self.update)

        self.setupPlot()
        self.setupAudio()
        self.setupUI()
        self.parseData()
        self.timer.start(100)

    def setupUI(self):

        # Load the UI settings
        if os.path.isfile('./settings.ini'):
            with open('./settings.ini', 'r') as f:
                data = json.loads(f.read())

            self.noteList = data["noteList"]
            self.cbNote.addItems(self.noteList)
            self.pathList = data["pathList"]
            self.cbPaths.addItems(self.pathList)

        else:
            self.noteList = {}
            self.pathList = {}

        self.colours = {"arm" : "white", "mallet" : "white", "hand" : "white"}

        self.btShoulderColour.setStyleSheet("background-color : white")
        self.btArmColour.setStyleSheet("background-color : white")
        self.btHandColour.setStyleSheet("background-color : white")
        self.btMalletColour.setStyleSheet("background-color : white")
        self.pens = [[255,255,255,255], [255,255,255,255],  [255,255,255,255], [255,255,255,255]] 

        self.btLoad.clicked.connect(self.loadData)

        self.btAddNote.clicked.connect(self.addNote)
        self.btAddPath.clicked.connect(self.addPath)

        self.cbPaths.currentIndexChanged.connect(self.parseData)
        self.cbNote.currentIndexChanged.connect(self.setupAudio)

        self.btShoulderColour.clicked.connect(lambda: self.updateColour(0))
        self.btArmColour.clicked.connect(lambda: self.updateColour(1))
        self.btHandColour.clicked.connect(lambda: self.updateColour(2))
        self.btMalletColour.clicked.connect(lambda: self.updateColour(3))

        self.btStart.clicked.connect(self.toggleVid)
        self.cbMute.toggled.connect(self.playAudio)

    def setupAudio(self, index = 0):
        self.sound.stop()
        if self.cbNote.currentText() != "":
            f = self.noteList[self.cbNote.currentText()]
            self.sound.setSource(QtCore.QUrl.fromLocalFile(f))
            self.sound.setVolume(50)
            self.sound.setLoopCount(0)
            self.playAudio()

    def setupPlot(self):
        self.parseData()

        self.lines = pg.GraphItem(pos = np.array([(0,0)]))
        self.plot.addItem(self.lines)
        
        plt = self.plot.getPlotItem()
        plt.setXRange(0, 10)
        plt.setYRange(0, 10)
        plt.showGrid(False, False)
        plt.showAxis('left', False)
        plt.showAxis('bottom', False)

    def loadData(self):
        fName = QtWidgets.QFileDialog.getOpenFileName(self, 'Open file', ".")
        self.parseData(data = fName[0])

    def parseData(self, index = 0, data = None):
        # If there's nothing in cbPaths, return
        if self.cbPaths.currentText() == "" and data == None:
            return

        if data == None:
            f = self.pathList[self.cbPaths.currentText()]
        else:
            f = data

        # List of data types: time and then x/y/colour/size/shape for each joint
        dtype = [np.dtype("f")] + [np.dtype("f"), np.dtype("f"), np.dtype("U15"), np.dtype("U10"), np.dtype("U10")] * (NUM_JOINTS)
        rawData = np.genfromtxt(f, delimiter = "	",skip_header=7, dtype = dtype)

        self.times = rawData['f0'].tolist()

        posData = []
        colData = []
        sizeData = []
        shapeData = []
        
        for i in range(NUM_JOINTS, 0, -1):
            basei = ((i-1)*5) + 1
            # Extracts the 2nd and 3rd column and arranges them into [x, y] arrays
            posData.append([[rawData['f%s' % str(basei)][j], rawData['f%s' % str(basei + 1)][j]] for j in range(0, len(rawData['f1']))])
            # Imports the colour array or assigns default if one doesnt exist
            colData.append([literal_eval(j) if j != "-" else self.defaultPen for j in rawData['f%s' % str(basei + 2)]])
            # Converts the size to an int and scales. Assigns default if one doesnt exist
            sizeData.append([float(j[4:]) * SIZE_SCALE if j != "-" else DEFAULT_SIZE for j in rawData['f%s' % str(basei + 3)]])
            # Uses the shape dict to convert shape strings into useable keys
            shapeData.append([SHAPE_DICT[j] for j in rawData['f%s'% str(basei + 4)]])

        # Create 2D arrays of frame data, each with the data for all joints for each frame
        self.pos = [[data[i] for data in posData] for i  in range(0, len(rawData['f1']))]
        self.pens = [[data[i] for data in colData] for i in range(0, len(rawData['f1']))]
        self.sizes = [[data[i] for data in sizeData] for i in range(0, len(rawData['f1']))]
        self.shapes = [[data[i] for data in shapeData] for i in range(0, len(rawData['f1']))]

        # Setup joint connections
        self.adj = np.array([[i, i+1] for i in range(0, NUM_JOINTS - 1)])

        self.frame = 0
        self.setupAudio()

        # Update UI elements
        self.refreshColour()
        self.refreshShapes()

        # Start the animation
        self.timer.start(100)

    def playAudio(self, state = None):
        if self.cbMute.isChecked():
            self.sound.stop()
        else:
            self.sound.play()
            

    def closeEvent(self, event):

        settings = {
            "noteList" : self.noteList,
            "pathList" : self.pathList,
        }

        with open('./settings.ini', 'w') as f:
            json.dump(settings, f)

        event.accept()
        
    def update(self):

        # Dont want to run if no paths are loaded
        if len(self.times) == 0:
            return

        # Start audio if on first frame
        if (self.frame == 0):
            self.playAudio()

        t0 = datetime.datetime.now()

        # Create pens and brushes. The lines need a more complex array
        pens = [pg.mkPen(color = [j for j in i]) for i in self.pens[self.frame]]
        brushes = [pg.mkBrush(color = [j for j in i]) for i in self.pens[self.frame]]
        lines = np.array([(i[0], i[1], i[2], 255, 1) for i in self.pens[self.frame]] , dtype=[('red',np.ubyte),('green',np.ubyte),('blue',np.ubyte),('alpha',np.ubyte),('width',float)])

        # Update the frame
        self.lines.setData(pos = np.array(self.pos[self.frame]), adj = self.adj, pen = lines, symbolPen = pens, symbolBrush = brushes, symbol = self.shapes[self.frame], size = self.sizes[self.frame])
        
        # Incriment the frame count and reset if reached the end
        self.frame += 1
        if self.frame == len(self.times) - 1:
            self.frame = 0
            if not self.cbLoop.isChecked():
                self.toggleVid()
                return
        
        self.refreshColour()
        self.refreshShapes()

        # Calculate required time between frames
        dt = int((self.times[self.frame + 1] - self.times[self.frame]) * 1000)

        # Calculate time required to update the frame
        t1 = datetime.datetime.now()
        tc = t1 - t0
        dt -= (tc.microseconds / 1000)

        # Subtract time to update frame from dt to keep in sync
        self.timer.setInterval( dt if dt > 0 else 0)
                
    def addNote(self):
        fNames = QtWidgets.QFileDialog.getOpenFileNames(self, 'Open file', ".")
        for fName in fNames[0]:
            fNameReduced = os.path.basename(fName)
            if fNameReduced in self.noteList.keys():
                error = QtWidgets.QErrorMessage()
                error.showMessage("File %s already added" % fNameReduced)
            else:
                self.noteList[fNameReduced] = fName
                self.cbNote.addItems([fNameReduced])

    def addPath(self):
        fNames = QtWidgets.QFileDialog.getOpenFileNames(self, 'Open file', ".")
        for fName in fNames[0]:
            fNameReduced = os.path.basename(fName)
            if fNameReduced in self.noteList.keys():
                error = QtWidgets.QErrorMessage()
                error.showMessage("File %s already added" % fNameReduced)
            else:
                self.pathList[fNameReduced] = fName
                self.cbPaths.addItems([fNameReduced])

    def updateColour(self, btn):
        col = QtWidgets.QColorDialog.getColor()
        if btn == 0:
            self.btShoulderColour.setStyleSheet("background-color : %s" % col.name())
            self.pens[0] = [col.red(), col.green(), col.blue(), col.alpha()]
            self.colours["shoulder"] = col.name()
        elif btn == 1:
            self.btArmColour.setStyleSheet("background-color : %s" % col.name())
            self.pens[1] = [col.red(), col.green(), col.blue(), col.alpha()]
            self.colours["arm"] = col.name()
        elif btn == 2:
            self.btHandColour.setStyleSheet("background-color : %s" % col.name())
            self.pens[2] = [col.red(), col.green(), col.blue(), col.alpha()]
            self.colours["hand"] = col.name()
        elif btn == 3:
            self.btMalletColour.setStyleSheet("background-color : %s" % col.name())
            self.pens[3] = [col.red(), col.green(), col.blue(), col.alpha()]
            self.colours["mallet"] = col.name()

    def refreshColour(self):
        pens = self.pens[self.frame]
        self.btShoulderColour.setStyleSheet("background-color : rgb(%s,%s,%s);" % (pens[0][0], pens[0][1], pens[0][2]))
        self.btArmColour.setStyleSheet("background-color : rgb(%s,%s,%s);" % (pens[1][0], pens[1][1], pens[1][2]))
        self.btHandColour.setStyleSheet("background-color : rgb(%s,%s,%s);" % (pens[2][0], pens[2][1], pens[2][2]))
        self.btMalletColour.setStyleSheet("background-color : rgb(%s,%s,%s);" % (pens[3][0], pens[3][1], pens[3][2]))

    def refreshShapes(self):
        shapes = self.shapes[self.frame]
        self.cbShoulderShape.setCurrentText(SHAPE_DICT_REVERSE[shapes[0]])
        self.cbArmShape.setCurrentText(SHAPE_DICT_REVERSE[shapes[1]])
        self.cbHandShape.setCurrentText(SHAPE_DICT_REVERSE[shapes[2]])
        self.cbMalletShape.setCurrentText(SHAPE_DICT_REVERSE[shapes[3]])

    def toggleVid(self):
        if self.playing:
            self.timer.stop()
            self.sound.stop()
            self.btStart.setText("Play")
        else:
            self.timer.start(int((self.times[self.frame + 1] - self.times[self.frame]) * 1000))
            self.btStart.setText("Pause")
        self.playing = not self.playing

    def getCurrentState(self):
        self.currentTest.note = self.noteList[self.cbNote.currentText()]
        self.currentTest.path = self.pathList[self.cbPath.currentText()]


if __name__ == '__main__':
    
    app = QtGui.QApplication(sys.argv)
    loop = QSelectorEventLoop(app)
    asyncio.set_event_loop(loop)
    gui = TestPlot()
    QtGui.QApplication.instance().exec_()