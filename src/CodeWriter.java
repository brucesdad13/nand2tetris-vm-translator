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
 * constant | Constants 0-32767         | Pseudo-segment; not part of RAM; seen by all f
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

public class CodeWriter {
    private final BufferedWriter writer;
    private String currentFileName = null; // current .VM file being translated
    private int labelCounter = 1; // counter for generating unique labels

    /**
     * Opens the output file/stream and gets ready to write into it
     * @param outputFileName the name of the .ASM output file
     */
    CodeWriter(String outputFileName) {
        // Assert that filename ends in .asm
        if (!outputFileName.toLowerCase().endsWith(".asm")) {
            throw new IllegalArgumentException("Invalid output file name: " + outputFileName);
        }

        try {
            writer = new BufferedWriter(new FileWriter(outputFileName));
            Debug.println("Opened output file: " + outputFileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Informs the code writer that the translation of a new VM file is started
     * @param fileName the name of the .VM file
     */
    void setFileName(String fileName) {
        this.currentFileName = fileName;
    }

    /**
     * Writes the assembly code that is the translation of the given arithmetic command
     * @param command one of the nine arithmetic/logical stack commands
     *                (add, sub, neg, eq, gt, lt, and, or, not)
     */
    void writeArithmetic(String command) {
        try {
            switch (command) {
                case "add": // add: binary operation, pop two, add, push one
                    // pop two operands, add them, and push the result to the stack
                    writer.write("// add\n"); // write a comment for readability
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("AM=M-1\n"); // decrement SP and point to the top operand
                    writer.write("D=M\n"); // D = first operand
                    writer.write("A=A-1\n"); // move to second operand
                    writer.write("M=M+D\n"); // add first operand to second operand
                    // result is stored in the second operand
                    // stack pointer is ready for the next operation
                    // note: the old operands are still in the stack as garbage
                    break;
                case "sub": // subtract: binary operation, pop two, subtract, push one
                    writer.write("// sub\n"); // write a comment for readability
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("AM=M-1\n"); // decrement SP and point to the top operand
                    writer.write("D=M\n"); // D = first operand
                    writer.write("A=A-1\n"); // move to second operand
                    writer.write("M=M-D\n"); // subtract first operand from second operand
                    // result is stored in the second operand
                    // stack pointer is ready for the next operation
                    // note: the old operands are still in the stack as garbage
                    break;
                case "neg": // negate: unary operation, pop one, negate, push one
                    writer.write("// neg\n"); // write a comment for readability
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("A=M-1\n"); // point to the top operand, stack pointer is unchanged
                    writer.write("M=-M\n"); // negate the top operand
                    // result is stored in the top operand
                    // stack pointer is ready for the next operation
                    // note: the old value is overwritten with the result
                    break;
                case "eq": // equal: binary operation, pop two, compare, push one
                    writer.write("// eq\n"); // write a comment for readability
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("AM=M-1\n"); // decrement SP and point to the top operand
                    writer.write("D=M\n"); // D = y
                    writer.write("A=A-1\n"); // point to x
                    writer.write("D=M-D\n"); // D = x - y (if the result is 0, x = y)
                    writer.write("@EQ_TRUE_" + labelCounter + "\n"); // load address of EQ_TRUE label into the A register
                    writer.write("D;JEQ\n"); // jump to EQ_TRUE if D = 0
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("A=M-1\n"); // point to the top operand
                    writer.write("M=0\n"); // false condition, set top operand to 0 (0x0000) for false
                    writer.write("@EQ_END_" + labelCounter + "\n"); // load address of EQ_END label into the A register
                    writer.write("0;JMP\n"); // unconditional jump to EQ_END
                    writer.write("(EQ_TRUE_" + labelCounter + ")\n"); // label for true condition
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("A=M-1\n"); // point to the top operand
                    writer.write("M=-1\n"); // true condition, set top operand to -1 (0xffff) for true
                    writer.write("(EQ_END_" + labelCounter + ")\n"); // label for end of comparison
                    break;
                case "gt": // greater than: binary operation, pop two, compare, push one
                    writer.write("// gt\n"); // write a comment for readability
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("AM=M-1\n"); // decrement SP and point to the top operand
                    writer.write("D=M\n"); // D = y
                    writer.write("A=A-1\n"); // point to x
                    writer.write("D=M-D\n"); // D = x - y (if the result is positive, x > y)
                    writer.write("@GT_TRUE_" + labelCounter + "\n"); // load address of GT_TRUE label into the A register
                    writer.write("D;JGT\n"); // jump to GT_TRUE if D > 0
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("A=M-1\n"); // point to the top operand
                    writer.write("M=0\n"); // false condition, set top operand to 0 (0x0000) for false
                    writer.write("@GT_END_" + labelCounter + "\n"); // load address of GT_END label into the A register
                    writer.write("0;JMP\n"); // unconditional jump to GT_END
                    writer.write("(GT_TRUE_" + labelCounter + ")\n"); // label for true condition
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("A=M-1\n"); // point to the top operand
                    writer.write("M=-1\n"); // true condition, set top operand to -1 (0xffff) for true
                    writer.write("(GT_END_" + labelCounter + ")\n"); // label for end of comparison
                    break;
                case "lt": // less than: binary operation, pop two, compare, push one
                    writer.write("// lt\n"); // write a comment for readability
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("AM=M-1\n"); // decrement SP and point to the top operand
                    writer.write("D=M\n"); // D = y
                    writer.write("A=A-1\n"); // point to x
                    writer.write("D=M-D\n"); // D = x - y (if the result is negative, x < y)
                    writer.write("@LT_TRUE_" + labelCounter + "\n"); // load address of LT_TRUE label into the A register
                    writer.write("D;JLT\n"); // jump to LT_TRUE if D < 0
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("A=M-1\n"); // point to the top operand
                    writer.write("M=0\n"); // false condition, set top operand to 0 (0x0000) for false
                    writer.write("@LT_END_" + labelCounter + "\n"); // load address of LT_END label into the A register
                    writer.write("0;JMP\n"); // unconditional jump to LT_END
                    writer.write("(LT_TRUE_" + labelCounter + ")\n"); // label for true condition
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("A=M-1\n"); // point to the top operand
                    writer.write("M=-1\n"); // true condition, set top operand to -1 (0xffff) for true
                    writer.write("(LT_END_" + labelCounter + ")\n"); // label for end of comparison
                    // note: the old y operand is still in the stack as garbage
                    // note: the old x operand is overwritten with the result
                    break;
                case "and": // and: binary operation, pop two, and, push one
                    writer.write("// and\n"); // write a comment for readability
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("AM=M-1\n"); // decrement SP and point to the top operand
                    writer.write("D=M\n"); // D = y
                    writer.write("A=A-1\n"); // point to x
                    writer.write("M=D&M\n"); // bitwise AND x and y and store the result in x
                    // note: the old y operand is still in the stack as garbage
                    // note: the old x operand is overwritten with the result
                    break;
                case "or": // or: binary operation, pop two, or, push one
                    writer.write("// or\n"); // write a comment for readability
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("AM=M-1\n"); // decrement SP and point to the top operand
                    writer.write("D=M\n"); // D = y
                    writer.write("A=A-1\n"); // point to x
                    writer.write("M=D|M\n"); // bitwise OR x and y and store the result in x
                    // note: the old y operand is still in the stack as garbage
                    // note: the old x operand is overwritten with the result
                    break;
                case "not": // not: unary operation, pop one, not, push one
                    writer.write("// not\n"); // write a comment for readability
                    writer.write("@SP\n"); // load the stack pointer into the A register
                    writer.write("A=M-1\n"); // point to x
                    writer.write("M=!M\n"); // bitwise NOT x and store the result in x
                    // note: the old x operand is overwritten with the result
                    break;
                default:
                    throw new IllegalArgumentException("Invalid arithmetic command: " + command);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        labelCounter++; // increment the label counter for unique labels
        Debug.println("Wrote Arithmetic command: " + command);
    }

    /**
     * Writes the assembly code that is the translation of the given command
     * where command is either C_PUSH or C_POP
     * @param command the command type (C_PUSH or C_POP)
     * @param segment the memory segment
     * @param index the index of the memory segment
     * Note: there is no pop constant i because constants are not actually part of the RAM
     */
    void writePushPop(int command, String segment, int index) {
        try {
            switch (command) { // is this a push or pop command?
                case Parser.C_PUSH: // handle push commands
                    switch (segment) { // which segment is being pushed?
                        case "argument": // push argument i: push ARG[i]
                            writer.write("// push argument " + index + "\n"); // write a comment for readability
                            writer.write("@" + index + "\n"); // load the index into the A register
                            writer.write("D=A\n"); // D = i
                            writer.write("@ARG\n"); // load the base address of the argument segment into the A register
                            writer.write("A=M+D\n"); // point to ARG[i] equivalent to ARG + i
                            writer.write("D=M\n"); // D = *ARG equivalent to *(ARG + i)
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("A=M\n"); // point to the top of the stack
                            writer.write("M=D\n"); // push *ARG[i] onto the stack
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("M=M+1\n"); // increment the stack pointer
                            break;
                        case "local": // push local i: push LCL[i]
                            writer.write("// push local " + index + "\n"); // write a comment for readability
                            writer.write("@" + index + "\n"); // load the index into the A register
                            writer.write("D=A\n"); // D = i
                            writer.write("@LCL\n"); // load the base address of the local segment into the A register
                            writer.write("A=M+D\n"); // point to LCL[i]
                            writer.write("D=M\n"); // D = *LCL[i]
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("A=M\n"); // point to the top of the stack
                            writer.write("M=D\n"); // push *LCL[i] onto the stack
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("M=M+1\n"); // increment the stack pointer
                            break;
                        case "static": // push static i: push filename.i
                            writer.write("// push static " + index + "\n"); // write a comment for readability
                            writer.write("@" + currentFileName + "." + index + "\n"); // load the static variable into the A register
                            writer.write("D=M\n"); // D = filename.i
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("A=M\n"); // point to the top of the stack
                            writer.write("M=D\n"); // push filename.i onto the stack
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("M=M+1\n"); // increment the stack pointer
                            break;
                        case "constant": // push constant i: push i
                            writer.write("// push constant " + index + "\n"); // write a comment for readability
                            // assert 0 <= index <= 32767
                            if (index < 0 || index > 32767) {
                                throw new IllegalArgumentException("Invalid constant index: " + index);
                            }
                            writer.write("@" + index + "\n"); // load the constant into the A register
                            writer.write("D=A\n"); // D = i
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("A=M\n"); // point to the top of the stack
                            writer.write("M=D\n"); // push the constant onto the stack
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("M=M+1\n"); // increment the stack pointer
                            break;
                        case "this": // push this i: push THIS[i]
                            writer.write("// push this " + index + "\n"); // write a comment for readability
                            writer.write("@" + index + "\n"); // load the index into the A register
                            writer.write("D=A\n"); // D = i
                            writer.write("@THIS\n"); // load the base address of the 'this' segment into the A register
                            writer.write("A=M+D\n"); // point to THIS[i]
                            writer.write("D=M\n"); // D = *THIS
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("A=M\n"); // point to the top of the stack
                            writer.write("M=D\n"); // push *THIS[i] onto the stack
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("M=M+1\n"); // increment the stack pointer
                            break;
                        case "that": // push that i: push THAT[i]
                            writer.write("// push that " + index + "\n"); // write a comment for readability
                            writer.write("@" + index + "\n"); // load the index into the A register
                            writer.write("D=A\n"); // D = i
                            writer.write("@THAT\n"); // load the base address of the that segment into the A register
                            writer.write("A=M+D\n"); // point to THAT[i]
                            writer.write("D=M\n"); // D = *THAT
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("A=M\n"); // point to the top of the stack
                            writer.write("M=D\n"); // push *THAT[i] onto the stack
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("M=M+1\n"); // increment the stack pointer
                            break;
                        case "pointer": // push pointer i: push THIS/THAT
                            writer.write("// push pointer " + index + "\n"); // write a comment for readability
                            if (index == 0) {
                                writer.write("@THIS\n"); // load the base address of the 'this' segment into the A register
                            } else if (index == 1) {
                                writer.write("@THAT\n"); // load the base address of the that segment into the A register
                            } else {
                                throw new IllegalArgumentException("Invalid pointer index: " + index);
                            }
                            writer.write("D=M\n"); // D = THIS or THAT
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("A=M\n"); // point to the top of the stack
                            writer.write("M=D\n"); // push THIS or THAT onto the stack
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("M=M+1\n"); // increment the stack pointer
                            break;
                        case "temp": // push temp i: push R5+i
                            writer.write("// push temp " + index + "\n"); // write a comment for readability
                            if (index < 0 || index > 7) { // assert 0 <= index <= 7
                                throw new IllegalArgumentException("Invalid temp index: " + index);
                            }
                            writer.write("@" + (5 + index) + "\n"); // load the base address of the temp segment into the A register
                            writer.write("D=M\n"); // D = R5+i
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("A=M\n"); // point to the top of the stack
                            writer.write("M=D\n"); // push R5+i onto the stack
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("M=M+1\n"); // increment the stack pointer
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid push segment: " + segment);
                    }
                    break;
                case Parser.C_POP:
                    switch (segment) {
                        case "argument": // pop argument i: pop ARG[i]
                            writer.write("// pop argument " + index + "\n"); // write a comment for readability
                            writer.write("@" + index + "\n"); // load the index into the A register
                            writer.write("D=A\n"); // D = i
                            writer.write("@ARG\n"); // load the base address of the argument segment into the A register
                            writer.write("D=M+D\n"); // D = ARG[i]
                            writer.write("@R13\n"); // load the temp register into the A register
                            writer.write("M=D\n"); // R13 = ARG[i]
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("AM=M-1\n"); // decrement SP and point to the top of the stack
                            writer.write("D=M\n"); // D = *SP
                            writer.write("@R13\n"); // load the temp register into the A register
                            writer.write("A=M\n"); // point to ARG[i]
                            writer.write("M=D\n"); // *ARG[i] = *SP
                            break;
                        case "local": // pop local i: pop LCL[i]
                            writer.write("// pop local " + index + "\n"); // write a comment for readability
                            writer.write("@" + index + "\n"); // load the index into the A register
                            writer.write("D=A\n"); // D = i
                            writer.write("@LCL\n"); // load the base address of the local segment into the A register
                            writer.write("D=M+D\n"); // D = LCL[i]
                            writer.write("@R13\n"); // load the temp register into the A register
                            writer.write("M=D\n"); // R13 = LCL[i]
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("AM=M-1\n"); // decrement SP and point to the top of the stack
                            writer.write("D=M\n"); // D = *SP
                            writer.write("@R13\n"); // load the temp register into the A register
                            writer.write("A=M\n"); // point to LCL[i]
                            writer.write("M=D\n"); // *LCL[i] = *SP
                            break;
                        case "static": // pop static i: pop filename.i
                            writer.write("// pop static " + index + "\n"); // write a comment for readability
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("AM=M-1\n"); // decrement SP and point to the top of the stack
                            writer.write("D=M\n"); // D = *SP
                            writer.write("@" + currentFileName + "." + index + "\n"); // load the static variable into the A register
                            writer.write("M=D\n"); // filename.i = *SP
                            break;
                        // Note: there is no case for pop constant i because constants are not actually part of the RAM
                        case "this": // pop this i: pop THIS[i]
                            writer.write("// pop this " + index + "\n"); // write a comment for readability
                            writer.write("@" + index + "\n"); // load the index into the A register
                            writer.write("D=A\n"); // D = i
                            writer.write("@THIS\n"); // load the base address of the 'this' segment into the A register
                            writer.write("D=M+D\n"); // D = THIS[i]
                            writer.write("@R13\n"); // load the temp register into the A register
                            writer.write("M=D\n"); // R13 = THIS[i]
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("AM=M-1\n"); // decrement SP and point to the top of the stack
                            writer.write("D=M\n"); // D = *SP
                            writer.write("@R13\n"); // load the temp register into the A register
                            writer.write("A=M\n"); // point to THIS[i]
                            writer.write("M=D\n"); // *THIS[i] = *SP
                            break;
                        case "that": // pop that i: pop THAT[i]
                            writer.write("// pop that " + index + "\n"); // write a comment for readability
                            writer.write("@" + index + "\n"); // load the index into the A register
                            writer.write("D=A\n"); // D = i
                            writer.write("@THAT\n"); // load the base address of the that segment into the A register
                            writer.write("D=M+D\n"); // D = THAT[i]
                            writer.write("@R13\n"); // load the temp register into the A register
                            writer.write("M=D\n"); // R13 = THAT[i]
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("AM=M-1\n"); // decrement SP and point to the top of the stack
                            writer.write("D=M\n"); // D = *SP
                            writer.write("@R13\n"); // load the temp register into the A register
                            writer.write("A=M\n"); // point to THAT[i]
                            writer.write("M=D\n"); // *THAT[i] = *SP
                            break;
                        case "pointer": // pop pointer i: pop THIS/THAT
                            writer.write("// pop pointer " + index + "\n"); // write a comment for readability
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("AM=M-1\n"); // decrement SP and point to the top of the stack
                            writer.write("D=M\n"); // D = *SP
                            if (index == 0) {
                                writer.write("@THIS\n"); // load the base address of the 'this' segment into the A register
                            } else if (index == 1) {
                                writer.write("@THAT\n"); // load the base address of the that segment into the A register
                            } else {
                                throw new IllegalArgumentException("Invalid pointer index: " + index);
                            }
                            writer.write("M=D\n"); // THIS or THAT = *SP
                            break;
                        case "temp": // pop temp i: pop R5+i
                            writer.write("// pop temp " + index + "\n"); // write a comment for readability
                            writer.write("@SP\n"); // load the stack pointer into the A register
                            writer.write("AM=M-1\n"); // decrement SP and point to the top of the stack
                            writer.write("D=M\n"); // D = *SP
                            writer.write("@" + (5 + index) + "\n"); // load the base address of the temp segment into the A register
                            writer.write("M=D\n"); // R5+i = *SP
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid pop segment: " + segment);
                    }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        Debug.println("Wrote Push/Pop command: " + command);
    }

    /**
     * Close the output file
     */
    void close() {
        try {
            if (writer != null) writer.close(); // close the output file
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Debug.println("Closed output file");
    }
}
