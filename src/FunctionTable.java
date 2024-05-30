/**
 * FunctionTable.java
 * Manages function forward references to functions within a set of .VM files
 * by Charles Stevenson (brucesdad13@gmail.com)
 * Revision History:
 * 2024-05-30: Initial version
 */
import java.util.*;
public class FunctionTable {
    /**
     * Map functionName to filePrefix (e.g. for foo.vm filePrefix = foo)
     */
    private final Map<String, String> table = new HashMap<>() {};

    /**
     * Add a new function mapping to the table
     * @param functionName the function to add
     * @param filePrefix the address of the function
     */
    public void addEntry(String functionName, String filePrefix ) {
        table.put(functionName, filePrefix);
    }

    /**
     * Does the function table contain the given function?
     * @param functionName the function to check
     * @return boolean true if the function is in the table
     */
    public boolean contains(String functionName) {
        return table.containsKey(functionName);
    }

    /**
     * Get the file that defines the given function
     * @param functionName the function to get the address of
     * @return String the .VM file that defines the function
     */
    public String getFile(String functionName) {
        return table.get(functionName);
    }

    /**
     * Print the function table
     */
    public void printTable() {
        // print function table sorted by value numerically
        table.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(System.out::println);
    }
}
