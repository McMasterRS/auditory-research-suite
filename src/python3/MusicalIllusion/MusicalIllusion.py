import sys
import asyncio
from PyQt5 import QtWidgets
from quamash import QSelectorEventLoop
from MusicalIllusion.Experiments.ExperimentBase import ExperimentBase


def startApp():
    app = QtWidgets.QApplication(sys.argv)
    loop = QSelectorEventLoop(app)
    asyncio.set_event_loop(loop)
    gui = ExperimentBase()
    QtWidgets.QApplication.instance().exec_()


if __name__ == "__main__":
    startApp()
