package com.codecool.klondike;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.control.Button;

public class Klondike extends Application {

    private static final double WINDOW_WIDTH = 1400;
    private static final double WINDOW_HEIGHT = 900;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Card.loadCardImages();
        Game game = new Game();
        game.setTableBackground(new Image("/table/green.png"));
        Game.prStage = primaryStage;

        Button restartButton = new Button("Restart");
        game.getChildren().add(restartButton);

        primaryStage.setTitle("Klondike Solitaire");
        primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();
    }

    public void restart(Stage primarystage) {
        primarystage.close();
        start(primarystage);
    }

    public void initBtn(Button button) {
        button.setPrefSize(100, 50);
        button.toFront();
        button.relocate(2, 692);
        //button.setOnMouseClicked();
    }

}
