import sys, asyncio
from PyQt5 import QtWidgets, QtGui, uic, QtCore
from quamash import QSelectorEventLoop

from Interfaces.TOJExperimentPanel import TOJExperimentPanel

if __name__ == '__main__':
    
    app = QtWidgets.QApplication(sys.argv)
    loop = QSelectorEventLoop(app)
    asyncio.set_event_loop(loop)
    gui = TOJExperimentPanel()
    QtWidgets.QApplication.instance().exec_()
    