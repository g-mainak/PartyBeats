package primary;

/*
 * 
 */
import java.awt.List;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javapns.Push;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import youtube.YouTubeVideo;
import youtube.YouTubeInterface;

public class ServiceThread extends Thread {

	private enum Action {
		ADD, UPVOTE, DOWNVOTE, PROMOTE, REMOVE
	}
	
	private ServerSocket welcomeSocket;
	private Map<String, Party> partyTable;
	private Map<String, Party> deviceTable;

	private BufferedReader inFromClient;
	private DataOutputStream outToClient;

	public ServiceThread(ServerSocket welcomeSocket,
			Map<String, Party> partyTable,
			Map<String, Party> deviceTable) {
		this.welcomeSocket = welcomeSocket;
		this.partyTable = partyTable;
		this.deviceTable = deviceTable;

	}

	public void run() {

		System.out.println("Thread " + this + " started.");
		while (true) {
			// get a new request connection
			Socket s = null;
			
			synchronized (welcomeSocket) {
				try {
					s = welcomeSocket.accept();
					System.out.println("Thread " + this + " process request "
							+ s);
				} catch (IOException e) {
				}
			} // end of extract a request

			processRequest(s);

		} // end while

	} // end run


	// Request:
	// GET_[functionName]?([params]=[value])+
	private void processRequest(Socket connSock) {

		try {
			// create read stream to get input
			inFromClient = new BufferedReader(new InputStreamReader(
					connSock.getInputStream()));
			outToClient = new DataOutputStream(connSock.getOutputStream());

			String query = inFromClient.readLine();
			System.out.println("query string: " + query);
			// check for special cases
			if (query == null) return;

			String[] request = query.split("\\s");
			if (request.length < 2 ||
					(!request[0].equals("GET") && !request[0].equals("OPTIONS"))) {
				outputError(500, "Bad request");
				connSock.close();
				return;
			}

			// server for player
			if (request[1].equals("/home")) {
				outputResponseHeader();
				outputResponseBody(readFile("/home/accts/ss2372/Desktop/CS434/FP/json.html"));
				connSock.close();
				return;
			}
			
			// now parse request identifier
			System.out.println("Request[1]:" + request[1]);
			String[] path = request[1].split("\\?");
			if (path.length < 2) {
				outputError(500, "Bad request");
				connSock.close();
				return;
			}
						
			String id = path[0];
			id = id.substring(1);
			String[] params = path[1].split("&");
			System.out.println("id: " + id);
			//	searchParty(loc, name)
			//  GET_searchParty?loc={}&name={}
			if (id.equals("searchParty")) {
				processSearchParty(params);
				
			// newParty (password, partyname, deviceID)
			// GET_newParty?loc={}&deviceID={}&name={}&password={}
			} else if (id.equals("newParty")) {
				newParty(params);
			
			// joinParty (password, partyname, deviceID)
			// GET_joinParty?password={}&partyName={}&deviceID={}
			} else if (id.equals("joinParty")) {
				processJoinParty(params);
			
			//  searchSong (song, deviceID)
			// GET_searchSong?song={}
			} else if (id.equals("searchSong")) {
				searchSong(params);
				
			//  addSong ( song, deviceID)
			// GET_addSong?name={}&url={}&deviceID={}
			} else if (id.equals("addSong")) {
				processSong(params, Action.ADD);
			
			//  addSong ( song, deviceID)
			// GET_upvoteSong?name={}&deviceID={}
			} else if (id.equals("upvoteSong")) {
				processSong(params, Action.UPVOTE);
			
			// downvoteSong ( song, deviceID)
			// GET_downvoteSong?name={}&deviceID={}
			} else if (id.equals("downvoteSong")) {
				processSong(params, Action.DOWNVOTE);
				
			// promoteSong ( song, deviceID)
			// GET_promoteSong?name={}&deviceID={}
			} else if (id.equals("promoteSong")) {
				processSong(params, Action.PROMOTE);
			
			// deleteSong ( song, deviceID)
			// GET_deleteSong?name={}&deviceID={}
			} else if (id.equals("deleteSong")) {
				processSong(params, Action.REMOVE);
			
			// leaveParty ( deviceID )
			// GET_leaveParty?deviceID={}
			} else if (id.equals("leaveParty")) {
				leaveParty(params);
			
			// getSongs ( deviceID )
			// GET_getSongs?deviceID={}
			} else if (id.equals("getSongs")) {
				getSongs(params);
				
			} else {
				outputError(500, "Bad request: check your request identifier");
			}
			
			connSock.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	} // end of serveARequest
	
	// newParty (password, partyname, deviceID)
	// GET_newParty?loc={}&deviceID={}&name={}&password={}
	private void newParty(String[] params) throws Exception {
		
		Map<String, String> paramsMap = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			paramsMap.put(name, value);
		}
		
		ArrayList<String> matches = new ArrayList<String>();
		// get parties with associated location
		String name = paramsMap.get("name");
		String deviceID = paramsMap.get("deviceID");
		String password = paramsMap.get("password");
		String locationStr = paramsMap.get("loc");
		String locationName = paramsMap.get("locationName");
			
		if (name == null || deviceID == null || password == null || locationStr == null || locationName == null) {
			error500("missing parameters");
			return;
		}
		
		String[] location = locationStr.split(":");
		if (location.length < 2) {
			error500("Location is of format lat:lng");
			return;
		}
			
		double latitude = Double.parseDouble(location[0]);
		double longitude = Double.parseDouble(location[1]);
		
		// add new party to table
		Party np = new Party(latitude, longitude, deviceID, name, password, locationName);
		partyTable.put(name, np);
		System.out.println("Putting deviceID into deviceTable: " + deviceID);
		deviceTable.put(deviceID, np);
		
		outputResponseHeader();
		outputResponseBody("created new party");
	}
	
