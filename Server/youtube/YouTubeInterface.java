package youtube;

import java.util.List;


public class YouTubeInterface {
	
	public static List<YouTubeVideo> searchSong(String arg) throws Exception
	{
		String clientID = "CS434Yale";
		int maxResults = 10;
		boolean filter = true;
		int timeout = 2000;
		
		YouTubeManager ym = new YouTubeManager(clientID);
		
		List<YouTubeVideo> videos = ym.retrieveVideos(arg, maxResults, filter, timeout);
		
		return videos;
		
	}
	
//	public static void main(String[] args) throws Exception
//	{
//		searchSong("Mainak");
//	}

}
