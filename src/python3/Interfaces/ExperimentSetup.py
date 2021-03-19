import os
from PyQt5 import QtWidgets, QtGui, uic, QtCore
from Utilities.GetPath import *
from Utilities.ReadProperties import readTojProperties, readSIProperties


class ExperimentData:
    def __init__(self, RAID, subjID, sessID, dataDir, demo, properties, propertiesVer):
        self.RAID = RAID
        self.subjID = subjID
        self.sessID = sessID
        self.dataDir = dataDir
        self.demo = demo
        self.properties = properties
        self.propertiesVer = propertiesVer


class ExperimentSetup(QtWidgets.QWidget):
    def __init__(self, parent):
        super(QtWidgets.QWidget, self).__init__()

        self.parent = parent
        self.done = False

        # Setup UI
        uic.loadUi(getGui('TOJSetup.ui'), self)
        # self.tbDataDirectory.setText(os.getcwd())
        self.btBrowse.clicked.connect(lambda: self.browseDataDirectory("data"))
        self.btBrowseProperties.clicked.connect(lambda: self.browseDataDirectory("prop"))
        self.rbDataDir.toggled.connect(self.toggleProperties)
        self.btStart.clicked.connect(self.startExperiment)
        self.btCancel.clicked.connect(self.close)
        self.show()

    def closeEvent(self, event):
        if self.done == False:
            self.parent.close()
        event.accept()

    # Open the directory browser
    def browseDataDirectory(self, dirType):
        dataDir = QtWidgets.QFileDialog.getExistingDirectory(self, "Select Directory")
        if dirType == "data":
            self.tbDataDirectory.setText(dataDir)
        else:
            self.tbPropertiesFile.setText(dataDir)

    def toggleProperties(self):
        self.tbPropertiesFile.setEnabled(not self.rbDataDir.isChecked())
        self.btBrowseProperties.setEnabled(not self.rbDataDir.isChecked())

    # Collect all the data and pass it to the next window
    def startExperiment(self):

        # Generate properties filename based on the selected radio button
        if self.rbDataDir.isChecked():
            propFile, exists = mergePathsVerify([self.tbDataDirectory.text(), self.cbProperties.currentText()])
        else:
            propFile, exists = mergePathsVerify([self.tbPropertiesFile.text(), self.cbProperties.currentText()])

        # Check if the properties file exists
        if not exists:
            msgbox = QtWidgets.QMessageBox.critical(self, "Error - File Not Found", "Unable to locate file {0}".format(propFile))
            return

        # Check which properties file to open and make sure it can be parsed
        if self.cbProperties.currentText() == "toj.properties":
            e, properties = readTojProperties(propFile)
        elif self.cbProperties.currentText() == "AV7B-si.properties":
            e, properties = readSIProperties(propFile)

        if not e:
            QtWidgets.QMessageBox.critical(self, "Error - Unable to parse properties", properties)
            return

        d = ExperimentData(
            RAID=self.tbRAID.text(),
            subjID=self.tbSubjID.text(),
            sessID=self.tbSessionID.text(),
            dataDir=self.tbDataDirectory.text(),
            demo=self.cbDemo.isChecked(),
            properties=properties,
            propertiesVer=self.cbProperties.currentText()
        )

        self.done = True
        self.parent.startExperiment(d)
        self.close()
