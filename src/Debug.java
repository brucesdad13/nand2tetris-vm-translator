/**
 * Generic Debug class for printing to the console
 * by Charles Stevenson (brucesdad13@gmail.com)
 * Revision History:
 * 2024-05-24: Initial version
 */
public class Debug {
    public static boolean DEBUG_MODE = true; // set to true to enable debugging

    /**
     * Print a message to the console
     * @param message the message to print
     */
    public static void print(String message) {
        if (DEBUG_MODE) System.out.print(message);
    }

    /**
     * Print a message to the console with a newline
     * @param message the message to print
     */
    public static void println(String message) {
        if (DEBUG_MODE) System.out.println(message);
    }
}
