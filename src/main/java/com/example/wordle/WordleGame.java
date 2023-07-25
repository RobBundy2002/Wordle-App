package com.example.wordle;
import java.io.IOException;
import java.nio.file.*;
import javafx.scene.image.*;
import java.util.*;
import javafx.geometry.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;

public class WordleGame extends Application {
    private Text[][] guessedLetterTexts;
    private static final String dictionaryFilePath = "dictionary2.txt";
    private static final int maxAttempts = 6;
    private static final int boardWidth = 5;
    private static final int boardHeight = 6;
    private String guessedWord;
    private String secretWord;
    private int remainingAttempts;
    private VBox previousGuessesBox;
    private GridPane boardPane;
    private Rectangle[][] boardSquares;
    private Text[] letterTexts;
    private TextField guessTextField;

    private void revealAnswer() {
        showAlert("The correct word is: " + secretWord);
        resetGame();
    }
    @Override
    public void start(Stage primaryStage) {
        try {
            secretWord = generateRandomWord();
            boardPane = createBoardLayout();
        } catch (IOException e) {
            showAlert("Error generating a random word from the dictionary.");
            return;
        }

        remainingAttempts = maxAttempts;
        previousGuessesBox = new VBox();
        previousGuessesBox.setAlignment(Pos.CENTER);

        // Create gamePane first
        Image image = new Image("1234.jpg");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(400);
        imageView.setFitHeight(150);

        VBox gamePane = new VBox(imageView, createGameLayout());
        gamePane.setAlignment(Pos.CENTER);
        gamePane.setSpacing(10);

        // Now create mainBox using the gamePane
        VBox mainBox = new VBox(gamePane, previousGuessesBox);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setSpacing(10);

        Scene scene = new Scene(mainBox, 400, 500);
        primaryStage.setTitle("Wordle Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private GridPane createGameLayout() {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setVgap(10);

        letterTexts = new Text[secretWord.length()];
        for (int i = 0; i < secretWord.length(); i++) {
            Text letterText = new Text();
            letterText.setFont(Font.font(20));
            letterText.setFill(Color.BLACK);
            letterText.setStyle("-fx-border-color: black");
            letterText.setUnderline(true);
            letterTexts[i] = letterText;

            gridPane.add(letterText, i, 0);
        }

        guessTextField = new TextField();
        guessTextField.setOnAction(e -> checkGuess());
        gridPane.add(guessTextField, 0, 1, 3, 1);

        Button revealButton = new Button("What's the Answer?");
        revealButton.setOnAction(e -> revealAnswer());
        gridPane.add(revealButton, 0, 2, 3, 1);

        gridPane.add(boardPane, 0, 3, 3, boardHeight);

        // Set up column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHalignment(HPos.CENTER);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHalignment(HPos.CENTER);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHalignment(HPos.CENTER);

        // Set up row constraints
        RowConstraints row1 = new RowConstraints();
        row1.setValignment(VPos.CENTER);
        RowConstraints row2 = new RowConstraints();
        row2.setValignment(VPos.CENTER);
        RowConstraints row3 = new RowConstraints();
        row3.setValignment(VPos.CENTER);

        // Apply the constraints to the GridPane
        gridPane.getColumnConstraints().addAll(col1, col2, col3);
        gridPane.getRowConstraints().addAll(row1, row2, row3);

        return gridPane;
    }
    private String generateRandomWord() throws IOException {
        List<String> dictionaryWords = Files.readAllLines(Paths.get(dictionaryFilePath));
        int index = new Random().nextInt(dictionaryWords.size());
        return dictionaryWords.get(index).toUpperCase();
    }
    private GridPane createBoardLayout() {
        GridPane boardPane = new GridPane();
        boardPane.setAlignment(Pos.CENTER);
        boardPane.setHgap(5);
        boardPane.setVgap(5);

        boardSquares = new Rectangle[boardWidth][boardHeight];
        guessedLetterTexts = new Text[boardWidth][boardHeight]; // Initialize the array to store the guessed letters

        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                Rectangle square = new Rectangle(30, 30, Color.WHITE);
                square.setStroke(Color.BLACK);
                boardSquares[col][row] = square;
                boardPane.add(square, col, row);

                // Initialize the Text nodes for guessed letters and add them to the board
                Text guessedLetterText = new Text();
                guessedLetterText.setFont(Font.font(24));
                guessedLetterText.setFill(Color.WHITE);
                guessedLetterTexts[col][row] = guessedLetterText;
                GridPane.setHalignment(guessedLetterText, HPos.CENTER);
                GridPane.setValignment(guessedLetterText, VPos.CENTER);
                boardPane.add(guessedLetterText, col, row);
            }
        }
        return boardPane;
    }
    private void checkGuess() {
        String guess = guessTextField.getText().toUpperCase();
        guessTextField.clear();

        if (guess.length() != secretWord.length()) {
            showAlert("Invalid guess! Please enter a " + secretWord.length() + "-letter word.");
            return;
        }

        if (guess.equals(secretWord)) {
            showAlert("Congratulations! You guessed the word!");
            resetGame();
            return;
        }

        remainingAttempts--;

        if (remainingAttempts == -1) {
            showAlert("Game Over! The secret word was: " + secretWord);
            resetGame();
            return;
        }

        boolean[] correctLetterPositions = new boolean[secretWord.length()];
        boolean[] guessedLettersInWord = new boolean[26];

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < secretWord.length(); i++) {
            char secretLetter = secretWord.charAt(i);
            char guessedLetter = guess.charAt(i);

            if (Character.toString(secretLetter).equals(Character.toString(guessedLetter))) {
                sb.append(secretLetter);
                letterTexts[i].setFill(Color.GREEN);
                correctLetterPositions[i] = true;
            } else if (secretWord.contains(Character.toString(guessedLetter))) {
                sb.append("_");
                letterTexts[i].setFill(Color.WHITE);
                guessedLettersInWord[guessedLetter - 'A'] = true;
            } else {
                sb.append("_");
                letterTexts[i].setFill(Color.BLACK);
            }

            int row = boardHeight - remainingAttempts - 1; // Calculate the row to place the guessed letter
            Text guessedLetterText = new Text(Character.toString(guessedLetter));
            guessedLetterText.setFont(Font.font(24)); // Set the font size
            guessedLetterText.setFill(Color.WHITE); // Set the text color
            GridPane.setHalignment(guessedLetterText, HPos.CENTER); // Center the text horizontally
            GridPane.setValignment(guessedLetterText, VPos.CENTER); // Center the text vertically
            boardPane.add(guessedLetterText, i, row);
        }

        guessedWord = sb.toString();

        // Update the board with the guessed letters
        for (int i = 0; i < secretWord.length(); i++) {
            char guessedLetter = guess.charAt(i);

            int row = boardHeight - remainingAttempts - 1; // Calculate the row to place the guessed letter
            if (secretWord.charAt(i) == guessedLetter) {
                boardSquares[i][row].setFill(Color.GREEN);
            } else if (secretWord.contains(Character.toString(guessedLetter))) {
                boardSquares[i][row].setFill(Color.YELLOWGREEN);
            } else {
                boardSquares[i][row].setFill(Color.BLACK);
            }
        }

        if (guessedWord.equals(secretWord)) {
            showAlert("Congratulations! You guessed the word!");
            resetGame();
        }
    }

  /*  private void onKeyboardButtonClick(Button button) {
        String letter = button.getText();
        if (!usedLetters.contains(letter.charAt(0))) {
            usedLetters.add(letter.charAt(0));
            button.setDisable(true);
            if (!secretWord.contains(letter)) {
                remainingAttempts--;
                if (remainingAttempts == 0) {
                    showAlert("Game Over! The secret word was: " + secretWord);
                    resetGame();
                }
            }
            guessTextField.appendText(letter); // Append the clicked letter to the guessTextField
        }
    } */
    private void resetGame() {
        try {
            secretWord = generateRandomWord();
        } catch (IOException e) {
            showAlert("Error generating a random word from the dictionary.");
            return;
        }

        remainingAttempts = maxAttempts;
        guessedWord = "";
        previousGuessesBox.getChildren().clear();
        guessTextField.clear();

        // Reset the letterTexts to their original state
        for (Text letterText : letterTexts) {
            letterText.setFill(Color.BLACK);
        }

        // Reset the board to its original state and remove guessed letters
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                boardSquares[col][row].setFill(Color.WHITE);
                guessedLetterTexts[col][row].setText(""); // Clear the guessed letter from the Text node
            }
        }
    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Wordle Game");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void main(String[] args) {
        launch(args);
    }
}