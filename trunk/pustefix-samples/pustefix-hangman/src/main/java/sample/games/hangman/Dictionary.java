package sample.games.hangman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class Dictionary {
	
	private Random random = new Random();
	private Map<Locale, List<String>> words = new HashMap<Locale, List<String>>();
	private int maxBeginnerLen = 13;
	private int maxAdvancedLen = 20;
	
	public Dictionary() {
	    
	    List<String> wordsDE = new ArrayList<String>();
	    List<String> wordsEN = new ArrayList<String>();
	    words.put(Locale.GERMAN, wordsDE);
	    words.put(Locale.ENGLISH, wordsEN);
	    
	    wordsDE.add("Reinigung");
        wordsDE.add("Regierung");
        wordsDE.add("Steuerung");
        wordsDE.add("Regulierung");
        wordsDE.add("Relativierung");
        wordsDE.add("Handbetrieb");
        wordsDE.add("Fehleinkauf");
	    wordsDE.add("Hausschwein");
	    wordsDE.add("Rollentausch");
        
	    wordsDE.add("Naherholungsgebiet");
        wordsDE.add("Eisenbahnschaffner");
        wordsDE.add("Revolutionsromantik");
        wordsDE.add("Panzerabwehrrakete");
        wordsDE.add("Reichweitenanalyse");
        wordsDE.add("Druckwasserreaktor");
        wordsDE.add("Bereitstellungskosten");
        wordsDE.add("Koalitionsverhandlung");
        wordsDE.add("Suchtberatungsstelle");
	    
		wordsDE.add("Transplantationsmedizin");
		wordsDE.add("Immatrikulationsbescheinigung");
		wordsDE.add("Wiederaufbereitungsanlage");
		wordsDE.add("Atomwaffensperrvertrag");
		wordsDE.add("Proliferationsabkommen");
		wordsDE.add("Chemiewaffenkonvention");
		wordsDE.add("Kampfmittelbeseitigungsdienst");
		wordsDE.add("Ertragsanteilsbesteuerung");
		wordsDE.add("Fertigungsprozessoptimierung");
		wordsDE.add("Einlassungsobliegenheit");
		wordsDE.add("Zentralsteuerungshypothese");
		wordsDE.add("Nahverkehrsgesellschaft");
		wordsDE.add("Imperialismustheorie");
		wordsDE.add("Hydrokulturpflanzen");
		wordsDE.add("Differentialquotient");
		wordsDE.add("Infinitesimalrechnung");
		wordsDE.add("Beschleunigungssensor");
		wordsDE.add("Kaltluftentstehungsgebiete");
		wordsDE.add("Revitalisierungskosten");
		
		wordsEN.add("affirmation");
		wordsEN.add("insurance");
		wordsEN.add("assertion");
		wordsEN.add("assurance");
		wordsEN.add("accumulator");
		wordsEN.add("warehouse");
		wordsEN.add("clearance");
		wordsEN.add("maintenance");
		wordsEN.add("brainwash");
		wordsEN.add("malediction");
		wordsEN.add("punishment");
		wordsEN.add("disadvantage");
		wordsEN.add("subversion");
		
		wordsEN.add("mispronunciation");
		wordsEN.add("tintinnabulation");
		wordsEN.add("plenipotentiary");
		wordsEN.add("prestidigitation");
		wordsEN.add("machiavellianism");
		wordsEN.add("hypervitaminosis");
		
		wordsEN.add("disproportionableness");
		wordsEN.add("antitransubstantiationalist");
		wordsEN.add("psychophysicotherapeutics");
		wordsEN.add("radioimmunoelectrophoresis");
		wordsEN.add("floccinaucinihilipilification");
		wordsEN.add("honorificabilitudinitatibus");
		wordsEN.add("antidisestablishment");
		wordsEN.add("antidisestablishmentarianism");
		
	}
	
	public String getRandomWord(Locale locale, DifficultyLevel level) {
	    List<String> localeWords = words.get(locale);
	    if(localeWords == null) localeWords = words.get(Locale.ENGLISH);
	    List<String> levelWords = new ArrayList<String>();
	    int minLen = maxAdvancedLen + 1;
	    int maxLen = Integer.MAX_VALUE;
	    if(level == DifficultyLevel.BEGINNER) {
	        minLen = 1;
	        maxLen = maxBeginnerLen;
	    } else if(level == DifficultyLevel.ADVANCED) {
	        minLen = maxBeginnerLen + 1;
	        maxLen = maxAdvancedLen;
	    }
	    for(String word: localeWords) {
	        if(word.length() >= minLen && word.length() <= maxLen) levelWords.add(word);
	    }
	    int rand = random.nextInt(levelWords.size());
		return levelWords.get(rand);
	}
	
	public DifficultyLevel getDifficultyLevel(String word) {
	    int len = word.length();
	    if(len <= maxBeginnerLen) return DifficultyLevel.BEGINNER;
	    else if(len <= maxAdvancedLen) return DifficultyLevel.ADVANCED;
	    else return DifficultyLevel.EXPERT;
	}
	
	public static void main(String[] args) {
		Dictionary dict = new Dictionary();
		for(int i=0;i<100;i++) {
			System.out.println(dict.getRandomWord(Locale.ENGLISH, DifficultyLevel.BEGINNER));
		}
	}

}
