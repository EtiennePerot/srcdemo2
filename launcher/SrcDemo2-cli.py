#!/usr/bin/env python2

import traceback

try:
	import sys
	if '--srcdemo-cmd' not in sys.argv:
		sys.argv.append('--srcdemo-cmd')
	if len(sys.argv) == 2: # Display help if no argument is provided
		sys.argv.append('-h')
	import SrcDemo2Launcher
	SrcDemo2Launcher.launch(False)
except:
	traceback.print_exc()
