package sample.games.hangman.context;

public class ContextPlay {

	private String word;
	private String displayWord;
	private int tries;
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public String getDisplayWord() {
		return displayWord;
	}
	public void setDisplayWord(String displayWord) {
		this.displayWord = displayWord;
	}
	
	public int getTries() {
		return tries;
	}
	
	public void incTries() {
		tries++;
	}
	
}
