import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.Scanner;

public class Game {
	private Deck draw; // the draw pile
	private Deck play;
	private Deck runStarts;
	private ArrayList<Player> players;
	private ArrayList<Integer> playHistory;
	private ArrayList<Integer> showIndices;
	private int numPlayers = 2;

	private int count;
	private int goCount;
	private int lastCountReset;
	private int showPoints;
	private int gameStage;

	private ArrayList<String> messages;

	private Deck crib;
	private Card cut;

	private int prevPlayer;
	private int currPlayer;
	private int dealer;
	private Player turn;
	private Scanner scnr = new Scanner(System.in);
	private int choice;

	private String input;
	private int index;
	private int prevIndex;

	private int winner;

	public Game() {
		draw = new Deck();
		play = new Deck();
		runStarts = new Deck();
		players = new ArrayList<Player>();
		messages = new ArrayList<String>();
		playHistory = new ArrayList<Integer>();
		showIndices = new ArrayList<Integer>();
		crib = new Deck();
		dealer = 0;

		Scanner scnr = new Scanner(System.in);
		System.out.println("Welcome to Cribbage!\n");

		for (int i = 0; i < numPlayers; i++) {
			players.add(new Player());
		}

		// Deck tmp = new Deck();
		// tmp.addCard(new Card(0, 2));
		// tmp.addCard(new Card(0, 12));
		// tmp.addCard(new Card(0, 11));
		// tmp.addCard(new Card(0, 10));
		// tmp.addCard(new Card(0, 11));
		// System.out.println(calculatePoints(tmp));

		gameLoop();
		scnr.close();
	}

	private void gameLoop() {
		while(!endgame()) {
			switch (gameStage) { 
				case 0:
					startRound();
					cribCreationLoop();
					cut();
					gameStage++;
					break;
				case 1:
					playLoop();
					gameStage++;
					break;
				case 2:
					showLoop();
					gameStage++;
					break;
				case 3:
					cribLoop();
					dealer++;
					dealer %= numPlayers;
					gameStage = 0;
					break;
			}
		}

		System.out.println("Congratulations, " + players.get(winner).getName() + "! You Won!");
		System.out.println("Final Score: \n" + displayPoints());
	}

	private void startRound() {
		// build a deck of 52 cards	
		draw.clear();
		for (int v = 0; v < 13; v++) {
			for (int s = 0; s < 4; s++) {
				draw.addCard(new Card(s, v, false));	
			}
		}
		draw.shuffleDeck();

		for (int i = 0; i < numPlayers; i++) {
			players.get(i).clearCards();
			drawCards(6, draw, players.get(i).getHand());
			players.get(i).getHand().sortDeck();
		}
	}
	private boolean drawCards(int numCards, Deck from, Deck to) {
		for (int i = 0; i < numCards; i++) {
			if (from.getSize() > 0) {
				to.addCard(from.removeCard(0));
			} else {
				return false;
			}
		}
		return true;
	}

