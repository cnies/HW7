/*
	 CSE 12 Homework 7
	 Christopher Nies
	 A11393577
	 Section B00
	 May 16th, 2014
 */
/**
 * TQTree.java
 * A Java class that supports a Binary Tree that plays the game of twenty questions
 * 
 * @author Christine Alvarado
 * @version 2.0
 * @date May 11, 2014
 */

import java.io.*;
import java.util.Scanner;
import java.text.ParseException;
import java.util.LinkedList;

public class TQTree {
	private TQNode root;

	/** Inner class that supports a node for a twenty questions tree.
	 * You should not need to change this class. */
	class TQNode {
		/*  You SHOULD NOT add any instance variables to this class */
		TQNode yesChild;  // The node's right child
		TQNode noChild;   // The node's left child
		String data;      // A question (for non-leaf nodes) 
		// or an item (for leaf nodes)

		int idx;        // index used for printing

		/** Make a new TWNode 
		 * @param data The question or answer to store in the node. 
		 */
		public TQNode( String data )
		{
			this.data = data;
		}    

		/** Setter for the noChild 
		 * @param noChild The new left (no) child
		 */
		public void setNoChild( TQNode noChild )
		{
			this.noChild = noChild;
		}


		/** Setter for the yesChild 
		 * @param yesChild The new right (yes) child
		 */
		public void setYesChild( TQNode yesChild )
		{
			this.yesChild = yesChild;
		}


		/** Getter for the yesChild 
		 * @return The node's yes (right) child
		 */
		public TQNode getYesChild()
		{
			return this.yesChild;
		}

		/** Getter for the noChild 
		 * @return The node's no (left) child
		 */
		public TQNode getNoChild()
		{
			return this.noChild;
		}

		/** Getter for the data
		 * @return The data stored in this node
		 */
		public String getData()
		{
			return this.data;
		}

		/** Setter for the index
		 * @param idx index of this for printing 
		 */
		public void setIndex(int idx) {
			this.idx = idx;
		}

		/** get the index
		 * @return idx index of this for printing 
		 */
		public int getIndex() {
			return this.idx;
		}
	}  // End TQNode


	/** Builds a starter TQ tree with 1 question and 2 answers */
	public TQTree()
	{
		TQNode rootNode = new TQNode("Is it bigger than a breadbox?");
		TQNode yesAnswer = new TQNode("a Computer Scientist");
		TQNode noAnswer = new TQNode("Spam");
		rootNode.setYesChild(yesAnswer);
		rootNode.setNoChild(noAnswer);
		this.root = rootNode;
	}

	/** Builds a new TQTree by reading from a file 
	 * @param filename The file containing the tree
	 * @throws FileNotFoundException if the file cannot be found or read.
	 * */
	public TQTree( String filename )
	{
		File f = new File( filename );
		LineNumberReader reader;
		try {
			reader = new LineNumberReader( new FileReader( f ));
		} catch ( FileNotFoundException e ) {
			System.err.println( "Error opening file " + filename );
			System.err.println( "Building default Question Tree." );
			buildDefault();
			return;
		}
		//Try to use the buildSubTree method here
		try{
			this.root = buildSubtree(reader); 
		}
		//If the file is in the wrong format, build default tree
		catch (ParseException e){
			System.err.println(e.getMessage());
			System.err.println("Building default Question Tree");
			buildDefault();
		}
		//More exception checking
		try {
			reader.close();
		} catch ( IOException e ) {
			System.err.println( "An error occured while closing file " + filename );
		}

	}

