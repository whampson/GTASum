

package thehambone.gtatools.gtasum;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import thehambone.gtatools.gtasum.checksum.Checksum;
import thehambone.gtatools.gtasum.gui.Window;

/**
 *
 * @author thehambone
 */
public class Main {
    public static final String PROGRAM_TITLE = "GTASum";
    public static final String PROGRAM_VERSION = "1.0.0";
    public static final String PROGRAM_BUILD_DATE = "January 7, 2015";
    public static final String PROGRAM_AUTHOR = "thehambone";
    public static final String PROGRAM_AUTHOR_EMAIL = "thehambone93@gmail.com";
    public static final String PROGRAM_AUTHOR_URL = "http://gtaforums.com/user/907241-thehambone/";
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_FAILURE = 0;
    public static void main(String[] args) {
        if (args.length > 0) {
            int exitValue = parseArgs(args);
            System.exit(exitValue);
        } else {
            Window.createAndShowGUI();
        }
    }
    private static int parseArgs(String[] args) {
        System.out.printf("%s %s\n", PROGRAM_TITLE, PROGRAM_VERSION);
        System.out.printf("Created by %s\n", PROGRAM_AUTHOR);
        if (args[0].equals("-help")
                || args[0].equals("--help")
                || args[0].equals("-?")
                || args[0].equals("--?")
                || args[0].equals("/?")) {
            System.out.printf("Usage: $s [file1] [file2] [...]\n\n", PROGRAM_TITLE);
            System.out.printf("Bug reports go to %s.\n", PROGRAM_AUTHOR_EMAIL);
            return EXIT_SUCCESS;
        }
        int filesUpdated = 0;
        int errorCount = 0;
        for (String potentialFile : args) {
            File f = new File(potentialFile);
            if (f.isDirectory()) {
                System.out.printf("[INFO]: Skipping directory - %s\n", f.getName());
                continue;
            }
            if (!f.exists()) {
                System.out.printf("[ERROR]: File not found - %s\n", f.getName());
                continue;
            }
            try {
                filesUpdated += Checksum.calculateChecksum(f) ? 1 : 0;
            } catch (IOException ex) {
                errorCount++;
                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                System.err.printf("[ERROR]: %s\n", sw.toString());
            }
        }
        System.out.printf("\n[INFO]: %d %s updated.\n", filesUpdated, (filesUpdated ==1) ? "file" : "files");
        return (errorCount == 0) ? EXIT_SUCCESS : EXIT_FAILURE;
    } 
}