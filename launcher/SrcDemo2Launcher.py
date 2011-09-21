import sys
import os
import subprocess
import threading
import win32api
import win32process

def module_path():
    if hasattr(sys, "frozen"):
        return os.path.dirname(unicode(sys.executable, sys.getfilesystemencoding()))
    return os.path.dirname(unicode(__file__, sys.getfilesystemencoding()))
selfDir = os.path.abspath(module_path())

class StreamRunner(threading.Thread):
    def __init__(self, process, streamIn, streamsOut):
        self.process = process
        self.streamIn = streamIn
        self.streamsOut = streamsOut
        threading.Thread.__init__(self)
    def run(self):
        while self.process.poll() is None:
            l = self.streamIn.readline()
            for s in self.streamsOut:
                s.write(l)

def launch(debugMode=False):
    global selfDir
    print 'Debug mode:', debugMode
    hiPriority = 'javaw.exe'
    loPriority = 'java.exe'
    if debugMode:
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
            break
    if foundJre is None:
        print 'JRE not found.'
        win32api.MessageBox(0, 'A 32-bit Java runtime (JRE) was not found.\nPlease download it from http://java.com/.\nEven if you are on 64-bit Windows, this program needs a 32-bit Java runtime to run.\n\nIf you are sure you have installed it already, please copy the jre folder next to SrcDemo2.exe.', 'Java not found.')
        return
    command = [foundJre, '-jar', 'SrcDemo2.jar'] + sys.argv[1:]
    outStreams = [sys.stdout]
    errStreams = [sys.stderr]
    if debugMode:
        print 'Debug mode allows the console output to be logged to a file.'
        print 'You may enter the complete path of the file to log to below.'
        print 'Make sure it is writable (i.e. don\'t put it in Program Files).'
        print 'If you don\'t want the output to be logged, leave the line blank.'
        while True:
            logFile = raw_input('Log file (blank to not log): ').strip()
            if logFile:
                try:
                    logHandle = open(logFile, 'wb')
                    logHandle.write(u'Opened log.\n'.encode('utf8'))
                    outStreams.append(logHandle)
                    errStreams.append(logHandle)
                    break
                except:
                    print 'Couldn\'t open this file for writing.'
                    print 'Please make sure the file is writable.'
            else:
                break
    while True:
        print 'Running', command
        p = subprocess.Popen(command, cwd=selfDir, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.PIPE, creationflags=win32process.CREATE_NO_WINDOW)
        p.stdin.close()
        if debugMode:
            sOut = StreamRunner(p, p.stdout, outStreams).start()
            sErr = StreamRunner(p, p.stderr, errStreams).start()
        returnCode = p.wait()
        print 'Process finished with return code:', returnCode
        if returnCode != 1337:
            break
    print 'Done.'
