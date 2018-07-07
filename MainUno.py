import client
import tkinter as tk

# MUJTABA NAAI
class Application(tk.Frame):
    def __init__(self, master=None):
        super().__init__(master)
        root.geometry('{}x{}'.format(700, 700))
        root.resizable(width=False, height=False)
        root.title("Uno Multi game")
        self.pack()
        self.Login_page()

    def Login_page(self):
        self.label_host_ip = tk.Label(self, text='Host ip: ')
        self.label_host_ip.pack(side="left")

        self.host_ip = tk.Entry(self)
        # self.user_name.insert(0, "a default value")
        self.host_ip.pack(side="left")


        self.label_user_name = tk.Label(self, text='Username')
        self.label_user_name.pack(side="left")

        self.user_name = tk.Entry(self)
        # self.user_name.insert(0, "a default value")
        self.user_name.pack(side="left")



        self.login_btn = tk.Button(self)
        self.login_btn["text"] = "Login"
        self.login_btn["command"] = self.Login_command
        self.login_btn.pack(side="left")


        self.quit = tk.Button(self, text="QUIT", fg="red", command=root.destroy)
        self.quit.pack(side="left")

        self.label_welcome = tk.Label(text= "login please")
        self.label_welcome.pack(side="bottom")


    def playing_page(self):
        self.clean_frame()


    def clean_frame(self):
        for widget in self.winfo_children():
            widget.destroy()


    def Login_command(self):
        print("hi there, everyone!")
        print(self.host_ip.get(), self.user_name.get())
        if client.login_to_game(self.host_ip.get(), self.user_name.get()):
            self.label_welcome['text'] = "You successfully loged in"
            self.playing_page()
        else:
            self.label_welcome['text'] = "You failed to login, try again"




if __name__ == '__main__':
    root = tk.Tk()
    app = Application(master=root)
    app.mainloop()
    # start_game()
