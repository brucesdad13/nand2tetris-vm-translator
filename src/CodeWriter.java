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
