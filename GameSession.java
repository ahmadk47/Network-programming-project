import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

public class GameSession extends Thread {

    private Question currentQuestion;
    private volatile String whoAnsweredFirst;
    private volatile boolean correctAnswerFound;
    private volatile ArrayList<String> chosenQuestions = new ArrayList<>();
    private ArrayList<Question> Questions = new ArrayList<>();
    private Random rand = new Random();
    private volatile ArrayList<Player> players = new ArrayList<>();
    private Player Player1;
    private Player Player2;
    private Socket Socket1;
    private Socket Socket2;
    private volatile int count = 0;
    private volatile ArrayList<Player> playersAnswered = new ArrayList<>();
    private Thread T1 = null;
    private Thread T2 = null;

    public GameSession(Socket PlayerSocket1, Socket PlayerSocket2, ArrayList<Question> questions, Player FirstPlayer,
            Player SecondPlayer) {
        this.Player1 = FirstPlayer;
        this.Player2 = SecondPlayer;
        this.Questions = questions;
        this.Socket1 = PlayerSocket1;
        this.Socket2 = PlayerSocket2;
    }

    @Override
    public void run() {
        // Add the current player to the game session
        synchronized (players) {
            players.add(Player1);
            players.add(Player2);
        }
        System.out.println(Player1.getUsername());// JUST FOR CHECK
        System.out.println();

        // Shuffle questions for this session
        ArrayList<Question> shuffledQuestions = new ArrayList<>(Questions);
        shuffleQuestions(shuffledQuestions);

        System.out.println(Socket2.toString());

        PlayerHandler PlayerTwo = new PlayerHandler(Socket2, Player2, shuffledQuestions);

        PlayerHandler PlayerOne = new PlayerHandler(Socket1, Player1, shuffledQuestions);
        T2 = new Thread(PlayerTwo);

        T1 = new Thread(PlayerOne);
        T1.start();
        T2.start();
        try {
            T1.join();
            T2.join();
        } catch (Exception e) {
        }
        System.out.println("LEFT");
        System.out.println(Socket2.toString());

    }

    private void shuffleQuestions(ArrayList<Question> questions) {
        Collections.shuffle(questions);
    }

    private class PlayerHandler extends Thread {
        private Socket clientSocket;
        private Player Player;
        private PrintWriter out;
        private BufferedReader in;
        private ArrayList<Question> shuffledQuestions;

