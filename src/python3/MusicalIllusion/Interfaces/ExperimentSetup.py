import os
from PyQt5 import QtWidgets, QtGui, uic, QtCore
from MusicalIllusion.Utilities.GetPath import *
from MusicalIllusion.Utilities.ReadProperties import readTojProperties, readSIProperties


class ExperimentData:
    def __init__(self, RAID, subjID, sessID, dataDir, demo, properties, propFile, propertiesVer):
        self.RAID = RAID
        self.subjID = subjID
        self.sessID = sessID
        self.dataDir = dataDir
        self.demo = demo
        self.properties = properties
        self.propFile = propFile
        self.propertiesVer = propertiesVer


class ExperimentSetup(QtWidgets.QWidget):
    def __init__(self, parent):
        super(QtWidgets.QWidget, self).__init__()

        self.parent = parent
        self.done = False

        # Setup UI
        uic.loadUi(getGui('Setup.ui'), self)
        self.tbDataDirectory.setText(os.getcwd())
        self.btBrowse.clicked.connect(self.browseDataDirectory)
        self.btBrowseProperties.clicked.connect(self.browseFile)
        self.rbDataDir.toggled.connect(self.toggleProperties)
        self.btStart.clicked.connect(self.startExperiment)
        self.btCancel.clicked.connect(self.close)
        self.show()

    def closeEvent(self, event):
        if self.done == False:
            self.parent.close()
        event.accept()

    # Open the directory browser
    def browseDataDirectory(self):
        dataDir = QtWidgets.QFileDialog.getExistingDirectory(self, "Select Directory")
        self.tbDataDirectory.setText(dataDir)

    def browseFile(self):
        f = QtWidgets.QFileDialog.getOpenFileName(self, "Open Properties File", ".", "Properties File (*.properties)")[0]
        self.tbPropertiesFile.setText(f)

    def toggleProperties(self):
        self.tbPropertiesFile.setEnabled(not self.rbDataDir.isChecked())
        self.btBrowseProperties.setEnabled(not self.rbDataDir.isChecked())

    # Collect all the data and pass it to the next window
    def startExperiment(self):

        # Generate properties filename based on the selected radio button
        if self.rbDataDir.isChecked():
            for filename in os.listdir(self.tbDataDirectory.text()):
                if os.path.splitext(filename)[1] == ".properties":
                    propFile = os.path.join(self.tbDataDirectory.text(), filename)
        else:
            propFile = self.tbPropertiesFile.text()

        # Check if the properties file exists
        if not os.path.exists(propFile):
            msgbox = QtWidgets.QMessageBox.critical(self, "Error - File Not Found", "Unable to locate properties file")
            return

        # Check which properties file to open and make sure it can be parsed
        if self.cbProperties.currentText() == "toj.properties":
            e, properties = readTojProperties(propFile)
        elif self.cbProperties.currentText() == "si.properties":
            e, properties = readSIProperties(propFile)

        if not e:
            QtWidgets.QMessageBox.critical(self, "Error - Unable to parse properties", properties)
            return

        d = ExperimentData(
            RAID=self.tbRAID.text(),
            subjID=self.tbSubjID.text(),
            sessID=self.tbSessionID.text(),
            dataDir=self.tbDataDirectory.text(),
            demo=False,  # self.cbDemo.isChecked(),
            properties=properties,
            propFile=propFile,
            propertiesVer=self.cbProperties.currentText()
        )

        self.done = True
        self.parent.startExperiment(d)
        self.close()
