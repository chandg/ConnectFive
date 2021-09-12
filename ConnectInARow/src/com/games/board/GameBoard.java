package com.games.board;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameBoard {

    /**
     * Game board consists of 54 squares (9 columns * 6 rows). Using a simple
     * array to depict this we can reference player moves and show this on the user interface
     * i.e. empty squares or chosen squares selected by the player as done in the original game.
     * If null, the corresponding square has not been chosen, otherwise the array cell stores
     * a reference to the player that selected it.
     */
    private Player[] gameBoard = {
            null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null};

    public Player currentPlayer;

    /**
     * moveMonitor() - This method monitors the client move's to see if it's valid.
     */
    public synchronized int moveMonitor(int location, Player player) {
        int locationMonitor = (location % 9) + 9 * 5;
        while (locationMonitor >= location) {
            if (player == currentPlayer && gameBoard[locationMonitor] == null) {
                gameBoard[locationMonitor] = currentPlayer;
                currentPlayer = currentPlayer.opponent;
                currentPlayer.opponentMoves(locationMonitor);
                return locationMonitor;
            }
            locationMonitor -= 9;
        }
        return -1;

    }

    /**
     * isBoardFull() - Validate if the board is full.
     *
     * @return If there is or isn't empty squares.
     */
    public boolean isBoardFull() {
        for (Player i : gameBoard) {
            if (i == null)
                return false;
        }
        return true;
    }


    /**
     * isWinner() - States the situations where a player has won.
     *
     * @return The current state of the board, determines if a player has won
     */
    public boolean isPlayerWon() {

        // 5 Across - HorizontalCheck
        for (int col = 0; col < 10 - 5; col++) { //column
            for (int row = 0; row < 54; row += 9) { //row
                if (gameBoard[row + col] != null &&
                        gameBoard[row + col] == gameBoard[row + col + 1] &&
                        gameBoard[row + col] == gameBoard[row + col + 2] &&
                        gameBoard[row + col] == gameBoard[row + col + 3] &&
                        gameBoard[row + col] == gameBoard[row + col + 4]) {
                    System.out.println("Horizontal win.");
                    return true;
                }
            }
        }
        //5 Down - VerticalCheck (Sort Issue)
        for (int row = 0; row < 27; row += 9) {
            for (int col = 0; col < 10; col++) {
                if (gameBoard[row + col] != null &&
                        gameBoard[row + col] == gameBoard[row + 9 + col] &&
                        gameBoard[row + col] == gameBoard[row + (18) + col] &&
                        gameBoard[row + col] == gameBoard[row + (27) + col] &&
                        gameBoard[row + col] == gameBoard[row + (36) + col]) {
                    System.out.println("Vertical win.");
                    return true;
                }
            }
        }
        //5 Diagonal - AscendingDiagonalCheck
        for (int row = 27; row < 54; row += 9) {
            for (int col = 0; col < 5; col++) {

                if (gameBoard[row + col] != null &&
                        gameBoard[row + col] == gameBoard[(row - 9) + col + 1] &&
                        gameBoard[(row - 9) + col + 1] == gameBoard[row - 18 + col + 2] &&
                        gameBoard[(row - 18) + col + 2] == gameBoard[(row - 27) + col + 3] &&
                        gameBoard[(row - 27) + col + 3] == gameBoard[(row - 36) + col + 4]) {
                    System.out.println("Ascending Diagonal.");
                    return true;
                }
            }
        }
        //5 Diagonal - DescendingDiagonalCheck
        for (int row = 27; row < 54; row += 9) {
            for (int col = 4; col < 9; col++) {
                if (gameBoard[row + col] != null &&
                        gameBoard[row + col] == gameBoard[(row - 9) + col - 1] &&
                        gameBoard[(row - 9) + col - 1] == gameBoard[(row - 18) + col - 2] &&
                        gameBoard[(row - 18) + col - 2] == gameBoard[(row - 27) + col - 3] &&
                        gameBoard[(row - 27) + col - 3] == gameBoard[(row - 36) + col - 4]) {
                    System.out.println("Descending Diagonal.");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Player class - extends helper threads for this multithreaded server game.
     */
    public class Player implements Runnable {
        private String colour;
        private Player opponent;
        private Socket socket;
        private BufferedReader input;
        private PrintWriter output;

        public Player(Socket socket, String colour) {
            this.socket = socket;
            this.colour = colour;
            try {
                input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("WELCOME " + colour);
                output.println("MESSAGE Waiting for your opponent to Connect!");

            } catch (IOException e) {
                System.out.println("Player Left: " + e);

            }
        }

        /**
         * Sets opponent Player.
         */
        public void setOpponent(Player opponent) {
            this.opponent = opponent;

        }

        /**
         * opponentMoves() - Handles messages from the opponent .
         */
        public void opponentMoves(int location) {
            output.println("OPPONENT_MOVED " + location);
            output.println(
                    isPlayerWon() ? "DEFEAT" : isBoardFull() ? "TIE" : "");
        }

        public void run() {
            try {
                output.println("MESSAGE All players connected");

                if (colour.equals("RED")) {
                    output.println("MESSAGE Your move");
                }
                while (true) {
                    String command = input.readLine();
                    if (command.startsWith("MOVE")) {
                        int location = Integer.parseInt(command.substring(5));
                        int validLocation = moveMonitor(location, this);
                        if (validLocation != -1) {
                            output.println("VALID_MOVE" + validLocation);
                            output.println(isPlayerWon() ? "VICTORY"
                                    : isBoardFull() ? "TIE"
                                    : "");
                        } else {
                            output.println("MESSAGE Wait your Turn");
                        }
                    } else if (command.startsWith("QUIT")) {
                        System.out.println("Player Exited. Game Over.");
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.println("Player left: " + e);

            } finally {
                if (opponent != null && opponent.output != null) {
                    opponent.output.println("OTHER_PLAYER_LEFT");
                }
                try {
                    socket.close();
                    System.out.println("Server Side Connection Closed. ");
                } catch (IOException e) {
                    System.out.println("Player left: " + e);
                    System.exit(1);
                }
            }
        }
    }
}