        public PlayerHandler(Socket socket, Player Player, ArrayList<Question> shuffledQuestions) {
            this.clientSocket = socket;
            this.Player = Player;
            this.shuffledQuestions = shuffledQuestions;

            try {
                this.out = new PrintWriter(socket.getOutputStream(), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("PlayerHandler created. Socket: " + socket.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                out.println("Game started!");

                for (int round = 0; round < 5; round++) {

                    try {
                        System.out.println("Round : " + round + 1);
                        Thread.sleep(2500);
                        setCurrentQuestion(shuffledQuestions.get(round));
                        PopQuestion(out, in, Player);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // Notify players of the game results
                notifyGameResults(out, in, Player1, Player2, Player);
                Player.clearthisGamePoints();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void PopQuestion(PrintWriter out, BufferedReader in, Player Player) throws IOException {
            SetcorrectAnswerFound(false);
            count = 0;
            out.println("Your question: " + getCurrentQuestion());
            System.out.println("The Answer Of this Question is " + getCurrentQuestion().getCorrectAnswer());

            String answerP1 = in.readLine();
            synchronized (this) {
                playersAnswered.add(Player);
            }
            System.out.println("playersAnswered : " + playersAnswered.size());

            System.out.println("FLAG 0");
            if ((checkAnswer(answerP1)) && !getcorrectAnswerFound()) {
                SetcorrectAnswerFound(true);
                Player.addPoints(getCurrentQuestion().getPoints());
                Player.addthisGamePoints(getCurrentQuestion().getPoints());
                SetwhoAnsweredFirst(Player.getUsername());
                System.out.println("Added Points To : " + Player.getUsername());

            }
            synchronized(this){
            count++;
            }
            System.out.println("Count : " + count);
            while (count != 2)
                ;
            System.out.println("FLAG 1");
            if (getcorrectAnswerFound()) {
                out.println("The Correct Answer : " + getCurrentQuestion().getCorrectAnswer()
                        + " The name of the player who answered first is : " + whoAnsweredFirst());
            }

            if (!getcorrectAnswerFound()) {
                out.println("Neither player provided a correct answer. The correct answer is: "
                        + getCurrentQuestion().getCorrectAnswer());

            }
            playersAnswered.clear();
            System.out.println("FLAG 3");

        }

        public void notifyGameResults(PrintWriter out, BufferedReader in, Player player, Player opponent,
                Player ThreadPlayer) {
            // Notify players of the game results
            System.out.println("Here To Notify !");
            String PlayerResult = "";
            String CreationTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
            // Player player = players.get(players.size() - 2);
            // Player opponent = players.get(players.size() - 1);

            int playerPoints = player.getthisGamePoints();
            System.out.println("Player 1 Points : " + playerPoints);
            int opponentPoints = opponent.getthisGamePoints();
            System.out.println("Player 2 Points : " + opponentPoints);

            if (playerPoints > opponentPoints) {
                System.out.println("First IF");
                PlayerResult = "(Winner) = " + player.getUsername() + " (Points) = " + player.getthisGamePoints()
                        + " vs " + "(Loser) = " + opponent.getUsername() + " (Points) = "
                        + opponent.getthisGamePoints();
                out.println(PlayerResult);
                ThreadPlayer.addGame(PlayerResult + "  " + CreationTime);
                // player.addGame(PlayerResult + " " + CreationTime);
                // opponent.addGame(PlayerResult + " " + CreationTime);
            }
            if (playerPoints < opponentPoints) {
                System.out.println("Second IF");
                PlayerResult = "(Winner) = " + opponent.getUsername() + " (Points) =" + opponent.getthisGamePoints()
                        + " vs " + "(Loser) = " + player.getUsername() + " (Points) =" + player.getthisGamePoints();
                out.println(PlayerResult);
                ThreadPlayer.addGame(PlayerResult + "  " + CreationTime);
                // player.addGame(PlayerResult + " " + CreationTime);
                // opponent.addGame(PlayerResult + " " + CreationTime);
            }
            if (playerPoints == opponentPoints) {
                System.out.println("Third IF");
                out.println("It's a tie! Both players have the same score " + " ( "
                        + opponent.getthisGamePoints() + " ) " +player.getUsername()+ " VS " + opponent.getUsername());
                ThreadPlayer.addGame(PlayerResult + "  " + CreationTime);
                // player.addGame(PlayerResult + " " + CreationTime);
                // opponent.addGame(PlayerResult + " " + CreationTime);
            }
        }
    }

    public String whoAnsweredFirst() {
        return whoAnsweredFirst;
    }

    public void SetwhoAnsweredFirst(String Player1) {
        this.whoAnsweredFirst = Player1;
    }

    public void SetcorrectAnswerFound(boolean Flag) {
        correctAnswerFound = Flag;
    }

    public boolean getcorrectAnswerFound() {
        return correctAnswerFound;
    }

    public Question selectRandomQuestion(ArrayList<Question> questions) {

        int i = rand.nextInt(28);
        if (!chosenQuestions.contains(questions.get(i).getQuestion()))
            currentQuestion = questions.get(i);
        return currentQuestion;
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(Question Q1) {
        currentQuestion = Q1;
    }

    public boolean checkAnswer(String answer) {
        // Implement answer validation logic
        // For simplicity, assuming the correct answer is stored in the Question class
        return answer != null && (Integer.parseInt(answer) == (getCurrentQuestion().getCorrectAnswer()));
    }
}
