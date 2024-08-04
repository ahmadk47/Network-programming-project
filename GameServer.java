import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;

public class GameServer {
    private static final int PORT_NUMBER = 8000;
    private static ArrayList<Player> users = new ArrayList<>();
    private static ArrayList<String> Active_users = new ArrayList<>();
    private static ArrayList<String> usernames = new ArrayList<>();
    private static ArrayList<Question> questions = new ArrayList<>();
    private static Player Player1;
    private static Player Player11;
    private static Player Player12;
    private static ArrayList<Socket> PlayerSockets = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        try (FileInputStream fin = new FileInputStream("questions.out");
                ObjectInputStream oin = new ObjectInputStream(fin)) {

            questions = (ArrayList<Question>) oin.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            ObjectInputStream run1 = new ObjectInputStream(new FileInputStream("Players.out"));
            users = (ArrayList<Player>) run1.readObject();
            run1.close();
        } catch (Exception e) {
        }

        for (Player player : users) {
            usernames.add(player.getUsername());
        }

        ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);
        System.out.println("Server started on port " + PORT_NUMBER);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler Client1 = new ClientHandler(clientSocket);
            Thread Thread1 = new Thread(Client1);
            Thread1.start();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private static volatile boolean GamesOn = false;
        private Thread Player1Thread = null;
        private static volatile ArrayList<Thread> ThreadsQueue = new ArrayList<>();

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                displayMenu(out);
                int choice = Integer.parseInt(in.readLine());

                switch (choice) {
                    case 1:
                        int num1 = handleSignUp(in, out);
                        // If the username is taken, ask the user to sign up again
                        while (num1 == 1)
                            num1 = handleSignUp(in, out);
                        // If the user chooses to exit, terminate the program
                        if (num1 == 2) {
                            out.println("Exit");
                        }

                        break;
                    case 2:
                        handleLogin(in, out);

                        break;

                    default:
                        out.println("Invalid choice. Please enter 1 or 2.");
                }
                try {
                    ObjectOutputStream out12 = new ObjectOutputStream(new FileOutputStream("Players.out"));
                    out12.writeObject(users);
                    out12.close();
                    for (Player player : users) {
                        System.out.println(player.getUsername());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (true) {
                    try {
                        ObjectOutputStream out12 = new ObjectOutputStream(new FileOutputStream("Players.out"));
                        out12.writeObject(users);
                        out12.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out.println("1. Start a new game:\n2. Show my results:\n3. Show leaderboard:\n4. Sign-out:\n");
                    choice = Integer.parseInt(in.readLine());

                    switch (choice) {
                        case 1:
                            StartnewGame(in, out, clientSocket, Player1);
                            break;
                        case 2:
                            showMyResults(in, out, Player1);
                            out.println("Exit");
                            break;

                        case 3:
                            showLeaderboard(in, out);
                            break;

                        case 4:
                            out.println("Exit");
                            break;
                        default:
                            out.println("Invalid choice. Please enter 1 or 2 or 3 or 4.");
                    }
                }
            } catch (

            IOException e) {
                e.printStackTrace();
            }

        }

        private void displayMenu(PrintWriter out) {
            out.println("1. Sign Up\n2. Login\n");
        }

        private int handleSignUp(BufferedReader in, PrintWriter out) throws IOException {
            out.println("Enter username:");
            String username = in.readLine().trim();
            while (username.isBlank()) {
                out.print("Invalid username. Please enter a valid and unique username:");
                username = in.readLine().trim();
            }
            if (usernames.contains(username)) {
                out.println("Username already taken.\t1. Sign Up Again ? \t2. Exit");
                int choice3 = Integer.parseInt(in.readLine());

                while (choice3 != 1 && choice3 != 2) {
                    out.println("Wrong Choice.");
                    choice3 = Integer.parseInt(in.readLine());

                }
                // Return the user's choice
                return choice3;
            }

            out.println("Enter password:");
            String password = in.readLine();

            users.add(new Player(username, password));
            usernames.add(username);
            Player1 = getPlayerByUsername(username);
            Active_users.add(username);
            out.println("User signed up successfully and logged in automatically.");

            return 5;
        }

        private void handleLogin(BufferedReader in, PrintWriter out) throws IOException {
            out.println("Enter username:");
            String username = in.readLine();

            if (!usernames.contains(username)) {
                out.println("No user is registered with the entered username. Login failed.");
                out.println("Exit");
            }

            if (Active_users.contains(username)) {
                out.println("The Player is playing using another device .");
                out.println("Exit");
            }
            out.println("Enter password:");
            String password = in.readLine();

            Player user = getPlayerByUsername(username);

            if (user != null && user.getPassword().equals(password)) {
                out.println("User logged in successfully.");
            } else {
                out.println("Incorrect password. Login failed.");
                out.println("Exit");
            }

            Player1 = getPlayerByUsername(username);

        }

        private Player getPlayerByUsername(String username) {
            for (Player player : users) {
                if (player.getUsername().equals(username)) {
                    return player;
                }
            }
            return null;
        }

        private void StartnewGame(BufferedReader in, PrintWriter out, Socket clientSocket, Player Player1)
                throws IOException {
            ArrayList<Thread> ThreadsQueueGame = new ArrayList<>();
            // Implement your StartnewGame logic here
            out.println("Starting a new game...");
            // Add your game initialization code
            // Notify the player that they are waiting for another player
            out.println("Waiting for another player to join...");
            synchronized (PlayerSockets) {
                PlayerSockets.add(clientSocket);
                ThreadsQueue.add(Thread.currentThread());
            }
            System.out.println(PlayerSockets.size());
            Thread ThreadGame = null;
            synchronized (this) {
                if (PlayerSockets.size() % 2 == 1) {
                    System.out.println("Player Joined 1:" + Player1.getUsername());
                    Player11 = Player1;
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        System.out.println("ERROR");
                        return;
                    }

                }
            }
            if (PlayerSockets.size() % 2 == 0) {
                System.out.println(ThreadsQueue.size());
                System.out.println("Player Joined 2:" + Player1.getUsername());
                Player12 = Player1;
                System.out.println(PlayerSockets.get(PlayerSockets.size() - 2));
                System.out.println(PlayerSockets.get(PlayerSockets.size() - 1));
                synchronized (PlayerSockets) {
                    GameSession Game = new GameSession(PlayerSockets.get(PlayerSockets.size() - 2),
                            PlayerSockets.get(PlayerSockets.size() - 1),
                            questions, Player11, Player12);
                    ThreadGame = new Thread(Game);
                    ThreadsQueueGame = new ArrayList<>(ThreadsQueue);
                    ThreadsQueue.clear();
                    ThreadGame.start();
                }

                try {
                    ThreadGame.join();
                    synchronized (this) {
                        ThreadsQueueGame.get(0).interrupt();
                        ThreadsQueueGame.clear();
                    }

                    System.out.println("GamesOn : " + GamesOn);
                    System.out.println("Game Finished !");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

        private void showMyResults(BufferedReader in, PrintWriter out, Player Player1)
                throws IOException {
            ArrayList<String> Games = Player1.getGamesPlayed();

            if (Games != null) {
                for (String game : Games) {
                    out.println(game);
                }
            } else {
                out.println("You have never played any game yet !");

            }
        }

        private void showLeaderboard(BufferedReader in, PrintWriter out) throws IOException {

            // Find the top 5 players or all players if there are fewer than 5
            int numPlayersToDisplay = Math.min(5, users.size());

            // Perform a simple bubble sort
            for (int i = 0; i < numPlayersToDisplay - 1; i++) {
                for (int j = 0; j < numPlayersToDisplay - i - 1; j++) {
                    if (users.get(j).getPoints() < users.get(j + 1).getPoints()) {
                        // Swap players
                        Player temp = users.get(j);
                        users.set(j, users.get(j + 1));
                        users.set(j + 1, temp);
                    }
                }
            }

            // Display the top 5 players or all players if there are fewer than 5
            for (int i = 0; i < numPlayersToDisplay; i++) {
                Player player = users.get(i);
                out.printf("%-5s %-10s - Points: %d%n", (i + 1), player.getUsername(),
                        player.getPoints());
            }

        }

    }
}
