package primary;

public class Song {

	private int index;
	private String title;
	private String url;
	private int votes;
	private int promotionNumber;
	
	public Song(int index, String title, String url) {
		this.index = index;
		this.title = title;
		this.url = url;
		this.votes = 0;
		this.promotionNumber = 0;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public void upVote() {
		if (this.votes == Integer.MAX_VALUE) return;
		this.votes++;
		System.out.println("votes: " + this.votes);
	}
	
	public void downVote() {
		if (this.votes == Integer.MAX_VALUE) return; 
		this.votes--;
	}
	
	public void promote(int promotionNumber) {
		this.promotionNumber = promotionNumber;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getURL() {
		return this.url;
	}
	
	public int getVotes() {
		return this.votes;
	}
	
	public int getPromotionNumber() {
		return this.promotionNumber;
	}
}
