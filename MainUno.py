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

        self.image = Image.open("download.png")
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
        self.login_btn["command"] = self.Login_command
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

<<<<<<< HEAD
=======
    def game_mode(self):
        self.quick_mode_btn = tk.Button(self, text="QUICK PLAY")
        self.quick_mode_btn.config(bg='yellow')
        self.quick_mode_btn.pack(side="left")
        self.two_player_btn = tk.Button(self, text="2V2")
        self.two_player_btn.config(bg='yellow')
        self.two_player_btn.pack(side="left")
        self.player_list = tk.Listbox(self)
        self.player_list.pack()
        self.update_btn = tk.Button(self, text="UPDATE")
        self.update_btn.config(bg="green")
        self.update_btn.pack()

>>>>>>> 0be76c9b65034fce20e465845999634ae760fa30
    def Login_command(self):
        print("hi there, everyone!")
        print(self.host_ip.get(), self.user_name.get())
        if client.login_to_game(self.host_ip.get(), self.user_name.get()):
            self.label_welcome['text'] = "You successfully loged in"
            self.playing_page()
            self.game_mode()
        else:
            self.label_welcome['text'] = "You failed to login, try again"


if __name__ == '__main__':
    root = tk.Tk()
    app = Application(master=root)
    app.mainloop()
    # start_game()
