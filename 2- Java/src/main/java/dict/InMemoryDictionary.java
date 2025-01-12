package dict;

import java.io.*;
import java.util.TreeMap;

/**
 * Implements a persistent dictionary that can be held entirely in memory.
 * When flushed, it writes the entire dictionary back to a file.
 * <p>
 * The file format has one keyword per line:
 * <pre>word:def</pre>
 * <p>
 * Note that an empty definition list is allowed (in which case the entry would have the form: <pre>word:</pre>
 *
 * @author talm
 */
public class InMemoryDictionary extends TreeMap<String, String> implements PersistentDictionary {
    private static final long serialVersionUID = 1L; // (because we're extending a serializable class)

    private final File dictFile;

    public InMemoryDictionary(File dictFile) { //constructor
        if (dictFile == null) {
            throw new IllegalArgumentException("File cannot be null.");
        }
        this.dictFile = dictFile;
    }

    @Override
    public void open() throws IOException {
        this.clear();   //removes all key value pairs in the current instance of the dictionary
        if (dictFile.exists() == false) return;

        try (BufferedReader read = new BufferedReader(new FileReader(dictFile))) { //opens file for reading and closes when it ends.
            String line;
            while ((line = read.readLine()) != null) {
                int index = line.indexOf(':');

                if (index >= 0) {
                    String key = line.substring(0, index); //takes string from to index -1
                    String value = line.substring(index + 1); //takes string right after the ':'
                    put(key, value); //put came from TreeMap.
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        try (BufferedWriter write = new BufferedWriter(new FileWriter(dictFile))) {  //opens file for writing and closes when it ends.

            for (String key : this.keySet()) { //goes through all the keys.
                write.write(key + ":" + this.get(key)); //writes the key:value
                write.newLine();
            }

        }
    }
}





 /*	readLine(): Reads one line of text from the file. This allows the program to process each line as a String.
 	1.	readLine():
	•	Reads an entire line of text.
	•	Returns null when the end of the stream is reached.
		2.	read():
	•	Reads a single character or an array of characters.
	•	Returns -1 if the end of the stream is reached.
	3.	close():
	•	Closes the reader and releases resources.

put(K key, V value), get(Object key), remove(Object key), firstKey(), lastKey(), higherKey(K key), keySet(), entrySet(), values()

TreeMap<String, String> map = new TreeMap<>();
map.put("dog", "animal");
map.put("cat", "animal");
map.put("bird", "animal");
System.out.println(map.get("cat")); // Output: animal
System.out.println(map.size());    // Output: 3
for (Map.Entry<String, String> entry : map.entrySet()) {
    System.out.println(entry.getKey() + " -> " + entry.getValue());
}
// Output:
// bird -> animal
// cat -> animal
// dog -> animal
System.out.println(map.firstKey());  // Output: bird
System.out.println(map.lastKey());   // Output: dog
System.out.println(map.higherKey("cat")); // Output: dog
System.out.println(map.lowerKey("dog"));  // Output: cat   */