package com.games.server;

import com.games.board.GameBoard;

import java.io.IOException;
import java.net.ServerSocket;


public class ConnectFiveServer {
    public static void main(String[] args) {
        try (ServerSocket listener = new ServerSocket(5000)) {
            System.out.println("Connect Five Server is Running and Listening at IP : " + listener.getInetAddress() + " on Port: " + listener.getLocalSocketAddress());
            System.out.println("Waiting on Connections... ");
            while (true) {
                GameBoard game = new GameBoard();
                GameBoard.Player firstPlayer = game.new Player(listener.accept(), "RED");
                System.out.println("One Player has connected. Waiting for Second to connect.");
                GameBoard.Player secondPlayer = game.new Player(listener.accept(), "YELLOW");
                System.out.println("Second Player is connected.");
                firstPlayer.setOpponent(secondPlayer);
                secondPlayer.setOpponent(firstPlayer);
                game.currentPlayer = firstPlayer;
                Thread firstPlayerThread = new Thread(firstPlayer);
                Thread secondPlayerThread = new Thread(secondPlayer);
                firstPlayerThread.start();
                secondPlayerThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}