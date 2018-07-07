#!/usr/bin/env python
import socket
import sys

def login_to_game(host, player_name):
    port = 4444  # web

    player_name += " / HTTP/1.0\r\n\r\n"
    # create socket
    print('# Creating socket')
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    except socket.error:
        print('Failed to create socket')
        #sys.exit()
        return False

    print('# Getting remote IP address')
    try:
        remote_ip = socket.gethostbyname(host)
    except socket.gaierror:
        print('Hostname could not be resolved. Exiting')
        #sys.exit()
        return False

    # Connect to remote server
    print('# Connecting to server, ' + host + ' (' + remote_ip + ')')
    s.connect((remote_ip, port))

    # Send data to remote server
    print('# Sending data to server')

    try:
        s.sendall(player_name.encode())
    except socket.error:
        print('Send failed')
        #sys.exit()
        return False

    # Receive data
    print('# Receive data from server')
    reply = s.recv(4096)

    print(reply)
    return True




def send_server_request():
    ## Send message

    print('# Sending data to server')

    try:
        s.sendall("<ReadyToPlay/> / HTTP/1.0\r\n\r\n".encode())
    except socket.error:
        print('Send failed')
        sys.exit()

    while (1):
        # Receive data
        print('# Receive data from server')
        reply = s.recv(4096)
        print(reply)