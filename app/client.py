#!/usr/bin/env python3
import socket
import errno
import sys

s = None


def login_to_game(host, user_name):
    port = 4444  # socket port number

    player_name = user_name+ " / HTTP/1.0\r\n\r\n"
    # create socket
    print('# Creating socket')
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        s.settimeout(5)
    except socket.error:
        print('Failed to create socket')

    print('# Getting remote IP address')
    try:
        remote_ip = socket.gethostbyname(host)
    except socket.gaierror:
        print('Hostname could not be resolved. Exiting')
        #sys.exit()
        return False

    try:
        # Connect to remote server
        print('# Connecting to server, ' + host + ' (' + remote_ip + ')')
        s.connect((remote_ip, port))

    except socket.error as serr:
        print(serr)
        if serr.errno == errno.EISCONN:
            print("{}".format(serr))
            s.close()
            # sys.exit()
        else:
            raise serr
    try:
        # Send data to remote server
        print('# Sending data to server')
        s.sendall(player_name.encode())
        # Receive data
        print('# Receive data from server')
        reply = s.recv(4096)

        print(reply)
        return reply

    except socket.error as e:
        print("Error creating socket: {}".format(e))
        return False


def send_server_request(command):
    try:
        command = command + " / HTTP/1.0\r\n\r\n"
        s.sendall(command.encode())
    except socket.error:
        print('Send failed')
        #sys.exit()
        return False

    # Receive data
    print('# Receive data from server')
    reply = s.recv(4096)

    print(reply)
    return reply