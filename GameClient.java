import java.io.*;
import java.net.*;
import java.util.Scanner;

public class GameClient {

    // private static String line;

    public static void main(String[] args) throws IOException {
        String hostName = "localhost"; // The server's hostname
        int portNumber = 8000; // The port number on which the server is listening
        Socket playerSocket = new Socket(hostName, portNumber);
        PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        try {

            // Read and print data from the server
            String line;
            for (int i = 0; i < 2; i++) {
                line = in.readLine();
                System.out.println(line);
            }
            // Get user input from the console
            System.out.print("Answer : ");
            String send = stdIn.readLine();

            // Send the user input to the server
            out.println(send);
            out.flush(); // Flush the PrintWriter to ensure data is sent

            while (true) {
                line = in.readLine();
                if (line.equals("Exit")) {
                    // Display goodbye message
                    System.out.println("Goodbye!");

                    // Terminate the program
                    System.exit(0);
                }
                if ((line == null) || (line.isEmpty())) {
                    continue;
                }
                System.out.println(line);
                if ((line.equals("User signed up successfully and logged in automatically."))
                        || (line.equals("User logged in successfully.")))
                    break;

                if (line.equals("Incorrect password. Login failed."))
                    continue;

                send = stdIn.readLine();
                out.println(send);
                out.flush();
            }
            while (true) {
                for (int i = 0; i < 4; i++) {
                    line = in.readLine();
                    System.out.println(line);
                }
                send = stdIn.readLine();
                out.println(send);
                line = in.readLine();

                if (Integer.parseInt(send) == 1) {

                    for (int i = 0; i < 3; i++) {
                        line = in.readLine();
                        System.out.println(line);
                    }
                    // while ((line = in.readLine()) != null)
                    // System.out.println(line);
                    int j = 0;
                    while (j < 5) {
                        for (int i = 0; i < 5; i++) {
                            line = in.readLine();
                            System.out.println(line);
                        }
                        send = stdIn.readLine();
                        out.println(send);
                        out.flush();
                        line = in.readLine();
                        line = in.readLine();
                        System.out.println(line);
                        j++;
                    }
                    line = in.readLine();
                    System.out.println(line);
                    send = "0";
                }

                if (Integer.parseInt(send) == 2) {
                    line = in.readLine();
                    while (!(line.equals("Exit"))) {

                        System.out.println(line);
                        line = in.readLine();
                    }
                }

                if (Integer.parseInt(send) == 3) {
                    for (int i = 0; i < 5; i++) {
                        line = in.readLine();
                        System.out.println(line);
                    }
                }
                if (Integer.parseInt(send) == 4)
                    System.exit(0);

            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception appropriately
        }
    }
}
