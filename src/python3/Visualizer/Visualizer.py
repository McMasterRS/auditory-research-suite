import pyqtgraph as pg
import numpy as np
from PyQt5 import QtCore
from ast import literal_eval

SHAPE_DICT = {"dot" : "o", "square" : "s", "diamond" : "d", "cross" : "+", "" : "o", "-" : "o", "d" : "d"}
SIZE_SCALE = 10

class VisualizerData():
    def __init__(self):
        self.pos = np.array([(-100,-100)])
        self.pens = [pg.mkPen(color = [0,0,0,0])]
        self.sizes = [1]
        self.shapes = ["o"]
        self.adj = []

    def loadData(self, f, numJoints, defaultSize):
        # Need to work out what format the data is stored in
        dat = open(f, 'r')
        # Start this at 2 to remove the regular headers
        headerCount = 2

        # Loop through entire file and see how many of the lines include
        # an "x=y" statement. technically inefficient but shouldnt cause
        # slowdown, we'll have ~2s between each trial to do this
        for line in dat:
            if len(line.strip().split("=")) == 2:
                headerCount += 1
        
        # If there are more than 2 lines for the header, its an extended file
        if headerCount > 2:
            self.readDataExtended(f, headerCount, numJoints, defaultSize)
        else:
            self.readData(f, numJoints, defaultSize)
       
    def readDataExtended(self, f, headerSkip, numJoints, defaultSize):
        # List of data types: time and then x/y/colour/size/shape for each joint
        dtype = [np.dtype("f")] + [np.dtype("f"), np.dtype("f"), np.dtype("U15"), np.dtype("U10"), np.dtype("U10")] * numJoints
        rawData = np.genfromtxt(f, delimiter = "	",skip_header=headerSkip, dtype = dtype)

        self.times = rawData['f0'].tolist()

        posData = []
        colData = []
        sizeData = []
        shapeData = []
        
        for i in range(numJoints, 0, -1):
            basei = ((i-1)*5) + 1
            # Extracts the 2nd and 3rd column and arranges them into [x, y] arrays
            posData.append([[rawData['f%s' % str(basei)][j], rawData['f%s' % str(basei + 1)][j]] for j in range(0, len(rawData['f1']))])
            # Imports the colour array or assigns default if one doesnt exist
            colData.append([literal_eval(j) if j != "-" else pg.mkPen(color = [255,255,255,255]) for j in rawData['f%s' % str(basei + 2)]])
            # Converts the size to an int and scales. Assigns default if one doesnt exist
            sizeData.append([float(j[4:]) * SIZE_SCALE if j != "-" else defaultSize for j in rawData['f%s' % str(basei + 3)]])
            # Uses the shape dict to convert shape strings into useable keys
            shapeData.append([SHAPE_DICT[j] for j in rawData['f%s'% str(basei + 4)]])

        # Create 2D arrays of frame data, each with the data for all joints for each frame
        self.pos = [[data[i] for data in posData] for i  in range(0, len(rawData['f1']))]
        self.pens = [[data[i] for data in colData] for i in range(0, len(rawData['f1']))]
        self.sizes = [[data[i] for data in sizeData] for i in range(0, len(rawData['f1']))]
        self.shapes = [[data[i] for data in shapeData] for i in range(0, len(rawData['f1']))]

        # Setup joint connections
        self.adj = np.array([[i, i+1] for i in range(0, numJoints - 1)])
    
    def readData(self, f, numJoints, defaultSize):
        # List of data types: time and then x/y/colour/size/shape for each joint
        dtype = [np.dtype("f")] + [np.dtype("f"), np.dtype("f")] * numJoints
        rawData = np.genfromtxt(f, delimiter = "	",skip_header=2, dtype = dtype)
        self.times = rawData['f0'].tolist()

        posData = []
        colData = []
        sizeData = []
        shapeData = []

        for i in range(numJoints, 0, -1):
            basei = ((i-1)*2) + 1
            # Extracts the 2nd and 3rd column and arranges them into [x, y] arrays
            posData.append([[rawData['f%s' % str(basei)][j], rawData['f%s' % str(basei + 1)][j]] for j in range(0, len(rawData['f1']))])
            # Creates colours based on default values
            colData.append([pg.mkPen(color = [255,255,255,255]) for j in rawData['f0']])
            # Creates sizes based on default sizes
            sizeData.append([defaultSize for j in rawData['f0']])
            # Creates a string of dots
            shapeData.append(["o" for j in rawData['f0']])

        # Create 2D arrays of frame data, each with the data for all joints for each frame
        self.pos = [[data[i] for data in posData] for i  in range(0, len(rawData['f1']))]
        self.pens = [[data[i] for data in colData] for i in range(0, len(rawData['f1']))]
        self.sizes = [[data[i] for data in sizeData] for i in range(0, len(rawData['f1']))]
        self.shapes = [[data[i] for data in shapeData] for i in range(0, len(rawData['f1']))]

        # Setup joint connections
        self.adj = np.array([[i, i+1] for i in range(0, numJoints - 1)])

class Visualizer():
    def __init__(self, plot, parent):

        self.parent = parent
        self.data = VisualizerData()
        self.frame = 0

        # Set the plot up for visualization
        self.lines = pg.GraphItem(pos = np.array([(-100,-100)]))
        plot.addItem(self.lines)
        p = plot.getPlotItem()
        p.setXRange(0, 10)
        p.setYRange(0, 10)
        p.showGrid(False, False)
        p.showAxis('left', False)
        p.showAxis('bottom', False)
        p.setMouseEnabled(False, False)
        p.hideButtons()

        self.plotTimer = QtCore.QTimer()
        self.plotTimer.timeout.connect(self.plotData)

    def setData(self, f, numJoints, defaultSize):
        self.data.loadData(f, numJoints, defaultSize)
        self.frame = 0

    def play(self):
        self.plotTimer.start(1)

    def plotData(self):
        self.lines.setData(pos = np.array(self.data.pos[self.frame]))
        self.frame += 1
        if self.frame < len(self.data.times) - 1:
            dt = int((self.data.times[self.frame] - self.data.times[self.frame - 1]) * 1000)
            self.plotTimer.setInterval(dt)
        else:
            self.plotTimer.stop()
            self.parent.updateState()