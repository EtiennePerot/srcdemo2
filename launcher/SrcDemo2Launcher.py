import sys
import os
import re
import time
import base64
import tempfile
import subprocess
import threading

def module_path():
	if hasattr(sys, "frozen"):
		return os.path.dirname(unicode(sys.executable, sys.getfilesystemencoding()))
	return os.path.dirname(unicode(__file__, sys.getfilesystemencoding()))
selfDir = os.path.abspath(module_path())

allowedCommands = {}
def addCommand(commandName, command):
	global allowedCommands
	allowedCommands[commandName] = command

stringRe = re.compile(r'"((?:[^"\\]|\\.)*)"')
def parse_command(command):
	global stringRe, allowedCommands
	command = base64.b64decode(command).decode('utf8')
	allStrings = stringRe.findall(command)
	if not allStrings:
		return
	allStrings = [x.replace(u'\\"', u'"').replace(u'\\\\', u'\\') for x in allStrings]
	commandName = allStrings[0]
	arguments = allStrings[1:]
	if commandName in allowedCommands:
		print '[C] Executing command', commandName, 'with arguments', arguments
		try:
			allowedCommands[commandName](*arguments)
		except:
			print 'Error while running command', commandName, 'with arguments', arguments

class StreamRunner(threading.Thread):
	def __init__(self, process, streamIn, streamsOut, parseCommands=False):
		self.process = process
		self.streamIn = streamIn
		self.streamsOut = streamsOut
		self.parseCommands = parseCommands
		threading.Thread.__init__(self)
	def run(self):
		while self.process.poll() is None:
			l = self.streamIn.readline()
			for s in self.streamsOut:
				s.write(l)
			if self.parseCommands and len(l) > 4 and l[:3] == '[C]':
				parse_command(l[4:])

def is_windows():
	return sys.platform[:3].lower() == 'win'

def is_osx():
	return sys.platform.lower().find('darwin') != -1 or sys.platform.lower().find('osx') != -1

def get_java(debugMode):
	if is_windows():
		hiPriority = 'java.exe'
		loPriority = 'javaw.exe'
		print 'Finding', hiPriority, '/', loPriority
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
				elif i.lower() == hiPriority:
					return f # Immediately return
				elif i.lower() == loPriority:
					found = f # Keep looking for the other, just in case
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
				return foundJre
	elif is_osx():
		return ['jre-1.7.0/bin/java', '-d32', '-XstartOnFirstThread']
	return None

def add_subprocess_creationflags(kwargs):
	if is_windows():
		import win32process
		kwargs['creationflags'] = win32process.CREATE_NO_WINDOW
	return kwargs

def subprocess_call(command, *args, **kwargs):
	args = args[:]
	kwargs = add_subprocess_creationflags(kwargs.copy())
	kwargs['stdout'] = subprocess.PIPE
	kwargs['stderr'] = subprocess.PIPE
	return subprocess.call(command, *args, **kwargs)

def subprocess_getoutput(command, *args, **kwargs):
	args = args[:]
	kwargs = add_subprocess_creationflags(kwargs.copy())
	kwargs['stderr'] = subprocess.PIPE
	return subprocess.check_output(command, *args, **kwargs)

def attempt_unmount(mountpoint):
	global selfDir
	if is_windows():
		subprocess_call([selfDir + os.sep + 'tools' + os.sep + 'windows' + os.sep + 'dokanctl' + os.sep + 'dokanctl.exe', '/u', mountpoint.encode(sys.getfilesystemencoding()), '/f'])
addCommand('unmount', attempt_unmount)

lastMountPoint = None
def register_mountpoint(mountpoint):
	global lastMountPoint
	lastMountPoint = mountpoint
addCommand('register_mountpoint', register_mountpoint)

def unmount_registered_mountpoint():
	global lastMountPoint
	if lastMountPoint is not None:
		print 'Attempting unmount of', lastMountPoint
		attempt_unmount(lastMountPoint)

def addJvmArgument(printFlags, jvmArgs, default, prefix=None, xxArg=None):
	if prefix is not None:
		for i in jvmArgs:
			if len(i) > len(prefix) and i[:len(prefix)] == prefix:
				return
		jvmArgs.append(prefix + default)
	elif xxArg is not None and printFlags is not None and xxArg in printFlags:
		for i in jvmArgs:
			if len(i) > 4 and i[:4] == '-XX:' and xxArg in i:
				return
		jvmArgs.append('-XX:' + default)

