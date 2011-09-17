from distutils.core import setup
import py2exe

import os
oldPath = os.getcwd()
os.chdir(os.path.abspath(os.path.dirname(__file__)))

import sys
sys.argv.append('py2exe')

setup(
	name = 'SrcDemo2',
	version = '1.0',
	description = 'SrcDemo2',
	author = 'Etienne Perot',
	windows = [
		{
			'script': 'SrcDemo2.py',
			'icon_resources': [
				(1, '..' + os.sep + 'img' + os.sep + 'icon.ico')
			]
		}
	],
	options = {
		'py2exe': {
			'compressed': 1,
			'optimize': 2,
			'bundle_files': 3,
			'excludes': ['_ssl', 'pyreadline', 'difflib', 'doctest', 'locale', 'optparse', 'pickle', 'calendar'],
			'dll_excludes': ['msvcr71.dll']
        }
	}
)
os.chdir(oldPath)