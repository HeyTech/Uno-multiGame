import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Arrays;

public class UnoServer {

	private static boolean accepting = true;
	private static Scanner stdIn =  new Scanner(new BufferedInputStream(System.in));
	private static ArrayList<Socket> theConnections = new ArrayList<Socket>();
	private static ArrayList<String> theClientsChoice = new ArrayList<String>();


	private static HashMap<String,Scanner> client_ins = new HashMap<String,Scanner>();
	private static HashMap<String,InputStream> client_inps = new HashMap<String,InputStream>();
	private static HashMap<String,PrintWriter> client_outs = new HashMap<String,PrintWriter>();

	public static void main(String[] args)throws java.io.FileNotFoundException, java.io.IOException  {
		int port = 4444;

		FileWriter writer = new FileWriter("uno.txt", false);
		writer.write("<Uno Multi Game Online Game>\n");
		writer.close();

		buildConnections(port);
	}

private static void buildConnections(int port){
	new Thread(new Runnable(){
		public void run(){
			stdIn.nextLine();
			accepting = false;
		}
	}).start();

	try {
		final ServerSocket serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(1000); // to be able to stop blocking now and then
		System.err.println("Started server on port " + port);
		while (accepting) {
			try{
				Socket clientSocket = serverSocket.accept();
				InputStream is = clientSocket.getInputStream();
				Scanner in = new Scanner(new BufferedInputStream(is));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

				String id_name = in.next();

				client_ins.put(id_name,in);
				client_inps.put(id_name,is);
				client_outs.put(id_name,out);

				System.err.println("Accepted connection from client " + id_name);

				out.println("<Accepted connection from " + id_name +"/>");
				out.flush();
			}catch(SocketTimeoutException e){}

			//Listen to connected clients
			if(client_ins.size()>0)
				ManageGame();
		}
	} catch (IOException ioe) { System.err.println("here" + ioe); }
}

	private static void ManageGame() throws java.io.IOException{


		ArrayList<String> theClients    = new  ArrayList<String>(client_ins.keySet());
		ArrayList<Scanner> ins          = new  ArrayList<Scanner>(client_ins.values());
		ArrayList<InputStream> inps     = new  ArrayList<InputStream>( client_inps.values());
		ArrayList<PrintWriter> outs     = new  ArrayList<PrintWriter>( client_outs.values());

		PrintWriter out;
		InputStream is;
		Scanner in;

		String s = "";
		int i1, i2;

		String clientName = "";

		for(int i = 0; i<ins.size();i++){
			in = ins.get(i);
			is = inps.get(i);
			out = outs.get(i);

			if(is.available()>0){
				s = in.nextLine();
				clientName = theClients.get(i);
				if(s.startsWith("<ReadyToPlay/>")) {
					System.out.println(clientName + " is now online.");
					ReadyToPlay(clientName, out);
					out.flush();
				} else if(s.startsWith("<OnlinePlayers/>")) {
					System.out.println(clientName + " asks for online players.");
					OnlinePlayers("<OnlinePlayer", out);
				} else if(s.startsWith("<Exit/>")) {
					System.out.println(clientName + " left the game.");
					LeaveGame(clientName, s, out);
				}else if(s.startsWith("<CreateRoom")){
					CreateRoom(s, out);
				}
						
				//else if (s.startsWith("<UpdateList/>")){
				//}
			}
		}
	}

private static void CreateRoom(String s, PrintWriter out) {
    try
    {
    	//String xmlString = "<CreateRoom Name='bla' Mode='single' Capacity='1/10' Players='username'/>";
    	String[] xmlarray = s.replace("/>", " ").replace("<CreateRoom ", "").split(" ");
    	Arrays.toString(xmlarray);
    	String room = "<Room " + String.join(" ", xmlarray) + "/>";
    	System.out.println(room);	
    	
    	
        File file = new File("uno.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "";
        Boolean nameNotFound = true;
        
        while((line = reader.readLine()) != null)
        {
        	if(line.startsWith("<Room " + xmlarray[0])){
        		nameNotFound = false;
        		break;
        	}
        }
        reader.close();

        FileWriter writer = new FileWriter("uno.txt",true);

        
        if(nameNotFound){
            writer.write(room+"\n");
            out.println("<GameRoom Created/>");
        }else{
        	out.println("<GameRoom Failed: Name taken/>");
        }
        writer.close();
        out.flush();

    }
    catch (IOException ioe)
    {
        ioe.printStackTrace();
    }
    
    
    

}

// 'Online players':['Mujtaba', 'Nandu', 'Ranju'] - 'Rooms Created': {['name1', '2v2', '2/4'], ['name2', '2v2', '4/4'], ['name3', 'single', '2/10']}
private static void ReadyToPlay(String s, PrintWriter out) throws IOException {
    try
    {
        out.println("<You are now ready to play Uno/>");
        out.flush();

        FileWriter writer = new FileWriter("uno.txt",true);
        writer.write("<OnlinePlayer name=\"" + s + "\" status=\"ready\"/>\n");
        writer.close();
    }
    catch (IOException ioe)
    {
        ioe.printStackTrace();
    }

}

private static  int OnlinePlayers(String s, PrintWriter out)
         {
	     int iSend=0;
         try
             {
             File file = new File("uno.txt");
             BufferedReader reader = new BufferedReader(new FileReader(file));
             String line = "", text = "";
             while((line = reader.readLine()) != null)
                 {

            	 if(line.startsWith(s)){
            		 iSend++;
            		 out.println(line);
                     out.flush();
            	 }
                 text += line + "\n";

             }
             reader.close();


             FileWriter writer = new FileWriter("uno.txt");
             writer.write(text);
             writer.close();
         }
         catch (IOException ioe)
             {
             ioe.printStackTrace();
         }
         return iSend;
     }

private static  void LeaveGame(String clientName, String swrite, PrintWriter out)
{
    try
    {
        File file = new File("uno.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "", text = "";

        while((line = reader.readLine()) != null)
        {

            if (line.startsWith("<OnlinePlayer name=\"" + clientName)){

                line = "";
                out.println("<Left Game/>");
                out.flush();
            } else if (line.startsWith("<GameSession") & line.indexOf(clientName) >= 0){

                line = "";
            } else if (line.startsWith("<PlayRequest") & line.indexOf(clientName) >= 0){

                line = "";
            }

            text += line + "\n";


        }
        reader.close();

        FileWriter writer = new FileWriter("uno.txt");
        writer.write(text);
        writer.close();
    }
    catch (IOException ioe)
    {
        ioe.printStackTrace();
    }

}

}




