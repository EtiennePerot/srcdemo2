import os
import sys
import subprocess
import win32api
import win32process

def module_path():
	if hasattr(sys, "frozen"):
		return os.path.dirname(unicode(sys.executable, sys.getfilesystemencoding()))
	return os.path.dirname(unicode(__file__, sys.getfilesystemencoding()))
selfDir = os.path.abspath(module_path())

def findJre(d):
	if not os.path.exists(d) or not os.path.isdir(d):
		return None
	found = None
	for i in os.listdir(d):
		f = d + os.sep + i
		if os.path.isdir(f):
			res = findJre(f)
			if res is not None:
				return res
		elif i.lower() == 'java.exe':
			found = f # Keep looking for javaw.exe
		elif i.lower() == 'javaw.exe':
			return f # Immediately return
	return found

lookIn=[selfDir]
if 'PROGRAMFILES(X86)' in os.environ:
	lookIn.append(os.environ['PROGRAMFILES(X86)'] + os.sep + 'Oracle')
	lookIn.append(os.environ['PROGRAMFILES(X86)'] + os.sep + 'Java')
if 'PROGRAMFILES' in os.environ:
	lookIn.append(os.environ['PROGRAMFILES'] + os.sep + 'Oracle')
	lookIn.append(os.environ['PROGRAMFILES'] + os.sep + 'Java')
foundJre = None
for p in lookIn:
	foundJre = findJre(p)
	if foundJre is not None:
		break

if foundJre is None:
	win32api.MessageBox(0, 'A 32-bit Java runtime (JRE) was not found.\nPlease download it from http://java.com/.\nEven if you are on 64-bit Windows, this program needs a 32-bit Java runtime to run.\n\nIf you are sure you have installed it already, please copy the jre folder next to SrcDemo2.exe.', 'Java not found.')
	sys.exit(1)

subprocess.call([foundJre, '-Xmx2048m', '-jar', 'SrcDemo2.jar'], cwd=selfDir, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, creationflags=win32process.CREATE_NO_WINDOW)
