import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

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
		
		
		obj.put("RoomInfo", roomInfo);
		obj.put("BoardInfo", boardInfo);
		obj.put("AvailableCards", new JSONArray());
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
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
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
			team.add(playerName);
			
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
			JSONArray readyPlayers = (JSONArray) roomInfo.get("ReadyPlayers");
			if(Arrays.asList(readyPlayers).contains(playerName)){
				readyPlayers.remove(playerName);
			}else{
				readyPlayers.add(playerName);
			}
			
			updateGameFile(obj, roomFile);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj;
	}

	private void updateGameFile(JSONObject obj, String roomFile) throws IOException {
		try (FileWriter gameFile = new FileWriter(roomFile)) {
			gameFile.write(obj.toString());
			System.out.println("Successfully Copied JSON Object to File...");
			System.out.println("JSON Object: " + obj);
		}
		
	}

	


}