	private void cribLoop() {
		swapPlayers(dealer);
		turn = players.get(dealer);

		while (canShow(crib) && !endgame()) {
			System.out.println(displayPoints());
			System.out.println("Cut card: " + cut.displayCard());
			System.out.println("Your crib is:\n" + crib.displayDeck());

			System.out.println("Please choose which cards to play(use commas to separate numbers): ");
			input = scnr.nextLine();
			input.trim();
			if (input.length() == 0) continue;
			if (input.charAt(input.length() - 1) != ',') {
				input += ',';
			}

			Deck tmpDeck = new Deck();

			prevIndex = 0;
			index = input.indexOf(',', prevIndex);
			showIndices.clear();
			while (index != -1) {
				try {
					showIndices.add(Integer.parseInt(input.substring(prevIndex, index)) - 1);
				} catch (NumberFormatException e) {
					System.out.println("\n\n\033[1;31mInvalid Input\n\n\033[0m");

					showIndices.clear();
					break;
				}
				for (int j = 0; j < showIndices.size(); j++) {
					if (showIndices.get(j) >= crib.getSize()) {
						System.out.println("\n\n\033[1;31mInvalid Input\n\n\033[0m");
						showIndices.clear();
						break;
					}
				}
				if (showIndices.size() == 0) break;
				prevIndex = index + 1;
				index = input.indexOf(',', prevIndex);
			}
			if (showIndices.size() == 0) continue;

			showIndices.sort(Comparator.naturalOrder());
			System.out.println(showIndices);
			for (int j = showIndices.size() - 1; j >= 0; j--) {
				tmpDeck.addCard(crib.removeCard(showIndices.get(j)));
			}
			tmpDeck.addCard(cut);
			showPoints = calculatePoints(tmpDeck);
			tmpDeck.removeCard(tmpDeck.getCardIndex(cut));
			if (showPoints == 0) {
				System.out.println("You can't get any points with those cards.");
				while (tmpDeck.getSize() > 0) { // move played cards back into hand
					crib.addCard(tmpDeck.removeCard(0));
				}
			} else {
				while (tmpDeck.getSize() > 0) { // move played cards into discard
					turn.getDiscard().addCard(tmpDeck.removeCard(0));
				}
				turn.addPoints(showPoints);
				System.out.println("You gained " + green(showPoints) + " points.");
			}
			System.out.println("Please press enter to continue");
			scnr.nextLine();
		}
		if (endgame()) return;
		System.out.println("You can't play anything else with this crib:\n" + crib.displayDeck());
		scnr.nextLine();
	}

	private void showLoop() {
		for (int i = 0; i < numPlayers; i++) {
			swapPlayers(i);
			turn = players.get(i);

			for (int j = 0; j < turn.getHand().getSize(); j++) {
				if (turn.getHand().getCard(j).getValue() == 10 && 
					turn.getHand().getCard(j).getSuit() == cut.getSuit()) {

					turn.addPoints(1);
					System.out.println("You gained" + green(1) + "point for having a Jack that's the same suit as the cut.");
					scnr.nextLine();
				}
			}

			for (int j = 1; j < turn.getHand().getSize(); j++) {
				if (turn.getHand().getCard(j).getSuit() != turn.getHand().getCard(0).getSuit()) {
					break;
				} else if (j == turn.getHand().getSize() - 1) {
					if (turn.getHand().getCard(0).getSuit() == cut.getSuit()) {
						turn.addPoints(4);
						System.out.println("You gained" + green(4) + "points for having all cards of the same suit.");
						scnr.nextLine();
					} else {
						turn.addPoints(5);
						System.out.println("You gained" + green(5) + "points for having all cards of the same suit, and the cut is the same suit.");
						scnr.nextLine();
					}
				}
			}

			while (canShow(turn.getHand()) && !endgame()) {
				System.out.println(displayPoints());
				System.out.println("Cut card: " + cut.displayCard());
				System.out.println("Your hand is:\n" + turn.getHand().displayDeck());

				System.out.println("Please choose which cards to play(use commas to separate numbers): ");
				input = scnr.nextLine();
				input.trim();
				if (input.length() == 0) continue;
				if (input.charAt(input.length() - 1) != ',') {
					input += ',';
				}

				Deck tmpDeck = new Deck();
	
				prevIndex = 0;
				index = input.indexOf(',', prevIndex);
				showIndices.clear();
				while (index != -1) {
					try {
						showIndices.add(Integer.parseInt(input.substring(prevIndex, index)) - 1);
					} catch (NumberFormatException e) {
						System.out.println("\n\n\033[1;31mInvalid Input\n\n\033[0m");

						showIndices.clear();
						break;
					}

					for (int j = 0; j < showIndices.size(); j++) {
						if (showIndices.get(j) >= turn.getHand().getSize() || 
							showIndices.get(j) < 0) {
							
							System.out.println("\n\n\033[1;31mInvalid Input\n\n\033[0m");
							showIndices.clear();
							break;
						}
					}
					for (int j = 0; j < showIndices.size() - 1; j++) {
						for (int k = j + 1; k < showIndices.size(); k++) {
							if (showIndices.get(j) == showIndices.get(k)) {
								System.out.println("\n\n\033[1;31mInvalid Input\n\n\033[0m");
								showIndices.clear();
							}
						}
					}
					if (showIndices.size() == 0) break;
					prevIndex = index + 1;
					index = input.indexOf(',', prevIndex);
				}
				if (showIndices.size() == 0) continue;

				showIndices.sort(Comparator.naturalOrder());
				for (int j = showIndices.size() - 1; j >= 0; j--) {
					tmpDeck.addCard(turn.getHand().removeCard(showIndices.get(j)));
				}
				tmpDeck.addCard(cut);
				showPoints = calculatePoints(tmpDeck);
				tmpDeck.removeCard(tmpDeck.getCardIndex(cut));
				if (showPoints == 0) {
					System.out.println("You can't get any points with those cards.");
					while (tmpDeck.getSize() > 0) { // move played cards back into hand
						turn.getHand().addCard(tmpDeck.removeCard(0));
					}
				} else {
					while (tmpDeck.getSize() > 0) { // move played cards into discard
						turn.getDiscard().addCard(tmpDeck.removeCard(0));
					}
					turn.addPoints(showPoints);
					System.out.println("You gained " + green(showPoints) + " points.");
				}
				System.out.println("Please press enter to continue");
				scnr.nextLine();
			}
			if (endgame()) return;
			System.out.println("You can't play anything else with this hand:\n" + turn.getHand().displayDeck());
			scnr.nextLine();
		}
	}

