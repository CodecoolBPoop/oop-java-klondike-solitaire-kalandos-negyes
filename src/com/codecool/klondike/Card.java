package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;


import java.lang.reflect.Array;
import java.util.*;

public class Card extends ImageView {
    enum Suits { HEARTS, DIAMONDS, SPADES, CLUBS }


    private int suit;
    private int rank;
    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;

    public Card(int suit, int rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));

        //FOR COLOURS
        //String colour = "red";

        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public int getSuit() {
        return suit;
    }

    public int getRank() {
        return rank;
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        return "S" + suit + "R" + rank;
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "The " + "Rank" + rank + " of " + "Suit" + suit;
    }

    public static boolean isOppositeColor(Card card1, Card card2) {
        String colour1;
        String colour2;
        int card1Suit = card1.suit;
        int card2Suit = card2.suit;


       if (card1Suit < 3) {
            colour1 = "red";
       } else {
           colour1 = "black";
       }

       if (card2Suit < 3 ){
            colour2 = "red";
       } else {
           colour2 = "black";
       }

       return !(colour1.equals(colour2));
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> result = new ArrayList<>();
        for (int suit = 1; suit < 5; suit++) {
            for (int rank = 1; rank < 14; rank++) {
                result.add(new Card(suit, rank, true));
            }
        }
        Collections.shuffle(result);
        return result;
    }


    public static void loadCardImages() {
        cardBackImage = new Image("card_images/card_back.png");
        String suitName = "";

        for (int i = 0; i < 4; i++) {
            suitName = Suits.values()[i].toString().toLowerCase();

            for (int rank = 1; rank < 14; rank++) {
                String cardName = suitName + rank;
                String cardId = "S" + (i+1) + "R" + rank;
                String imageFileName = "card_images/" + cardName + ".png";
                System.out.println(cardId);
                cardFaceImages.put(cardId, new Image(imageFileName));
                //System.out.println(imageFileName);
            }
        }

    }
}

