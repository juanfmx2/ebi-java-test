package ebi.jfmx2;

import java.util.TreeMap;

/**
 * Trie tree implementation
 * @author juanfmx2@gmail.com
 */
public class Trie {

	/**
	 * Trie node for one character
	 * @author juanfmx2@gmail.com
	 */
	public class TrieNode {
		
		// The character corresponding with this node
		private char nodeChar;
		// The total String with all the characters from the root of the Trie
		private String totalStr;
		// If set to true the current node represents a word
		private boolean isWord;
		public void setIsWord(boolean isWord){this.isWord = isWord;}
		// The parent node of this node
		private TrieNode parent;
		public TrieNode getParent(){return this.parent;}
		// The children nodes of the current node
		private TreeMap<Character,TrieNode> children;
		
		/**
		 * Default constructor
		 * @param nodeChar - the character of the current node
		 * @param parent - the parent node
		 */
		private TrieNode(char nodeChar, TrieNode parent) {
			super();
			this.nodeChar = nodeChar;
			this.totalStr = (parent!=null?parent.totalStr:"")+this.nodeChar;
			this.isWord = false;
			this.parent = parent;
			this.children = new TreeMap<>();
		}
		
		/**
		 * Adds a word recursively to the child nodes
		 * @param word - word to include on the Trie
		 */
		public void addWord(String word){
			if(word.length() == 0)
				this.setIsWord(true);
			else{
				char nextChar = word.charAt(0);
				if(!this.children.containsKey(nextChar))
					this.children.put(nextChar, new TrieNode(nextChar, this));
				this.children.get(nextChar).addWord(word.substring(1));
			}
		}

		/**
		 * Checks if in the current TrieNode and its children
		 * there is an specific word
		 * @param word - word to search
		 * @return true if it is contained by this TrieNode and its children, false otherwise
		 */
		public boolean hasWord(String word){
			TrieNode curNode = this;
			for (int i = 0; i < word.length(); i++) {
				char cI = word.charAt(i);
				if(!curNode.children.containsKey(cI))
					return false;
				curNode = curNode.children.get(cI);
			}
			return curNode.isWord;
		}

		/**
		 * Checks which is the longest word in the TrieNode and its
		 * children that is a substring of a specific word
		 * @param word - word to check
		 * @return The longest word found, null otherwise
		 */
		public String longestWordSubstringOf(String word){
			TrieNode curNode = this;
			String lastFound = null;
			for (int i = 0; i < word.length(); i++) {
				char cI = word.charAt(i);
				if(!curNode.children.containsKey(cI))
					return curNode.isWord?curNode.totalStr:lastFound;
				curNode = curNode.children.get(cI);
				if(curNode.isWord)
					lastFound = curNode.totalStr;
			}
			return curNode.isWord?curNode.totalStr:lastFound;
		}
		
	}
	
	//The first level of the Trie
	private TreeMap<Character,TrieNode> firstLevel;
	//Indicates wether case should be ignored
	private boolean ignoreCase;
	//Indicates which is the length of the longest word in the Trie
	private int longestWordLenght;
	public int getLongestWordLenght(){return this.longestWordLenght;}

	/**
	 * default constructor, ignores case by default
	 */
	public Trie(){
		this.firstLevel = new TreeMap<>();
		this.ignoreCase = true;
		this.longestWordLenght = 0;
	}
	
	/**
	 * @param ignoreCase - indicates whether case should be ignored or not
	 *                     true indicates it should be ignored.
	 */
	public Trie(boolean ignoreCase){
		this.firstLevel = new TreeMap<>();
		this.ignoreCase = ignoreCase;
		this.longestWordLenght = 0;
	}

	/**
	 * Adds a word to thr Trie
	 * @param word - word to include on the Trie
	 */
	public void addWord(String word){
		if(word == null || word.length() == 0)
			System.out.println("Unvalid empty word for Trie!");
		else{
			if(this.ignoreCase)
				word = word.toUpperCase();
			if(word.length() > this.longestWordLenght)
				this.longestWordLenght = word.length();
			char nextChar = word.charAt(0);
			if(!this.firstLevel.containsKey(nextChar))
				this.firstLevel.put(nextChar, new TrieNode(nextChar, null));
			this.firstLevel.get(nextChar).addWord(word.substring(1));
		}
	}

	/**
	 * Checks if Trie contains a specific word
	 * @param word - word to search
	 * @return true if it is contained by this TrieNode and its children, false otherwise
	 */
	public boolean hasWord(String word){
		if(word == null || word.length() == 0)
			return false;
		if(this.ignoreCase)
			word = word.toUpperCase();
		TrieNode node = this.firstLevel.get(word.charAt(0));
		return (node != null)? node.hasWord(word.substring(1)):false;
	}

	/**
	 * Checks which is the longest word in the Trie 
	 * that is a substring of a specific word
	 * @param word - word to check
	 * @return The longest word found, null otherwise
	 */
	public String longestWordSubstringOf(String wordToTest){
		if(wordToTest == null || wordToTest.length() == 0)
			return null;
		if(this.ignoreCase)
			wordToTest = wordToTest.toUpperCase();
		String curWordFound = null;
		int curWFLenght = 0;
		String curWord = wordToTest;
		while(curWord.length() > curWFLenght){
			TrieNode node = this.firstLevel.get(curWord.charAt(0));
			String nextFound = (node != null)? node.longestWordSubstringOf(curWord.substring(1)):null;
			if(nextFound != null && nextFound.length() > curWFLenght){
				curWordFound = nextFound;
				curWFLenght = nextFound.length();
			}
			curWord = curWord.substring(1);
		}
		return curWordFound;
	}
}