	private void playLoop() {
		count = 0;
		goCount = 0;
		prevPlayer = 1;
		currPlayer = (dealer + 1) % numPlayers; // start after the dealer
		lastCountReset = -1;
		messages.clear();
		while (playersHaveCards()) {
			messages.add("The current count is: " + count);
			messages.add(displayPoints());
			swapPlayers(currPlayer, messages);
			turn = players.get(currPlayer);
			

			switch (play.getSize()) {
				case 0:
					break;
				case 1:
					System.out.println(players.get((currPlayer + 1) % 2).getName() + " played a " + play.getCard(0).displayCard() + "\n");
					break;
				default:
					if (playHistory.get(playHistory.size() - 1) == prevPlayer) {
						System.out.println(players.get(prevPlayer).getName() + " played a " + play.getCard(play.getSize() - 1).displayCard());

						if (playHistory.get(playHistory.size() - 2) == currPlayer) {
							System.out.println("You played a " + play.getCard(play.getSize() - 2).displayCard() + " last turn\n");
						}
					} else {
						System.out.println("You played a " + play.getCard(play.getSize() - 1).displayCard() + " last turn\n");
					}
					
					break;
			}
			System.out.println("Your Hand:\n" + turn.getHand().displayDeck());

			choice = -1;
			if (canGo(currPlayer)) {
				goCount = 0;
				do {
					System.out.println(turn.getName() + ", which card would you like to play?");

					try {
						choice = scnr.nextInt();
						
						if (choice < 1 || choice > turn.getHand().getSize()) {
							choice = -1;
							System.out.println("\n\n\033[1;31mInvalid Input\n\n\033[0m");
						} else {
							count += turn.getHand().getCard(choice - 1).getGameValue();
							if (count > 31) {
								System.out.println("You can't play that card, it makes the count too high.");
								count -= turn.getHand().getCard(choice - 1).getGameValue();
								choice = -1;
							}
						}

					} catch (Exception e) {
						System.out.println("\n\n\033[1;31mNot A Number\n\n\033[0m");
					}
					scnr.nextLine();
				} while (choice == -1);

				play.addCard(turn.getHand().getCard(choice - 1));
				playHistory.add(currPlayer);
				turn.getDiscard().addCard(turn.getHand().removeCard(choice - 1)); // use the discard to temporarily store the player's hand

				checkForPlayPoints(currPlayer);
				if (count == 15 && !playersHaveCards()) {
					turn.addPoints(2);
					System.out.println("Fifteen-" + green(2) + ", and end of play. Please press enter to continue.");
					scnr.nextLine();
					if (endgame()) return;

					count = 0;
					goCount = 0;
				} else if (count == 15) {
					turn.addPoints(2);
					System.out.println("Fifteen-" + green(2) + ". Please press enter to continue.");
					scnr.nextLine();
					if (endgame()) return;
				} if (count == 31 && !playersHaveCards()) {
					turn.addPoints(2);
					System.out.println("31 for" + green(2) + ", and end of play. Please press enter to continue.");
					scnr.nextLine();
					if (endgame()) return;

					count = 0;
					goCount = 0;
				} if (count == 31) {
					turn.addPoints(2);
					System.out.println("31 for" + green(2) + ". Please press enter to continue.");
					scnr.nextLine();
					if (endgame()) return;

					count = 0;
					goCount = 0;
					lastCountReset = play.getSize() - 1;
				} else if (!playersHaveCards()) {
					turn.addPoints(1);
					System.out.println("Last card, and end of play. Please press enter to continue.");
					scnr.nextLine();
					if (endgame()) return;

					count = 0;
					goCount = 0;
				}

				prevPlayer = currPlayer; // only set prevPlayer when a card was played
			} else {
				goCount++;

				if (goCount == numPlayers) {
					goCount = 0;
					count = 0;
					lastCountReset = play.getSize() - 1;
					
					System.out.println("You are unable to play anything, and have gained a point for playing the last card. Please press enter to continue.");
					messages.add(turn.getName() + " got" + green(1) + "point for playing the last card.");
					turn.addPoints(1);
					scnr.nextLine();
					if (endgame()) return;
				} else {
					System.out.println("You are unable to play anything. Please press enter to continue.");
					messages.add(turn.getName() + " said Go.");
					scnr.nextLine();
				}
			}
			
			currPlayer++;
			currPlayer %= numPlayers;
		}

		for (int i = 0; i < numPlayers; i++) { // return cards to the players' hands for the show loop
			drawCards(4, players.get(i).getDiscard(), players.get(i).getHand());
		}
	}

