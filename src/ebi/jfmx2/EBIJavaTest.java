package ebi.jfmx2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;


public class EBIJavaTest {

	public interface FileLineProcessor{
		public void processFileLine(String line, int lineNum) throws Exception;
	}
	
	public class Match implements Comparable<Match>{
		private Integer lenght;
		private String result;
		
		public Match(Integer lenght, String result) {
			super();
			this.lenght = lenght;
			this.result = result;
		}

		public Integer getLenght() {
			return lenght;
		}

		public String getResult() {
			return result;
		}

		@Override
		public int compareTo(Match o) {
			if(o == null)
				return 1;
			return this.lenght.compareTo(o.lenght);
		}
	}
	
	
	public static void readFileByLines(String strUrl,boolean isGzip,FileLineProcessor lineHandler) throws IOException,Exception{
		InputStream is = null;
		try {
			URL url = new URL(strUrl);
			is = url.openStream();
			if(isGzip)
				is = new GZIPInputStream(is);
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String inputLine;
			long time = System.currentTimeMillis();
			int lineNum = 0;
			while ((inputLine = in.readLine()) != null){
				if(lineHandler != null)
					lineHandler.processFileLine(inputLine, lineNum);
				lineNum++;
			}
			time = System.currentTimeMillis()-time;
			System.out.println(String.format("URL: %1$s\nPROCESSED IN: %2$s ms",strUrl,time));
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
	}
	
	public static final String chem_data_url="http://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBLdb/latest/chembl_21_chemreps.txt.gz";
	public static final String dictionary_url="https://raw.githubusercontent.com/jonbcard/scrabble-bot/master/src/dictionary.txt";
	
	private int numberOfWords;
	private Trie trieDictionary;
	
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
	
	public void findMatches() throws IOException, Exception{
		try{
			final TreeSet<Match> matches = new TreeSet<>();
			final Trie dict = this.trieDictionary;
			EBIJavaTest.readFileByLines(chem_data_url, true, new FileLineProcessor() {
				
				@Override
				public void processFileLine(String line, int lineNum) throws Exception {
					if(lineNum != 0){
						String[] parts = line.trim().split("\t");
						if(parts.length >= 4){
							String wordSbtrOf = dict.longestWordSubstringOf(parts[3]);
							if(wordSbtrOf != null){
								matches.add(new Match(wordSbtrOf.length(), String.format("%1$s, %2$s, %3$s", parts[3], wordSbtrOf, parts[0])));
							}
						}
						else{
							System.err.println(String.format("WARNING! LINE %2$s COULD NOT BE PROCESSED:\n%1$s",line,lineNum));
						}
					}
				}
			});
			Iterator<Match> it = matches.descendingIterator();
			for (int i = 0; i < this.numberOfWords && it.hasNext(); i++) {
				System.out.println(it.next().getResult());
			}
		}catch(Exception e){
			throw new Exception("It is not possible to continue because it was not possible to process the ChEMBL database!",e);
		}
	}
	
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
