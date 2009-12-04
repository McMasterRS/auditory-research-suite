#!/usr/bin/env python

"""
Script for building sensory integration application bundle
"""

from distutils.core import setup
import sys, os


distDir=os.path.join(os.environ.get('HOME'), "Desktop/Auditory Experiments")

setupOpts = {
    'name' : 'Aud Research Suite',
    'description' : 'Perception Experiements, PI: Michael Schutz',
    'version' : '40',
    'author' : 'Simeon H.K. Fitch',
    'author_email' : 'simeon.fitch@mseedsoft.com'
}

if sys.platform == 'darwin':
    import py2app
    sys.argv.append("py2app")
    setupOpts['app'] = ['../SensoryIntegrationMain.py']
    setupOpts['options'] = {
        'py2app': {
            # This is a shortcut that will place MyApplication.icns
            # in the Contents/Resources folder of the application bundle,
            # and make sure the CFBundleIcon plist key is set appropriately.
            'iconfile': '../resources/MustardSeedIcon.icns',
            'dist_dir' : distDir,        
            'packages': ['wx', 'wxPython','ctypes']    
        },
#        'bdist-base' : ['/tmp/build-si']
    }

setup(**setupOpts)


# On the Mac, create a disk image of distribution
if sys.platform == 'darwin':
    print 'Creating disk image...'
    text = 'To install, drag application to another folder'
    f = open(os.path.join(distDir, text), 'w')
    f.close()
    os.system('hdiutil create -ov -format UDZO -scrub -imagekey zlib-level=9 -srcfolder "' + distDir + '" "' + distDir + '.dmg"')
    print 'Deleting build directory...'
    import shutil
    shutil.rmtree('./build', ignore_errors=True)
    print '...done'
    
    
