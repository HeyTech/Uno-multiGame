import java.net.Socket;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class UnoServer {

	private static boolean accepting = true;
	private static Scanner stdIn =  new Scanner(new BufferedInputStream(System.in));
	private static ArrayList<Socket> theConnections = new ArrayList<Socket>();
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
						theConnections.add(clientSocket);

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
				if(s.startsWith("<CreateRoom")){
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
					//TODO: InformPlayers()     //*******************************************************
				}else if(s.startsWith("<ChooseTeam ")){
					ChooseTeam(s, out);
					//TODO: InformPlayers()     //*******************************************************
				}else if(s.startsWith("<GettingReady ")){
					GettingReady(s, out);
					// InformPlayers()         //*******************************************************
				}else if(s.startsWith("<StartGame")) {
					// <StartGame 'RoomName'/>
					StartGame(s, out);
				}else if(s.startsWith("<Uno ")) {
					// <Uno 'RoomName' 'PlayerName'/>
					String playerName = s.split("'")[3];
					System.out.println("'"+clientName + "' pressed on uno button on " + playerName);
					int giveCards = 2;
					UnoPlay(s, clientName, giveCards, out);
					// InformPlayers()         //*******************************************************
				}else if(s.startsWith("<NewCard ")) {
					// <NewCard 'RoomName'/>
					System.out.println("'"+clientName + "' draw a new card.");
					NewCard(s, clientName, out);
					// InformPlayers()         //*******************************************************
				}else if(s.startsWith("<LeaveRoom ")) {
					// "<LeaveRoom 'roomName'/>"
					String roomName = s.split("'")[1];
					System.out.println(clientName + " wants to leave room: '" + roomName + "'");
					LeaveRoom(clientName, s, out);	
					
				}else if(s.startsWith("<Exit/>")) {
					System.out.println(clientName + " wants to Exit the game.");
					ExitGame(clientName, s, out, client_ins, client_inps, client_outs, theConnections, i);
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

	private static void StartGame(String s, PrintWriter out) throws IOException {
		String roomName = s.split("'")[1];
		boolean ableToStartGame = false;
		JsonFormater cls = new JsonFormater();
		JSONObject obj =  cls.FetchGameInfo(roomName + ".txt");
		JSONObject roomInfo = (JSONObject) obj.get("RoomInfo");
		JSONArray readyPlayers = (JSONArray) roomInfo.get("ReadyPlayers");
		JSONArray players = (JSONArray) roomInfo.get("Players");
		if(players.size() == readyPlayers.size()){
			String mode = (String) roomInfo.get("Mode");
			if(mode.toLowerCase().contains("v") & mode.length() == 3){ // to make teamGame dynamic (xVx)
				JSONObject teams = (JSONObject) roomInfo.get("Teams");
				JSONArray teamA = (JSONArray) teams.get("TeamA");
				JSONArray teamB = (JSONArray) teams.get("TeamB");
				if(teamA.size() == teamB.size() & players.size() >= 4){ // Teams must have same amount of players and at-least 4 players
					ableToStartGame = true;
				}else{
					System.out.println("<StartGame Failed: Teams must have same amont and at-least 4 players/>");
					out.print("<StartGame Failed: Teams must have same amont and at-least 4 players/>");
					out.flush();
				}
			}else if(mode.toLowerCase().equals("single")){ // if sinle game
				if(players.size() >= 2){ // Teams must have same amount of players and at-least 4 players
					ableToStartGame = true;
				}else{
					System.out.println("<StartGame Failed: Atleast 2 players needed to start Single a Game/>");
					out.print("<StartGame Failed: Atleast 2 players needed to start Single a Game/>");
					out.flush();
				}
			}
			
		}else{
			System.out.println("<StartGame Failed: All player must be ready/>");
			out.print("<StartGame Failed: All player must be ready/>");
			out.flush();
		}

		if(ableToStartGame){
			String roomFile = roomName + ".txt";
			JSONObject Newobj = cls.NewGameDealCards(roomFile);
			cls.updateGameFile(Newobj, roomFile);

			//change titles for all players
			for(int p = 0;  p < players.size(); p++){
				ChangeTitle(players.get(p).toString(), "idle");
			}
			
			
			
			JSONObject tempJson = new JSONObject();
			tempJson.put("RoomInfo", obj.get("RoomInfo"));
			tempJson.put("BoardInfo", obj.get("BoardInfo"));
			out.print(tempJson);
			out.flush();
		}
		
		
		
	}

	private static void NewCard(String s, String playerName, PrintWriter out) throws IOException {
		int giveCards = 1;
		
		String roomName = s.split("'")[1];
		String roomFile = roomName + ".txt";
		
		JsonFormater cls = new JsonFormater();
		JSONObject obj =  cls.FetchGameInfo(roomFile);
		JSONObject BoardInfo = (JSONObject) obj.get("BoardInfo");
		JSONObject PlayersInfo = (JSONObject) BoardInfo.get("PlayersInfo");
		JSONObject player = (JSONObject) PlayersInfo.get(playerName);

		List<String> cards = (List<String>) player.get("Cards");

		JSONObject CardsInfo = (JSONObject) obj.get("CardsInfo");
		JSONObject tempCardsInfo = cls.UpdateCardsInfo(CardsInfo, giveCards); // update the list if avilableCards is less then giveCards

		List<String> AvailableCards = (List<String>) tempCardsInfo.get("AvailableCards");
		List<String> DiscardedCards = (List<String>) tempCardsInfo.get("DiscardedCards");

		CardsInfo.put("AvailableCards", AvailableCards);
		CardsInfo.put("DiscardedCards", DiscardedCards);	

		List<String> tempNewCards = (List<String>) tempCardsInfo.get("giveCards");
		cards.addAll(tempNewCards);
		player.put("Cards", cards);
		player.put("NumberOfCards", cards.size());
		player.put("Uno", false);

		cls.updateGameFile(obj, roomName+".txt");
		
		JSONObject tempJson = new JSONObject();
		tempJson.put("RoomInfo", obj.get("RoomInfo"));
		tempJson.put("RoomInfo", obj.get("BoardInfo"));
		out.print(tempJson);
		out.flush();
		
	}

	private static void UnoPlay(String s, String clientName, int giveCards, PrintWriter out) throws IOException {

		String roomName = s.split("'")[1];
		String playerName = s.split("'")[3];
		String roomFile = roomName + ".txt";
		
		JsonFormater cls = new JsonFormater();
		JSONObject obj =  cls.FetchGameInfo(roomFile);
		JSONObject BoardInfo = (JSONObject) obj.get("BoardInfo");
		JSONObject PlayersInfo = (JSONObject) BoardInfo.get("PlayersInfo");
		JSONObject player = (JSONObject) PlayersInfo.get(playerName);

		List<String> cards = (List<String>) player.get("Cards");
		if(cards.size() == 1){ // if the Player has only one card in hand
			boolean unoStatus = (boolean) player.get("Uno");
			if(!unoStatus){ // if the player has not pressed uno yet
				if(clientName.equals(playerName)){ // if the player himself pressed uno
					player.put("Uno", true);
				}else{ // give player 2 cards
					JSONObject CardsInfo = (JSONObject) obj.get("CardsInfo");
					JSONObject tempCardsInfo = cls.UpdateCardsInfo(CardsInfo, giveCards); // update the list if avilableCards is less then (i)

					List<String> AvailableCards = (List<String>) tempCardsInfo.get("AvailableCards");
					List<String> DiscardedCards = (List<String>) tempCardsInfo.get("DiscardedCards");

					CardsInfo.put("AvailableCards", AvailableCards);
					CardsInfo.put("DiscardedCards", DiscardedCards);	

					List<String> tempNewCards = (List<String>) tempCardsInfo.get("giveCards");
					cards.addAll(tempNewCards);
					player.put("Cards", cards);
					player.put("NumberOfCards", cards.size());
				}
			}
		}
		cls.updateGameFile(obj, roomName+".txt");
		
		JSONObject tempJson = new JSONObject();
		tempJson.put("RoomInfo", obj.get("RoomInfo"));
		tempJson.put("RoomInfo", obj.get("BoardInfo"));
		out.print(tempJson);
		out.flush();
		
	}

	// s = "<LeaveRoom 'roomName'/>"  -->  "<Left Room Successfully />"
	private static void LeaveRoom(String clientName, String s, PrintWriter out) throws IOException {

		String roomName = s.split("'")[1];
		System.out.println(clientName + "  " +roomName);
		JsonFormater cls = new JsonFormater();			
		cls.RemovePlayerFromRoom(clientName, roomName);


		File file = new File("uno.txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = "", text = "";

		while((line = reader.readLine()) != null){
			if(line.startsWith("<Room Name='" +roomName +"'")){
				String onCap = line.substring(line.indexOf("Capacity='") + 10, line.indexOf("' Player="));
				int on = Integer.parseInt(onCap.split("/")[0]);
				int cap = Integer.parseInt(onCap.split("/")[1]);

				if((on-1) != 0){
					line = line.replace((on +"/"+cap).toString(), ((on-1) +"/"+cap).toString());
					line.replace(clientName +", ", "").replace(", " + clientName, "");
				}else{
					line = "";
				}
			}
			text += line  + "\n";
		}
		reader.close();

		FileWriter writer = new FileWriter("uno.txt");
		writer.write(text);
		writer.close();

		ChangeTitle(clientName, "idle");
		out.print("<Left Room Successfully />");
		out.flush();
	}

	private static void GettingReady(String s, PrintWriter out) throws IOException {
		//String s = "<ChooseTeam 'RoomName' 'UserName' 'teamA'/>";
		String[] a = s.replace("' '", "'").split("'");
		String roomFile = a[1]+".txt";
		String playerName = a[2];

		JsonFormater cls = new JsonFormater();					
		JSONObject obj = cls.PlayerGettingReady(roomFile, playerName); // updates the roomFile too
		
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
			boolean changePlayerTitle = false;

			while((line = reader.readLine()) != null){
				if(line.startsWith("<Room Name='" +roomName +"'")){
					roomNotFound = false;
					String[] b = line.split("'");
					rdy = Integer.parseInt(b[5].split("/")[0]);
					cap = Integer.parseInt(b[5].split("/")[1]);

					if(line.contains(playerName)){ // if player is already in a game (rejoins)
						changePlayerTitle = true;
						JSONObject newObj = cls.FetchGameInfo(roomName + ".txt");						
						
						JSONObject tempJson = new JSONObject();
						tempJson.put("RoomInfo", newObj.get("RoomInfo"));
						out.print(tempJson);
						out.flush();
						System.out.println("Successfully join room: "+ newObj);
					}
					else if(rdy < cap){ // if player want to join a new Game Room
						changePlayerTitle = true;
						line = line.replace((rdy +"/"+cap).toString(), ((rdy+1) +"/"+cap).toString());
						line = line.replaceAll("'/>", ", " + playerName + "'/>");

						String roomFile = roomName +".txt";

						JSONObject newObj = cls.AddNewPlayerToRoom(roomFile, playerName, ((rdy+1) +"/"+cap).toString());
						try (FileWriter gameFile = new FileWriter(roomName+".txt")) {
							gameFile.write(newObj.toString());
							System.out.println("Successfully join room: "+ newObj);
						}

						//out.println("<Join Room Successfully/>");
						JSONObject tempJson = new JSONObject();
						tempJson.put("RoomInfo", newObj.get("RoomInfo"));
						out.print(tempJson);
						out.flush();

					}else{
						System.out.println("'" + playerName +"' Failed to Join room: Room "+ roomName +" is already full");
						out.print("<Join Room Failed: Room "+ roomName +" is already full />");
						out.flush();
					}
					text += line  + "\n";
				}
				else{
					text += line  + "\n";
				}

			}



			reader.close();

			FileWriter writer = new FileWriter("uno.txt");
			writer.write(text);
			writer.close();

			if(roomNotFound){
				out.print("<Join Room Failed: Room "+ roomName +" dose not exist />");
				out.flush();
			}
			if(changePlayerTitle){
				ChangeTitle(playerName, "Waiting");
			}
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
				ChangeTitle(admin, "Created Room");

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


	private static void ChangeTitle(String playerName, String status) {
		try{
			File file = new File("uno.txt");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "", text = "";

			while((line = reader.readLine()) != null){
				if(line.startsWith("<OnlinePlayer name='" + playerName)){
					line = "<OnlinePlayer name='" + playerName + "' status='" + status + "'/>";
				}
				text += line  + "\n";
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

	private static  void ExitGame(String clientName, String swrite, PrintWriter out, HashMap<String, Scanner> client_ins, HashMap<String, InputStream> client_inps, HashMap<String, PrintWriter> client_outs, ArrayList<Socket> theConnections, int i)
	{
		try
		{
			File file = new File("uno.txt");
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = "", text = "";

			while((line = reader.readLine()) != null)
			{

				if (line.startsWith("<OnlinePlayer name='" + clientName + "'")){
					line = "";
					System.out.println("Player: " + clientName + " left the game");
					out.println("<LeftGame Successfully/>");
					out.flush();
				} 

				text += line + "\n";


			}
			reader.close();

			FileWriter writer = new FileWriter("uno.txt");
			writer.write(text);
			writer.close();

			client_ins.remove(clientName);
			client_inps.remove(clientName);
			client_outs.remove(clientName);
			theConnections.get(i).close();
			theConnections.remove(i);

		}
		catch (IOException ioe){
			ioe.printStackTrace();
		}
	}
}