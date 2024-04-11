import java.util.ArrayList;
import java.lang.Math;

public class Deck {
	private ArrayList<Card> cards;

	public Deck() {
		cards = new ArrayList<Card>();
	}

	public void clear() {
		cards.clear();
	}

	public void addCard(Card c) {
		cards.add(c);
	}
	public boolean addCard(int pos, Card c) {
		if (pos < cards.size() && pos >= 0) {
			cards.add(pos, c);
			return true;
		} else {
			return false;
		}
	}
	public Card removeCard(int pos) {
		if (pos >= 0 && pos < cards.size()) {
			return cards.remove(pos);
		} else { 
			return null;
		}
	}

	public Card getCard(int index) {
		return cards.get(index);
	}
	
	public ArrayList<Card> getCards() {
		return cards;
	}

	public int getValueIndex(int val) { // returns index of first card with this value
		for (int i = 0; i < cards.size(); i++) {
			if (cards.get(i).getValue() == val)
				return i;
		}

		return -1;
	}

	public int getSuitIndex(int suit) { // returns index of first card with this value
		for (int i = 0; i < cards.size(); i++) {
			if (cards.get(i).getSuit() == suit)
				return i;
		}

		return -1;
	}

	public int getCardIndex(int val, int suit) { // returns index of first card with this value
		for (int i = 0; i < cards.size(); i++) {
			if (cards.get(i).getSuit() == suit && cards.get(i).getValue() == val)
				return i;
		}

		return -1;
	}
	public int getCardIndex(Card c) { // returns index of first card with this value
		for (int i = 0; i < cards.size(); i++) {
			if (cards.get(i).getSuit() == c.getSuit() && cards.get(i).getValue() == c.getValue())
				return i;
		}

		return -1;
	}



	public int getSize() {
		return cards.size();
	}
	public String displayDeck() {
		String ret = "";

		for (int i = 0; i < cards.size(); i++) {
			ret += (i + 1) + ": " + cards.get(i).displayCard() + "\n";
		}

		return ret;
	}

	public void shuffleDeck() {
		Deck newDeck = new Deck();
		int index;

		// randomly grab cards and put them in this new deck
		for (int i = cards.size(); i > 0; i--) {
			index = (int) (Math.random() * i);

			newDeck.addCard(removeCard(index));
		}


		// shuffle cards back into this deck
		for (int i = newDeck.getSize(); i > 0; i--) {
			index = (int) (Math.random() * i);

			addCard(newDeck.removeCard(index));
		}
	}

	public int getCount(int v) {
		int count = 0;
		for (Card c : cards) {
			if (c.getValue() == v) {
				count++;
			}
		}

		return count;
	}

	private int getSortValue(int i) {
		return cards.get(i).getValue() * 10 + cards.get(i).getSuit();
	}

	// Sorts by value 
	public void sortDeck() {
		int lowest;
		for (int i = 0; i < cards.size() - 1; i++) {
			lowest = getSortValue(i);
			for (int j = i + 1; j < cards.size(); j++) {
				if (getSortValue(j) < lowest) {
					lowest = getSortValue(j);
					cards.add(i, cards.remove(j));
				}
			}
		}
	}

	public Deck split(int low, int high) { // the low index is inclusive, while the high index is exclusive
		Deck newDeck = new Deck();

		for (int i = low; i < high && i < getSize(); i++) {
			newDeck.addCard(getCard(i));
		}

		return newDeck;
	}
}
