import pyqtgraph as pg
import numpy as np
from PyQt5 import QtCore

from Visualizer.VisualizerData import VisualizerData


class Visualizer():
    def __init__(self, plot, parent, defaultSize, defaultGain, connectDots):

        self.parent = parent
        self.data = VisualizerData(defaultSize, defaultGain)
        self.frame = 0
        self.connectDots = connectDots

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
        self.plotTimer.setTimerType(QtCore.Qt.PreciseTimer)
        self.plotTimer.timeout.connect(self.plotData)

        self.audTimer = QtCore.QTimer()
        self.audTimer.setTimerType(QtCore.Qt.PreciseTimer)
        self.audTimer.timeout.connect(self.playAudio)
        
    def setData(self, data):
        self.data.loadData(data)
        self.frame = 0

    def clearPlot(self):
        self.lines.setData(pos = np.array([(-100,-100)]))
        self.plotData

    def play(self):
        self.plotTimer.start(0)
        self.audTimer.start(self.data.audOffset)
        self.frame = 0

    def playAudio(self):
        self.data.audio.play()
        self.audTimer.stop()

    def plotData(self):

        pens = [pg.mkPen(color = [j for j in i]) for i in self.data.pens[self.frame]]
        brushes = [pg.mkBrush(color = [j for j in i]) for i in self.data.pens[self.frame]]
        lines = np.array([(i[0], i[1], i[2], 255, 1) for i in self.data.pens[self.frame]] , dtype=[('red',np.ubyte),('green',np.ubyte),('blue',np.ubyte),('alpha',np.ubyte),('width',float)])
        
        # Ugly but the only way it'll let me toggle the lines
        if self.connectDots:
            self.lines.setData(pos = np.array(self.data.pos[self.frame]),
                            adj = adj,
                            pen = lines,
                            symbolPen = pens,
                            symbolBrush = brushes,
                            symbol = self.data.shapes[self.frame],
                            size = self.data.sizes[self.frame]
            )
        else:
            self.lines.setData(pos = np.array(self.data.pos[self.frame]),
                            pen = lines,
                            symbolPen = pens,
                            symbolBrush = brushes,
                            symbol = self.data.shapes[self.frame],
                            size = self.data.sizes[self.frame]
            )
        
        self.frame += 1
        if self.frame < len(self.data.times) - 1:
            dt = int((self.data.times[self.frame] - self.data.times[self.frame - 1]) * 1000)
            self.plotTimer.setInterval(dt)
        else:
            self.plotTimer.stop()
            self.parent.updateState()