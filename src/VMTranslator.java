/**
 * VMTranslator.java
 * Project 7: VM Translator Stage I - Handling Stack Arithmetic Commands
 * by Charles Stevenson (brucesdad13@gmail.com)
 * Revision History:
 * 2024-05-25: Initial version
 * 2024-05-28: Added support for parsing label, goto, if-goto, function, return, and call commands
 * 2024-05-29: Fixed labelCounter bug in writeCall() method
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
        String outputFileName;
        CodeWriter codewriter;

        // If the program's argument is a directory, process all .vm files in the directory
        File input = new File(inputFileName);
        if (input.isDirectory()) {
            Debug.println("Processing directory: " + inputFileName);

            // isolate the directory name and append .asm (remove trailing path separator, if any)
            outputFileName = input.getName() + ".asm"; // use the directory name as the output file name per API convention
            outputFileName = input.getPath() + File.separator + outputFileName; // concatenate the directory name with the output file name
            codewriter = new CodeWriter(outputFileName); // instantiate the CodeWriter class

            File[] files = input.listFiles();
            if (files == null) {
                System.out.println("No files found in directory: " + inputFileName);
                return;
            }
            int fileCount = 0;
            for (File file : files) {
                if (file.getName().toLowerCase().endsWith(".vm")) {
                    System.out.println("Processing file: " + file.getName());
                    Parser parser = new Parser(file.getPath()); // unique parser object for each file per API
                    parseInput(parser, file.getPath(), codewriter); // shared output file
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
            parseInput(parser, inputFileName, codewriter); // shared output file
        }
        codewriter.close(); // close the output file
    }

    /**
     * Parse the input file VM code, generate the Hack assembly code, and write it to the output file
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
