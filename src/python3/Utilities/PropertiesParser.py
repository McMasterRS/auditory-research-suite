class PropertiesParser:
    def __init__(self, raw):
        self.prop = {}
        self.raw = raw 

    # Parse the raw data using a dict of keys and types
    def parseProperties(self, typeDict):
        for key, keyType in typeDict.items():
            try:
                self.parseCopy(key, keyType)
            except Exception as e:
                return False, "error parsing property '{0}'\n\n{1}".format(key, e)
        
        return True, self.prop

    # Take a key and a copy type and call the relevant copy function
    def parseCopy(self, key, copyType):
        
        if copyType == "str":
            self.copyKeyAsString(key)
        elif copyType == "int":
            self.copyKeyAsInt(key)
        elif copyType == "float":
            self.copyKeyAsFloat(key)
        elif copyType == "bool":
            self.copyKeyAsBool(key)
        elif copyType == "array":
            self.copyKeyAsArray(key)
        
    # Directly copy key from one dict to the other
    def copyKeyAsString(self, key):
        self.prop[key] = self.raw[key]

    # Convert key into int and copy
    def copyKeyAsInt(self, key):
        self.prop[key] = int(self.raw[key])

    # Convert key to float and copy
    def copyKeyAsFloat(self, key):
        self.prop[key] = float(self.raw[key])

    # Convert key into bool and copy
    def copyKeyAsBool(self, key):
        boolConv = {"true" : 1, "false" : 0}
        self.prop[key] = boolConv[self.raw[key].lower()]

    # Convert key into array of strings and copy
    def copyKeyAsArray(self, key):
        s = self.raw[key].replace(" ", "")
        self.prop[key] = s.split(",")