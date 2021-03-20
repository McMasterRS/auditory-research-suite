from PyQt5 import QtWidgets
from MusicalIllusion.Interfaces.ExperimentSetup import ExperimentSetup
from MusicalIllusion.Interfaces.SIExperimentPanel import SIExperimentPanel
from MusicalIllusion.Interfaces.TOJExperimentPanel import TOJExperimentPanel


class ExperimentBase(QtWidgets.QWidget):
    def __init__(self):
        super(QtWidgets.QWidget, self).__init__()

        self.interface = []
        self.setup = ExperimentSetup(self)

    def startExperiment(self, data):
        if data.propertiesVer == "toj.properties":
            self.interface = TOJExperimentPanel(self)
        elif data.propertiesVer == "si.properties":
            self.interface = SIExperimentPanel(self)

        self.interface.startExperiment(data)