	// process the search party request
	//  GET_searchParty?loc={}&name={}
	private void processSearchParty(String[] params) throws Exception {
		Map<String, String> paramsMap = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			paramsMap.put(name, value);
		}
		
		double latitude = -1;
		double longitude = -1;
		
		ArrayList<Party> matches = new ArrayList<Party>();
		// get parties with associated location
		if (!paramsMap.get("loc").equals("null")) {
			String[] location = paramsMap.get("loc").split(":");
			if (location.length < 2) {
				error500("Location is of format lat:lng");
				return;
			}
			
			latitude = Double.parseDouble(location[0]);
			longitude = Double.parseDouble(location[1]);
			String name = paramsMap.get("name");
			if (name == null) {
				error500("did not specify name");
				return;
			}
			
			for (Party p : partyTable.values()) {
				if (p.locMatches(latitude, longitude)) {
					matches.add(p);
				}
			}
			
		// get parties with associated name
		} else if (!paramsMap.get("name").equals("null")) {
			String nameQuery = paramsMap.get("name");
			for (Party p : partyTable.values()) {
				if (p.getName().equals(nameQuery)) {
					matches.add(p);
				}
			}
			
		} else {
			error500("No name or location specified");
			return;
		}
		
		JSONArray jsa = new JSONArray();
		// write matching parties as JSON object	
		String partiesList;
		if (matches.size() > 0) {
			StringWriter partiesListWriter = new StringWriter();
			for (Party p : matches) {
				JSONObject jso = new JSONObject();
				jso.put("name", p.getName());
				jso.put("distance", p.getDistance(latitude, longitude));
				jso.put("locationName", p.getLocationName());
				jsa.add(jso);
			}
		}
		
