/**
 * Hack VM Translator Parser class
 * by Charles Stevenson (brucesdad13@gmail.com)
 * Revision History:
 * 2024-05-24: Initial version
 */

import java.nio.file.*;
import java.io.*;

public class Parser {
    private BufferedReader reader = null;
    private String line = null;

    private String currentCommand = null;
    public static final int C_ARITHMETIC = 0; // arithmetic command
    public static final int C_PUSH = 1; // push command
    public static final int C_POP = 2; // pop command
    public static final int C_LABEL = 3; // label command
    public static final int C_GOTO = 4; // goto command
    public static final int C_IF = 5; // if-goto command
    public static final int C_FUNCTION = 6; // function command
    public static final int C_RETURN = 7; // return command
    public static final int C_CALL = 8; // call command

    /**
     * Open the input file and get ready to parse it
     * @param filename the name of the file to open
     */
    public Parser(String filename) {
        // Ensure the input file exists and is writable
        Path file = Paths.get(filename);
        InputStream input;
        // open the input file for reading
        try
        {
            file.getFileSystem().provider().checkAccess(file, AccessMode.READ); // check access
            input = Files.newInputStream(file);
            this.reader = new BufferedReader(new InputStreamReader(input));
        }
        catch (IOException e)
        {
            System.out.println("Hack VM Translator Input File I/O Exception: " + e);
        }
    }

    /**
     * Check the file for additional commands
     * @return boolean true if there are more commands, false if not
     */
    boolean hasMoreCommands() {
        try
        {
            while ((line = reader.readLine()) != null) // while not end of file
            {
                // remove comments
                line = line.replaceAll("//.*", "");

                // remove leading and trailing whitespace
                line = line.replaceAll("^\\s+|\\s+$", "");

                if (line.isEmpty()) continue; // ignore empty lines

                return true;
            }
        }
        catch (IOException e)
        {
            System.out.println("Hack VM Translator Input File I/O Exception: " + e);
        }
        return false;
    }

    /**
     * Advance to the next command in the file by setting
     * the currentCommand to the most recent command line
     * processed by hasMoreCommands()
     */
    void advance() {
        currentCommand = line; // line already has the next command
    }

    /**
     * Get the type of command per API
     * @return int the type of command
     */
    int commandType() {
        if (currentCommand.contains("push"))
            return C_PUSH;
        else if (currentCommand.contains("pop"))
            return C_POP;
        else if (currentCommand.contains("label"))
            return C_LABEL;
        else if (currentCommand.contains("goto"))
            return C_GOTO;
        else if (currentCommand.contains("if-goto"))
            return C_IF;
        else if (currentCommand.contains("function"))
            return C_FUNCTION;
        else if (currentCommand.contains("return"))
            return C_RETURN;
        else if (currentCommand.contains("call"))
            return C_CALL;
        else
            return C_ARITHMETIC; // add, sub, neg, eq, gt, lt, and, or, not
    }

    /**
     * Get the first argument of the current command
     * @return String the first argument
     */
    public String arg1() {
        // Should not be called if the command is C_RETURN
        if (commandType() == C_RETURN)
            throw new IllegalArgumentException("Command is a return command");
        else if (commandType() == C_ARITHMETIC)
            return currentCommand;
        else
            return currentCommand.split(" ")[1];
    }

    /**
     * Get the second argument of the current command
     * @return int the second argument
     */
    public int arg2() {
        // Should only be called if the command is C_PUSH, C_POP, C_FUNCTION, or C_CALL
        if (commandType() != C_PUSH && commandType() != C_POP && commandType() != C_FUNCTION && commandType() != C_CALL)
            throw new IllegalArgumentException("Command is not a push, pop, function, or call command");
        return Integer.parseInt(currentCommand.split(" ")[2]); // FIXME: handle arbitrary whitespace
    }

    /**
     * Close the input file
     */
    public void close() {
        try
        {
            reader.close();
        }
        catch (IOException e)
        {
            System.out.println("I/O Exception: " + e);
        }
    }
}