def launch(debugMode=False):
	global selfDir
	debugMode = debugMode or '--srcdemo-debug' in sys.argv[1:]
	print 'Debug mode:', debugMode
	foundJre = get_java(debugMode)
	if foundJre is None:
		print 'JRE not found.'
		if is_windows():
			import win32api
			win32api.MessageBox(0, 'A 32-bit Java runtime environment (JRE) was not found.\nPlease download it from http://java.com/.\nEven if you are on 64-bit Windows, this program needs a 32-bit Java runtime to run.\n\nIf you are sure you have installed it already, please copy the jre folder next to SrcDemo2.exe.', 'Java not found.')
			return
		else:
			print 'The Java runtime environment was not found.'
	if type(foundJre) is not type([]):
		foundJre = [foundJre]
	javaHome = os.path.abspath(os.path.dirname(os.path.dirname(foundJre[0])))
	javaEnv = os.environ.copy()
	javaEnv['JAVA_HOME'] = javaHome
	javaVmArgs = []
	for i in sys.argv[1:]:
		if len(i) > 11 and i[:11] == '--jvm-args=':
			javaVmArgs.extend(i[11:].split(' '))
	jvmType = '-client'
	if '-server' not in javaVmArgs and '-client' not in javaVmArgs:
		# Probe for server JVM
		if subprocess_call([foundJre, '-server', '-version']) == 0:
			jvmType = '-server'
			javaVmArgs = ['-server'] + javaVmArgs
	# Get available flags
	printFlags = None
	try:
		printFlags = subprocess_getoutput([foundJre, jvmType, '-XX:+PrintFlagsFinal'])
	except:
		pass
	addJvmArgument(printFlags, javaVmArgs, '1024M',                   prefix='-Xmx')
	addJvmArgument(printFlags, javaVmArgs, '512k',                    prefix='-Xss')
	addJvmArgument(printFlags, javaVmArgs, ':none',                   prefix='-Xverify')
	addJvmArgument(printFlags, javaVmArgs, '+UseParallelGC',          xxArg='GC')
	addJvmArgument(printFlags, javaVmArgs, '+AggressiveOpts',         xxArg='AggressiveOpts')
	addJvmArgument(printFlags, javaVmArgs, '+UseFastAccessorMethods', xxArg='UseFastAccessorMethods')
	addJvmArgument(printFlags, javaVmArgs, '+UseStringCache',         xxArg='UseStringCache')
	addJvmArgument(printFlags, javaVmArgs, '+UseCompressedStrings',   xxArg='UseCompressedStrings')
	addJvmArgument(printFlags, javaVmArgs, '+OptimizeStringConcat',   xxArg='OptimizeStringConcat')
	addJvmArgument(printFlags, javaVmArgs, 'CompileThreshold=100',    xxArg='CompileThreshold')
	del printFlags
	command = foundJre + javaVmArgs + ['-jar', 'SrcDemo2.jar']
	outStreams = []
	errStreams = []
	if debugMode:
		outStreams.append(sys.stdout)
		errStreams.append(sys.stderr)
		command.append('--srcdemo-debug')
		print 'Debug mode allows the console output to be logged to a file.'
		print 'You may enter the complete path of the file to log to below.'
		print 'Make sure it is writable (i.e. don\'t put it in the installation directory).'
		print 'If you don\'t want the output to be logged, leave the line blank.'
		print 'If you\'re not sure what to type, type "?" and SrcDemo2 will guess a filename for you.'
		while True:
			logFile = raw_input('Log file (blank to not log, "?" for auto): ').strip()
			if logFile:
				if logFile in (u'"?"', u'?'):
					logFile = tempfile.mkstemp(suffix='.log', prefix='srcdemo2-' + time.strftime('%Y-%m-%d-at-%H-%M-%S') + '-', text=False)
					os.close(logFile[0])
					logFile = logFile[1]
					print 'Guessed log file:', logFile
				try:
					logHandle = open(logFile, 'wb')
					logHandle.write(u'Opened log.\n'.encode('utf8'))
					outStreams.append(logHandle)
					errStreams.append(logHandle)
					print 'Log file:', logFile
					break
				except:
					print 'Couldn\'t open this file for writing.'
					print 'Please make sure the file is writable.'
			else:
				break
	command.append('--srcdemo-jvm' + jvmType)
	command.extend(sys.argv[1:])
	returnCode = 0
	kwargs = add_subprocess_creationflags({
		'cwd': selfDir,
		'env': javaEnv,
		'stdin': subprocess.PIPE,
		'stdout': subprocess.PIPE,
		'stderr': subprocess.PIPE
	})
	while True:
		print 'Running', command
		p = subprocess.Popen(command, **kwargs)
		p.stdin.close()
		StreamRunner(p, p.stdout, outStreams, parseCommands=True).start()
		StreamRunner(p, p.stderr, errStreams).start()
		returnCode = p.wait()
		print 'Process finished with return code:', returnCode
		unmount_registered_mountpoint()
		if returnCode != 1337:
			break
	print 'Done.'
	if returnCode:
		sys.exit(returnCode)
