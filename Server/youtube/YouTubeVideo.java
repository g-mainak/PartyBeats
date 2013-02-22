package youtube;

public class YouTubeVideo {
	
	private String webPlayerUrl;
	private String embeddedWebPlayerUrl;
	private String title;
	private String videoID;
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	public String getID()
	{
		return videoID;
	}
	
	public void setID(String ID)
	{
		this.videoID = ID;
	}
	
	public String getWebPlayerUrl() {
		return webPlayerUrl;
	}
	public void setWebPlayerUrl(String webPlayerUrl) {
		this.webPlayerUrl = webPlayerUrl;
	}

	public String getEmbeddedWebPlayerUrl() {
		return embeddedWebPlayerUrl;
	}
	public void setEmbeddedWebPlayerUrl(String embeddedWebPlayerUrl) {
		this.embeddedWebPlayerUrl = embeddedWebPlayerUrl;
	}
	
	

}
