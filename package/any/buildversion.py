import time

f = open('version.txt', 'wb')
f.write(time.strftime('%Y-%m-%d'))
f.close()