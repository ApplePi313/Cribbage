// this class represents each card in a deck

public class Card {
    /*
     * 0: Hearts
     * 1: Spades
     * 2: Diamonds
     * 3: Clubs
     * 4: Joker
     */
    private int suit = 0;
    private int value = 1; // Ace=0 to King=12, Joker = 13
    private boolean visible; // true: can see the card, false: card is facedown

    public Card(int s, int v) {
        suit = s;
        value = v;
        visible = false;
    }
    public Card(int s, int v, boolean vis) {
        suit = s;
        value = v;
        visible = vis;
    }

    public int getSuit() {
        return suit;
    }
    public int getValue() {
        return value;
    }
    public int getGameValue() {
        return value < 10 ? value + 1 : 10; // Ace - 10, return the value where value begins at 1. J,Q,K all are 10.
    }
    public boolean getVisible() {
        return visible;
    }

    public boolean setValue(int v) {
        if (v >= 0 && v <= 13) {
            value = v;

            return true;
        } else {
            return false; // invalid input for value
        }
    }
    public boolean setSuit(int s) {
        if (s >= 0 && s <= 4) {
            value = s;

            return true;
        } else {
            return false; // invalid input for suit
        }
    }
    public boolean toggleVisible() {
        visible = !visible;
        return visible;
    }
    public void setVisible(boolean v) {
        visible = v;
    }

    public String displaySuit() {
        switch (suit) {
            case 0:
                return "Hearts";
            case 1:
                return "Spades";
            case 2:
                return "Diamonds";
            case 3:
                return "Clubs";
            case 4:
                return "Joker";
            default:
                return "error in suit number";
        }
    }
    public String displayValue() {
        switch (value) {
            case 0:
                return "Ace";
            case 10:
                return "Jack";
            case 11: 
                return "Queen";
            case 12: 
                return "King";
            case 13: 
                return "Joker";
            default:
                return "" + (value + 1);
        }
    }
    public String displayCard() {
        return displayValue() + " of " + displaySuit();
    }
}
