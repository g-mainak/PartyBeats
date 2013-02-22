package youtube;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaGroup;

public class YouTubeManager {
	
	private static final String YOUTUBE_URL = "http://gdata.youtube.com/feeds/api/videos";
	private static final String YOUTUBE_EMBEDDED_URL = "http://www.youtube.com/v/";
	
	private String clientID;
	
	public YouTubeManager(String clientID) {
		this.clientID = clientID;
	}
	
	public List<YouTubeVideo> retrieveVideos(String textQuery, int maxResults, 
			boolean filter, int timeout) throws Exception {
		
		YouTubeService service = new YouTubeService(clientID);
		service.setConnectTimeout(timeout); // millis
		YouTubeQuery query = new YouTubeQuery(new URL(YOUTUBE_URL));
		
		query.setOrderBy(YouTubeQuery.OrderBy.RELEVANCE);
		query.setFullTextQuery(textQuery);
		query.setSafeSearch(YouTubeQuery.SafeSearch.NONE);
		query.setMaxResults(maxResults);
		query.setFormats(5);

		VideoFeed videoFeed = service.query(query, VideoFeed.class);		
		List<VideoEntry> videos = videoFeed.getEntries();
		
		return convertVideos(videos);
		
	}
	
	private List<YouTubeVideo> convertVideos(List<VideoEntry> videos) {
		
		List<YouTubeVideo> youtubeVideosList = new LinkedList<YouTubeVideo>();
		
		for (VideoEntry videoEntry : videos) {
			
			YouTubeVideo ytv = new YouTubeVideo();
			
			YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
			ytv.setTitle(videoEntry.getTitle().getPlainText());
			ytv.setID(mediaGroup.getVideoId());
			String webPlayerUrl = mediaGroup.getPlayer().getUrl();
			ytv.setWebPlayerUrl(webPlayerUrl);
			
			String query = "?v=";
			int index = webPlayerUrl.indexOf(query);
			String embeddedWebPlayerUrl = webPlayerUrl.substring(index+query.length());
			embeddedWebPlayerUrl = YOUTUBE_EMBEDDED_URL + embeddedWebPlayerUrl;
			ytv.setEmbeddedWebPlayerUrl(embeddedWebPlayerUrl);
			
			
			youtubeVideosList.add(ytv);
			
		}
		
		return youtubeVideosList;
		
	}
	
}
