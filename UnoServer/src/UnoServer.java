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
import java.util.Iterator;

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
						if(line.startsWith("<OnlinePlayer name='" + id_name + "'")){
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
				System.out.println(s);
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
					//TODO: InformPlayers()     //*******************************************************
				}else if(s.startsWith("<Uno ")) {
					// <Uno 'RoomName' 'PlayerName'/>
					String playerName = s.split("'")[3];
					int giveCards = 2;
					UnoPlay(s, clientName, giveCards, out);
					// InformPlayers()         //*******************************************************
				}else if(s.startsWith("<NewCard ")) {
					// <NewCard 'RoomName'/>
					NewCard(s, clientName, out);
					// InformPlayers()         //*******************************************************
				}else if(s.startsWith("<PlayingCard ")) {
					// <PlayingCard 'roomName' 'cardName'/>
					PlayingCard(s, clientName, out);

					// InformPlayers()         //*******************************************************
				}else if(s.startsWith("<LeaveRoom ")) {
					// "<LeaveRoom 'roomName'/>"
					String roomName = s.split("'")[1];
					System.out.println(clientName + " wants to leave room: '" + roomName + "'");
					LeaveRoom(clientName, s, out);	

				}else if(s.startsWith("<Exit/>")) {
					System.out.println(clientName + " wants to Exit the game.");
					ExitGame(clientName, s, out, client_ins, client_inps, client_outs, theConnections, i);
				}else if(s.startsWith("<FetchGameRoom ")) {
					// "<FetchGameRoom 'roomName'/>"
					FetchGameRoom(s, clientName, out);
				}
				/*
				try{
					for(int k = 0; i < outs.size(); k++){
						PrintWriter send = outs.get(k);v 
						String msg = clientName + " did some action";
						send.print(msg);
						send.flush();
					}	
				}catch(Exception ioe){continue;}
				 */
			}

		}
	}

	private static void FetchGameRoom(String s, String clientName, PrintWriter out) {

		String roomFile =s.split("'")[1] + ".txt";
		JsonFormater cls = new JsonFormater();
		JSONObject obj =  cls.FetchGameInfo(roomFile);
		JSONObject roomInfo = (JSONObject) obj.get("RoomInfo");
		JSONObject boardInfo = (JSONObject) obj.get("BoardInfo");
		JSONObject playersInfo = (JSONObject) boardInfo.get("PlayersInfo");

		if(roomInfo.get("Mode").equals("Single")){ // if the mode is single then just send for the individual player (ClientName)
			for(Iterator it = playersInfo.keySet().iterator(); it.hasNext();){ 
				String pName = (String) it.next();
				if(!pName.equals(clientName)){
					JSONObject player = (JSONObject) playersInfo.get(pName);
					player.put("Cards", "");
				}				
			}

		}else{ // if game mode is xVx, send only cards for people in the same team
			JSONObject teams = (JSONObject) roomInfo.get("Teams");
			ArrayList<String> hideNames = new ArrayList<String>();

			for(Iterator it = teams.keySet().iterator(); it.hasNext();){
				String key = (String) it.next();
				JSONArray team = (JSONArray) teams.get(key.toString());
				if(!team.contains(clientName)){
					hideNames.addAll(team);
				}
			}

			System.out.println(hideNames.toString());
			for(int i = 0 ; i < hideNames.size(); i++){
				String pName = (String) hideNames.remove(0);
				JSONObject player = (JSONObject) playersInfo.get(pName.toString());
				player.put("Cards", "");
			}
		}
		
		System.out.println(obj);
		JSONObject tempJson = new JSONObject();
		tempJson.put("RoomInfo", roomInfo);
		tempJson.put("BoardInfo", boardInfo);
		out.print(tempJson);
		out.flush();

	}

	private static void PlayingCard(String s, String clientName, PrintWriter out) throws IOException {
		String roomFile = s.split("'")[1] + ".txt";
		String playCard = s.split("'")[3];
		
		// fetch game informatin from the roomFile
		JsonFormater cls = new JsonFormater();
		JSONObject obj =  cls.FetchGameInfo(roomFile);
		JSONObject boardInfo = (JSONObject) obj.get("BoardInfo");
		String currentTurn = (String) boardInfo.get("CurrentTurn");

		JSONObject playerInfo = (JSONObject) boardInfo.get("PlayersInfo");
		JSONObject player = (JSONObject) playerInfo.get(clientName);
		JSONArray pCards = (JSONArray) player.get("Cards");
		
		// Check if it's the players current turn and if the player owns the playing card
		if(currentTurn.equals(clientName) & pCards.contains(playCard)){
			List<String> tempCardsToBlockedPlayer = null;
			boolean pass = false;
			boolean playable = false;
			boolean blockable = false;
			boolean reversible = false;
			int giveCards = 0;


			JSONObject roomInfo = (JSONObject) obj.get("RoomInfo");
			JSONArray readyPlayers = (JSONArray) roomInfo.get("ReadyPlayers");			

			JSONObject cardsInfo =(JSONObject) obj.get("CardsInfo");
			String openCard = (String) boardInfo.get("OpenCard");

			if(playCard.equals("pass")){
				pass = true;
				playable = true;
				
			}else if(playCard.contains("wc")){ // wild cards 4+
				playable = blockable = true; giveCards = 4;
				System.out.println("Card: '"+playCard +"': give 4+ and block next");

			}else if(playCard.contains("cc")){ // wild card Change color
				playable = true;	// Changed name to deleted it the same name as provided in the lists
				System.out.println("Card: '"+playCard +"': change color");

			//}else if(openCard.indexOf(playCard.charAt(0)) > -1){ // if open card and new playCard are the same color
			}if(openCard.matches(".*[" + playCard+ "].*")){
				playable = true;
				if("prs".indexOf(playCard.charAt(1)) == 0){ // same color 2+
					System.out.println("Card: '"+playCard +"': Same color 2+");
					blockable = true;
					giveCards = 2;
					
				}else if("prs".indexOf(playCard.charAt(1)) == 1){ // same color reverse
					System.out.print("Card: '"+playCard +"': Same color reverse");

					reversible = true;
					if(readyPlayers.size() <= 2){ // if there are 2 only 2 players, than reverse block the player too
						System.out.print(" and block (1v1)");
						blockable = true;
					}
					System.out.println("");

				}else if("prs".indexOf(playCard.charAt(1)) == 2){ // same color stop(block)
					System.out.println("Card: '"+playCard +"': Same color block");
					blockable = true;	
				}else{
					System.out.println("Card: '"+playCard +"': Same number");
				}

			}
			/*
			 else if(openCard.indexOf(playCard.charAt(1)) == 1){ // Same number card
				System.out.println("Card: '"+playCard +"': Same number card");
				playable = true;
			}
			*/

			if(playable){
				int nextIndex = 0;
				int giveCardsToPlayerIndex = 0;
				
				int blocked = 0; // will change to 1 if a card is blockable
				long reverseLong = 0; // will change to -1 if card is reversible
				int reverse = 0; 
				
				long NumberOfCardsLong = 0; // number of cards to be handed to next player (+2/4)
				int NumberOfCards = 0;
				
				long scoreLong = 0; // How many game have the current player won (+1 when game is over)
				int score = 0;
				
				if(reversible){ // just reverse play
					reverseLong = (long) cardsInfo.get("Reverse");
					reverse = ((Long) reverseLong).intValue();
					cardsInfo.put("Reverse", reverse*(-1));

				}else if(blockable){
					blocked = 1;
					if(giveCards > 0){
						JSONObject tempCardsInfo = cls.UpdateCardsInfo(cardsInfo, giveCards); // update the list if avilableCards is less then giveCards

						List<String> AvailableCards = (List<String>) tempCardsInfo.get("AvailableCards");
						List<String> DiscardedCards = (List<String>) tempCardsInfo.get("DiscardedCards");

						cardsInfo.put("AvailableCards", AvailableCards);
						cardsInfo.put("DiscardedCards", DiscardedCards);	

						tempCardsToBlockedPlayer = (List<String>) tempCardsInfo.get("giveCards"); // a list of cards that will be given to the next player
					}
				}

				/* To assign next player to play */
				int cIndex = readyPlayers.indexOf(currentTurn);
				
				try{
					reverseLong = (long) cardsInfo.get("Reverse");
					reverse = ((Long) reverseLong).intValue();
				}catch(Exception e){
					reverse = (int) cardsInfo.get("Reverse");
				}
				
				if(readyPlayers.size() <= 2){ // if there are only 2 players, (1v1)
					if(reversible | blockable){
						nextIndex = cIndex;
					}else{
						nextIndex = (int)(cIndex + 1)%readyPlayers.size();
					}
					giveCardsToPlayerIndex = (int)(cIndex + 1)%readyPlayers.size();
					
				}else{
					if(reverse == 1){
						nextIndex = (int)(cIndex + reverse + blocked)%readyPlayers.size();
						giveCardsToPlayerIndex = (int)(cIndex + reverse)%readyPlayers.size();
	
					}else{
						nextIndex = (readyPlayers.size() + (cIndex + reverse - blocked))%readyPlayers.size();
						giveCardsToPlayerIndex = (int)(readyPlayers.size() + (cIndex + reverse))%readyPlayers.size();
	
					}
				}
				boardInfo.put("CurrentTurn", readyPlayers.get(nextIndex)); // new CUrrentTurn (for next round)

				if(!pass){ // If player didn't pass (empty card) 
					if(giveCards > 0){
						String playerName = (String) readyPlayers.get(giveCardsToPlayerIndex);
						JSONObject playerGiveCards = (JSONObject) playerInfo.get(playerName); // to get playername of
						List<String> cards = (List<String>) playerGiveCards.get("Cards");
						cards.addAll(tempCardsToBlockedPlayer);
						playerGiveCards.put("Cards", cards);
						playerGiveCards.put("NumberOfCards", cards.size());
					}
	
					// check if player won game
					try{
						NumberOfCardsLong = (long) player.get("NumberOfCards");
						NumberOfCards = ((Long) NumberOfCardsLong).intValue();
					}catch(Exception e){
						NumberOfCards = (int) player.get("NumberOfCards");
					}
					
					
					if(NumberOfCards == 1){ // 1, because the current playing card  
						try{
							scoreLong = (long)player.get("Score");
							score =((Long) scoreLong).intValue(); 
						}catch(Exception e){
							score = (int) player.get("Score");
						} 
						player.put("Score", score +1);
						player.put("NumberOfCards", NumberOfCards-1);
						System.out.println("GAME ENDED!!!!! " + clientName + "won with score:" + (score +1));
						out.println("<playCard WON: MATCH OVER/>");
						out.flush();
					}
					// not needed if the game ends, put the new card to discarded array
					JSONObject CardsInfo = (JSONObject) obj.get("CardsInfo");
					JSONArray DiscardedCards = (JSONArray) CardsInfo.get("DiscardedCards");

					boardInfo.put("OpenCard", playCard);
					
					if(playCard.length() > 2){ // if is wild card with color (wcr, ccb...)
						String tempPlayCard = playCard.substring(0, 2);
						pCards.remove(tempPlayCard);
						DiscardedCards.add(tempPlayCard);
					}else{
						pCards.remove(playCard);
						DiscardedCards.add(playCard);
					}

				}
			}
		}
		
		System.out.println(obj);
		cls.updateGameFile(obj, roomFile);
		JSONObject tempJson = new JSONObject();
		tempJson.put("RoomInfo", obj.get("RoomInfo"));
		tempJson.put("BoardInfo", obj.get("BoardInfo"));
		out.print(tempJson);
		out.flush();
	}

	private static void StartGame(String s, PrintWriter out) throws IOException {
		String roomName = s.split("'")[1];
		boolean ableToStartGame = false;
		JsonFormater cls = new JsonFormater();
		JSONObject obj =  cls.FetchGameInfo(roomName + ".txt");
		JSONObject roomInfo = (JSONObject) obj.get("RoomInfo");
		JSONArray readyPlayers = (JSONArray) roomInfo.get("ReadyPlayers");
		JSONArray players = (JSONArray) roomInfo.get("Players");
		
		//Assign player order
		if(players.size() == readyPlayers.size()){ // check if all players are online (#ready == players online)
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


			System.out.println(Newobj);
			JSONObject tempJson = new JSONObject();
			tempJson.put("RoomInfo", Newobj.get("RoomInfo"));
			tempJson.put("BoardInfo", Newobj.get("BoardInfo"));
			out.print(tempJson);
			out.flush();
		}



	}

	private static void NewCard(String s, String clientName, PrintWriter out) throws IOException {
		int giveCards = 1;
		String roomName = s.split("'")[1];
		String roomFile = roomName + ".txt";
		
		JsonFormater cls = new JsonFormater();
		JSONObject obj =  cls.FetchGameInfo(roomFile);
		JSONObject BoardInfo = (JSONObject) obj.get("BoardInfo");
		String currentTurn = (String) BoardInfo.get("CurrentTurn");

		if(currentTurn.equals(clientName)){
			JSONObject PlayersInfo = (JSONObject) BoardInfo.get("PlayersInfo");
			JSONObject player = (JSONObject) PlayersInfo.get(clientName);
	
			List<String> cards = (List<String>) player.get("Cards");
	
			JSONObject CardsInfo = (JSONObject) obj.get("CardsInfo");
			JSONObject tempCardsInfo = cls.UpdateCardsInfo(CardsInfo, giveCards); // update the list if avilableCards is less then giveCards
	
			List<String> AvailableCards = (List<String>) tempCardsInfo.get("AvailableCards");
			List<String> DiscardedCards = (List<String>) tempCardsInfo.get("DiscardedCards");
	
			CardsInfo.put("AvailableCards", AvailableCards);
			CardsInfo.put("DiscardedCards", DiscardedCards);	
	
			List<String> NewCard = (List<String>) tempCardsInfo.get("giveCards");
			cards.addAll(NewCard);
			player.put("Cards", cards);
			player.put("NumberOfCards", cards.size());
			player.put("Uno", false);
	
			cls.updateGameFile(obj, roomName+".txt");
			System.out.println("'"+clientName + "' draw a new card: " + NewCard);
		}
		System.out.println("'"+clientName + "' Failed draw a new card: not his/her turn");
		JSONObject tempJson = new JSONObject();
		tempJson.put("RoomInfo", obj.get("RoomInfo"));
		tempJson.put("BoardInfo", obj.get("BoardInfo"));
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
					System.out.println("'"+clientName + "' pressed on uno");
					player.put("Uno", true);
				}else{ // give player 2 cards
					System.out.println("'"+clientName + "' pressed on uno button on " + playerName +" : +2");
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
			}else{
				System.out.println("'"+clientName + "' pressed uno on '" + playerName +"': player already pressed on uno");
			}
		}else{
			System.out.println("'"+clientName + "' pressed uno on '" + playerName +"': cards > 1");
		}
		cls.updateGameFile(obj, roomName+".txt");

		JSONObject tempJson = new JSONObject();
		tempJson.put("RoomInfo", obj.get("RoomInfo"));
		tempJson.put("BoardInfo", obj.get("BoardInfo"));
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
				if(line.startsWith("<Room Name='" +roomName +"'")){  // Check if room exist
					roomNotFound = false;
					String[] b = line.split("'");
					rdy = Integer.parseInt(b[5].split("/")[0]);
					cap = Integer.parseInt(b[5].split("/")[1]);

					JSONObject obj = cls.FetchGameInfo(roomName + ".txt");						
					JSONObject RoomInfo = (JSONObject) obj.get("RoomInfo");
					boolean gameStarted = (boolean) RoomInfo.get("GameStarted");
							
					if(line.contains(playerName)){ // if player is already in a game (rejoins)
						changePlayerTitle = true;

						JSONObject tempJson = new JSONObject();
						tempJson.put("RoomInfo", obj.get("RoomInfo"));
						tempJson.put("BoardInfo", obj.get("BoardInfo"));
						out.print(tempJson);
						out.flush();
						System.out.println("'" + playerName + "' Successfully join room: "+ obj);
					}
					else if(rdy < cap && !gameStarted){ // if player want to join a new Game Room
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
			String mode = ( sArr[3].equals("")) ? "Single" : sArr[3];		 // To avoid errors that occur if mode not specified 	
			String cap = ( sArr[5].equals("")) ? "1/8" : sArr[5];		 // To avoid errors that occur if mode not specified 
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
				//<Room Name='mujtaba' Mode='Single' Capacity='2/8' Player='mujtaba, Metin'/>

				FileWriter writer = new FileWriter("uno.txt",true);
				String tempRoom = "<Room Name='" +roomName +"' Mode='" +mode+"' Capacity='"+cap+"' Player='"+admin+"'/>";
				writer.write(tempRoom+"\n");
				writer.close();


				//To generate JSON String
				JsonFormater cls = new JsonFormater();
				JSONObject room = cls.generateRoomJson(roomName, mode, cap, admin);
				try (FileWriter gameFile = new FileWriter(roomName+".txt")) {
					gameFile.write(room.toString());
					System.out.println("Successfully Created Room: " + room);
					gameFile.close();

					JSONObject tempJson = new JSONObject();
					tempJson.put("RoomInfo", room.get("RoomInfo"));
					tempJson.put("BoardInfo", room.get("BoardInfo"));
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