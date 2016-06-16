# EBI Java Developer Test
This application solves the problem stated in https://gist.github.com/mnowotka/ea119ab19add7cddbcfa9b01d0b55c7b 

#How to use
To execute the application please go to ./build and execute:

    java -jar inchi.jar [N]

Where N is an optional parameter used to indicate how many matches should the program search for.

The jar was compiled by Eclipse and is targeted for java 1.7.

#Complexity
To reduce dictionary matching times a Trie was used. A Trie search complexity is O(M). Where M is the length of the longest word in the dictionary. However, to reduce the space usage instead of hash tables the implementation uses Treemap. Which results in a complexity of O(M*log(MAX_CHARS+1)) for a singular search on the Trie. Where MAX_CHARS is the maximum amount of characters that define the words included in the Trie.

In order to use the Trie to realize matching with each InChI key. The Trie has to be searchd in the worst case as many times as the key has characters. For this reason the complexity of matching and finding a word that is a substring of and InChI key is O(INCHI_KEY_LENGTH*M*log(MAX_CHARS+1)). Which lead to a final complexity of O(N*INCHI_KEY_LENGTH*M*log(MAX_CHARS+1)). Where N is the number of lines in the chemical components database.

#How to test
Import the project in Eclipse and go to the package ebi.jfmx2.test and Run the ComplexityTest.java file as JUnit test.

Don't forget to include the dictionary and the chem database in the following paths to reduce networking interference during the test.

    ./data/cache/dictionary.txt
    ./data/cache/chembl_21_chemreps.txt.gz
