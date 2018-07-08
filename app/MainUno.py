import re
from PIL import ImageTk, Image
import client
import tkinter as tk


class Application(tk.Frame):
    def __init__(self, master=None):
        self.player_name = ''
        self.selected_option = ''
        super().__init__(master)
        root.geometry('{}x{}'.format(700, 700))
        # root.config(bg='black')
        root.resizable(width=False, height=False)
        root.title("Uno Multi game")
        self.pack()
        self.login_page()

    def login_page(self):
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

    def clean_frame(self):
        for widget in self.winfo_children():
            widget.destroy()

    def game_play(self):
        self.clean_frame()

    def get_name(self):
        self.name_of_the_room = self.room_name.get()
        if self.selected_option == "Single":
            self.capacity = "1/10"
        else:
            self.capacity = "4/10"
        print("<CreateRoom Name=" + str('\'') + str(self.name_of_the_room) + str('\'') + " Mode=" + str('\'') +
              str(self.selected_option) + str('\'') + " Capacity=" + str('\'') + str(self.capacity) + str('\'') +
              " Player=" + str('\'') + str(self.player_name) + str('\'/>'))
        self.game_play()

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

    def update_list(self):
        self.online_list = self.splitted[0].split(":")
        self.players_list = [re.sub(r"[^a-zA-Z0-9]+", ' ', k) for k in self.online_list[1].split(",")]
        self.rooms_list = self.splitted[1].split(":")
        self.game_mode_list = [re.sub(r"[^a-zA-Z0-9]+/", ' ', k) for k in self.rooms_list[1].split(",")]
        ch = ['{', '}', '[', ']', "'"]
        for i in range(len(self.game_mode_list)):
            for le in self.game_mode_list[i]:
                if le in ch:
                    self.game_mode_list[i] = self.game_mode_list[i].replace(le, "")
        for mode in self.game_mode_list:
            self.game_list.insert(tk.END, mode)
        for players in self.players_list:
            self.player_list.insert(tk.END, players)

        self.game_list.pack(side="left")
        self.player_list.pack(side="right")

    def game_mode(self):
        self.clean_frame()
        string = "'Online players':['Mujtaba playing', 'Nandu playing', 'Ranju ready'] - 'Rooms Created': {'name1 2v2 2/4', 'name2 2v2 4/4', 'name3 single 2/10'}"
        self.splitted = string.split(" - ")
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
                print(r)
                self.game_list_selected = self.game_mode_list[r]
                print(self.game_list_selected)
                self.game_label.config(text=self.game_list_selected)
                self.game_label.pack()
            except:
                pass
        self.game_list.bind("<<ListboxSelect>>", get_game_mode)

        def get_player(*x):
            try:
                w = self.player_list.curselection()[0]
                print(w)
                self.player_list_selected = self.players_list[w]
                print(self.player_list_selected)
                self.player_label.config(text=self.player_list_selected)
                self.player_label.pack()
            except:
                pass
        self.player_list.bind("<<ListboxSelect>>", get_player)

    def login_command(self):


        print("hi there, everyone!")
        self.player_name = self.user_name.get()
        print(self.host_ip.get(), self.user_name.get())
        if client.login_to_game(self.host_ip.get(), self.user_name.get()):
            self.label_welcome['text'] = "You successfully loged in"
            # self.playing_page()
            self.game_mode()
        else:
            self.label_welcome['text'] = "You failed to login, try again"

        self.game_mode()


if __name__ == '__main__':
    root = tk.Tk()
    app = Application(master=root)
    app.mainloop()
    # start_game()
