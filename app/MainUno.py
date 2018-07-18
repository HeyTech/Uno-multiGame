import sys
from PIL import ImageTk, Image
import client
import tkinter as tk
import os
import json
from collections import OrderedDict
import time, threading


class Application(tk.Frame):
    def __init__(self, master=None):
        self.client = client.Socket_class()
        self.player_name = ''
        self.selected_option = ''
        super().__init__(master)
        root.geometry('{}x{}'.format(700, 700))
        # root.config(bg='black')
        root.resizable(width=False, height=False)
        root.title("Uno Multi game")
        self.pack()
        self.login_page()

    def clean_frame(self):
        for widget in self.winfo_children():
            widget.destroy()

    def login_page(self):
        self.clean_frame()
        self.label_welcome_uno = tk.Label(self, text='Welcome to UNO Game',
                                          font=("Arial Bold", 35), foreground='black', justify='center')
        self.label_welcome_uno.pack()

        self.image = Image.open("images/download.png")
        self.image = self.image.resize((500, 500), Image.ANTIALIAS)
        self.img_copy = self.image.copy()
        self.background_image = ImageTk.PhotoImage(self.img_copy)
        self.background_label = tk.Label(self, image=self.background_image)
        self.background_label.place(x=0, y=0, relwidth=1, relheight=1)
        self.background_label.pack()

        self.label_host_ip = tk.Label(self, text='Host ip: ')
        self.label_host_ip.pack(side="left")

        self.host_ip = tk.Entry(self)
        self.host_ip.pack(side="left")

        self.label_user_name = tk.Label(self, text='Username')
        self.label_user_name.pack(side="left")

        self.user_name = tk.Entry(self)
        self.user_name.pack(side="left")

        self.login_btn = tk.Button(self)
        self.login_btn["text"] = "LOGIN"
        self.login_btn["command"] = self.login_command
        self.login_btn.config(bg='green')
        self.login_btn.pack(side="left")

        self.quit = tk.Button(self, text="QUIT", fg="black", command=root.destroy)
        self.quit.config(bg='red')
        self.quit.pack(side="left")

        self.label_welcome = tk.Label(text="login please")
        self.label_welcome.pack(side="bottom")

    def playing_page(self):
        self.clean_frame()

    def exit(self):
        from_server = (self.client.send_server_request("<Exit/>")).decode()
        if "Successfully" in from_server:
            sys.exit()
    def winning_page(self):
        pass

    def putdowncard_to_server(self, card, room):
        message_to_server_to_throw_cards = "<PlayingCard " + "\'" + room + "\' \'" + card + "\'/>"
        print(message_to_server_to_throw_cards)
        put_down_card_to_server = self.client.send_server_request(message_to_server_to_throw_cards).decode()
        print(put_down_card_to_server)
        if "Over" in put_down_card_to_server:
            self.winning_page()
        else:
            put_down_from_server = json.loads(put_down_card_to_server)
            self.game_play(put_down_from_server)

    def uno_to_server(self, roomname, player):
        uno_message = "<Uno " + "\'" + roomname + "\' " + "\'" + player + "\'/>"
        print(uno_message)
        uno_return_from_server = json.loads(self.client.send_server_request(uno_message).decode())
        print(uno_return_from_server)
        self.game_play(uno_return_from_server)

    def new_card_to_server(self,roomname):
        new_card_message = "<NewCard " + "\'" + roomname + "\'/>"
        print(new_card_message)
        new_card_return_from_server = json.loads(self.client.send_server_request(new_card_message).decode())
        print(new_card_return_from_server)
        self.game_play(new_card_return_from_server)

    def fetch_game_to_server(self, roomname, joinroom):
        fetch_game_message = "<FetchGameRoom '" + roomname + "'/>"
        self.fetch_game_decoded = self.client.send_server_request(fetch_game_message).decode()
        print(self.fetch_game_decoded)
        self.fetch_game_response = json.loads(self.fetch_game_decoded)
        room_info = self.fetch_game_response["RoomInfo"]
        game_status = room_info["GameStarted"]
        if game_status:
            self.game_play(self.fetch_game_response)
        else:
            self.go_to_wait_frame(joinroom)

    def game_play(self, game_start):
        #while(1):
            #game_start = input('mujtaba is the stupid: ')

            self.clean_frame()
            cards_string_json = game_start
            # cards_string_json = {"RoomInfo":{"Mode":"2v2","Teams":{"TeamA":["Mujtaba","Mona"],"TeamB":["Ranju","Nandu"]},"Admin":"Mona",
            #                                  "Online":"4/4","Name":"Naai 1123","Players":["Mona","Mujtaba","Ranju","Nandu"],
            #                                  "ReadyPlayers":["Mujtaba","Ranju"],"GameStarted":True},
            #                      "BoardInfo":{"OpenCard":"y5","CurrentTurn":"Ranju","Blocked":"Nandu","NextTurn":"Mona","TurnTime":"10",
            #                                   "PlayersInfo":{"Mujtaba":{"Cards":[],"NumberOfCards":3,"Score":0,"Uno":False},"Mona":{"Cards":[],
            #                                                                                                                         "NumberOfCards":6,"Score":0,"Uno":False},"Ranju":{"Cards":["r1","r3","r4","r8"],"NumberOfCards":2,"Score":0,"Uno":False},
            #                                                  "Nandu":{"Cards":["g1","y3","y4","y8"],"NumberOfCards":11,"Score":0,"Uno":False}}},
            #                      "avilableCards":["b1","b3","b4","b8","..."]}
            room_info = cards_string_json["RoomInfo"]
            board_info = cards_string_json["BoardInfo"]
            players = board_info["PlayersInfo"]
            room_name = room_info["RoomName"]
            folder_path = os.getcwd()
            collection = "Images"
            file_path = os.path.join(folder_path, collection)
            back_image = 'cb.png'
            back_image_path = os.path.join(file_path, back_image)
            side = ["top", "bottom", "left", "right"]
            open_card = board_info["OpenCard"] + '.png'
            open_card_path = os.path.join(file_path, open_card)

            i = 0
            for player in players:
                cards = (players[player]["Cards"])
                number_of_cards = (players[player]["NumberOfCards"])

                if player != self.player_name:
                    player_name = player
                    player_frame = tk.LabelFrame(self)
                    player_frame.pack(side=side[i])
                    player_button = tk.Button(player_frame, text=player_name, command=lambda pl_name=player_name: self.uno_to_server(room_name, pl_name))
                    player_button.pack(side='top')
                    if os.path.exists(back_image_path):
                        back_img = Image.open(back_image_path)
                        back_img = back_img.resize((30, 30), Image.ANTIALIAS)
                        back_img_dis = ImageTk.PhotoImage(back_img)
                        image_label_for_other_players = tk.Label(player_frame, image=back_img_dis)
                        image_label_for_other_players.image = back_img_dis
                        image_label_for_other_players.pack(side='bottom')
                        no_of_cards = tk.Label(player_frame, text=int(number_of_cards))
                        no_of_cards.pack(side='bottom')
                else:
                    player_name = player
                    team_cards = cards
                    player_frame = tk.LabelFrame(self)
                    player_frame.pack(side=side[i])
                    player_label = tk.Label(player_frame, text=player_name)
                    player_label.pack(side='top')
                    for card in team_cards:
                        image_name = card + ".png"
                        image_path = os.path.join(file_path, image_name)
                        if os.path.exists(image_path):
                            img = Image.open(image_path)
                            img = img.resize((30, 30),Image.ANTIALIAS)
                            img_dis = ImageTk.PhotoImage(img)
                            if player_name == self.player_name:
                                label = tk.Button(player_frame, image=img_dis, command= lambda card_name=card : self.putdowncard_to_server(card_name, room_name))
                                label.image = img_dis
                                label.pack(side='left')
                            else:
                                label = tk.Button(player_frame, image=img_dis, command= lambda card_name=card : self.putdowncard_to_server(card_name, room_name))
                                label.image = img_dis
                                label.pack(side='left')
                i +=1
            self.new_card_button = tk.Button(self, text="New Card", command=lambda: self.new_card_to_server(room_name))
            self.uno_button = tk.Button(self, text="Uno", command=lambda: self.uno_to_server(room_name, self.player_name))
            self.new_card_button.pack(side='left')
            self.uno_button.pack(side='right')
            if os.path.exists(open_card_path):
                open_card_img = Image.open(open_card_path)
                open_card_img = open_card_img.resize((30, 30), Image.ANTIALIAS)
                open_card_dis = ImageTk.PhotoImage(open_card_img)
                open_card_label = tk.Label(self, image=open_card_dis)
                open_card_label.image = open_card_dis
                open_card_label.pack(anchor=tk.CENTER)
            #self.fetch_game_to_server(room_name, cards_string_json)
            fetch_game_info = tk.Button(self, text="Fetch Game Information", command=lambda rm=room_name: self.fetch_game_to_server(rm, cards_string_json))
            fetch_game_info.pack(side='bottom')

    def get_name(self):
        name_of_the_room = self.room_name.get()
        if self.selected_option == "Single":
            capacity = "1/8"
        else:
            capacity = "1/4"
        print("<CreateRoom Name=" + str('\'') + str(name_of_the_room) + str('\'') + " Mode=" + str('\'') +
              str(self.selected_option) + str('\'') + " Capacity=" + str('\'') + str(capacity) + str('\'') +
              " Player=" + str('\'') + str(self.player_name) + str('\'/>'))
        command_to_server = str("<CreateRoom Name=") + str('\'') + str(name_of_the_room) + str('\'') + str(" Mode=") + str('\'') + str(self.selected_option) + str('\'') + " Capacity=" + str('\'') + str(capacity) + str('\'') + str(" Player=") + str('\'') + str(self.player_name) + str('\'/>')
        room_message = self.client.send_server_request(command_to_server).decode()
        print(room_message)
        if "Failed" in room_message:
            self.label_room = tk.Label(self, text="Room name is already taken. Try with a different room name")
            self.label_room.pack()
        else:
            room_message = json.loads(room_message)
            self.go_to_wait_frame(room_message)
            #self.game_play(room_message)

    def go_back_to_game_mode(self, roomname, join_room):
        to_server = "<LeaveRoom " + "\'" + str(roomname) + "\'"
        message_from_server = (self.client.send_server_request(to_server)).decode()
        if "Successfully" in message_from_server:
            self.game_mode()
        else:
            self.go_to_wait_frame(join_room)

    def selection_made(self):
        self.selected_option = self.var.get()

    def create_room(self):
        self.var = tk.StringVar()
        self.clean_frame()
        self.frame_room_name = tk.LabelFrame(self, padx=5, pady=5)
        self.frame_room_name.pack(padx=10, pady=10)
        self.label_room_name = tk.Label(self.frame_room_name, text="Room Name")
        self.label_room_name.pack(side="left")
        self.room_name = tk.Entry(self.frame_room_name)
        self.room_name.pack(side="left")

        self.frame_room_mode = tk.LabelFrame(self, padx=5, pady=5)
        self.frame_room_mode.pack(padx=10, pady=10)
        self.label_room_mode = tk.Label(self.frame_room_mode, text="Room Mode")
        self.label_room_mode.pack(side="left")
        self.multi_btn = tk.Radiobutton(self.frame_room_mode, text="2V2", variable=self.var, value="2V2", command=self.selection_made)
        self.multi_btn.pack(anchor=tk.W)
        self.single_btn = tk.Radiobutton(self.frame_room_mode, text="Single", variable=self.var, value="Single", command=self.selection_made)
        self.single_btn.pack(anchor=tk.W)

        self.btn_frame = tk.LabelFrame(self, padx=5, pady=5)
        self.btn_frame.pack(padx=10, pady=10)
        self.btn_create = tk.Button(self.btn_frame, text="Create", command=self.get_name)
        self.btn_create.pack(side="left")
        self.btn_cancel = tk.Button(self.btn_frame, text="Cancel", command=self.game_mode)
        self.btn_cancel.pack(side="left")
        self.exit_button = tk.Button(self, text="Exit", command=self.exit)
        self.exit_button.pack(side='bottom')

    def update_list(self):
        update_command = "<UpdateLists/>"
        message_from_server = self.client.send_server_request(update_command)
        print(message_from_server)
        players_rooms = json.loads(message_from_server.decode())
        self.players_list = players_rooms['Online players']
        self.game_mode_list = players_rooms["Rooms Created"]
        self.game_mode_list = (list(OrderedDict.fromkeys(self.game_mode_list)))
        self.players_list = (list(OrderedDict.fromkeys(self.players_list)))
        self.game_list.delete(0, tk.END)
        self.player_list.delete(0, tk.END)
        for mode in self.game_mode_list:
            self.game_list.insert(tk.END, mode)
        for players in self.players_list:
            self.player_list.insert(tk.END, players)

        self.game_list.pack(side="left")
        self.player_list.pack(side="right")

    def go_to_wait_frame(self, join_room):
        self.clean_frame()
        decoded_server = join_room["RoomInfo"]
        mode = decoded_server["Mode"]
        players = decoded_server["Players"]
        teams = decoded_server["Teams"]
        admin = decoded_server["Admin"]
        ready_players = decoded_server["ReadyPlayers"]
        room_name = decoded_server["RoomName"]

        def start_game_to_server(roomname, joinroom):
            start_game_message = "<StartGame '" + roomname + "\'/>"
            self.start_game_response_decoded = self.client.send_server_request(start_game_message).decode()
            print(self.start_game_response_decoded)
            if 'Failed' in self.start_game_response_decoded:
                self.go_to_wait_frame(joinroom)
            else:
                self.start_game_response = json.loads(self.start_game_response_decoded)
                self.game_play(self.start_game_response)




        def getting_ready():
            ready_message = "<GettingReady '" + room_name + '\' \'' + self.player_name + '\'/>'
            self.ready_message = json.loads((self.client.send_server_request(ready_message)).decode())
            print(self.ready_message)
            self.go_to_wait_frame(self.ready_message)
        self.ready_button = tk.Button(self, text="Ready", command=getting_ready)
        self.ready_button.pack(side="bottom")

        def button_team_click(name):
            command_to_server_teamselection = "<ChooseTeam '" + room_name + '\' \'' + self.player_name + '\' \'' + name + '\'/>'
            self.team_join_message = json.loads((self.client.send_server_request(command_to_server_teamselection)).decode())
            print(self.team_join_message)
            self.go_to_wait_frame(self.team_join_message)
        if mode == "Single":
            self.listbox_single = tk.Listbox(self)
            for player in players:
                self.listbox_single.insert(tk.END, player)
                if player in ready_players:
                    self.listbox_single.itemconfig(tk.END, bg="green")
            self.listbox_single.pack(side="bottom")
            self.exit_button = tk.Button(self, text="Exit", command=self.exit)
            self.exit_button.pack(side='bottom')

        else:
            i = 0
            side_waitframe = ["right", "left"]
            for team in teams:
                self.team = team
                team_members = teams[team]
                self.team_label = tk.LabelFrame(self)
                self.team_label.pack(side=side_waitframe[i])
                self.team_name = tk.Button(self.team_label, text=team, command=lambda team=team: button_team_click(team))
                self.team_name.pack(side='bottom')
                self.team_members_listbox = tk.Listbox(self.team_label)
                for member in team_members:
                   self.team_members_listbox.insert(tk.END, member)
                   if member in ready_players:
                        self.team_members_listbox.itemconfig(tk.END, bg="green")
                self.team_members_listbox.pack(side="bottom")
                i += 1
            self.exit_button = tk.Button(self, text="Exit", command=self.exit)
            self.exit_button.pack(side='bottom')
        if admin == self.player_name:
            start_game = tk.Button(self, text="Start Game", command=lambda rm=room_name: start_game_to_server(rm, join_room))
            start_game.pack(side='bottom')
        else:
            fetch_game_info = tk.Button(self, text="Fetch Game Information", command=lambda rm=room_name: self.fetch_game_to_server(rm, join_room))
            fetch_game_info.pack(side='bottom')
        #self.fetch_game_to_server(room_name, join_room)
        back_to_join_room_page = tk.Button(self, text="Leave Room")
        back_to_join_room_page["command"] = lambda: self.go_back_to_game_mode(room_name, join_room)
        back_to_join_room_page.pack(side='bottom')

    def game_mode(self):
        self.clean_frame()
        self.game_mode_frame = tk.LabelFrame(self, padx=5, pady=5)
        self.game_mode_frame.pack(side="left", padx=10, pady=10)
        self.game_mode_name = tk.Label(self.game_mode_frame, text="Game Modes")
        self.game_mode_name.pack()
        self.game_list = tk.Listbox(self.game_mode_frame)

        self.player_list_frame = tk.LabelFrame(self, padx=5, pady=5)
        self.player_list_frame.pack(side="right", padx=10, pady=10)
        self.player_list_name = tk.Label(self.player_list_frame, text="Player list")
        self.player_list_name.pack()
        self.player_list = tk.Listbox(self.player_list_frame)

        self.update_btn = tk.Button(self, text="Update", command=self.update_list)
        self.update_btn.pack(side="bottom")
        self.create_btn = tk.Button(self, text="Create Room", command=self.create_room)
        self.create_btn.pack(side="bottom")
        self.game_label = tk.Label(self, text='Select Mode')
        self.player_label = tk.Label(self, text='Select Player')

        self.game_label.pack(side="bottom")
        self.player_label.pack(side="bottom")

        def get_game_mode(*x):
            try:
                r = self.game_list.curselection()[0]
                game_list_selected = self.game_mode_list[r]
                self.game_label.config(text=game_list_selected)
                self.game_label.pack()
                if '2V2' in game_list_selected:
                    to_join_room = game_list_selected.split(' 2V2 ')[0]
                else:
                    to_join_room = game_list_selected.split(' Single ')[0]
                new_command = "<JoinRoom '" + str(to_join_room) + "' '" + str(self.player_name) + "'/>"
                self.join_room = self.client.send_server_request(new_command).decode()
                print(self.join_room)
                if 'Failed' in self.join_room:
                    self.join_label = tk.Label(self, text=self.join_room)
                    self.join_label.pack()
                else:
                    self.join_room = json.loads(self.join_room)
                    roominfo = (self.join_room["RoomInfo"])
                    game_status = roominfo["GameStarted"]
                    print(game_status)
                    if game_status:
                        self.game_play(self.join_room)
                    else:
                        self.go_to_wait_frame(self.join_room)
            except:
                pass
        self.game_list.bind("<<ListboxSelect>>", get_game_mode)

        def get_player(*x):
            try:
                w = self.player_list.curselection()[0]
                player_list_selected = self.players_list[w]
                self.player_label.config(text=player_list_selected)
                self.player_label.pack()
            except:
                pass
        self.player_list.bind("<<ListboxSelect>>", get_player)
        self.exit_button = tk.Button(self, text="Exit", command=self.exit)
        self.exit_button.pack(side='bottom')

    def login_command(self):
        try:
            print("hi there, everyone!")
            self.player_name = self.user_name.get()
            print(self.host_ip.get(), self.user_name.get())
            login_message_from_server = (self.client.login_to_game(self.host_ip.get(), self.user_name.get())).decode()
            if "Accepted connection" in str(login_message_from_server):
                self.label_welcome['text'] = "You successfully loged in"
                self.playing_page()
                self.game_mode()
            else:
                self.label_welcome['text'] = login_message_from_server[1:-3] + str("\ntry again with a different name")
        except:
            self.label_welcome['text'] = "An error has occurred, Close the client and try again"


if __name__ == '__main__':
    root = tk.Tk()
    app = Application(master=root)
    app.mainloop()
    # start_game()
