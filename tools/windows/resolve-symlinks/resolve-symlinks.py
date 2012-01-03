import os, sys, re

if sys.version_info < (3,):
	print('This version of Python does not support symlink resolution on Windows. Please upgrade.', file=sys.stderr)
	sys.exit(1)

# Make strings binary
sys.stdin = sys.stdin.detach()
sys.stdout = sys.stdout.detach()

path = sys.stdin.readline()

path = os.path.abspath(path.decode('utf8').strip())
finalPath = []
while len(path) and os.path.dirname(path) != path:
	if os.path.islink(path):
		path = os.readlink(path)
	else:
		finalPath.insert(0, os.path.basename(path))
		path = os.path.dirname(path)
finalPath.insert(0, path)
finalPath = re.sub(re.escape(os.sep) + '{2,}', re.escape(os.sep), os.sep.join(finalPath))
sys.stdout.write(finalPath.encode('utf8'))
