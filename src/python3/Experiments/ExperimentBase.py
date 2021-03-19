from PyQt5 import QtWidgets
from Interfaces.ExperimentSetup import ExperimentSetup
from Interfaces.SIExperimentPanel import SIExperimentPanel
from Interfaces.TOJExperimentPanel import TOJExperimentPanel


class ExperimentBase(QtWidgets.QWidget):
    def __init__(self):
        super(QtWidgets.QWidget, self).__init__()

        self.interface = []
        self.setup = ExperimentSetup(self)

    def startExperiment(self, data):
        if data.propertiesVer == "toj.properties":
            self.interface = TOJExperimentPanel(self)
        elif data.propertiesVer == "AV7B-si.properties":
            self.interface = SIExperimentPanel(self)

        self.interface.startExperiment(data)
