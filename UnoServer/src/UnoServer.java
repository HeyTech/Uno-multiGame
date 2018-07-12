import java.net.Socket;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

	public static void main(String[] args)throws java.io.FileNotFoundException, java.io.IOException, JSONException  {
		int port = 4444;

		FileWriter writer = new FileWriter("uno.txt", false);
		writer.write("<Uno Multi Game Online Game>\n");
		writer.close();

		buildConnections(port);
	}

private static void buildConnections(int port) throws JSONException{
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

	private static void ManageGame() throws java.io.IOException, JSONException{


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
				}else if(s.startsWith("<Exit/>")) {
					System.out.println(clientName + " left the game.");
					LeaveGame(clientName, s, out);
				}else if(s.startsWith("<CreateRoom")){
					System.out.println(clientName + " asks to create a new room.");
					CreateRoom(s, out);
				}else if(s.startsWith("<UpdateLists/>")){
					System.out.println(clientName + " asks to update the lists (onlinePlayers and Games)");
					UpdateLists(s, out);
				}else if(s.startsWith("<JoinRoom ")){
					String[] a = s.replace("' '", "'").split("'");
					System.out.println(clientName + " asks to join room: " + a[1]);
					String roomName = a[1];
					String playerName = a[2];
					JoinRoom(playerName, roomName, out);
				}else if(s.startsWith("<ChooseTeam ")){
					ChooseTeam(s, out);
					//TODO: InformPlayers()     //*******************************************************
					
				}else if(s.startsWith("<GettingReady ")){
					GettingReady(s, out);
										// InformPlayers()
				}
				
				
				/*
				try{
					for(int k = 0; i < outs.size(); k++){
						PrintWriter send = outs.get(k);
						String msg = clientName + " did some action";
						send.print(msg);
						send.flush();
					}	
				}catch(Exception ioe){continue;}
				*/
			}
		}
	}

private static void GettingReady(String s, PrintWriter out) {
	//String s = "<ChooseTeam 'RoomName' 'UserName' 'teamA'/>";
	String[] a = s.replace("' '", "'").split("'");
	String roomFile = a[1]+".txt";
	String playerName = a[2];
	
	JsonFormater cls = new JsonFormater();					
	JSONObject obj = cls.PlayerGettingReady(roomFile, playerName);

	JSONObject tempJson = new JSONObject();
	tempJson.put("RoomInfo", obj.get("RoomInfo"));
	out.print(tempJson);
    out.flush();
		
	}

private static void ChooseTeam(String s, PrintWriter out) {
	//String s = "<ChooseTeam 'RoomName' 'UserName' 'teamA'/>";
	String[] a = s.replace("' '", "'").split("'");
	String roomFile = a[1]+".txt";
	String playerName = a[2];
	String teamName = a[3];
	
	JsonFormater cls = new JsonFormater();
	JSONObject obj = cls.ChangePlayerTeam(roomFile, playerName, teamName);

	JSONObject tempJson = new JSONObject();
	tempJson.put("RoomInfo", obj.get("RoomInfo"));
	out.print(tempJson);
    out.flush();
		
	}

