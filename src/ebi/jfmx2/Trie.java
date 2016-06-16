package ebi.jfmx2;

import java.util.TreeMap;

public class Trie {

	public class TrieNode {
		
		private char nodeChar;
		private String cumulatedStr;
		private boolean isWord;
		private TrieNode parent;
		private TreeMap<Character,TrieNode> childs;
		
		public TrieNode(char nodeChar, TrieNode parent) {
			super();
			this.nodeChar = nodeChar;
			this.cumulatedStr = (parent!=null?parent.cumulatedStr:"")+this.nodeChar;
			this.isWord = false;
			this.parent = parent;
			this.childs = new TreeMap<>();
		}
		
		public void setIsWord(boolean isWord){
			this.isWord = isWord;
		}
		
		public TrieNode getParent(){
			return this.parent;
		}
		
		public void addWord(String word){
			if(word.length() == 0)
				this.setIsWord(true);
			else{
				char nextChar = word.charAt(0);
				if(!this.childs.containsKey(nextChar))
					this.childs.put(nextChar, new TrieNode(nextChar, this));
				this.childs.get(nextChar).addWord(word.substring(1));
			}
		}

		public boolean hasWord(String word){
			TrieNode curNode = this;
			for (int i = 0; i < word.length(); i++) {
				char cI = word.charAt(i);
				if(!curNode.childs.containsKey(cI))
					return false;
				curNode = curNode.childs.get(cI);
			}
			return curNode.isWord;
		}

		public String longestWordSubstringOf(String word){
			TrieNode curNode = this;
			String lastFound = null;
			for (int i = 0; i < word.length(); i++) {
				char cI = word.charAt(i);
				if(!curNode.childs.containsKey(cI))
					return curNode.isWord?curNode.cumulatedStr:lastFound;
				curNode = curNode.childs.get(cI);
				if(curNode.isWord)
					lastFound = curNode.cumulatedStr;
			}
			return curNode.isWord?curNode.cumulatedStr:lastFound;
		}
		
	}
	
	private TreeMap<Character,TrieNode> firstLevel;
	private boolean ignoreCase;

	public Trie(){
		this.firstLevel = new TreeMap<>();
		this.ignoreCase = true;
	}
	
	public Trie(boolean ignoreCase){
		this.firstLevel = new TreeMap<>();
		this.ignoreCase = ignoreCase;
	}
	
	public void addWord(String word){
		if(word == null || word.length() == 0)
			System.out.println("Unvalid empty word for Trie!");
		else{
			if(this.ignoreCase)
				word = word.toUpperCase();
			char nextChar = word.charAt(0);
			if(!this.firstLevel.containsKey(nextChar))
				this.firstLevel.put(nextChar, new TrieNode(nextChar, null));
			this.firstLevel.get(nextChar).addWord(word.substring(1));
		}
	}

	public boolean hasWord(String word){
		if(word == null || word.length() == 0)
			return false;
		if(this.ignoreCase)
			word = word.toUpperCase();
		TrieNode node = this.firstLevel.get(word.charAt(0));
		return (node != null)? node.hasWord(word.substring(1)):false;
	}

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
