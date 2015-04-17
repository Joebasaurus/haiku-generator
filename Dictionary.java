package haiku;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

enum PartOfSpeech { NOUN, VERB, ADVERB, ADJECTIVE, PREPOSITION, ARTICLE, BLANK };

public class Dictionary {
	
	//stores the location of an external dictionary file
	private static final String DEFAULT_FILENAME = "dictionary.txt";
	
	//stores information about loaded words
	private Map<String, PartOfSpeech> dictionary;
	
	
	/**
	 * Default constructor for class Dictionary. Loads the default dictionary into memory.
	 * @throws IOException default dictionary.txt file not found
	 */
	public Dictionary() throws IOException {
		dictionary = new java.util.HashMap<String, PartOfSpeech>();
		System.out.print("   Loading dictionary file...");
		load(DEFAULT_FILENAME);
		System.out.println("done");
	}
	
	/**
	 * This constructor only loads the specified dictionary text file into memory.
	 * @param filename the dictionary text file to initially load
	 * @throws IOException specified dictionary txt file not found
	 */
	public Dictionary(String filename) throws IOException {
		dictionary = new java.util.HashMap<String, PartOfSpeech>();
		System.out.print("   Loading dictionary file...");
		load(filename);
		System.out.println("done");
	}
	
	//Read the given dictionary text file, and add its contents to this class' internal dictionary.
	public void load(String filename) throws IOException {
		Scanner inFile = new Scanner(new File(filename));		

		while(inFile.hasNextLine()) { //Load entries		
			String inLine = inFile.nextLine();  //stores each new line for parsing
			
			if (inLine.matches(".*|.*")) {	 //enforce preset format (WORD | POS)				
				//prune everything in line before delimiter (inclusive)
				String posString = inLine.substring(inLine.indexOf('|') + 1);				
				//prune everything in line after delimiter
				String word = inLine.substring(0, inLine.indexOf('|'));
				
				if(posString.contains(" NOUN"))
					dictionary.put(word, PartOfSpeech.NOUN);
				if(posString.contains(" VERB"))
					dictionary.put(word, PartOfSpeech.VERB);
				if(posString.contains(" ADJECTIVE"))
					dictionary.put(word, PartOfSpeech.ADJECTIVE);
				if(posString.contains(" ADVERB"))
					dictionary.put(word, PartOfSpeech.ADVERB);
				if(posString.contains(" PREPOSITION"))
					dictionary.put(word, PartOfSpeech.PREPOSITION);
				if(posString.contains(" ARTICLE"))
					dictionary.put(word, PartOfSpeech.ARTICLE);
				//if(posString.contains(" BLANK"))
					//dictionary.put(word, PartOfSpeech.BLANK);
			}
		}		
		inFile.close();
	}

	
	/**********************************************************\
	 *	The following methods deal with reading and modifying 
	 * 	word elements in the internal dictionary.
	 *
	\**********************************************************/
	
	//Return a set containing all words with the given POS
	public Set<String> wordSet(PartOfSpeech pos) {
		Set<String> set = new java.util.HashSet<String>();
		
		for(Entry<String, PartOfSpeech> element : dictionary.entrySet())
			if(element.getValue() == pos)
				set.add(element.getKey());
		return set;
	}
	
	//Return a set containing all words with the given POS and the given number of syllables
	public Set<String> wordSet(PartOfSpeech pos, int syllables) {
		Set<String> set = new java.util.HashSet<String>();
		
		for(Entry<String, PartOfSpeech> element : dictionary.entrySet())
			if(element.getValue() == pos)
				if(syllableCount(element.getKey()) == syllables)
					set.add(element.getKey());
		return set;
	}
	
	//Return a set containing all words with given POS, and a syllable count that's bounded by sMin and sMax (inclusive).
	public Set<String> wordSet(PartOfSpeech pos, int sMin, int sMax) {
		Set<String> set = new java.util.HashSet<String>();
		
		for(Entry<String, PartOfSpeech> element : dictionary.entrySet())
			if(element.getValue() == pos) {
				int syllables = syllableCount(element.getKey());
				if(sMin <= syllables && syllables <= sMax)
					set.add(element.getKey());
			}
		return set;
	}
	
	// Returns the part of speech of the given word.
	public PartOfSpeech getPOS(String word) {
		return dictionary.get(word);
	}
	
	
	
	
	/**********************************************************\
	 *	The following methods deal with syllable analysis.
	 *
	\**********************************************************/
	
	//Counts the syllables in a word (handles upper/lowercase)
	public static int syllableCount(String word) {
		word = word.trim();
		
		System.out.println(" Analyzing syllables in \"" + word + "\": " + (vowelCount(word) - (diphthongCount(word) + silentVowels(word)))
				+ "  [V:" + vowelCount(word) + "  D:" + diphthongCount(word) + "  S:" + silentVowels(word) + "]");
		return vowelCount(word) - (diphthongCount(word) + silentVowels(word));
	}
	
	//handles upper/lowercase
	public static boolean isVowel(char c) {
		switch(c) {
		case 'a':
		case 'A':
		case 'e':
		case 'E':
		case 'i':
		case 'I':
		case 'o':
		case 'O':
		case 'u':
		case 'U':
		case 'y':
		case 'Y':	return true;
			
		default: 	return false;
		}
	}
	
	//handles upper/lowercase
	public static int diphthongCount(String word) {
		word = word.toUpperCase();
		int count = 0;
		
		if(word.matches(".*A[EIUY].*")) count++;
		if(word.matches(".*E[AEIUY].*")) count++;
		if(word.matches(".*I[AEOU].*")) count++;
		if(word.matches(".*O[AIOUY].*")) count++;
		if(word.matches(".*U[AEIUY].*")) count++;
		if(word.matches(".*[A-Z&&[^AEIOU]]Y[AEIOU].*")) count++;
		
		return count;
	}
	
	public static int vowelCount(String word) {
		int count = 0;
		for(int i = 0; i < word.length(); i++)
			if(isVowel(word.charAt(i)))
				count++;
		return count;
	}
	
	//removes silent vowels from a word (handles upper/lowercase)
	public static int silentVowels(String word) {
		word = word.toUpperCase();
		if(word.length() > 1) {
			if(word.endsWith("E") && vowelCount(word) > 1)
				if (!isVowel(word.charAt(word.length() - 2)))
					return 1;
		}
		return 0;
	}

	
	/**********************************************************\
	 *	The following methods provide information about the underlying data.
	 *
	\**********************************************************/
	
	public boolean isEmpty() {
		return dictionary.isEmpty();
	}
	
	public int size() {
		return dictionary.size();
	}
	
	public boolean contains(String word) {
		return dictionary.containsValue(word);
	}


	public String[] toArray() {
		String[] temp = new String[dictionary.size()];
		
		int i = 0;
		for(String element: dictionary.keySet()) {
			temp[i] = element;
			i++;
		}
		return temp;
	}

	public boolean add(String s, PartOfSpeech pos) {
		if (s == null || pos == null)
			return false;
		if (s.length() == 0)
			return false;
		if (pos == PartOfSpeech.BLANK)
			return false;

		dictionary.put(s, pos);
		return true;
	}
}