private static void JoinRoom(String playerName, String roomName, PrintWriter out) {
	try
    {
		JsonFormater cls = new JsonFormater();

        File file = new File("uno.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "", text = "";
        int rdy, cap = 0;
        boolean roomNotFound = true;
      
        while((line = reader.readLine()) != null){
        	if(line.startsWith("<Room Name='" +roomName +"'")){
        		roomNotFound = false;
        		String[] b = line.split("'");
        		rdy = Integer.parseInt(b[5].split("/")[0]);
        		cap = Integer.parseInt(b[5].split("/")[1]);
        		
        		if(rdy < cap){
        			line = line.replace((rdy +"/"+cap).toString(), ((rdy+1) +"/"+cap).toString());
        			line = line.replaceAll("'/>", ", " + playerName + "'/>");
        	        
        			// Save to file ****************************************
        			String roomFile = roomName +".txt";
        			
        			JSONObject newObj = cls.AddNewPlayerToRoom(roomFile, playerName, ((rdy+1) +"/"+cap).toString());
        			System.out.println(newObj);
        			
        			try (FileWriter gameFile = new FileWriter(roomName+".txt")) {
        				gameFile.write(newObj.toString());
        				System.out.println("Successfully join room: "+ newObj);
        				//System.out.println("JSON Object: " + newObj);
        			}
        			
        			//out.println("<Join Room Successfully/>");
        			JSONObject tempJson = new JSONObject();
        			tempJson.put("RoomInfo", newObj.get("RoomInfo"));
        			out.print(tempJson);
        	        out.flush();
        	        
        		}else{
        	        out.print("<Join Room Failed: Room "+ roomName +" is already full />");
        	        out.flush();
        		}
    			text += line  + "\n";
        	}
        	else{
        		text += line  + "\n";
        	}
        	
        }
        
        if(roomNotFound){
        	out.print("<Join Room Failed: Room "+ roomName +" dose not exist />");
	        out.flush();
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

private static void UpdateLists(String s, PrintWriter out) throws JSONException {

    JSONObject obj = new JSONObject();

	
	JSONArray plyersOnline = OnlinePlayers("<OnlinePlayer");
	//System.out.println(plyersOnline);
	obj.put("Online players", plyersOnline);
	
	
	JSONArray roomsCreated = CreatedGames("<Room ");
	//System.out.println(plyersOnline);
	obj.put("Rooms Created", roomsCreated);
	
	out.print(obj);
	out.flush();
}


private static JSONArray OnlinePlayers(String onlinePlayers){
		JSONArray playersOnline = new JSONArray();
         try
             {
             File file = new File("uno.txt");
             BufferedReader reader = new BufferedReader(new FileReader(file));
             String line = "";
             while((line = reader.readLine()) != null){
            	 if(line.startsWith(onlinePlayers)){
            		 // String onlinePlayers= "<OnlinePlayer name='mujtaba' status='idle'/>";
            		 String[] temp = line.split("'");
            		 //System.out.println(" ", temp[1], temp[3]));            	 
            		 playersOnline.add(String.join(" ", temp[1], temp[3]));
            	 }

             }
             reader.close();
         }
         catch (IOException ioe)
             {
             ioe.printStackTrace();
         }
         return playersOnline;
     }

// returns the games that are created to <UpdateLists>
private static  JSONArray CreatedGames(String createdGames){
	JSONArray roomCreated = new JSONArray();

    try
        {
        File file = new File("uno.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "";
        while((line = reader.readLine()) != null){
       	 if(line.startsWith(createdGames)){
         	//String roomsCreated= "<RoomName='bla' Mode='single' Capacity='1/10' Players='username'/>";
        	String[] temp = line.split("'");
        	String room = String.join(" ", temp[1], temp[3], temp[5]);
        	roomCreated.add(room);
        	//System.out.println(room); 
       	 }

        }
        reader.close();
    }
    catch (IOException ioe)
        {
        ioe.printStackTrace();
    }
    return roomCreated;
}


private static void CreateRoom(String s, PrintWriter out) throws JSONException {
    try{

		//String s = "<CreateRoom Name='Naai 1123' Mode='2v2' Capacity='1/4' Players='Mona'/>";
    	String[] sArr = s.replace("/>", "").replace("<CreateRoom ", "").split("'");

		String roomName = sArr[1];
		String mode = sArr[3];
		String online = sArr[5];
		String admin = sArr[7];
		

		
        File file = new File("uno.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = "";
        Boolean nameNotFound = true;
        // check if the room name already exist
        while((line = reader.readLine()) != null)
        {
        	if(line.startsWith("<Room Name='" + roomName +"'")){
        		nameNotFound = false;
        		break;
        	}
        }
        reader.close();
        
        if(nameNotFound){
            FileWriter writer = new FileWriter("uno.txt",true);
        	String temp = s.replace("/>", "").replace("<CreateRoom ", "");
        	String tempRoom = "<Room " +temp  + "/>";
            writer.write(tempRoom+"\n");
            writer.close();

            
        	//To generate JSON String
        	JsonFormater cls = new JsonFormater();
    		JSONObject room = cls.generateRoomJson(roomName, mode, online, admin);
    		try (FileWriter gameFile = new FileWriter(roomName+".txt")) {
    			gameFile.write(room.toString());
    			System.out.println("Successfully Created Room: " + room);
    			gameFile.close();
    			
    			JSONObject tempJson = new JSONObject();
    			tempJson.put("RoomInfo", room.get("RoomInfo"));
    			out.print(tempJson);
    		}
        }else{
        	out.println("<GameRoom Failed: Name taken/>");
        }
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