	/** Play one round of the game of Twenty Questions using this game tree 
	 * Augments the tree if the computer does not guess the right answer
	 */ 
	public void play()
	{
		//Declare some helpful stuff
		Scanner input = new Scanner(System.in);
		TQNode currentNode = this.root;
		TQNode lastQuestion;
		boolean lastAnswerYes;
		//We know we need to do this at least once, so do...while works well here
		do{
			System.out.println(currentNode.getData());
			String response = input.nextLine();
			//If user's response is affirmative, move down yes part of the tree
			if (readResponse(response)) {
				lastAnswerYes = true;
				lastQuestion = currentNode;
				currentNode = currentNode.getYesChild();
			}
			//If the user's response is negative, move down no side of the tree
			else{
				lastAnswerYes = false;
				lastQuestion = currentNode;
				currentNode = currentNode.getNoChild();
			}
		} while (!isLeaf(currentNode));
		//Once we are at a leaf, take a guess
		System.out.println("My guess is "+currentNode.getData()+". Am I correct?");
		String response = input.nextLine();
		if (readResponse(response)){
			System.out.println("I win!");
			return;
		}
		else{
			//Set up conditions for adding to the TQTree with a new answer
			System.out.println("Okay, what was it?");
			String newAnswer = fixString(input.nextLine(), false);
			System.out.println("Give me a question that would distinguish "+newAnswer+
					" from "+currentNode.getData());
			String newQuestion = fixString(input.nextLine(), true);
			System.out.println("And would the answer to that question for "+newAnswer
					+" be yes or no?");
			boolean isYes = readResponse(input.nextLine());
			addNewQuestion(newQuestion, newAnswer, isYes, lastQuestion, lastAnswerYes);
		}
	}

	/** Save this Twenty Questions tree to the file with the given name
	 * @param filename The name of the file to save to
	 * @throws FileNotFoundException If the file cannot be used to save the tree
	 */
	public void save( String filename ) throws FileNotFoundException
	{
		File toWrite = new File(filename);
		PrintWriter writer = new PrintWriter(toWrite);
		//Get the linkedlist containing the nodes inorder
		LinkedList<TQNode> nodes = traversePreorder();
		TQNode node;
		//While the linkedlist contains nodes to save
		while (nodes.size()>0){
			//Get the next node
			node = nodes.poll();
			//Save in proper format (question or answer?)
			if (!isLeaf(node)){
				writer.println("Q:"+node.getData());
			}
			else{
				writer.println("A:"+node.getData());
			}
		}
		writer.close();
	}


	/** Print a level-order traversal of the tree to standard out (System.out)
	 * */ 
	public void print()
	{
		//Get a list of nodes in level order
		LinkedList<TQNode> nodes = traverseLevelOrder();
		TQNode node;
		//While the linkedlist contains nodes to print
		while (nodes.size()>0){	
			node = nodes.poll();
			int i = node.getIndex();
			//If statement to prevent nullpointer exceptions
			if (!isLeaf(node)){
				int iY = node.getYesChild().getIndex();
				int iN = node.getNoChild().getIndex();
				//Print with index of current node and the children
				System.out.printf("%d:   '%s'   no: (%d)   yes: (%d)\n", i, node.getData(),
						iN, iY);
			}
			else
				System.out.printf("%d:   '%s'   no: (null)   yes: (null)\n",
						i, node.getData());
		}
	}






	// PRIVATE HELPER METHODS
	private void buildDefault(){
		TQNode rootNode = new TQNode("Is it bigger than a breadbox?");
		TQNode yesAnswer = new TQNode("A Computer Scientist");
		TQNode noAnswer = new TQNode("Spam");
		rootNode.setYesChild(yesAnswer);
		rootNode.setNoChild(noAnswer);
		this.root = rootNode;
	}

	//Bleh, just a helper method to put responses in the correct format
	private String fixString(String toFix, boolean isQuestion){
		if (toFix.length() == 0){
			if (isQuestion) return toFix+="?";
			else return toFix;
		}
			if (isQuestion){
				if (toFix.charAt(toFix.length()-1) != '?')
					toFix+="?";
				if (!Character.isUpperCase(toFix.charAt(0))){
					char[] charArray = toFix.toCharArray();
					charArray[0] = Character.toUpperCase(charArray[0]);
					toFix = String.valueOf(charArray);
				}
			}
			else{
				if (toFix.charAt(0)=='A'){
					char[] charArray = toFix.toCharArray();
					charArray[0] = Character.toLowerCase(charArray[0]);
					toFix = String.valueOf(charArray);
				}
			}
		return toFix;
	}



