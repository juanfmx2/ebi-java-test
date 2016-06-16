package ebi.jfmx2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

/**
 * Class to implement the Java Developer Test for EMBL-EBI
 * @author juanfmx2@gmail.com
 */
public class EBIJavaTest {

	//-----------------------------------------------------------------------
	// Util Classes
	//-----------------------------------------------------------------------
	
	/**
	 * Interface used to define a function that should process each line of
	 * a file while is being read
	 * @author juanfmx2@gmail.com
	 */
	public interface FileLineProcessor{
		public void processFileLine(String line, int lineNum) throws Exception;
	}
	
	/**
	 * Represents a match of an InChI key with a word in the dictionary
	 * @author juanfmx2@gmail.com
	 */
	public class Match implements Comparable<Match>{
		//Represents the length of the word from the dictionary
		private Integer length;
		public Integer getLenght() {return length;}
		//Contains the string that should be printed in the console
		private String result;
		public String getResult() {return result;}
		
		/**
		 * default constructor
		 * @param lenght - the length of the word from the dictionary
		 * @param result - the string that should be printed in the console
		 */
		public Match(Integer lenght, String result) {
			super();
			this.length = lenght;
			this.result = result;
		}

		@Override
		public int compareTo(Match o) {
			if(o == null)
				return 1;
			return this.length.compareTo(o.length);
		}
	}
	
	//-----------------------------------------------------------------------
	// Static Context
	//-----------------------------------------------------------------------
	
	/**
	 * Helper method to read URL's line by line
	 * @param strUrl - the URL to read
	 * @param isGzip - indicates whether or not the GZIPInputStream should be 
	 *                 used, true indicates it should be used
	 * @param lineHandler - interface that is in charge of processing each one of
	 *                      of the lines of the file
	 * @return the number of lines processed
	 * @throws IOException - if there is a networking or other IO exception
	 * @throws Exception - in case something else prevents the URL from being read
	 */
	public static int readFileByLines(String strUrl,boolean isGzip,FileLineProcessor lineHandler) throws IOException,Exception{
		InputStream is = null;
		int lineNum    = 0;
		long time = System.currentTimeMillis();
		try {
			URL url = new URL(strUrl);
			is = url.openStream();
			if(isGzip)
				is = new GZIPInputStream(is);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String inputLine;
			while ((inputLine = in.readLine()) != null){
				if(lineHandler != null)
					lineHandler.processFileLine(inputLine, lineNum);
				lineNum++;
			}
		} catch (IOException e) {
			throw new IOException(String.format("It is not possible to read the URL with isGzip set to %2$s: %1$s",strUrl,isGzip));
		} catch (Exception e) {
			throw e;
		}finally {
			try{
				is.close();
			} catch (Exception e) {
				//ignored
			}
		}
		time = System.currentTimeMillis()-time;
		System.out.println(String.format("URL: %1$s\nPROCESSED IN: %2$s ms",strUrl,time));
		return lineNum;
	}
	// Constant to define the location of the chem database file
	public static String chem_data_url="http://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBLdb/latest/chembl_21_chemreps.txt.gz";

	// Constant to define the location of the dictionary file
	public static String dictionary_url="https://raw.githubusercontent.com/jonbcard/scrabble-bot/master/src/dictionary.txt";
	
	//-----------------------------------------------------------------------
	// Instance Context
	//-----------------------------------------------------------------------
	
	// Number of word to be searched
	private int numberOfWords;
	// Trie dictionary
	private Trie trieDictionary;
	public Trie getTrieDictionary() {return trieDictionary;}
	
	/**
	 * Downloads the dictionary file, loads it in the Trie and
	 * creates an instance of the searcher
	 * @param numberOfWords - the number of words to search for
	 * @throws Exception - if the dictionary can not be loaded
	 */
	public EBIJavaTest(int numberOfWords) throws Exception{
		this.numberOfWords = numberOfWords;
		this.trieDictionary = new Trie();
		try{
			final Trie dict = this.trieDictionary;
			EBIJavaTest.readFileByLines(dictionary_url, false, new FileLineProcessor() {
				
				@Override
				public void processFileLine(String line, int lineNum) throws Exception {
					dict.addWord(line);
				}
			});
		}catch(Exception e){
			throw new Exception("It is not possible to continue because it was not possible to load the dictionary!",e);
		}
	}

	/**
	 * Downloads,unzips and reads the file line by line, searching for 
	 * each one of the components a word that is a substring of the InChI key
	 * keeping in memory only a maximum of this.numberOfWords
	 * @throws IOException - if there is a networking or other IO exception
	 * @throws Exception - in case something else prevents the search from being completed
	 */
	public void findMatches() throws IOException, Exception{
		try{
			final int numberOfWords = this.numberOfWords;
			final ArrayList<Match> matches = new ArrayList<Match>();
			final Trie dict = this.trieDictionary;
			EBIJavaTest.readFileByLines(chem_data_url, true, new FileLineProcessor() {
				
				@Override
				public void processFileLine(String line, int lineNum) throws Exception {
					if(lineNum != 0){
						String[] parts = line.trim().split("\t");
						if(parts.length >= 4){
							String wordSbtrOf = dict.longestWordSubstringOf(parts[3]);
							if(wordSbtrOf != null){
								Match mI = new Match(wordSbtrOf.length(), String.format("%1$s, %2$s, %3$s", parts[3], wordSbtrOf, parts[0]));
								int pos = Collections.binarySearch(matches, mI);
								if (pos < 0) 
									matches.add(-pos-1, mI);
								else
									matches.add(pos, mI);
								// keeps the size fixed to only the numberOfWords
								if(matches.size()>numberOfWords)
									matches.remove(0);
							}
						}
						else{
							System.err.println(String.format("WARNING! LINE %2$s COULD NOT BE PROCESSED:\n%1$s",line,lineNum));
						}
					}
				}
			});
			//Prints out in descending order
			Collections.reverse(matches);
			for(Match mI:matches)
				System.out.println(mI.getResult());
		}catch(Exception e){
			throw new Exception("It is not possible to continue because it was not possible to process the ChEMBL database!",e);
		}
	}
	

	//-----------------------------------------------------------------------
	// main
	//-----------------------------------------------------------------------
	
	/**
	 * Runs the application
	 * @param args - $1 specifies the number InChI to match, if omitted it will be 10
	 */
	public static void main(String[] args){
		try{
			int n = 10;
			try{
				n = Integer.parseInt(args[0].trim());
			}catch(Exception e){}
			EBIJavaTest test = new EBIJavaTest(n);
			test.findMatches();
		} catch(Exception e){
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}
	
}
