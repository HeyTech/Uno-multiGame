#!/usr/bin/env python3
import socket
import sys

s = None
print('# Creating socket')
try:
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
except socket.error:
    print('Failed to create socket')


def login_to_game(host, user_name):
    port = 4444  # socket port number

    player_name = user_name+ " / HTTP/1.0\r\n\r\n"
    # create socket


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

# namn = input("namn: ")
# print("------", namn)
# login_to_game('192.168.233.164', str(namn))
# send_server_request("<CreateRoom Name='bla' Mode='single' Capacity='1/10' Players='username'/>")

"""
while (1):
    print('# Receive data from server')
    reply = s.recv(4096)
    print(reply)
"""




