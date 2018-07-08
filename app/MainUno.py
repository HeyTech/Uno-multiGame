from PIL import ImageTk, Image
import client
import tkinter as tk


class Application(tk.Frame):
    def __init__(self, master=None):
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

    def game_mode(self):
        self.clean_frame()
        self.game_mode_list = ['Single Mode', '2V2']
        self.players_list = ['Monisha', 'Mujtaba', 'Nandhini', 'Ranjani']
        self.game_list = tk.Listbox(self)
        for mode in self.game_mode_list:
            self.game_list.insert(tk.END, mode)
        self.game_list.pack()
        self.player_list = tk.Listbox(self)
        for players in self.players_list:
            self.player_list.insert(tk.END, players)
        self.player_list.pack()
        self.update_btn = tk.Button(self, text="Update")
        self.update_btn.pack()
        self.start_btn = tk.Button(self, text="Start")
        self.start_btn.pack()
        self.game_label = tk.Label(self, text='Select Mode')
        self.player_label = tk.Label(self, text='Select Player')


        self.game_label.pack()
        self.player_label.pack()

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
                self.player_label.config(text=self.player_list_selected)
                self.player_label.pack()
            except:
                pass
        self.player_list.bind("<<ListboxSelect>>", get_player)

    def login_command(self):
        self.game_mode()
        """
        print("hi there, everyone!")
        print(self.host_ip.get(), self.user_name.get())
        if client.login_to_game(self.host_ip.get(), self.user_name.get()):
            self.label_welcome['text'] = "You successfully loged in"
            # self.playing_page()
            self.game_mode()
        else:
            self.label_welcome['text'] = "You failed to login, try again"
        """

if __name__ == '__main__':
    root = tk.Tk()
    app = Application(master=root)
    app.mainloop()
    # start_game()
