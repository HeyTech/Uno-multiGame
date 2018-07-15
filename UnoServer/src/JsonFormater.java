import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonFormater {
	static JSONObject generateRoomJson(String roomName, String mode, String online, String admin) {

		JSONObject obj = new JSONObject();

		// To generate RoomInfo
		JSONObject roomInfo = new JSONObject();
		roomInfo.put("RoomName", roomName);
		roomInfo.put("Mode", mode);
		roomInfo.put("Online", online);
		roomInfo.put("Admin", admin);
		roomInfo.put("GameStarted", false);

		// only if 2v2
		JSONArray teamA = new JSONArray();
		JSONArray teamB = new JSONArray();
		JSONObject teams = new JSONObject();

		teams.put("TeamA", teamA);
		teams.put("TeamB", teamB);
		roomInfo.put("Teams", teams);
		// end of 2v2

		JSONArray readyPlayers = new JSONArray();
		roomInfo.put("ReadyPlayers", readyPlayers);

		JSONArray players = new JSONArray();
		players.add(admin); // Just to put the admin as online player
		roomInfo.put("Players", players);



		// To Generate BoardInfo
		JSONObject boardInfo = new JSONObject();
		boardInfo.put("OpenCard", "");
		boardInfo.put("CurrentTurn", "");
		boardInfo.put("NextTurn", "");
		boardInfo.put("Blocked", "");

		boardInfo.put("TurnTime", new Integer(10));

		JSONObject playersInfo = new JSONObject();

		JSONObject p1 = GeneratePlayerCards(admin, new JSONArray() ,0);
		playersInfo.put(admin, p1); // p1 name

		boardInfo.put("PlayersInfo", playersInfo);


		JSONObject cardsInfo = new JSONObject();
		List<String> cardArr = new ArrayList<>(Arrays.asList("b4", "y3", "yr", "r2", "cc", "y5", "g7", "bs", "g8", "g3", "br", "gs", "wc", "g2", "b8", "r3", "yp", "b6", "rr", "rp", "ys", "rs", "y2", "yp", "g4", "ys", "g3", "rs", "br", "b0", "y4", "r5", "rr", "b5", "gr", "bp", "yr", "b1", "g6", "y1", "yr", "y3", "gs", "r1", "gs", "r0", "y4", "r4", "wc", "r8", "br", "bs", "g1", "r1", "r8", "rp", "g5", "b8", "rp", "y6", "bs", "y7", "b7", "b2", "r5", "b3", "y7", "gr", "r7", "g6", "cc", "r6", "r2", "y5", "yp", "wc", "rp", "cc", "bp", "gs", "b4", "gp", "gr", "y1", "bp", "g1", "g8", "b5", "gp", "b6", "r6", "b7", "ys", "g0", "g4", "yp", "rs", "b1", "b2", "r4", "rs", "r3", "rr", "wc", "bp", "bs", "cc", "y8", "br", "gr", "y0", "yr", "r7", "y6", "gp", "gp", "g7", "rr", "b3", "g5", "ys", "y8", "g2", "y2"));       // Java 1.7+
		//List<String> cardArr = new ArrayList<>(Arrays.asList("b4", "y3", "yr", "r2", "cc", "y5", "g7"));
		Collections.shuffle(cardArr);
		cardsInfo.put("AvailableCards", cardArr);
		cardsInfo.put("DiscardedCards", new JSONArray());
		
		
		obj.put("RoomInfo", roomInfo);
		obj.put("BoardInfo", boardInfo);
		obj.put("CardsInfo", cardsInfo);
		

		return obj;
	}

	private static JSONObject GeneratePlayerCards(String playerName, JSONArray cards, int score){
		JSONObject pObj = new JSONObject();
		pObj.put("Cards", cards);
		pObj.put("Score", score);
		pObj.put("Uno", false);
		pObj.put("NumberOfCards", cards.size());

		return pObj;
	}

	public JSONObject AddNewPlayerToRoom(String roomFile, String playerName, String newCap) {
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject) parser.parse(new FileReader(roomFile));
			JSONObject newObj = (JSONObject) obj;
			JSONObject roomInfo = (JSONObject) newObj.get("RoomInfo");
			roomInfo.put("Online", newCap);
			JSONArray players = (JSONArray) roomInfo.get("Players");
			players.add(playerName);


			JSONObject boardInfo = (JSONObject) newObj.get("BoardInfo");
			JSONObject playersInfo = (JSONObject) boardInfo.get("PlayersInfo");
			JSONObject p2 = GeneratePlayerCards(playerName, new JSONArray() ,0);
			playersInfo.put(playerName, p2); // p1 name



		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return obj;
	}


	public JSONObject ChangePlayerTeam(String roomFile, String playerName, String teamName) {
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject) parser.parse(new FileReader(roomFile));
			JSONObject newObj = (JSONObject) obj;
			JSONObject roomInfo = (JSONObject) newObj.get("RoomInfo");
			JSONObject teams = (JSONObject) roomInfo.get("Teams");
			JSONArray team = (JSONArray) teams.get(teamName);
			JSONArray readyPlayers = (JSONArray) roomInfo.get("ReadyPlayers");

			if(team.toString().contains(playerName)){
				team.remove(playerName);
				readyPlayers.remove(playerName);
			}else{

				// Check if the player is added in the other team
				String otherTeamName = "";
				if(teamName.equals("TeamA")){
					otherTeamName = "TeamB";
				}else{
					otherTeamName = "TeamA";
				}

				JSONArray otherTeam = (JSONArray) teams.get(otherTeamName);

				if(otherTeam.toString().contains(playerName)){
					otherTeam.remove(playerName);
				}
				team.add(playerName);
				readyPlayers.remove(playerName);
			}

			updateGameFile(obj, roomFile);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return obj;
	}

	public JSONObject PlayerGettingReady(String roomFile, String playerName) {
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject) parser.parse(new FileReader(roomFile));
			JSONObject newObj = (JSONObject) obj;
			JSONObject roomInfo = (JSONObject) newObj.get("RoomInfo");
			String mode = (String) roomInfo.get("Mode");
			JSONArray readyPlayers = (JSONArray) roomInfo.get("ReadyPlayers");


			if(readyPlayers.toString().contains(playerName)){
				readyPlayers.remove(playerName);
			}else{
				if((mode.toLowerCase()).equals("2v2")){
					// check first if player has choosen a team
					JSONObject teams = (JSONObject) roomInfo.get("Teams");
					JSONArray teamA = (JSONArray) teams.get("TeamA");
					JSONArray teamB = (JSONArray) teams.get("TeamB");
					if(teamA.toString().contains(playerName) || teamB.toString().contains(playerName)){
						readyPlayers.add(playerName);
					}
				}else{
					readyPlayers.add(playerName);
				}
			}


			updateGameFile(obj, roomFile);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return obj;
	}

	public void updateGameFile(JSONObject obj, String roomFile) throws IOException {
		try (FileWriter gameFile = new FileWriter(roomFile)) {
			gameFile.write(obj.toString());
			// System.out.println("Successfully Copied JSON Object to File...");
			// System.out.println("JSON Object: " + obj);
		}

	}

	public JSONObject FetchGameInfo(String roomFile) {
		JSONParser parser = new JSONParser();
		JSONObject obj = null;
		try {
			obj = (JSONObject) parser.parse(new FileReader(roomFile));
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

		return obj;
	}

	public void RemovePlayerFromRoom(String clientName, String roomName) throws IOException {
		String roomFile = roomName + ".txt";
		
		JSONObject obj = FetchGameInfo(roomFile);
		JSONObject RoomInfo = (JSONObject) obj.get("RoomInfo");
		String Online = (String) RoomInfo.get("Online");
		int on = Integer.parseInt(Online.split("/")[0]);
		
		if((on - 1) != 0){
			int cap = Integer.parseInt(Online.split("/")[1]);
			RoomInfo.put("Online", String.format("%d/%d", (on-1), cap));
		
			JSONObject boardInfo = (JSONObject) obj.get("BoardInfo");
			JSONObject PlayersInfo = (JSONObject) boardInfo.get("PlayersInfo");
			PlayersInfo.remove(clientName);
			
			JSONArray ReadyPlayers = (JSONArray) RoomInfo.get("ReadyPlayers");
			ReadyPlayers.remove(clientName);
			
			JSONObject Teams = (JSONObject) RoomInfo.get("Teams");
			JSONArray TeamA = (JSONArray) Teams.get("TeamA");
			TeamA.remove(clientName);
			JSONArray TeamB = (JSONArray) Teams.get("TeamB");
			TeamB.remove(clientName);
			
			JSONArray Players = (JSONArray) RoomInfo.get("Players");
			Players.remove(clientName);
			
			String Admin = (String) RoomInfo.get("Admin");
			if(Admin.equals(clientName)){
				RoomInfo.put("Admin", Players.get(0));
			}
			
			System.out.println(obj);
			updateGameFile(obj, roomFile); 
		}else{
			RemoveGameFile(roomFile, "Because it got empty");
    		
		}
		
	}

	private void RemoveGameFile(String roomFile, String reason) {
		File file = new File(roomFile);
		if(file.delete()){
			System.out.println("'" + file.getName() + "' Deleted: " + reason);
		}else{
			System.out.println("Delete '" + roomFile +"' operation is failed.");
		}
	}

	public JSONObject UpdateCardsInfo(JSONObject cardsInfo, int i) {
		List<String> avilableCards = (List<String>) cardsInfo.get("AvailableCards");
		if(avilableCards.size() < i){ // if avilableCards has less than 2 cards, shuffle discardedcards and give player 2 cards
			List<String> DiscardedCards = (List<String>) cardsInfo.get("DiscardedCards");
			Collections.shuffle(DiscardedCards);
			avilableCards.addAll(DiscardedCards);			
		}
		JSONObject tempCardsInfo = new JSONObject();
		List<String> giveCards = avilableCards.subList(0, i);	
		avilableCards = avilableCards.subList(i, avilableCards.size());
		tempCardsInfo.put("giveCards", giveCards);
		tempCardsInfo.put("AvailableCards", avilableCards);
		tempCardsInfo.put("DiscardedCards", new JSONArray());
		
		return tempCardsInfo;
		
	}

	public JSONObject NewGameDealCards(String roomFile) {
		
		JsonFormater cls = new JsonFormater();
		JSONObject obj =  cls.FetchGameInfo(roomFile);
		
		JSONObject cardsInfo = (JSONObject) obj.get("CardsInfo");
		JSONObject BoardInfo = (JSONObject) obj.get("BoardInfo");

		List<String> avilableCards = (List<String>) cardsInfo.get("AvailableCards");
		
		Collections.shuffle(avilableCards);
		String openCard = "";
		boolean openCardNotAWildCard = false;
		int k = 0;
		
		while(!openCardNotAWildCard){ // choose a opencard that is not a wild card
			openCard = avilableCards.get(k);
			if("cpsr".indexOf(openCard.charAt(1)) == -1){ // "cpsr" = wild cards
				System.out.println("OpenCard: "+ openCard);
				BoardInfo.put("OpenCard", openCard);

				// found a not a wild card
				openCardNotAWildCard = true;
				avilableCards.remove(k);
			}
			k++;
		}
		
		//hand 7 cards for every player, 1 card each 
		JSONObject PlayersInfo = (JSONObject) BoardInfo.get("PlayersInfo");
		JSONObject roomInfo = (JSONObject) obj.get("RoomInfo");
		
		String mode = (String) roomInfo.get("Mode");
		if(mode.toLowerCase().equals("single")){
			List<String> tempPlayers = (List<String>) roomInfo.get("Players");
			Collections.shuffle(tempPlayers);
			roomInfo.put("Players", tempPlayers);
		}

		JSONArray players = (JSONArray) roomInfo.get("Players");
		for(int i = 0; i <7; i++){
			for(int p = 0;  p < players.size(); p++){
				String card = avilableCards.remove(0);
				JSONObject player = (JSONObject) PlayersInfo.get(players.get(p));
				JSONArray cards = (JSONArray) player.get("Cards");
				cards.add(card);	
			}
		}
		cardsInfo.put("AvailableCards", avilableCards);
	
		for(int p = 0;  p < players.size(); p++){
			JSONObject player = (JSONObject) PlayersInfo.get(players.get(p));
			player.put("NumberOfCards", 7);
		}
		
		
		// Set "GameStarted" to true
		roomInfo.put("GameStarted", true);
		
		
		// playing order 
		BoardInfo.put("CurrentTurn", players.get(0));
		BoardInfo.put("NextTurn", players.get(1));		
		return obj;
	}

}
