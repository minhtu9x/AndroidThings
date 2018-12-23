import socket
import sys
import os

# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
# Bind the socket to the port
server_address = ('0.0.0.0', 8088)
print >>sys.stderr, 'starting up on %s port %s' % server_address
sock.bind(server_address)

def readState(txt):
        file = open(txt,'r')
        return file.read(1)

def writeState(txt,state):
        file = open(txt,'w')
        file.write(state)


# Listen for incoming connections
sock.listen(1)

while True:
    # Wait for a connection
    print >>sys.stderr, 'waiting for a connection'
    connection, client_address = sock.accept()
    try:
        print >>sys.stderr, 'connection from', client_address

        # Receive the data in small chunks and retransmit it
        while True:
            data = connection.recv(16)
            print >>sys.stderr,'received "%s"' % data
            if data:
                #print >>sys.stderr, 'sending data back to the client'
                #connection.sendall(data)
		if('1' in data):
			os.system('python tien.py > /dev/null 2>/dev/null &')
		elif('2' in data):
                       	os.system('python lui.py > /dev/null 2>/dev/null &')
		elif('3' in data):
                        os.system('python trai.py > /dev/null 2>/dev/null &')
		elif('4' in data):
                        os.system('python phai.py > /dev/null 2>/dev/null & ')
		else:
			os.system('python dung.py > /dev/null 2>/dev/null &')
            else:
                print >>sys.stderr, 'no more data from', client_address
                break
            
    finally:
        # Clean up the connection
        connection.close()
