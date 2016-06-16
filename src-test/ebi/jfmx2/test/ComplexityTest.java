package ebi.jfmx2.test;

import static org.junit.Assert.*;

import java.io.File;
import java.net.MalformedURLException;

import org.junit.BeforeClass;
import org.junit.Test;

import ebi.jfmx2.EBIJavaTest;
import ebi.jfmx2.Trie;
import ebi.jfmx2.EBIJavaTest.FileLineProcessor;

/**
 * Test the execution times of the Trie and the InChI Matching
 * @author juanfmx2@gmail.com
 */
public class ComplexityTest {
	
	/**
	 * Sets the default URLs to point to local files
	 * to reduce networking interference with the execution time
	 */
	@BeforeClass
	public static void setup(){
		try {
			File dictFile = new File("./data/cache/dictionary.txt");
			if(!dictFile.exists()){
				fail("To reduce networking interference, please download the dictionary file and place it in ./data/cache/dictionary.txt");
			}
			File chemFile = new File("./data/cache/chembl_21_chemreps.txt.gz");
			if(!chemFile.exists()){
				fail("To reduce networking interference, please download the chem database file and place it in ./data/cache/chembl_21_chemreps.txt.gz");
			}
			EBIJavaTest.dictionary_url = dictFile.toURI().toURL().toExternalForm();
			EBIJavaTest.chem_data_url = chemFile.toURI().toURL().toExternalForm();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Tests that the execution time to load of the Trie is not bigger than:
	 * trfo+ecpo*lines*lwl*Math.log(27+1)
	 * where:
	 *  - trfo  -> Time Reading the File Only
	 *  - ecpo  -> Estimated Cost Per Operation = (trfo/lines)
	 *  - lines -> The number of lines in the file
	 *  - lwl   -> Longest Word Length in the dictionary
	 *  - lwl*Math.log(27+1) is the complexity of searching the Trie
	 */
	@Test
	public void testDictionaryComplexity() {
		try {
			
			final Trie dict = new Trie();
			// measures the Trie load execution time
			long timeLoadingDict = System.currentTimeMillis();
			EBIJavaTest.readFileByLines(EBIJavaTest.dictionary_url, false, new FileLineProcessor() {
				
				@Override
				public void processFileLine(String line, int lineNum) throws Exception {
					dict.addWord(line);
				}
			});
			timeLoadingDict = System.currentTimeMillis()-timeLoadingDict;
			
			// measures the file only execution time
			long timeLoadingFileOnly = System.currentTimeMillis();
			int lines = EBIJavaTest.readFileByLines(EBIJavaTest.dictionary_url, false, null );
			timeLoadingFileOnly = System.currentTimeMillis()-timeLoadingFileOnly;
			
			// Estimates and validates the execution times
			double estCostOps = ((double)timeLoadingFileOnly)/((double)lines);
			double estimatedCost = timeLoadingFileOnly+estCostOps*lines*dict.getLongestWordLenght()*(Math.log(27+1));
			System.out.println(String.format("Estimated max time: %s",estimatedCost));
			assertTrue(String.format("The excution time should not be more than %1$s, but it was  %2$s.",estimatedCost,timeLoadingDict), timeLoadingDict<estimatedCost);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Should not go into exception!");
		}
	}

	/**
	 * Tests that the execution time to find matches is not bigger than:
	 * trfo+ecpo*lines*li*lwl*Math.log(27+1)
	 * where:
	 *  - trfo  -> Time Reading the File Only
	 *  - ecpo  -> Estimated Cost Per Operation = (trfo/lines)
	 *  - lines -> The number of lines in the file
	 *  - li    -> Lenght of an InChI key
	 *  - lwl   -> Longest Word Length in the dictionary
	 *  - lwl*Math.log(27+1) is the complexity of searching the Trie
	 */
	@Test
	public void testFindMatchesComplexity() {
		try {
			// measures the search execution time
			EBIJavaTest test = new EBIJavaTest(10);
			long timeSearching = System.currentTimeMillis();
			test.findMatches();
			timeSearching = System.currentTimeMillis()-timeSearching;

			// measures the file only execution time
			long timeLoadingFileOnly = System.currentTimeMillis();
			int lines = EBIJavaTest.readFileByLines(EBIJavaTest.chem_data_url, true, null );
			timeLoadingFileOnly = System.currentTimeMillis()-timeLoadingFileOnly;

			// Estimates and validates the execution times
			int longIchiKey = 27;
			double estCostOps = ((double)timeLoadingFileOnly)/((double)lines);
			double estimatedCost = timeLoadingFileOnly+estCostOps*lines*longIchiKey*test.getTrieDictionary().getLongestWordLenght()*(Math.log(27+1));
			System.out.println(String.format("Estimated max time: %s",estimatedCost));
			assertTrue(String.format("The excution time should not be more than %1$s, but it was  %2$s.",estimatedCost,timeSearching), timeSearching<estimatedCost);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Should not go into exception!");
		}
	}

}
