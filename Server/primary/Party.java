package primary;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Party {
	
	private final double deltaLong = 1000; 
	private final double deltaLat = 1000;
	
	private double earthRadius = 3958.75;

	double longitude;
	double latitude;
	String name;
	String password;
	ArrayList<Song> songList;
	String hostDeviceID;
	private int promotionNumber;
	private int songIdx;
	private String locationName;

	public Party (double longitude, double latitude, String hostDeviceID, String name, String password, String locationName) {
		this.longitude = longitude;
		this.latitude = latitude;
		this.password = password;
		this.hostDeviceID = hostDeviceID;
		this.name = name;
		songList = new ArrayList<Song>();
		this.promotionNumber = 1;
		this.songIdx = 0;
		this.locationName = locationName;
	}

	public String getName() {
		return name;
	}
	
	public String getHostID() {
		return hostDeviceID;
	}
	
	public String getLocationName() {
		return this.locationName;
	}
	
	public String getDistance(double latitude, double longitude) {
		// credit: from stack overflow question
		double dLat = Math.toRadians(this.latitude - latitude);
		double dLong = Math.toRadians(this.longitude - longitude);
		double sindLat = Math.sin(dLat/2);
		double sindLng = Math.sin(dLong/2);
	    double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
	            * Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(latitude));
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;
	    DecimalFormat df = new DecimalFormat("#.##");
	    
	    return df.format(dist);
	}
	
	public boolean locMatches(double latitude, double longitude)
	{
		if ((Math.abs(latitude - this.latitude) <= deltaLat) &&
			(Math.abs(longitude - this.longitude) <= deltaLong))
			return true;
		else
			return false;
	}
	
	public boolean isHost(String deviceID) {
		return deviceID.equals(hostDeviceID);
	}

	private Song findSongByURL(String URL){    
		for (Song song : songList) {
			if (song.getURL().equals(URL)) {
				return song;
			}
		}
		return null; 
	}
	
	private Song findSongByName(String name){    
		for (Song song : songList) {
			if (song.getTitle().equals(name)) {
				return song;
			}
		}
		return null; 
	}

	public void upVoteSong(String name) 
	{
		Song s = findSongByName(name);
		if (s != null) {
			s.upVote();
		}
	}

	public void downVoteSong(String name) 
	{
		Song s = findSongByName(name);
		if (s != null) 
			s.downVote();
	}

	public void addSong(String name, String url) 
	{
		songList.add(new Song(songIdx++, name, url));	
	}

	public void deleteSong(String name) 
	{
		Song s = findSongByName(name);
		if (s == null) return;
		songList.remove(songList.indexOf(s));			
	}

	public void promoteSong(String name) 
	{
		Song s = findSongByName(name);
		if (s == null) return;
		s.promote(promotionNumber++);		
	}

	public boolean joinParty(String password) 
	{
		if (password.equals(this.password))
			return true;
		else return false;
	}

	public ArrayList<Song> getSongs() {
		return songList;
	}

}