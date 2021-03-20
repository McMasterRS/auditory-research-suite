import os.path

# Gets the OS appropriate path for any file in the code
def getAppPath(path):
    return os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)),"..", *path))

def getGui(path):
    return getAppPath(["Interfaces/InterfaceLayouts/", path])

# Merges strings to make a path and verifies if it exists
def mergePathsVerify(paths):
    path = os.path.normpath(os.path.join(*paths))
    return path, os.path.exists(path)