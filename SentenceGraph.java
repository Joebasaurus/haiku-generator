package haiku;

import java.util.Random;

public class SentenceGraph {
	
	/**
	 * The order of this array corresponds to the order that respective words would occur in a sentence. 
	 * Each element stores the data that would be held in a corresponding vertex.
	 * 
	 * NOTE: any subsequent mention of a 'node index' refers to the index of a part of speech in this array.
	 */
	public final PartOfSpeech[] data = {
			PartOfSpeech.BLANK,
			PartOfSpeech.ADVERB, 
			PartOfSpeech.PREPOSITION, 
			PartOfSpeech.ARTICLE, 
			PartOfSpeech.ADJECTIVE, 
			PartOfSpeech.NOUN, 
			PartOfSpeech.VERB, 
			PartOfSpeech.ADVERB, 
			PartOfSpeech.PREPOSITION, 
			PartOfSpeech.ARTICLE, 
			PartOfSpeech.ADJECTIVE, 
			PartOfSpeech.NOUN, 
			PartOfSpeech.BLANK
			};
	
	
	/**
	 * This matrix stores the edges that link the vertices above.
	 * 
	 *      [row][colummn]   -->  [current vertex][adjacent vertex]
	 * 
	 * Each edge, if it exists, is assigned a decimal number between 0.0 and 2.0, exclusive;
	 * these numbers correspond to the relative weights of the edges (IE, which one is more "important" to follow).
	 * 
	 * A weight of 0.0 means that the edge is untraversable.
	 */
	private double[][] matrix;
	
	//-- an internal cursor; this stores the index of the vertex last visited
	private int iterator;
	
	
	
	/************************************\
	 *         CONSTRUCTOR
	 * 
	\************************************/

	public SentenceGraph() {
		reset();
	}

	
	
