  1. Install official MacOS Python 2.6.x from python.org.
  1. Make sure /usr/local/bin proceeds /usr/bin in PATH variable. Typing "python -V" should report version 2.6.x. Another option is to just make sure you run "python2.6" instead of "python" when running commands.
  1. Install wxWindows for Python 2.6 (ansi version)
  1. Install pygame for Python 2.6
  1. Build and install PyOpenGL from lib in SVN (expand source and run "python2.6 setup.py install" in distribution).
  1. Test running SensoryIntegrationMain.py.
  1. To package standalone executable:
    1. Install modulegraph-0.7.2.tgz from lib in SVN
    1. Install py2app-0.4.2.tgz from lib in SVN

Links to libraries needed to run SI code

  * [wxPython](http://www.wxpython.org/download.php): GUI toolkit
  * [PyGame](http://www.pygame.org/download.shtml): Audio playback support (to be replaced)
  * [PyOpenGL](http://pyopengl.sourceforge.net/): Animation rendering

Development environment:
  * [Aptana Studio for Python](http://www.aptana.org/studio/download):
    * Use default studio installer and then select the PyDev add-on after first launch.
    * SVN support is already provided.