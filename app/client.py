#!/usr/bin/env python3
import socket
import errno
import sys


class Socket_class(object):
    def __init__(self):
        super(Socket_class, self).__init__()
        # create socket
        print('# Creating socket')
        try:
            self.s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.s.settimeout(10)
            print('# Done Creating socket')
        except socket.error:
            print('Failed to create socket')

        self.port = 4444  # socket port number

    def login_to_game(self, host, user_name):

        player_name = user_name + " / HTTP/1.0\r\n\r\n"

        print('# Getting remote IP address')
        try:
            remote_ip = socket.gethostbyname(host)
        except socket.gaierror:
            print('Hostname could not be resolved. Exiting')
            # sys.exit()
            return False

        try:
            # Connect to remote server
            print('# Connecting to server, ' + host + ' (' + remote_ip + ')')
            self.s.connect((remote_ip, self.port)) # Change to self.s.bind()...

        except socket.error as serr:
            print(serr)
            if serr.errno == errno.EISCONN:
                print("{}".format(serr))
                self.s.close()
                # sys.exit()
            else:
                raise serr
        try:
            # Send data to remote server
            print('# Sending data to server')
            self.s.sendall(player_name.encode())
            # Receive data
            print('# Receive data from server')
            reply = self.s.recv(4096)

            print(reply)
            return reply

        except socket.error as e:
            print("Error creating socket: {}".format(e))
            return False

    def send_server_request(self, _command):
        try:
            command = _command + " / HTTP/1.0\r\n\r\n"
            self.s.sendall(command.encode())
        except socket.error:
            print('Send failed')
            # sys.exit()
            return False

        # Receive data
        print('# Receive data from server')
        reply = self.s.recv(4096)
        print(reply)
        return reply