	private void cut() {
		choice = 0;
		do {
			System.out.println(players.get((dealer + 1) % numPlayers).getName() + ", please choose where to cut the deck. (1-" + draw.getSize() + ")");

			try {
				choice = scnr.nextInt();
				if (choice < 1 || choice > draw.getSize()) {
					choice = 0;
					System.out.println("\n\n\033[1;31mInvalid Input\n\n\033[0m");
				}
			} catch (Exception e) {
				System.out.println("\n\n\033[1;31mNot A Number\n\n\033[0m");
			}
			scnr.nextLine();
		} while (choice == 0);

		// Add a bit of randomness because nobody can get an exact position
		choice = choice - 1 + ((new Random()).nextInt(7) - 3);
		choice = choice < 0 ? 0 : choice;
		choice = choice > draw.getSize() - 1 ? draw.getSize() - 1 : choice;
		cut = draw.getCard(choice);
		if (cut.getValue() == 10) {
			System.out.println("The cut was a Jack, so" + players.get(dealer).getName() + "has gained" + green(2) + "points. Press enter to continue.");
			players.get(dealer).addPoints(2);
			scnr.nextLine();
		}

		System.out.println("Cut Card: " + cut.displayCard() + ". Please press enter to continue.");
		scnr.nextLine();
	}

