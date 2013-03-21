#!/usr/bin/env python

import sys, time

if '-' in sys.argv:
	print(time.strftime('%Y-%m-%d'))
else:
	f = open('version.txt', 'w')
	f.write(time.strftime('%Y-%m-%d'))
	f.close()
