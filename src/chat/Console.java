package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Handle console output and input from users.
 *
 * @author Khoa Le
 * @version 1.0
 */
public class Console {

    private static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    /**
     * Read a string from command line.
     *
     * @param prompt        the prompt to print before reading the input
     * @return              a string
     * @throws IOException  if there is error while reading from command line
     */
    public static String readLine(String prompt) throws IOException {
        System.out.print(prompt);
        return in.readLine();
    }
}
