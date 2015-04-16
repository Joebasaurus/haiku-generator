package haiku;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;

/**
 * This program is a haiku generator.  It loads words from a dictionary file, and arranges them according to
 * part of speech and syllabic order.
 * 
 * @author Jobin
 * @version 0.2.8
 */
public class Haiku extends JFrame implements ActionListener {
	
     // =========================== INTERNAL COMPONENTS =========================== \\
     
		//stores the location of an external dictionary file
		private static final String DICTIONARY_FILE_PATH = "dictionary.txt";
		//stores desired sentence structure
		private static final SentenceGraph graph = new SentenceGraph();
	
		public enum PartOfSpeech { NOUN, VERB, ADVERB, ADJECTIVE, PREPOSITION, ARTICLE, BLANK };
	
		//stores information about loaded words
		private HashMap<String, PartOfSpeech> dictionary;
	

		// GUI components
		private JButton generateButton;
		private JTextArea output;

	
	
     // =========================== CONSTRUCTOR AND MAIN =========================== \\
	
	public Haiku() {
		setupDictionary();
		setupWindow();
		System.out.println("   SETUP COMPLETE");
	}
	
	
	public static void main(String[] args) {
		//create an instance of 'Haiku' to invoke the setup functions
		Haiku program = new Haiku();
		program.output.setText("    Clicking down below \n"
				+ "   will generate a haiku \n"
				+ "   why don't you try it?");		
	}
	
	
     // ============================ PRIMARY METHODS ================================ \\
     
	/**
	 * The backbone of the program.
	 * @return a complete haiku.
	 */
	private String generate() {
		System.out.print("   Generating a haiku...");		
		graph.reset();
		
		String outString = "";		
		outString += buildSentence(5, graph.getIndex()) + "\n";
		outString += buildSentence(7, graph.getIndex()) + "\n";
		outString += buildSentence(5, graph.getIndex()) + "\n";
		System.out.println("done");
		return outString;
	}
	
	
	/**
	 * This method recursively traverses the supporting sentence structure graph.
	 * @param syllableCount the number of syllables remaining in the current line.
	 * @param startIndex the index of the current graph node.
	 * @return a string containing the current haiku line
	 */
	private String buildSentence(int syllablesLeft, int startIndex) {
		
		//BASE CASE: the current line contains exactly (target) syllables
		if (syllablesLeft <= 0)
			return "";
		
		//BASE CASE: end of sentence is reached
		if (startIndex >= graph.last() - 1 && syllablesLeft <= 0)
			return "";
		
		
		//Pick a word (in this call) to add. If the dictionary runs out, or if 0 syllables are specified,
		// this will return null.
		PartOfSpeech nextPos = graph.getNode(startIndex);
		String word = getNextWord(nextPos, syllablesLeft);
		
		// if (word == null), no words can be found that meet the criteria.
		if(word != null) {
			
			// Iterate through the edges accessible from this position
			int i = graph.nextEdge(startIndex);
			
			//this stops the sentence from ending on a preposition or article
			if( 	(graph.reachedEnd() || syllablesLeft - syllables(word) <1) && 
				(nextPos == PartOfSpeech.ARTICLE || nextPos == PartOfSpeech.PREPOSITION)
			     	) {
				System.out.println(" Error: cannot end on a preposition or article. (BACKTRACKING)");
				return null;
			}
			
			while (graph.hasNextEdge(i) && i < graph.last()) {
				
				//attempt travel to the next available edge
				System.out.println("attempting travel to edge: " + i + "    (pos: " + graph.getNode(i) + ")");
				String temp = buildSentence(syllablesLeft - syllables(word), i);
				
				// if sentence can be completed by following this edge, commit the result.
				// if (temp == null), method is backtracking (a dead end was reached in subsequent recursion).
				if (temp != null)
					return word + temp;
				
				i = graph.nextEdge(i);
			}
		}
		// if this point is reached, the method either has no more available edges or no words.
		System.out.println("\n           DEAD END -- BACKTRACKING\n");
		return null;
	}
	
	
	/**
	 * Find the number of syllables in a given string.
	 * This method doesn't yet work, so dictionary words with > 1 syllables should be avoided for now.
	 */
	private static int syllables(String word) {
		if (word == null) 
			return 0;
		if (word.length() == 0) 
			return 0;
		
		return 1;
	}
	
	
	/**
	 * Pick a random word from the dictionary that fits the given criteria.
	 * @param pos the desired part of speech
	 * @param sylCount the MAXIMUM number of syllables that the word can have
	 */
	private String getNextWord(PartOfSpeech pos, int sylMax) {
		//TODO: make this more efficient than brute-force
		//TODO: this method currently ONLY retrieves the first word matching criteria, NOT a randomized one		
		System.out.println(" Searching for a " + pos + " with <" + sylMax + " syllables...");
		
		if (pos == PartOfSpeech.BLANK)
			return "";
		
		// Create a set of all words that meet desired criteria
		Set<String> words = new HashSet<String>();
		for(String current : dictionary.keySet()) {
			//System.out.print("\n  - " + current);
			if( 
			current.length() > 0 && 
			pos == dictionary.get(current) && 
			syllables(current) <= sylMax) {
				//System.out.print(" <- possible choice");
				words.add(current);
				
			}
		}
		
		// Choose one word from this set at random
		int target = new Random().nextInt(words.size());
		int i = 0;
		for(String s : words) {
			if (i == target) {
				System.out.println("\n Word chosen: " + s);
				return s;
			}
		 	i++;
		}
		return null;
	}
	
	
	
	// =================== SETUP METHODS ========================= \\
	
	/**
	 * Initialize the supporting data structure for a Haiku generator.
	 */
	private void setupDictionary() {
		System.out.print("   Loading dictionary...");
		dictionary = new HashMap<String, PartOfSpeech>();
		
		//Deal with checked file IO exceptions
		try {
			Scanner inFile = new Scanner(new File(DICTIONARY_FILE_PATH));			
			
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
			System.out.println("done");
		} catch (IOException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
				this, 
				"Error encountered while loading file \"" + DICTIONARY_FILE_PATH 
					+ "\": file not found",
				"Error loading file", 
				JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}
	
	
	/**
	 * Initialize the GUI components of a Haiku generator window.
	 */
	private void setupWindow() {
		System.out.print("  Creating GUI window...");		
		this.getContentPane().setLayout(new BorderLayout());
		
		//setup generation button
		generateButton = new JButton("Haiku");
		generateButton.addActionListener(this);
		this.getContentPane().add(generateButton, BorderLayout.SOUTH);
		
		//setup output field 
		output = new JTextArea();
		output.setEditable(false);
		output.setLineWrap(true);
		//output.setWrapStyleWord(true);
		this.getContentPane().add(output, BorderLayout.CENTER);
		
		//window setup -- general
		this.setTitle("Haiku Generator");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setSize(400,300);
		this.setVisible(true);
		
		System.out.println("done");
	}
	
	
	/**
	 * Catch an ActionEvent -- used for identification of button clicks
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == generateButton)
			output.setText(generate());
	}
}
