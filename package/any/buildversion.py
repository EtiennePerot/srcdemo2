import time

f = open('version.txt', 'w')
f.write(time.strftime('%Y-%m-%d'))
f.close()
