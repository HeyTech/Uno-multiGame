import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/* 
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
 */

public class JsonFormater {
	static JSONObject generateRoomJson(String roomName, String mode, String online, String admin) {

		JSONObject obj = new JSONObject();
		
		// To generate RoomInfo
		JSONObject roomInfo = new JSONObject();
		roomInfo.put("RoomName", roomName);
		roomInfo.put("Mode", mode);
		roomInfo.put("Online", online);

		// only if 2v2
		JSONArray teamA = new JSONArray();
		JSONArray teamB = new JSONArray();
		JSONObject teams = new JSONObject();

		teams.put("TeamA", teamA);
		teams.put("TeamB", teamB);
		roomInfo.put("Teams", teams);
		// end of 2v2

		JSONArray players = new JSONArray();
		players.add(admin);
		roomInfo.put("Players", players);

		
		// To Generate BoardInfo
		JSONObject boardInfo = new JSONObject();
		boardInfo.put("OpenCard", "");
		boardInfo.put("CurrentTurn", "");
		boardInfo.put("NextTurn", "");
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

	


}
