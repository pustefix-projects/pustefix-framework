package sample.games.hangman;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dictionary {
	
	private Random random = new Random();
	private List<String> words = new ArrayList<String>();
	
	public Dictionary() {
		words.add("Transplantationsmedizin");
		words.add("Immatrikulationsbescheinigung");
		words.add("Naherholungsgebiet");
		words.add("Eisenbahnschaffner");
		words.add("Koalitionsverhandlung");
		words.add("Suchtberatungsstelle");
		words.add("Wiederaufbereitungsanlage");
		words.add("Atomwaffensperrvertrag");
		words.add("Proliferationsabkommen");
		words.add("Chemiewaffenkonvention");
		words.add("Druckwasserreaktor");
		words.add("Bereitstellungskosten");
		words.add("Revolutionsromantik");
		words.add("Panzerabwehrrakete");
		words.add("Reichweitenanalyse");
		words.add("Kampfmittelbeseitigungsdienst");
		words.add("Ertragsanteilsbesteuerung");
		words.add("Fertigungsprozessoptimierung");
		words.add("Einlassungsobliegenheit");
		words.add("Zentralsteuerungshypothese");
		words.add("Nahverkehrsgesellschaft");
		words.add("Imperialismustheorie");
		words.add("Hydrokulturpflanzen");
		words.add("Differentialquotient");
		words.add("Infinitesimalrechnung");
		words.add("Beschleunigungssensor");
		words.add("Kaltluftentstehungsgebiete");
		words.add("Revitalisierungskosten");
	}
	
	public String getRandomWord() {
		int rand = random.nextInt(words.size());
		return words.get(rand);
	}
	
	public static void main(String[] args) {
		Dictionary dict = new Dictionary();
		for(int i=0;i<100;i++) {
			System.out.println(dict.getRandomWord());
		}
	}

}