		JSONObject jso = new JSONObject();
		jso.put("parties", jsa);
		outputResponseHeader();
		outputResponseBody(jso.toJSONString());
	}
	
	// searchSong( song )
	// GET_searchSong?song={}
	private void searchSong(String [] params) throws Exception {
		Map<String, String> paramsMap = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			paramsMap.put(name, value);
		}
		
		String songName = paramsMap.get("song");
		if (songName == null) return;
		
		LinkedList<YouTubeVideo> listVideos = (LinkedList<YouTubeVideo>) YouTubeInterface.searchSong(songName);
		JSONArray songList = new JSONArray();
		for (YouTubeVideo ytv : listVideos) {
			JSONObject obj = new JSONObject();
			obj.put("name", ytv.getTitle());
			obj.put("url", ytv.getID());
			songList.add(obj);
			
		}
		
		JSONObject returnObj = new JSONObject();
		returnObj.put("songs", songList);
		outputResponseHeader();
		outputResponseBody(returnObj.toString());
	}
	
	
	// getSongs ( deviceID )
	// GET_getSongs?deviceID={}
	private void getSongs(String[] params) throws Exception {
		Map<String, String> paramsMap = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			paramsMap.put(name, value);
		}
		
		String deviceID = paramsMap.get("deviceID");
		String partyName = paramsMap.get("partyName");
		if (deviceID == null && partyName == null) {
			error500("no device ID or party name in parameters");
			return;
		}
		
		Party p;
		if (deviceID != null) {
			p = deviceTable.get(deviceID);
		} else {
			p = partyTable.get(partyName);
		}
		
		if (p == null) {
			error500("unauthorized access of party");
			return;
		}
		
		// write songs out to JSON
		ArrayList<Song> partySongs = p.getSongs();
		JSONArray songList = new JSONArray();
		for (Song s : partySongs) {
			JSONObject obj = new JSONObject();
			obj.put("name", s.getTitle());
			obj.put("url", s.getURL());
			obj.put("votes", s.getVotes());
			obj.put("promotion_number", s.getPromotionNumber());
			obj.put("index", s.getIndex());
			songList.add(obj);
			
		}
		
		JSONObject returnObj = new JSONObject();
		returnObj.put("songs", songList);
		outputResponseHeader();
		outputResponseBody(returnObj.toString());
	}
	
	// process the join party request
	// GET_joinParty?password={}&partyName={}&deviceID={}
	private void processJoinParty(String[] params) throws Exception {
		Map<String, String> paramsMap = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			paramsMap.put(name, value);
		}
		
		String password = paramsMap.get("password");
		String partyName = paramsMap.get("partyName");
		String deviceID = paramsMap.get("deviceID");
		
		if (password == null || partyName == null || deviceID == null) {
			error500("missing parameters");
			return;
		}
		
		// get the party to be joined
		Party p = partyTable.get(partyName);
		if (p == null) {
			error500("unauthorized access to party");
			return;
		}
		
		// try to join the party, if successful, route further requests from this
		// device ID to that party
		String resultMsg;
		JSONObject jso = new JSONObject();
		if (p.joinParty(password)) {
			synchronized(deviceTable) {
				System.out.println("joining device with deviceID:" + deviceID + " to party " + p.getName());
				deviceTable.put(deviceID, p);
				if (p.isHost(deviceID)) {
					resultMsg = "you are the host";
				} else {
					resultMsg = "joined party";
				}
			}
		} else {
			resultMsg = "wrong password";
		}
		
		outputResponseHeader();
		outputResponseBody(resultMsg);
	}
	
	//  addSong ( song, deviceID)
	// GET_addSong?name={}&url={}&deviceID={}
	private void processSong(String[] params, Action action) throws Exception {
		Map<String, String> paramsMap = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			paramsMap.put(name, value);
		}
		
		String song = paramsMap.get("name");
		String deviceID = paramsMap.get("deviceID");
		String partyName = paramsMap.get("partyName");
		
		if (song == null || deviceID == null) {
			error500("missing parameters - must specify song and deviceID");
			return;
		}
		
		Party p = deviceTable.get(deviceID);
		if (deviceID.equals("0")) {
			// message coming from player
			if (partyName == null) {
				error500("requires a party name");
				return;
			}
			
			p = partyTable.get(partyName);
			
			if (p == null) {
				error500("invalid party name");
				return;
			}
			
		} else if (p == null) {
			error500("Unauthorized access to party");
			return;
		}
		
		
		
		String returnMsg = "done";
		synchronized (p) {
			if (action == Action.ADD) {
				String url = paramsMap.get("url");
				if (url == null) {
					returnMsg = "no url specified for add";
					return;
				}
				p.addSong(song, url);
			} else if (action == Action.DOWNVOTE) {
				p.downVoteSong(song);
			} else if (action == Action.UPVOTE) {
				p.upVoteSong(song);
			} else if (action == Action.PROMOTE) {
				if (!deviceID.equals(p.getHostID())) {
					returnMsg = "permissions error: you are not the host";
				}
				
				p.promoteSong(song);
			} else if (action == Action.REMOVE) {
				System.out.println("entered remove if block");
				if (!deviceID.equals(p.getHostID()) &&
						!deviceID.equals("0")) {
					returnMsg = "permissions error: you are not the host";
				} else {
					p.deleteSong(song);
				}
			}
		}
		
		outputResponseHeader();
		outputResponseBody(returnMsg);
		
		// propagate update to all clients using APNS
		for (String id : deviceTable.keySet()) {
			if (deviceTable.get(id) == p) {
				System.out.println("trying to send a push notification");
				Push.alert("New msg sent!", 
						"/home/accts/ss2372/Desktop/CS434/FP/Apple/iphone_dev.p12",
						"partybeats123",
						false,
						id);
			}
		}
		
	}
	
	private void leaveParty(String[] params) throws Exception {
		Map<String, String> paramsMap = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			paramsMap.put(name, value);
		}
		
		String deviceID = paramsMap.get("deviceID");
		if (deviceID == null) return;
		
		synchronized(deviceTable) {
			deviceTable.remove(deviceID);
		}
		outputResponseHeader();
		outputResponseBody("done");
	}
	
	private void error500(String msg) throws IOException {
		outputError(500, msg);

	}

	private void outputResponseHeader() throws Exception {
		outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n");
	}

	private void outputResponseBody(String out) throws Exception {
		outToClient.writeBytes("Content-Length: " + out.length() + "\r\n");
		outToClient.writeBytes("\r\n");
		outToClient.writeBytes(out);

	}

	void outputError(int errCode, String errMsg) {
		try {
			outToClient.writeBytes("HTTP/1.0 " + errCode + " " + errMsg
					+ "\r\n");
		} catch (Exception e) {
		}
	}
	
	// read a file into a string
	public static String readFile( String file ) throws IOException {
		BufferedReader reader = new BufferedReader( new FileReader (file));
		String line  = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		while( ( line = reader.readLine() ) != null ) {
			stringBuilder.append( line );
			stringBuilder.append( ls );
		}
		reader.close();
		return stringBuilder.toString();
 	}
} // end ServiceThread