	/**
	 * Returns the size of this graph (number of vertices).
	 */
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
	 * Returns the part of speech for a provided vertex.
	 */
	public PartOfSpeech getNode(int index) {
		return data[index];
	}
	
	
	/**
	 * Finds the connecting edge linking the current vertex to another that's weighted most heavily.
	 */
	public int nextEdge(int currentNode) {
		double largestWeight = 0.0;
		int targetNode = -1;
		
		for(int i = 0; i < data.length; i++) {
			double total = getAdjustedWeight(currentNode, i, 0.2);
			
			if(total > largestWeight) {
				largestWeight = total;
				targetNode = i;
			}
		}
		iterator = targetNode;
		System.out.println("         index of heaviest edge: A[" + targetNode + "]  (" + largestWeight + ")");
		
		//-- adjust any edge weights that have changed from this move
		adjustMatrix(currentNode, targetNode);
		
		return targetNode;
	}
	
	
	/**
	 * Calculate an adjusted weight value for random edge selection.
	 * 
	 * The variance parameter adjusts how tightly bound the random calculation is to the expected value.
	 *   With a variance of 0.10, the returned value would fall randomly in the range
	 *   between the expected value (ie, stored weight value) plus or minus 10%.
	 *   
	 *   With 0.20, the calculation would fall randomly between (0.8 * weight) and (1.2 * weight).
	 * 
	 * @param variance the percentage of total weight is affected by a random multiplier
	 */
	private double getAdjustedWeight(int current, int destination, double variance) {
		double r = (new Random().nextInt(10) / 5.0) - 1.0;   // a random decimal  [0 < r < 10]
		double e = matrix[current][destination];			 // the weight of this edge (recorded in matrix)		
		double result = e + (r * variance);
		
		//-- This keeps a returned value from being locked out of an edge
		if (e > 0.0 && result <= 0.0)
			return 0.01;
		return result;
	}
	
	
	/**
	 * Determine whether the given vertex has any accessible, adjacent edges.
	 */
	public boolean hasNextEdge(int index) {
		for(int i = 0; i < data.length; i++)
			if(matrix[index][i] > 0) {
				return true;
			}
		return false;
	}
	
	
	/**
	 * Determine whether traversal of the graph has completed (end of a sentence).
	 */
	public boolean reachedEnd() {
		return (iterator == data.length - 1);
	}
	
	
	/**
	 * This method dynamically adjusts stored weight values for edges of the graph.
	 * It is called once for each edge traversal, and it tweaks values for certain edges,
	 * based on the path of traversal.
	 * 
	 * @param current the current vertex
	 * @param next the next vertex to be visited
	 */
	private void adjustMatrix(int current, int next) {
		//TODO: modify edge weights
		
		// ============= START ======================
		if(current == 0 && next == 5) { //start -> NOUN
			setEdge(5, 7, 0.0);
			modifyEdge(6, 7, 2.0);  	// V -> ADV +
			modifyEdge(7, 6, 2.0);		// ADV -> V +
			modifyEdge(6, 8, 2.0);
			modifyEdge(7, 8, 2.0);
			modifyEdge(6, 12, 0.1);
			modifyEdge(7, 12, 0.1);
		}
		
		// ============= ADVERB 1 ===================
		if(current == 1 && next == 1) //ADV cycle 1
			modifyEdge(1, 1, 0.5);
		
		// ============= PREP 1 =====================
		
		if(next == 2) { 	 //land on PREP 1
			//1. block travel past noun
			setEdge(5, 6, 0.0);
			setEdge(5, 7, 0.0);
			
			//2. enable returning from noun
			setEdge(5, 4, 0.2);  // enable N -> ADJ
			setEdge(5, 3, 0.8);  // enable N -> ART
		}
		
			//3. reenable travel past noun
		if(current == 5 && next == 3) {   // [N -> ART]
			modifyEdge(3, 4, 0.5);  // reduce ART -> ADJ
			setEdge(5, 6, 1.0);		// reenable N -> V
			setEdge(5, 7, 1.0);		// reenable N -> ADV
		}
		
		if(current == 5 && next == 4) {   // [N -> ADJ]
			modifyEdge(4, 3, 0.5);  // reduce ADJ -> ART
			setEdge(5, 6, 1.0);		// reenable N -> V
			setEdge(5, 7, 1.0);		// reenable N -> ADV
		}
							
		// ================== VERB ====================
			// Block travel past verb
			if (current == 5 && next == 7) { 	// [N -> ADV]
				setEdge(7, 8, 0.0);		// block ADV -> PREP 2
				setEdge(7, 12, 0.0);	// block ADV -> end
			}
			// Reenable travel past verb
			if (next == 6) {			// V is landed on
				setEdge(7, 8, 1.0);		// enable ADV -> PREP 2
				setEdge(7, 12, 0.1);	// enable ADV -> end
			}
			
			//Interrupt potential V -> ADV -> V -> ADV cycle
			if(current == 7 && next == 6) // ADV -> V: prohibit returning to ADV
				modifyEdge(6, 7, 0.3);
			if(current == 6 && next == 7) // V -> ADV: stop from returning to V
				setEdge(7, 6, 0.0);
		
		// ============================================		
		
		if(current == 7 && next == 7) // ADV cycle 2
			modifyEdge(7, 7, 0.5);
		
	}

	
	/**
	 * The below matrix stores the likelihood of traveling to a given connecting edge.
	 * A random number between one and ten is added to each choice in a row, and the edge with the largest value
	 * is chosen.
	 * 
	 * Play around with any nonzero edge weights to experiment.
	 *   traversible values: (0.0, 2.0) exclusive.
	 */
	public void reset() {
		iterator = 0;
							// 0	1	 2	 3	   4   5    6     7    8    9    10  11    12
						   //start adv* prep art  adj  n    v    adv* prep art  adj* n    end <--DESTINATION
		matrix = new double[][]{ 																
				new double[] {0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //start		0
				new double[] {0.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //adv*		1
				new double[] {0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //prep		2
				new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //art		3
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //adj  [sb] 4
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //noun [sb] 5
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.1}, //verb [pr] 6
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.3}, //adv* [pr] 7
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0}, //prep		8
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0}, //art		9
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0}, //adj*		10
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0}, //noun		11
				new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, //end		12
		};																						// ^ SOURCE
		
	}

	
	
	// ==== methods included as debugging tools === //
	
	/**
	 * Multiply the weight of an edge by the provided amount.
	 */
	public void modifyEdge(int source, int target, double mod) {
		matrix[source][target] = mod * matrix[source][target];
	}
	
	
	public void setEdge(int i, int j, double v) {
		matrix[i][j] = v;
	}
	
	
	public double getEdge(int i, int j) {
		// TODO Auto-generated method stub
		return matrix[i][j];
	}
}
