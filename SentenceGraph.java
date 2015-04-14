package haiku;

public class SentenceGraph {
	
	//stores word order
	public final Haiku.PartOfSpeech[] data = 
			{Haiku.PartOfSpeech.BLANK, Haiku.PartOfSpeech.ADVERB, Haiku.PartOfSpeech.PREPOSITION, 
			Haiku.PartOfSpeech.ARTICLE, Haiku.PartOfSpeech.ADJECTIVE, Haiku.PartOfSpeech.NOUN, 
			Haiku.PartOfSpeech.VERB, Haiku.PartOfSpeech.ADVERB, Haiku.PartOfSpeech.PREPOSITION, 
			Haiku.PartOfSpeech.ARTICLE, Haiku.PartOfSpeech.ADJECTIVE, Haiku.PartOfSpeech.NOUN, 
			Haiku.PartOfSpeech.BLANK};
	
	//stores edge weights
	private int[][] matrix;
	
	//an internal cursor; this stores the index of the POS last visited
	private int iterator;

	public SentenceGraph() {
		reset();
	}

	public int size() {
		return data.length;
	}
	
	/**
	 * Returns the index of the last visited part of speech.
	 */
	public int getIndex() {
		return iterator;
	}

	/**
	 * Returns the part of speech for the given index.
	 */
	public Haiku.PartOfSpeech getNode(int index) {
		return data[index];
	}
	
	
	/**
	 * Given a graph node, choose one edge pseudorandomly from the available
	 * connecting edges.
	 * @param index the index of the current part of speech
	 */
	public int nextEdge(int index) {
		//TODO: incorporate random edge movement instead of heaviest/first
		java.util.Random random = new java.util.Random();
		int max = 0;
		int maxIndex = -1;
		for(int i = 0; i < data.length; i++) {
			int temp = random.nextInt(10);
			if(matrix[index][i] + temp > max) {
				maxIndex = i;
				max = matrix[index][maxIndex] + temp;
			}
		}
		iterator = maxIndex;
		System.out.println("         index of heaviest edge: A[" + maxIndex + "]");	
		return maxIndex;
	}
	
	
	/**
	 * 
	 */
	public boolean hasNextEdge(int index) {
		for(int i = 0; i < data.length; i++)
			if(matrix[index][i] > 0) {
				return true;
			}
		return false;
	}

	
	/**
	 * The below matrix stores the likelihood of traveling to a given connecting edge.
	 * A random number between one and ten is added to each choice in a row, and the edge with the largest value
	 * is chosen.
	 * 
	 * Play around with any nonzero edge weights to experiment.
	 */
	public void reset() {
		iterator = 0;
		matrix = new int[][]{ 	
				new int[] {0, 10, 10, 10, 10, 0, 0, 0, 0, 0, 0, 0, 0}, //start
				new int[] {0, 3, 10, 10, 10, 0, 0, 0, 0, 0, 0, 0, 0}, //adv
				new int[] {0, 0, 0, 10, 10, 5, 0, 0, 0, 0, 0, 0, 0}, //prep
				new int[] {0, 0, 0, 0, 9, 10, 0, 0, 0, 0, 0, 0, 0}, //art
				new int[] {0, 0, 0, 0, 0, 10, 0, 0, 0, 0, 0, 0, 0}, //adj
				new int[] {0, 0, 0, 0, 0, 0, 10, 10, 0, 0, 0, 0, 0}, //noun
				new int[] {0, 0, 0, 0, 0, 0, 0, 2, 10, 0, 0, 0, 10}, //verb
				new int[] {0, 0, 0, 0, 0, 0, 4, 0, 8, 0, 0, 0, 1}, //adv
				new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1}, //prep
				new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0}, //art
				new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 10, 0}, //adj
				new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, //noun
				new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, //end
		};
	}

	
	// ==== methods included as debugging tools === //
	public void setEdge(int i, int j, int v) {
		matrix[i][j] = v;
	}
	
	public int getEdge(int i, int j) {
		// TODO Auto-generated method stub
		return matrix[i][j];
	}
}
