#!/usr/bin/env python

import os, sys

selfDir = os.path.abspath(os.path.dirname(sys.argv[0]))

template = open(selfDir + os.sep + 'Info.plist.template', 'rb').read(-1)
version = open(selfDir + os.sep + os.pardir + os.sep + os.pardir + os.sep + 'version.txt', 'rb').read(-1)

template = template.replace('%version%', version)

outputFile = open(selfDir + os.sep + 'Info.plist', 'wb')
outputFile.write(template)
outputFile.close()
