package com.codecool.klondike;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


import java.awt.*;
import java.util.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("ALL")
public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();
    List<Card> multiDraggedCards = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();
    public static Stage prStage;

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;


    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }

        if (e.getClickCount() == 2) {
            for (Pile pile: foundationPiles) {
                if (!pile.isEmpty() && pile.getTopCard().getRank() == (card.getRank() - 1) && pile.getTopCard().getSuit() == card.getSuit()) {
                    draggedCards.add(card);
                    handleValidMove(card, pile);
                    isGameWon();
                    if (card.getContainingPile().getCardUnderTopCard() != null) {
                        if (card.getContainingPile().getCardUnderTopCard().isFaceDown()) {
                            card.getContainingPile().getCardUnderTopCard().flip();
                        }
                    }
                }
                ;
            }
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();

    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK) {
            return;
        }
        if(!card.isFaceDown()){
            if(activePile.getTopCard() == card) {
            double offsetX = e.getSceneX() - dragStartX;
            double offsetY = e.getSceneY() - dragStartY;

            draggedCards.clear();
            draggedCards.add(card);

            card.getDropShadow().setRadius(20);
            card.getDropShadow().setOffsetX(10);
            card.getDropShadow().setOffsetY(10);

            card.toFront();
            card.setTranslateX(offsetX);
            card.setTranslateY(offsetY);
        } else {
                List<Card> sourcePileCards = card.getContainingPile().getCards();
                //List<Card> multiDraggedCards = FXCollections.observableArrayList();
                int whichCardToMove = 0;

                for (Card myCard : sourcePileCards){
                    if(myCard == card ){
                        whichCardToMove = sourcePileCards.indexOf(card);
                        multiDraggedCards = sourcePileCards.subList(whichCardToMove, sourcePileCards.size());

                    }
                }
                double offsetX = e.getSceneX() - dragStartX;
                double offsetY = e.getSceneY() - dragStartY;

                draggedCards.clear();
                for(Card myCard : multiDraggedCards){
                    draggedCards.add(myCard);
                    myCard.getDropShadow().setRadius(20);
                    myCard.getDropShadow().setOffsetX(10);
                    myCard.getDropShadow().setOffsetY(10);

                    myCard.toFront();
                    myCard.setTranslateX(offsetX);
                    myCard.setTranslateY(offsetY);

                    }
                }

            }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;
        Card card = (Card) e.getSource();

        Pile pile = getValidIntersectingPile(card, tableauPiles);

        Pile pile2 = getValidIntersectingPile(card, foundationPiles);
        if (pile != null) {
            handleValidMove(card, pile);
            if(card.getContainingPile().getCardUnderTopCard() != null){
                if(card.getContainingPile().getCardUnderTopCard().isFaceDown()){
                    card.getContainingPile().getCardUnderTopCard().flip();}

                    else if((card.getContainingPile().getCardfromTop(multiDraggedCards.size() + 1).isFaceDown()) &&
                        (card.getContainingPile().getCardfromTop(multiDraggedCards.size() + 1) != null)){
                        card.getContainingPile().getCardfromTop(multiDraggedCards.size() + 1).flip();
                }
            }
        } else if(pile2 != null){
            handleValidMove(card, pile2);
            if(card.getContainingPile().getCardUnderTopCard() != null){
                if(card.getContainingPile().getCardUnderTopCard().isFaceDown()){
                    card.getContainingPile().getCardUnderTopCard().flip();
                }
            }
            isGameWon();
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards = FXCollections.observableArrayList();
          }


    };


    public boolean isGameWon() {
        int numOfFoundCards = 1;
        for (Pile pile : foundationPiles)
            numOfFoundCards += pile.numOfCards();
        if (numOfFoundCards == 52) {
            showWinPopup();
             return true;
        }
        else
            return false;
    }

    private void showWinPopup() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Application information");
        alert.setHeaderText("Congratulations, you won!");
        alert.setContentText("Do you want to replay?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Klondike klondike = new Klondike();
            klondike.restart(prStage);
        }
        else {
            Platform.exit();
        }
    }

    public Game() {
        deck = Card.createNewDeck();
        initPiles();
        dealCards();
    }


    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        List<Card> reverseDiscard = discardPile.getCards();
        Collections.reverse(reverseDiscard);
        for (Card card : reverseDiscard) {
            card.flip();
            stockPile.addCard(card);
        }
        discardPile.clear();

        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        if (destPile.getPileType().equals(Pile.PileType.TABLEAU)) {
            if (card.getRank() == 13 && destPile.isEmpty()) {
                return true;

            } else if (!destPile.isEmpty() &&
                    (destPile.getTopCard().getRank() - card.getRank() == 1) &&
                    (Card.isOppositeColor(card, destPile.getTopCard())) ) {
                return true;
            } else {
                return false;
            }

        } else if(destPile.getPileType().equals(Pile.PileType.FOUNDATION)) {
            if(card.getRank() == 1 && destPile.isEmpty()) {
                return true;
            } else if((!destPile.isEmpty() &&
                    destPile.getTopCard().getRank() == card.getRank() - 1) &&
                    (destPile.getTopCard().getSuit() == card.getSuit()))
                return true;
            else
                return false;
        } else
            return false;
    }
    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to a new pile.", card);
        } else {
            msg = String.format("Placed %s to %s.", card, destPile.getTopCard());
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }


    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(550 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(30 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        for (int tableauIndex = 0; tableauIndex < tableauPiles.size(); tableauIndex++) { // place cards on tableau piles
            for (int j = 0; j <= tableauIndex; j++) {
                Card card = deckIterator.next();
                tableauPiles.get(tableauIndex).addCard(card);
                addMouseEventHandlers(card);
                getChildren().add(card);
            }
            tableauPiles.get(tableauIndex).getTopCard().flip(); // flip top card
        }
        deckIterator.forEachRemaining(card -> { // place remaining cards to stock
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });
    }


    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
