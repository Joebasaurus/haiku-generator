package haiku;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

enum PartOfSpeech { NOUN, VERB, ADVERB, ADJECTIVE, PREPOSITION, ARTICLE, BLANK };

public class Dictionary {
	
	//-- stores the location of an external dictionary file
	private static final String DEFAULT_FILENAME = "dictionary.txt";
	
	//-- stores information about loaded words
	private Map<String, PartOfSpeech> dictionary;
	
	
	/**
	 * Default constructor for class Dictionary. Loads the default dictionary into memory.
	 * @throws IOException default dictionary.txt file not found
	 */
	public Dictionary() throws FileNotFoundException {
		dictionary = new java.util.HashMap<String, PartOfSpeech>();
		System.out.print("   Loading dictionary file...");
		
		//-- Dictionary is loaded here. This returns false if loading fails
		if(!load(DEFAULT_FILENAME))
			throw new FileNotFoundException("Error loading file \"" 
					+ DEFAULT_FILENAME + "\": file not found");
		
		System.out.println("done");
	}
	
	/**
	 * This constructor only loads the specified dictionary text file into memory.
	 * @param filename the dictionary text file to initially load
	 * @throws IOException specified dictionary txt file not found
	 */
	public Dictionary(String filename) throws FileNotFoundException {
		dictionary = new java.util.HashMap<String, PartOfSpeech>();
		System.out.print("   Loading dictionary file...");
		
		//-- Dictionary is loaded here
		if (!load(filename))
			throw new FileNotFoundException("Error loading file \"" 
					+ filename + "\": file not found");
		
		System.out.println("done");
	}
	
	
	/**
	 * Read the given dictionary text file, and add its contents to this class' internal dictionary.
	 * 
	 * @param filename the filename of a dictionary text file
	 * @return if the dictionary file was loaded successfully
	 */
	public boolean load(String filename) {
		try {
			Scanner inFile = new Scanner(new File(filename));		
	
			while(inFile.hasNextLine()) {	
				String inLine = inFile.nextLine();  // stores each new line for parsing
				
				if (inLine.matches(".*|.*")) {	 // this line enforces entry format, (word | PARTOFSPEECH) without parenthesis
					
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
				}
			}		
			inFile.close();
			return true;
		} catch (IOException exception) {
			System.out.println(" SYSTEM ERROR: IOException thrown during LOAD attempt (" + filename + ")");
			exception.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * This method saves the dictionary loaded in memory to the default text file ("dictionary.txt").
	 */
	public boolean save() {
		return save(DEFAULT_FILENAME);
	}
	
	/**
	 * This method saves the dictionary loaded in memory to a specified text file.
	 */
	public boolean save(String filename) {
		try {
			java.io.PrintWriter outFile = new java.io.PrintWriter(filename);
			for(Entry<String, PartOfSpeech> element : dictionary.entrySet())
				outFile.println(element.getKey() + " | " + element.getValue());
			outFile.close();
			return true;
		} catch (IOException exception) {
			exception.printStackTrace();
			return false;
		}
	}
	
	
	/**********************************************************\
	 *	The following methods deal with reading and modifying 
	 * 	word elements in the internal dictionary.
	 *
	\**********************************************************/
	
	/**
	 * Returns a set containing all dictionary words with the given part of speech.
	 */
	public Set<String> wordSet(PartOfSpeech pos) {
		Set<String> set = new java.util.HashSet<String>();
		
		for(Entry<String, PartOfSpeech> element : dictionary.entrySet())
			if(element.getValue() == pos)
				set.add(element.getKey());
		return set;
	}
	
	/**
	 * Returns a set containing all dictionary words that have both the specified part of speech,
	 *  and the specified number of syllables
	 */
	public Set<String> wordSet(PartOfSpeech pos, int syllables) {
		Set<String> set = new java.util.HashSet<String>();
		
		for(Entry<String, PartOfSpeech> element : dictionary.entrySet())
			if(element.getValue() == pos)
				if(syllableCount(element.getKey()) == syllables)
					set.add(element.getKey());
		return set;
	}
	
	/**
	 * Returns a set containing all the dictionary words that have the specified part of speech, 
	 * as well as a syllable count between sMin and sMax (inclusive).
	 * @param pos the part of speech
	 * @param sMin the smallest number of syllables usable
	 * @param sMax the largest number of syllables usable
	 */
	public Set<String> wordSet(PartOfSpeech pos, int sMin, int sMax) {
		Set<String> set = new java.util.HashSet<String>();
		System.out.println("INITIAL WORDSET SIZE: " + set.size()
				+ "\n Populating with " + pos + " with length between " + sMin + " and " + sMax);
		
		for(Entry<String, PartOfSpeech> element : dictionary.entrySet())
			if(element.getValue() == pos) {
				int syllables = syllableCount(element.getKey());
				if(sMin <= syllables && syllables <= sMax)
					set.add(element.getKey());
			}
		System.out.println("RETURNING WORD SET WITH SIZE: " + set.size());
		return set;
	}
	
	/**
	 *  Returns the part of speech of the given word.
	 */
	public PartOfSpeech getPOS(String word) {
		return dictionary.get(word);
	}
	
	
	
	
	/**********************************************************\
	 *	The following methods deal with syllable analysis.
	 *
	\**********************************************************/
	
	/**
	 * Counts the number of syllables in a word.
	 */
	public static int syllableCount(String word) {
		if(word == null) 
			return 0;
		
		word = word.trim().toUpperCase();		
		int vowels = vowelCount(word);
		
		if(vowels > 1)
			if(word.matches(".*[A-Z && [^AEIOUY]]ED?"))
				vowels--;
		
		return vowels - diphthongCount(word);
	}
	
	/**
	 * Counts the number of diphthongs (one-syllable vowel pairs) in a word.
	 */
	private static int diphthongCount(String word) {
		word = word.toUpperCase();
		
		int count = 0;
		if(word.matches(".*A[EIUY].*")) 	count++;
		if(word.matches(".*E[AEIUY].*")) 	count++;
		if(word.matches(".*I[AEOU].*")) 	count++;
		if(word.matches(".*O[AIOUY].*")) 	count++;
		if(word.matches(".*U[AEIUY].*")) 	count++;
		if(word.matches(".*[A-Z&&[^AEIOU]]Y[AEIOU].*")) count++;
		
		return count;
	}
	
	/**
	 * Counts the number of vowels in a word.
	 */
	public static int vowelCount(String word) {
		int count = 0;
		for(int i = 0; i < word.length(); i++)
			if(isVowel(word.charAt(i)))
				count++;
		return count;
	}
	
	
	/**
	 * Determines whether a given character is a vowel.
	 */
	public static boolean isVowel(char c) {
		switch(c) {
		case 'A': case 'a':	
		case 'E': case 'e':
		case 'I': case 'i':
		case 'O': case 'o':
		case 'U': case 'u':
		case 'Y': case 'y':
					return true;
					
		default: 	return false;
		}
	}

	
	/*************************************************************************\
	 *	The following methods provide information about the underlying data.
	 *
	\*************************************************************************/
	
	/**
	 * Returns true if this dictionary contains no elements (words).
	 */
	public boolean isEmpty() {
		return dictionary.isEmpty();
	}
	
	
	/**
	 * Returns the number of entries in this dictionary.
	 */
	public int size() {
		return dictionary.size();
	}
	
	
	/**
	 * Returns true if this dictionary contains the specified word.
	 */
	public boolean contains(String word) {
		return dictionary.containsValue(word);
	}
	
	
	/**
	 * Returns an array containing all of the entries in this dictionary.
	 */
	public String[] toArray() {
		
		String[] temp = new String[dictionary.size()];
		
		int i = 0;
		for(String element: dictionary.keySet()) {
			temp[i] = element;
			i++;
		}
		return temp;
	}
	

	/**
	 * Add the specified entry to this dictionary.
	 */
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