	private void cribCreationLoop() {
		System.out.println(players.get(dealer).getName() + " is the dealer this round. Please press enter to continue.");
		scnr.nextLine();
		for (int i = 0; i < numPlayers; i++) {
			swapPlayers(i);

			do {
				System.out.println(players.get(i).getName() + ", choose your first card to discard to the crib");
				System.out.println(players.get(i).getHand().displayDeck());
				
				choice = 0;
				try {
					choice = scnr.nextInt();
					if (choice < 1 || choice > players.get(i).getHand().getSize()) {
						choice = 0;
						System.out.println("\n\n\033[1;31mInvalid Input\n\n\033[0m");
					}
				} catch (Exception e) {
					System.out.println("\n\n\033[1;31mNot A Number\n\n\033[0m");
				}
				scnr.nextLine();
			} while (choice == 0);
			crib.addCard(players.get(i).getHand().removeCard(choice - 1));	
			System.out.println("\n\n");	

			do {
				System.out.println(players.get(i).getName() + ", choose your second card to discard to the crib");
				System.out.println(players.get(i).getHand().displayDeck());
				
				choice = 0;
				try {
					choice = scnr.nextInt();
					if (choice < 1 || choice > players.get(i).getHand().getSize()) {
						choice = 0;
						System.out.println("\n\n\033[1;31mInvalid Input\n\n\033[0m");
					}
				} catch (Exception e) {
					System.out.println("\n\n\033[1;31mNot A Number\n\n\033[0m");
				}
				scnr.nextLine();
			} while (choice == 0);
			crib.addCard(players.get(i).getHand().removeCard(choice - 1));	
			System.out.println("\n\n");	
		}


		// System.out.println("The Crib: \n" + crib.displayDeck());
	
	}

	private int checkFifteenTwos(Deck deck, int currCount) {
		int points = 0;
		if (currCount == 15) {
			return 2;
		} else if (deck.getSize() == 0 || currCount > 15) {
			return 0;
		} else {
			for (int i = 0; i < deck.getSize(); i++) {
				if (checkFifteenTwos(deck.split(i + 1, deck.getSize()), currCount + deck.getCard(i).getGameValue()) != 0) {
					points += checkFifteenTwos(deck.split(i + 1, deck.getSize()), currCount + deck.getCard(i).getGameValue());
				}
			}
			return points;
		}
	}
	private int checkRuns(Deck deck, int currSize, int prevValue) {
		int points = 0;
		if (deck.getValueIndex(prevValue - currSize) != -1) {
			// this means there's a possible run longer than the current run being checked, so we don't want to count this one.
			return 0; 
		}
		if (deck.getSize() == 0) {
			return currSize >= 3 ? currSize : 0;
		} else {
			for (int i = 0; i < deck.getSize(); i++) {
				if (deck.getCard(i).getValue() == prevValue + 1) {
					points += checkRuns(deck, currSize + 1, prevValue + 1);
				}
			}
			if (points == 0) {
				points += currSize >= 3 ? currSize : 0;
			}
			
			return points;
		}
	}

	private boolean canShow(Deck deck) {
		deck.addCard(cut);

		if (deck.getSize() <= 1) {
			deck.removeCard(deck.getCardIndex(cut));
			return false;
		}

		if (checkFifteenTwos(deck, 0) != 0) {
			deck.removeCard(deck.getCardIndex(cut));
			return true;
		}

		for (int i = 0; i < deck.getSize() - 1; i++) {
			for (int j = i + 1; j < deck.getSize(); j++) {
				if (deck.getCard(i).getValue() == deck.getCard(j).getValue()) {
					deck.removeCard(deck.getCardIndex(cut));
					return true; // at least a pair can be played
				}
			}
		}

		Deck tmpDeck = new Deck();
		// if the deck contains a card of a given value, put only one card card of that value into tmpDeck
		for (int i = 0; i < 14; i++) {
			for (int j = 0; j < deck.getSize(); j++) {
				if (deck.getCard(j).getValue() == i) {
					tmpDeck.addCard(deck.getCard(j));
					continue;
				}
			}
		}
		tmpDeck.sortDeck();

		for (int i = 0; i < tmpDeck.getSize() - 2; i++) {
			if (tmpDeck.getCard(i).getValue() == (tmpDeck.getCard(i + 1).getValue() - 1) && 
				tmpDeck.getCard(i).getValue() == (tmpDeck.getCard(i + 2).getValue() - 2)) {

				deck.removeCard(deck.getCardIndex(cut));
				return true; // a run of at least 3 is possible
			}
		}

		deck.removeCard(deck.getCardIndex(cut));
		return false;
	}
	private boolean canGo(int player) {
		for (Card c : players.get(player).getHand().getCards()) {
			if (count + c.getGameValue() <= 31) return true;
		}
		return false;
	}

