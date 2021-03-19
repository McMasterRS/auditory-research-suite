import pyqtgraph as pg
import numpy as np
from PyQt5 import QtCore
from datetime import datetime

from Visualizer.VisualizerData import VisualizerData


class Visualizer():
    def __init__(self, plot, parent, defaultSize, defaultGain, connectDots):

        self.parent = parent
        self.data = VisualizerData(defaultSize, defaultGain)
        self.frame = 0
        self.connectDots = connectDots

        # Timings used for the output file
        self.timings = {}

        # Set the plot up for visualization
        self.lines = pg.GraphItem(pos=np.array([(-100, -100)]))
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

        self.audComplete = QtCore.QTimer()
        self.audComplete.timeout.connect(self.progressAudio)

    def setData(self, data):
        self.data.loadData(data)
        self.frame = 0
        self.timings = {}

    def clearPlot(self):
        self.lines.setData(pos=np.array([(-100, -100)]))

    def play(self):
        self.plotTimer.start(0)
        if self.data.isAudio:
            self.audTimer.start(self.data.audOffset)
        self.frame = 0
        time = datetime.now()
        self.timings = {"animationStart": time, "animationDelay": self.data.audOffset,
                        "audioStart": time, "audioDelay": 0}

    def playAudio(self):
        self.data.audio.play()
        self.audTimer.stop()
        self.audComplete.start(1000)

    def progressAudio(self):
        if not self.data.audio.isPlaying():
            self.audComplete.stop()
            self.parent.state.currentTrialData.update(self.timings)
            self.parent.state.updateState()
        return

    def plotData(self):

        if self.data.isAnim == False:
            self.plotTimer.stop()
            return

        pens = [pg.mkPen(color=[j for j in i]) for i in self.data.pens[self.frame]]
        brushes = [pg.mkBrush(color=[j for j in i]) for i in self.data.pens[self.frame]]
        lines = np.array([(i[0], i[1], i[2], 255, 1) for i in self.data.pens[self.frame]], dtype=[('red', np.ubyte), ('green', np.ubyte), ('blue', np.ubyte), ('alpha', np.ubyte), ('width', float)])

        # Ugly but the only way it'll let me toggle the lines
        # Pushed a fix for this to matplotlib but not sure when it'll be
        # included in the full version. Ugly will have to do for now
        if self.connectDots:
            self.lines.setData(pos=np.array(self.data.pos[self.frame]),
                               adj=self.data.adj,
                               pen=lines,
                               symbolPen=pens,
                               symbolBrush=brushes,
                               symbol=self.data.shapes[self.frame],
                               size=self.data.sizes[self.frame]
                               )
        else:
            self.lines.setData(pos=np.array(self.data.pos[self.frame]),
                               pen=lines,
                               symbolPen=pens,
                               symbolBrush=brushes,
                               symbol=self.data.shapes[self.frame],
                               size=self.data.sizes[self.frame]
                               )

        self.frame += 1
        if self.frame < len(self.data.times) - 1:
            dt = int((self.data.times[self.frame] - self.data.times[self.frame - 1]) * 1000)
            self.plotTimer.setInterval(dt)
        else:
            self.plotTimer.stop()
            self.lines.setData(pos=np.array([(-100, -100)]))
            self.parent.state.currentTrialData.update(self.timings)
            self.parent.state.updateState()
