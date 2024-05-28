/**
 * CodeWriter.java
 * Writes the Hack machine code to the output file
 * The 8 memory segments seen by every VM function qre (indexes start at 0):
 * ---------+---------------------------+-----------------------------------------
 * argument | Stores the f's args       | Dynamic alloc. by VM when f entered
 * ---------+---------------------------+-----------------------------------------
 * local    | Stores the f's local vars | Dynamic. alloc. by VM init. to zero when f entered
 * ---------+---------------------------+-----------------------------------------
 * static   | Stores static vars        | Alloc. by VM for each .vm file; shared by all f
 * ---------+---------------------------+-----------------------------------------
 * this     | General purpose segments  | Any VM function can use these segments
 * that     | Correspond to heap        | to manipulate the heap
 * ---------+---------------------------+-----------------------------------------
 * pointer  | Two-entry segment that    | Any VM f can set pointer 0 (or 1) to some address
 *          | holds the base addresses  | aligning the this (or that) segment with the
 *          | of this and that segments | heap area beginning at the given address
 * ---------+---------------------------+-----------------------------------------
 * temp     | Temp variables            | 8 multi-purpose temp variables; shared by all f
 * ---------+---------------------------+-----------------------------------------
 * constant | Constants 0-32767         | Psuedo-segment; not part of RAM; seen by all f
 * ---------+---------------------------+-----------------------------------------
 * Memory address layout (decimal):
 *         Register | Name | Usage
 * -----------------+------+--------------------------------------------------------------
 *           RAM[0] | SP   | stack pointer: points to the topmost location in the stack
 *           RAM[1] | LCL  | points to the base of the current VM function's local segment
 *           RAM[2] | ARG  | points to the base of the current VM function's argument segment
 *           RAM[3] | THIS | points to the base of the current VM function's this segment (within the heap)
 *           RAM[4] | THAT | points to the base of the current VM function's that segment (within the heap)
 * -----------------+------+--------------------------------------------------------------
 *        RAM[5-12] | N/A  | Holds the contents of the temp segment
 *       RAM[13-15] | N/A  | General-purpose registers
 *      RAM[16-255] | N/A  | Static variables
 *    RAM[256-2047] | N/A  | Stack
 *  RAM[2048-16383] | N/A  | Heap
 * RAM[16384-24575] | N/A  | Video memory mapped I/O
 *       RAM[24576] | N/A  | Keyboard memory mapped I/O
 * -----------------+------+--------------------------------------------------------------
 * Implicit data structures (never mentioned in the VM language):
 * - Stack: grows downward from 256 to 2047 (managed by push/pop)
 * - Heap: grows upward from 2048 to 16383
 * @author Charles Stevenson
 * @version 2024-05-28
 */

import java.io.*;
import java.nio.file.*;

public class CodeWriter {
    CodeWriter(String outputFileName) {
        // Ensure the output file exists and is writable
        Path outputPath = Paths.get(outputFileName);
        try
        {
            outputPath.getFileSystem().provider().checkAccess(outputPath, AccessMode.WRITE);
            Debug.println("VM Translator output file exists and is writable");
        }
        catch (IOException e)
        {
            Debug.println("VM Translator output file I/O Exception: " + e);
        }
    }
}