	//Really helpful, though exactly what it says on the tin
	private boolean isLeaf(TQNode toCheck){
		if (toCheck.getNoChild() == null && toCheck.getYesChild() == null)
			return true;
		else
			return false;
	}

	//Also a bit helpful
	private void addAt(TQNode toAdd, TQNode addAt, boolean isYes){
		if (isYes) {
			addAt.setYesChild(toAdd);
		}
		else {
			addAt.setNoChild(toAdd);
		}
	}


	//Adds in a new answer, with a new question, the new answer to that question
	//and the old answer to the new question (one of them yes, the other no). Used
	//when the user thinks of something not in the tree itself
	private void addNewQuestion(String newQuestion, String newAnswer,
			boolean isNewAnswerYes, TQNode oldQuestion, 
			boolean wasOldAnswerYes){
		TQNode question = new TQNode(newQuestion);
		TQNode answer = new TQNode(newAnswer);
		addAt(answer, question, isNewAnswerYes);
		if (wasOldAnswerYes)
			addAt(oldQuestion.getYesChild(), question, !isNewAnswerYes);
		else
			addAt(oldQuestion.getNoChild(), question, !isNewAnswerYes);
		addAt(question, oldQuestion, wasOldAnswerYes);
	}

	//Returns true if response is affirmative, false if otherwise
	private boolean readResponse(String response){
		response = response.trim();
		if (response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes"))
			return true;
		else
			return false;
	}

	//Iterative version, returns a queue containing a preorder list of the
	//elements in the tree, for use in the save() method
	private LinkedList<TQNode> traversePreorder(){
		LinkedList<TQNode> theStack = new LinkedList<TQNode>();
		LinkedList<TQNode> toReturn = new LinkedList<TQNode>();
		theStack.push(this.root);
		while (theStack.size()>0){
			TQNode currentNode = theStack.pop();
			toReturn.add(currentNode);
			if (!isLeaf(currentNode)){
				theStack.push(currentNode.getYesChild());
				theStack.push(currentNode.getNoChild());
			}
		}
		return toReturn;
	}

	//Iterative version, returns a Queue that contains a list of all the nodes
	//in the tree in level-order, for use in the print() method
	private LinkedList<TQNode> traverseLevelOrder(){
		LinkedList<TQNode> theQueue = new LinkedList<TQNode>();
		LinkedList<TQNode> toReturn = new LinkedList<TQNode>();
		int indexCounter = 0;
		theQueue.add(this.root);
		while (theQueue.size()>0){
			TQNode currentNode = theQueue.poll();
			currentNode.setIndex(indexCounter);
			indexCounter++;
			toReturn.add(currentNode);
			if (!isLeaf(currentNode)){
				theQueue.add(currentNode.getNoChild());
				theQueue.add(currentNode.getYesChild());
			}
		}
		return toReturn;
	}

	/** Recursive method to build a TQTree by reading from a file.
	 * @param reader A LineNumberReader that reads from the file
	 * @return The TQNode at the root of the created tree
	 * @throws ParseException If the file format is incorrect
	 */
	private TQNode buildSubtree( LineNumberReader reader ) throws ParseException 
	{

		String line;
		try {
			line = reader.readLine();
		}
		catch ( IOException e ) {
			throw new ParseException( "Error reading tree from file: " + e.getMessage(),
					reader.getLineNumber() );
		}

		if ( line == null ) {
			// We should never be calling this method if we don't have any more input
			throw new ParseException( "End of file reached unexpectedly", reader.getLineNumber() );
		}

		String[] lineSplit = line.split( ":", 2 );
		String qOrA = lineSplit[0];
		String data = lineSplit[1];

		TQNode subRoot = new TQNode( data );
		if ( qOrA.equals( "Q" ) ) {
			subRoot.setNoChild( buildSubtree( reader ) );
			subRoot.setYesChild( buildSubtree( reader ) );
		}    
		return subRoot;
	}


}