	private boolean playersHaveCards() {
		for (Player p : players) {
			if (p.getHand().getSize() > 0) return true;
		}
		return false;
	}

	private int calculatePoints(Deck deck) {
		int points = 0;

		deck.sortDeck(); // check for pairs, triplets, etc...
		for (int i = 0; i < deck.getSize() - 1; i++) {
			for (int j = i + 1; j < deck.getSize(); j++) {
				if (deck.getCard(i).getValue() == deck.getCard(j).getValue()) {
					points += 2;
				}
			}
		}

		points += checkFifteenTwos(deck, 0);

		// check for runs
		deck.sortDeck();
		for (int i = 0; i < deck.getSize(); i++) {
			points += checkRuns(deck, 1, deck.getCard(i).getValue());
		}

		return points;
	}

	private void checkForPlayPoints(int player) {
		int i = play.getSize() - 2;
		if (i < 0) return; // only one card has been played so far
		while (play.getCard(i).getValue() == play.getCard(play.getSize() - 1).getValue()) {
			if (i <= lastCountReset) {
				break;
			}
			i--;
			if (i < 0) break;
		}
		switch (play.getSize() - 1 - i - 1) { // extra -1 because the difference will start at 1
			case 1:
				System.out.println(players.get(player).getName() + " played a pair, and gained" + green(2) + "points. Please press enter to continue.");
				players.get(player).addPoints(2);
				scnr.nextLine();
				break;
			case 2:
				System.out.println(players.get(player).getName() + " played a pair royal, and gained" + green(6) + "points. Please press enter to continue.");
				players.get(player).addPoints(6);
				scnr.nextLine();
				break;
			case 3:
				System.out.println(players.get(player).getName() + " played a double pair royal, and gained" + green(12) + "points. Please press enter to continue.");
				players.get(player).addPoints(12);
				scnr.nextLine();
				break;
		}

		Deck tmpDeck = new Deck();
		int runLen = 0;
		for (i = lastCountReset + 1; i <= play.getSize() - 3; i++) {
			if (runLen != 0) break;

			tmpDeck.clear();
			for (int j = 0; j <= play.getSize() - 1 - i; j++) {
				tmpDeck.addCard(play.getCard(i + j));
			}
			tmpDeck.sortDeck();

			runLen = play.getSize() - i;
			for (int j = 0; j <= tmpDeck.getSize() - 2; j++) {
				if (tmpDeck.getCard(j).getValue() + 1 != tmpDeck.getCard(j + 1).getValue()) {
					runLen = 0;
					break;
				}
			}
		}

		if (runLen != 0) {
			System.out.println("You completed a run " + runLen + " cards long, and have received " + green(runLen) + " points. Please press enter to continue.");
			players.get(player).addPoints(runLen);
			scnr.nextLine();
		}
	}

	private String displayPoints() {
		String pointsStr = "";

		for (int i = 0; i < numPlayers; i++) {
			pointsStr += players.get(i).getName() + " has " + "\033[0;32m" + players.get(i).getPoints() + "\033[0m" + " points.\n";
		}

		return pointsStr;
	}

	private boolean endgame() {
		for (int i = 0; i < numPlayers; i++) {
			if (players.get(i).getPoints() >= 121) {
				winner = i;
				return true;
			}
		}
		return false;
	}

	private String green(Object o) {
		return "\033[0;32m" + o + "\033[0m";
	}

	private void swapPlayers(int i) {
		clearScreen();
		System.out.println(players.get(i).getName() + ", please press enter when you are ready.\n");
		scnr.nextLine();
		// this may or may not work on windows
		System.out.println("\033[0;30m~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\033[0m");
	}
	private void swapPlayers(int i, ArrayList<String> msgs) {
		clearScreen();
		System.out.println(players.get(i).getName() + ", please press enter when you are ready.\n");
		scnr.nextLine();
		// this may or may not work on windows
		System.out.println("\033[0;30m~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\033[0m");
		for (String s : msgs) {
			System.out.println(s);
		}
		
		msgs.clear();
	}

	private void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();	
	}
}
