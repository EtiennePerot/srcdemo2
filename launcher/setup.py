from distutils.core import setup
import py2exe

import os
oldPath = os.getcwd()
os.chdir(os.path.abspath(os.path.dirname(__file__)))

import sys
sys.argv.append('py2exe')

py2exe_options = {
	'excludes': ['_ssl', 'pyreadline', 'difflib', 'doctest', 'locale', 'optparse', 'pickle', 'calendar'],
	'dll_excludes': ['msvcr71.dll'],
	'compressed': True
}

setup(
	name = 'SrcDemo2',
	version = '1.0',
	description = 'SrcDemo2',
	author = 'Etienne Perot',
	windows = ['SrcDemo2.py'],
	options = {
		'py2exe': py2exe_options
	}
)
os.chdir(oldPath)