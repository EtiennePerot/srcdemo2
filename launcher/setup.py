from distutils.core import setup
import py2exe

import os
oldPath = os.getcwd()
os.chdir(os.path.abspath(os.path.dirname(__file__)))

import sys
sys.argv.append('py2exe')

setupName = 'SrcDemo2'
setupVersion= '1.0'
setupDescription = 'SrcDemo2'
setupAuthor = 'Etienne Perot'
mainOptionsRelease = [
	{
		'script': 'SrcDemo2.py',
		'icon_resources': [
			(1, '..' + os.sep + 'img' + os.sep + 'icon.ico')
		]
	}
]
mainOptionsDebug = [
	{
		'script': 'SrcDemo2-debug.py',
		'icon_resources': [
			(1, '..' + os.sep + 'img' + os.sep + 'debug.ico')
		]
	}
]
setupOptions = {
	'py2exe': {
		'compressed': 1,
		'optimize': 2,
		'bundle_files': 3,
		'excludes': ['_ssl', 'pyreadline', 'difflib', 'doctest', 'locale', 'optparse', 'pickle', 'calendar'],
		'dll_excludes': ['msvcr71.dll']
	}
}

setup(
	name = setupName,
	version = setupVersion,
	description = setupDescription,
	author = setupAuthor,
	windows = mainOptionsRelease,
	options = setupOptions
)
setup(
	name = setupName,
	version = setupVersion,
	description = setupDescription,
	author = setupAuthor,
	console = mainOptionsDebug,
	options = setupOptions
)
os.chdir(oldPath)
