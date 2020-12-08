import pyqtgraph as pg
import numpy as np
from PyQt5 import QtCore, QtMultimedia
from ast import literal_eval

SHAPE_DICT = {"dot" : "o", "square" : "s", "diamond" : "d", "cross" : "+", "" : "o", "-" : "o", "d" : "d"}
SIZE_SCALE = 10

class VisualizerData():
    def __init__(self, defaultSize, defaultGain):

        # Animation data
        self.pos = np.array([(-100,-100)])
        self.pens = [pg.mkPen(color = [0,0,0,0])]
        self.sizes = [1]
        self.shapes = ["o"]
        self.adj = []
        self.defaultSize = defaultSize * 100

        # Audio data
        self.audio = QtMultimedia.QSoundEffect()
        self.audio.setLoopCount(0)
        self.audio.setVolume(defaultGain * 100)
        self.audOffset = 0
        self.defaultOffset = 0

    def loadData(self, data):

        # Audio data
        if (data["audioFile"] is not None):
            self.audio.stop()
            self.audio.setSource(QtCore.QUrl.fromLocalFile(data["audioFile"]))
            self.audOffset = data["audioOffset"]

        # Animation data
        if data["visFile"] is None:
            return

        # Need to work out what format the data is stored in
        dat = open(data["visFile"], 'r')
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
            self.readDataExtended(data["visFile"], headerCount, data["numDots"])
        else:
            self.readData(data["visFile"], data["numDots"])
       
    def readDataExtended(self, f, headerSkip, numJoints):
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
            colData.append([literal_eval(j) if j != "-" else [255,255,255,255] for j in rawData['f%s' % str(basei + 2)]])
            # Converts the size to an int and scales. Assigns default if one doesnt exist
            sizeData.append([float(j[4:]) * self.defaultSize if j != "-" else self.defaultSize for j in rawData['f%s' % str(basei + 3)]])
            # Uses the shape dict to convert shape strings into useable keys
            shapeData.append([SHAPE_DICT[j] for j in rawData['f%s'% str(basei + 4)]])

        # Create 2D arrays of frame data, each with the data for all joints for each frame
        self.pos = [[data[i] for data in posData] for i  in range(0, len(rawData['f1']))]
        self.pens = [[data[i] for data in colData] for i in range(0, len(rawData['f1']))]
        self.sizes = [[data[i] for data in sizeData] for i in range(0, len(rawData['f1']))]
        self.shapes = [[data[i] for data in shapeData] for i in range(0, len(rawData['f1']))]

        # Setup joint connections
        self.adj = np.array([[i, i+1] for i in range(0, numJoints - 1)])

        # Find minimum y value of the mallet
        mn = np.argmin(rawData['f2'])
        minT = rawData['f0'][mn] * 1000
        # Use this to find the audio offset
        self.audOffset = minT - self.defaultOffset - self.audOffset 
    
    def readData(self, f, numJoints):
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
            colData.append([[255,255,255,255] for j in rawData['f0']])
            # Creates sizes based on default sizes
            sizeData.append([self.defaultSize for j in rawData['f0']])
            # Creates a string of dots
            shapeData.append(["o" for j in rawData['f0']])

        # Create 2D arrays of frame data, each with the data for all joints for each frame
        self.pos = [[data[i] for data in posData] for i  in range(0, len(rawData['f1']))]
        self.pens = [[data[i] for data in colData] for i in range(0, len(rawData['f1']))]
        self.sizes = [[data[i] for data in sizeData] for i in range(0, len(rawData['f1']))]
        self.shapes = [[data[i] for data in shapeData] for i in range(0, len(rawData['f1']))]

        # Setup joint connections
        self.adj = np.array([[i, i+1] for i in range(0, numJoints - 1)])

        # Find minimum y value of the mallet
        mn = np.argmin(rawData['f2'])
        minT = rawData['f0'][mn] * 1000
        # Use this to find the audio offset
        self.audOffset = minT - self.audOffset - self.defaultOffset
