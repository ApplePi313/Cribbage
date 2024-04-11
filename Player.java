import java.util.Scanner;

public class Player {
	private String name;
	private Deck hand;
	private Deck discard;
	private int points;

	private static int numPlayers = 0;

	public Player() {
		numPlayers++;
		Scanner scnr = new Scanner(System.in);
		System.out.print("\033[1;3" + (4 + numPlayers) + "m" + "Player " + numPlayers + "\033[0m" + ", what is your name? ");
		name = "\033[1;3" + (4 + numPlayers) + "m" + scnr.nextLine() + "\033[0m";
	
		points = 0;

		hand = new Deck();
		discard = new Deck();
	}

	public void clearCards() {
		hand.clear();
		discard.clear();
	}

	public int getPoints() {
		return points;
	}
	public void setPoints(int p) {
		points = p;
	}
	public void addPoints(int p) {
		points += p;
	}

	public Deck getHand() {
		return hand;
	}
	public Deck getDiscard() {
		return discard;
	}
	public String getName() {
		return name;
	}
}
