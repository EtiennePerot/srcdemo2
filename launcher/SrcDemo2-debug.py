#!/usr/bin/env python2

import traceback

try:
	import SrcDemo2Launcher
	SrcDemo2Launcher.launch(True)
except:
	traceback.print_exc()
try:
	from SrcDemo2Launcher import is_windows
	if is_windows():
		raw_input('Press Enter to close this window...')
except:
	raw_input('Press Enter to continue.')

