import java.net.Socket;
import java.net.Inet4Address;
import java.net.InetAddress;
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
		System.err.println("Started server on port " + port + " Host: "+ Inet4Address.getLocalHost().getHostAddress());

		while (accepting) {
			try{
				Socket clientSocket = serverSocket.accept();
				InputStream is = clientSocket.getInputStream();
				Scanner in = new Scanner(new BufferedInputStream(is));
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				String id_name = in.next();				
				
				File file = new File("uno.txt");
		        BufferedReader reader = new BufferedReader(new FileReader(file));
		        String line = "";
		        Boolean alreadyIdle = false;
		      
		        while((line = reader.readLine()) != null){
		            if(line.startsWith("<OnlinePlayer name='" + id_name + "' status=")){
		            	alreadyIdle = true;
		            	break;
		            }
		        }
		        reader.close();
		        
		        if(alreadyIdle){
		        	System.err.println("<Refused connection: " + id_name +" is taken/>");
					out.println("<Refused connection: " + id_name +" is taken/>");
					out.flush();
					// clientSocket.close();
		        }else{
					System.err.println("Accepted connection from client " + id_name);
					client_ins.put(id_name,in);
					client_inps.put(id_name,is);
					client_outs.put(id_name,out);
					
			        FileWriter writer = new FileWriter("uno.txt",true);
			        writer.write("<OnlinePlayer name='" + id_name + "' status='idle'/>\n");
			        writer.close();
					out.println("<Accepted connection from " + id_name +"/>");
					out.flush();
		        }

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
				s = s.replaceAll(" / HTTP/1.0", "");
				clientName = theClients.get(i);
				if(s.startsWith("<ReadyToPlay/>")) {
					System.out.println(clientName + " is now online.");
					ReadyToPlay(clientName, out);
					out.flush();
				}else if(s.startsWith("<Exit/>")) {
					System.out.println(clientName + " left the game.");
					LeaveGame(clientName, s, out);
				}else if(s.startsWith("<CreateRoom")){
					System.out.println(clientName + " asks to create a new room.");
					CreateRoom(s, out);
				}else if(s.startsWith("<UpdateLists/>")){
					System.out.println(clientName + " asks to update the lists (onlinePlayers and Games)");
					UpdateLists(s, out);
				}else{
					out.flush();
				}
			}
		}
	}

private static void UpdateLists(String s, PrintWriter out) {

	String plyersOnline = OnlinePlayers("<OnlinePlayer");
	System.out.println(plyersOnline);
	
	String roomsCreated = CreatedGames("<Room ");
	System.out.println(plyersOnline);

	String sendOut = plyersOnline.replace("''", "', '") + " - " +roomsCreated.replace("''", "', '");
	out.print(sendOut);
	out.flush();
}


private static  String OnlinePlayers(String onlinePlayers){
	     String plyersOnline = "'Online players':[";
         try
             {
             File file = new File("uno.txt");
             BufferedReader reader = new BufferedReader(new FileReader(file));
             String line = "";
             while((line = reader.readLine()) != null){
            	 if(line.startsWith(onlinePlayers)){
            		 // String onlinePlayers= "<OnlinePlayer name='mujtaba' status='idle'/>";
            		 String[] temp = line.split("'");
            		 String player = String.join(" ", temp[1], temp[3]);
            		 plyersOnline += "'"+player+"'";
            		 //System.out.println(player);            	 
            	 }

             }
             reader.close();
             plyersOnline += "]";
         }
         catch (IOException ioe)
             {
             ioe.printStackTrace();
         }
         return plyersOnline;
     }

private static  String CreatedGames(String createdGames){
    String roomCreated = "'Rooms Created':[";
    try
        {
        File file = new File("uno.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "";
        while((line = reader.readLine()) != null){
       	 if(line.startsWith(createdGames)){
         	//String roomsCreated= "<Room Name='bla' Mode='single' Capacity='1/10' Players='username'/>";
        	String[] temp = line.split("'");
        	String room = String.join(" ", temp[1], temp[3], temp[5]);
        	roomCreated += "'" + room + "'";
        	//System.out.println(room); 
       	 }

        }
        reader.close();
        roomCreated += "]";
    }
    catch (IOException ioe)
        {
        ioe.printStackTrace();
    }
    return roomCreated;
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

private static void ReadyToPlay(String clientName, PrintWriter out) throws IOException {
    try
    {


        File file = new File("uno.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "", text = "";
      
        while((line = reader.readLine()) != null){
        	if(line.startsWith("<OnlinePlayer name='" + clientName)){
        		line = "<OnlinePlayer name='" + clientName + "' status='ready'/>";
        	}
        	text += line  + "\n";
        }
        reader.close();
        
        
        FileWriter writer = new FileWriter("uno.txt");
        writer.write(text);
        writer.close();
        
        out.println("<You are now ready to play Uno/>");
        out.flush();
    }
    catch (IOException ioe)
    {
        ioe.printStackTrace();
    }

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




