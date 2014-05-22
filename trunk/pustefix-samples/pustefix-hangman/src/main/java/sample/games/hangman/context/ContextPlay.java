package sample.games.hangman.context;

import sample.games.hangman.DifficultyLevel;


public class ContextPlay {
   
	private String word;
	private DifficultyLevel level;
	private String displayWord;
	private int misses;
	private long startTime;
	private long endTime;
	
	public void reset() {
	    word = null;
	    level = null;
	    displayWord = null;
	    misses = 0;
	    startTime = 0;
	    endTime = 0;
	}
	
	public String getWord() {
		return word;
	}
	
	public void setWord(String word) {
		this.word = word;
		setDisplayWord(word.replaceAll("." , "_"));
	}
	
	public DifficultyLevel getLevel() {
	    return level;
	}
	
	public void setLevel(DifficultyLevel level) {
	    this.level = level;
	}
	
	public void setDisplayWord(String displayWord) {
		this.displayWord = displayWord;
	}
	
	public String getDisplayWord() {
	    return displayWord;
	}
	
	public void guess(char ch) {
	    StringBuilder sb = new StringBuilder();
	    boolean ok = false;
        for(int i=0; i<word.length(); i++) {
            if(Character.toUpperCase(word.charAt(i)) == ch) {
                sb.append(ch);
                ok = true;
            } else {
                sb.append(displayWord.charAt(i));
            }
        }
        setDisplayWord(sb.toString());
        if(!ok) misses++;
        if(isCompletedSuccessful()) {
            end();
        }
	}
	
	public int getMisses() {
		return misses;
	}
	
	public boolean isCompletedSuccessful() {
	    return !displayWord.contains("_");
	}
	
	public boolean isCompletedFaulty() {
	    return misses > 5;
	}
	
	public boolean isCompleted() {
	    return isCompletedSuccessful() || isCompletedFaulty();
	}
	
	public void start() {
	    startTime = System.currentTimeMillis();
	}
	
	public void end() {
	    endTime = System.currentTimeMillis();
	}
	
	public long getTime() {
	    if(startTime == 0) return 0;
	    return (endTime == 0 ? System.currentTimeMillis() : endTime) - startTime;
	}
	
}
