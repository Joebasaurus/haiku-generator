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
 * @version 0.0.7
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
				+ "   something blah blah blah.");		
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
	private String buildSentence(int syllableCount, int startIndex) {
		
		//BASE CASE: the current line contains exactly (target) syllables
		if (syllableCount <= 0)
			return "";
		
		//BASE CASE: end of sentence is reached
		if (startIndex >= graph.size() - 1 && syllableCount > 0)
			return null;
		
		
		//Pick a word (in this call) to add. If the dictionary runs out, or if 0 syllables are specified,
		// this will return null.
		String word = getNextWord(graph.getNode(startIndex), syllableCount);
		System.out.println("Current POS: " + graph.getNode(startIndex)
			+ "\n Current recursion call word: " + word);
		
		// if (word == null), no words can be found that meet the criteria.
		if(word != null) {
			
			// Iterate through the edges accessible from this position
			for(int i = graph.nextEdge(startIndex); graph.hasNextEdge(i); i = graph.nextEdge(i)) {
				
				//attempt travel to the next available edge
				System.out.println("attempting travel to edge: " + i);
				String temp = buildSentence(syllableCount - syllables(word), i);
				
				// if sentence can be completed by following this edge, commit the result.
				// if (temp == null), a dead end was reached in subsequent recursion.
				if (temp != null)
					return word + temp;
			}
		}
		// if this point is reached, the method either has no more available edges or no words.
		//          either way, the method needs to backtrack.
		return null;
	}
	
	
	/**
	 * Find the number of syllables in a given string.
	 * This method doesn't yet work, so dictionary words with > 1 syllables should be avoided for now.
	 */
	private static int syllables(String word) {
		return 1;
	}
	
	
	/**
	 * Pick a random word from the dictionary that fits the given criteria.
	 * @param pos the desired part of speech
	 * @param sylCount the MAXIMUM number of syllables that the word can have
	 */
	private String getNextWord(PartOfSpeech pos, int sylCount) {
		//TODO: make this more efficient than brute-force
		//TODO: this method currently ONLY retrieves the first word matching criteria, NOT a randomized one
		
		Random random = new Random();
		HashMap<Integer, String> temp = new HashMap<Integer, String>();
		
		for(Entry<String, PartOfSpeech> current: dictionary.entrySet()) {
			if(pos == current.getValue() && sylCount >= syllables(current.getKey()))
				return current.getKey();
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
					
					PartOfSpeech pos = null;
					if(posString.contains("NOUN"))
						pos = PartOfSpeech.NOUN;
					if(posString.contains("VERB"))
						pos = PartOfSpeech.VERB;
					if(posString.contains("ADJECTIVE"))
						pos = PartOfSpeech.ADJECTIVE;
					if(posString.contains("ADVERB"))
						pos = PartOfSpeech.ADVERB;
					if(posString.contains("PREPOSITION"))
						pos = PartOfSpeech.PREPOSITION;
					if(posString.contains("ARTICLE"))
						pos = PartOfSpeech.ARTICLE;
					if(posString.contains("BLANK"))
						pos = PartOfSpeech.BLANK;
					
					//prune everything in line after delimiter
					String word = inLine.substring(0, inLine.indexOf('|'));
					
					dictionary.put(word, pos);
				}
			}
			
			inFile.close();
			System.out.println("done");
		} catch (IOException exception) {
			exception.printStackTrace();
			JOptionPane.showMessageDialog(
				this, 
				"Error encountered while loading file \"" + DICTIONARY_FILE_PATH + "\": file not found",
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
