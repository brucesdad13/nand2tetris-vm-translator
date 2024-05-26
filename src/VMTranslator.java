/**
 * VMTranslator.java
 * Project 7: VM Translator Stage I - Handling Stack Arithmetic Commands
 * by Charles Stevenson (brucesdad13@gmail.com)
 * Revision History:
 * 2024-05-25: Initial version
 */

import java.io.*;
import java.nio.file.*;

public class VMTranslator {
    public static void main (String[] args) {
        // Ensure the input file and output file are provided as command line arguments
        if (args.length != 1) {
            System.out.println("Usage: java VMTranslator [<input file>.vm or <directory>]");
            System.out.println("Output file will be generated in the same directory as the input file named <input file>.asm");
            return;
        }

        String inputFileName = args[0];
        String outputFileName = "";
        CodeWriter codewriter = null;

        // If the program's argument is a directory, process all .vm files in the directory
        File input = new File(inputFileName);
        if (input.isDirectory()) {
            Debug.println("Processing directory: " + inputFileName);

            // isolate the directory name and append .asm (remove trailing /, if necessary)
            outputFileName = inputFileName + ".asm";
            codewriter = new CodeWriter(outputFileName); // instantiate the CodeWriter class

            File[] files = input.listFiles();
            if (files == null) {
                System.out.println("No files found in directory: " + inputFileName);
                return;
            }
            int fileCount = 0;
            for (File file : files) {
                if (file.getName().endsWith(".vm")) {
                    System.out.println("Processing file: " + file.getName());
                    Parser parser = new Parser(file.getPath()); // unique parser object for each file per API
                    parseInput(parser, file.getPath(), outputFileName); // shared output file
                    fileCount++;
                }
            }
            if (fileCount == 0) {
                System.out.println("No .vm files found in directory: " + inputFileName);
            }
        }
        else // single file
        {
            System.out.println("Processing file: " + inputFileName);

            // Generate output file name from input file name
            outputFileName = inputFileName.substring(0, inputFileName.lastIndexOf('.')) + ".asm"; // replace .vm with .asm
            codewriter = new CodeWriter(outputFileName); // instantiate the CodeWriter class

            Parser parser = new Parser(inputFileName); // unique parser object for each file per API
            parseInput(parser, inputFileName, outputFileName); // shared output file TODO: pass codewriter
        }
        return;
    }

    /**
     * Parse the input file, generate the Hack assembly code, and write it to the output file
     * @param parser the Parser object
     * @param inputFileName the name of the input file
     * @param outputFileName the name of the output file
     */
    public static void parseInput(Parser parser, String inputFileName, String outputFileName) {
        // Ensure the input file exists, is readable, and has the .vm extension
        Path inputFile = Paths.get(inputFileName);
        try {
            inputFile.getFileSystem().provider().checkAccess(inputFile, AccessMode.READ);
            if (!inputFileName.endsWith(".vm")) {
                System.out.println("Input file must have a .vm extension");
                return;
            }
        } catch (IOException e) {
            System.out.println("Hack VM Translator I/O Exception: " + e);
            return;
        }

        // TODO: Implement the VM Translator

        // Close the Hack Assembler input file and exit
        parser.close();
    }
}
