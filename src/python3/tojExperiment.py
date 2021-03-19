import sys
import asyncio
from PyQt5 import QtWidgets
from quamash import QSelectorEventLoop
from Experiments.ExperimentBase import ExperimentBase

if __name__ == '__main__':

    app = QtWidgets.QApplication(sys.argv)
    loop = QSelectorEventLoop(app)
    asyncio.set_event_loop(loop)
    gui = ExperimentBase()
    QtWidgets.QApplication.instance().exec_()
