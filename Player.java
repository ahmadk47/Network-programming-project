import java.util.ArrayList;

public class Player implements java.io.Serializable {
    private  String username;
    private String password;
    private ArrayList<String> gamesPlayed;
    private int points;
    private int thisGamePoints;



    public Player(String username, String password) {
        this.username = username;
        this.password = password;
        this.gamesPlayed = new ArrayList<>();
        this.points = 0;
  
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<String> getGamesPlayed() {
        return gamesPlayed;
    }

    public String getNumOfGames() {
        return "Played : "+ gamesPlayed.size();
    }


    public int getPoints() {
        return points;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addGame(String game) {
        this.gamesPlayed.add(game);
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public void addthisGamePoints(int points) {
        this.thisGamePoints += points;
    }

    public void clearthisGamePoints() {
        this.thisGamePoints = 0 ;
    }

    public int getthisGamePoints() {
        return thisGamePoints;
    }

    @Override
    public String toString() {
        return "Player{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
