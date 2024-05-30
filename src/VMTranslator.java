/**
 * VMTranslator.java
 * A full-scale VM-to-Hack translator for the Hack computer platform. Conforms to the Nand2Tetris course
 * VM specification (Parts I and II). Translates VM code to Hack assembly code.
 * by Charles Stevenson (brucesdad13@gmail.com)
 * Revision History:
 * 2024-05-25: Initial version
 * 2024-05-28: Added support for parsing label, goto, if-goto, function, return, and call commands
 * 2024-05-29: Fixed labelCounter bug in writeCall() method
 * 2024-05-30: Refactored to more properly support unique label generation for function calls
 */

import java.io.*;
import java.nio.file.*;

public class VMTranslator {
    public static void main (String[] args) {
        // Ensure the input file and output file are provided as command line arguments
        if (args.length != 1) {
            System.out.println("Usage: java VMTranslator <path-to-vm-file-or-directory>");
            System.out.println("The translator will generate a single .asm file in <path-to-vm-file-or-directory>.");
            return;
        }

        String inputFileName = args[0]; // input file name
        String outputFileName; // output file name
        CodeWriter codewriter; // instantiate the CodeWriter class
        FunctionTable functionTable = new FunctionTable(); // instantiate the FunctionTable class

        // If the program's argument is a directory, process all .vm files in the directory
        File input = new File(inputFileName);
        if (input.isDirectory()) {
            Debug.println("Processing directory: " + inputFileName);

            // isolate the directory name and append .asm (remove trailing path separator, if any)
            outputFileName = input.getName() + ".asm"; // use the directory name as the output file name per API convention
            outputFileName = input.getPath() + File.separator + outputFileName; // concatenate the directory name with the output file name
            codewriter = new CodeWriter(outputFileName, functionTable); // instantiate the CodeWriter class

            File[] files = input.listFiles();
            if (files == null) throw new IllegalArgumentException("No files found in directory: " + inputFileName);

            // First pass: parse all the functions and generate a mapping
            int fileCount = 0;
            for (File file : files) {
                if (file.getName().toLowerCase().endsWith(".vm")) {
                    System.out.println("First pass looking for functions in file: " + file.getName());
                    Parser parser = new Parser(file.getPath()); // unique parser object for each file per API
                    parseFunctions(parser, file.getPath(), functionTable); // shared output file
                    fileCount++;
                }
            }
            // if no .vm files found, throw an exception -- nothing to do
            if (fileCount == 0) throw new IllegalArgumentException("No .vm files found in directory: " + inputFileName);

            // print final function table for debugging
            Debug.println("Function table:");
            if (Debug.DEBUG_MODE) functionTable.printTable(); // print the function table (for debugging

            // write the bootstrap code to initialize the VM when translating a directory
            codewriter.writeInit();

            // Second pass: refer to the mapping when creating function labels
            for (File file : files) {
                if (file.getName().toLowerCase().endsWith(".vm")) {
                    System.out.println("Processing file: " + file.getName());
                    Parser parser = new Parser(file.getPath()); // unique parser object for each file per API
                    parseInput(parser, file.getPath(), codewriter); // shared output file
                }
            }
        }
        else // single file
        {
            System.out.println("Processing file: " + inputFileName);

            // Generate output file name from input file name
            outputFileName = inputFileName.substring(0, inputFileName.lastIndexOf('.')) + ".asm"; // replace .vm with .asm

            // instantiate the CodeWriter class; function table is empty not necessary for single file
            codewriter = new CodeWriter(outputFileName, functionTable);

            // Do not write the bootstrap code when translating a single file. Otherwise, online grader will fail.
            Debug.println("Skipping writing bootstrap code to output file");

            Parser parser = new Parser(inputFileName); // unique parser object for each file per API
            parseInput(parser, inputFileName, codewriter); // shared output file
        }
        codewriter.close(); // close the output file
    }

    /**
     * First pass: Parse all the functions and generate a function to file mapping
     * @param parser the Parser object
     * @param inputFileName the name of the input file
     */
    public static void parseFunctions(Parser parser, String inputFileName, FunctionTable functionTable) {
        File input = new File(inputFileName);
        String fileName = input.getName().substring(0, input.getName().lastIndexOf('.')); // remove the .vm extension

        while (parser.hasMoreCommands()) {
            parser.advance(); // advance to the next command
            int commandType = parser.commandType(); // get the type of command

            if (commandType == Parser.C_FUNCTION) {
                // Add the function name and filename to the map
                functionTable.addEntry(parser.arg1(), fileName);
            }
        }
    }

    /**
     * Second pass: Parse the input file VM code, generate the Hack assembly code, and write it to the output file
     * @param parser the Parser object
     * @param inputFileName the name of the input file
     * @param codeWriter the CodeWriter object
     */
    public static void parseInput(Parser parser, String inputFileName, CodeWriter codeWriter) {
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

        File input = new File(inputFileName);
        String fileName = input.getName().substring(0, input.getName().lastIndexOf('.')); // remove the .vm extension
        codeWriter.setFileName(fileName); // set the file name

        while (parser.hasMoreCommands()) {
            parser.advance(); // advance to the next command
            int commandType = parser.commandType(); // get the type of command
            switch (commandType) {
                case Parser.C_ARITHMETIC:
                    Debug.print("C_ARITHMETIC: ");
                    Debug.println(parser.arg1());
                    codeWriter.writeArithmetic(parser.arg1()); // write the arithmetic command
                    break;
                case Parser.C_PUSH:
                    Debug.print("C_PUSH: ");
                    Debug.println(parser.arg1() + " // Index: " + parser.arg2());
                    codeWriter.writePushPop(Parser.C_PUSH, parser.arg1(), parser.arg2());
                    break;
                case Parser.C_POP:
                    Debug.print("C_POP: ");
                    Debug.println(parser.arg1() + " // Index: " + parser.arg2());
                    codeWriter.writePushPop(Parser.C_POP, parser.arg1(), parser.arg2());
                    break;
                case Parser.C_LABEL:
                    Debug.println("C_LABEL: ");
                    Debug.println(parser.arg1());
                    codeWriter.writeLabel(parser.arg1());
                    break;
                case Parser.C_GOTO:
                    Debug.println("C_GOTO: ");
                    Debug.println(parser.arg1());
                    codeWriter.writeGoto(parser.arg1());
                    break;
                case Parser.C_IF:
                    Debug.println("C_IF: ");
                    Debug.println(parser.arg1());
                    codeWriter.writeIf(parser.arg1());
                    break;
                case Parser.C_FUNCTION:
                    Debug.println("C_FUNCTION: ");
                    Debug.println(parser.arg1() + " // Number of local variables: " + parser.arg2());
                    codeWriter.writeFunction(parser.arg1(), parser.arg2());
                    break;
                case Parser.C_RETURN:
                    Debug.println("C_RETURN");
                    codeWriter.writeReturn();
                    break;
                case Parser.C_CALL:
                    Debug.println("C_CALL: ");
                    Debug.println(parser.arg1() + " // Number of arguments: " + parser.arg2());
                    codeWriter.writeCall(parser.arg1(), parser.arg2());
                    break;
                default:
                    Debug.println("Command type: UNKNOWN");
                    break;
            }
        }

        // Close the Hack Assembler input file and exit
        parser.close();
    }
}
