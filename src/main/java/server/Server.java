package server;

import java.net.*;
import java.io.*;

enum Turn { BLUE, RED }
enum Paint { White, Blue, Red, DeadBlue, DeadRed }

public class Server
{
    // Connection
    static private int port = 8080;
    static Socket playerBlue;
    static Socket playerRed;
    static DataInputStream inB;
    static DataOutputStream outB;
    static DataInputStream inR;
    static DataOutputStream outR;

    // game statistic
    static Paint[][] Field;
    static Turn turn;
    static String move;
    static final int ROWS = 10;
    static final int COLUMNS = 10;
    public static void main( String[] args ) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started");
        while (!isOpen()) {
            // game connect
            playerBlue = serverSocket.accept();
            inB = new DataInputStream(playerBlue.getInputStream());
            outB = new DataOutputStream(playerBlue.getOutputStream());
            playerRed = serverSocket.accept();
            inR = new DataInputStream(playerRed.getInputStream());
            outR = new DataOutputStream(playerRed.getOutputStream());
            SendTurn(outB, Turn.BLUE);
            SendTurn(outR, Turn.RED);

            // preparations
            turn = Turn.BLUE;
            Field = new Paint[10][10];
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    Field[i][j] = Paint.White;
                }
            }
            Field[0][9] = Paint.Blue;
            Field[9][0] = Paint.Red;

            // game maintenance
            while (!isOver()) {
                move = "";
                SendMove(outB, "NO");
                SendMove(outR, "NO");
                SendTurn(outB, turn);
                SendTurn(outR, turn);
                if (turn == Turn.BLUE) {
                    move = ReadMove(inB);
                    ParseMove(move);
                    SendMove(outR, move);
                    nextTurn();
                } else if (turn == Turn.RED) {
                    move = ReadMove(inR);
                    ParseMove(move);
                    SendMove(outB, move);
                    nextTurn();
                }
            }
            // ending
            SendMove(outB, "YES");
            SendMove(outR, "YES");
            nextTurn();
            SendTurn(outB, turn);
            SendTurn(outR, turn);
            System.out.println("Game Complete");
        }
        serverSocket.close();
    }
    private static boolean isOpen() {
        return false;
    }
    private static void SendMove(DataOutputStream out, String move) throws IOException {
        out.writeUTF(move);
    }
    static private String ReadMove(DataInputStream in) throws IOException {
        return in.readUTF();
    }
    static void SendTurn(DataOutputStream out, Turn _turn) throws IOException {
            out.writeUTF(_turn.toString());
    }
    static void ParseMove(String move) {
        for (int i = 0; i < move.length(); i+=2) {
            int x = getNumeric(i);
            int y = getNumeric(i+1);
            if (turn == Turn.BLUE) {
                if (Field[x][y] == Paint.White) {
                    Field[x][y] = Paint.Blue;
                } else if (Field[x][y] == Paint.Red) {
                    Field[x][y] = Paint.DeadBlue;
                }
            }
            if (turn == Turn.RED) {
                if (Field[x][y] == Paint.White) {
                    Field[x][y] = Paint.Red;
                } else if (Field[x][y] == Paint.Blue) {
                    Field[x][y] = Paint.DeadRed;
                }
            }
        }
    }
    static int getNumeric(int i) {
        return Character.getNumericValue(move.charAt(i));
    }
    static void nextTurn() {
        if (turn == Turn.BLUE) {
            turn = Turn.RED;
        } else {
            turn = Turn.BLUE;
        }
    }
    static boolean isOver() {
        int countblue = 0;
        int countdeadblue = 0;
        int countred = 0;
        int countdeadred = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (Field[i][j] == Paint.Blue || Field[i][j] == Paint.DeadBlue) {
                    countblue++;
                    if (!isOption(i,j,Paint.Red)) {
                        countdeadblue++;
                    }
                } else if (Field[i][j] == Paint.Red || Field[i][j] == Paint.DeadRed) {
                    countred++;
                    if (!isOption(i,j,Paint.Blue)) {
                        countdeadred++;
                    }
                }
            }
        }
        if ( countblue == 0 ||
            countred == 0 ||
            countblue == countdeadblue ||
            countred == countdeadred ) {
                return true;
        }
        return false;
    }
    private static boolean isOption(int x, int y, Paint color) {
        Paint cell;
        Paint white = Paint.White;
        if (x + 1 < ROWS) {
            cell = Field[x+1][y];
            if (cell == white || cell == color) { return true; }
        }
        if (x + 1 < ROWS && y - 1 > -1) {
            cell = Field[x+1][y-1];
            if (cell == white || cell == color) { return true; }
        }
        if (y - 1 > -1) {
            cell = Field[x][y-1];
            if (cell == white || cell == color) { return true; }
        }
        if (x - 1 > -1 && y - 1 > -1) {
            cell = Field[x-1][y-1];
            if (cell == white || cell == color) { return true; }
        }
        if (x - 1 > -1) {
            cell = Field[x-1][y];
            if (cell == white || cell == color) { return true; }
        }
        if (x - 1 > -1 && y + 1 < COLUMNS) {
            cell = Field[x-1][y+1];
            if (cell == white || cell == color) { return true; }
        }
        if (y + 1 < COLUMNS) {
            cell = Field[x][y+1];
            if (cell == white || cell == color) { return true; }
        }
        if (x + 1 < ROWS && y + 1 < COLUMNS) {
            cell = Field[x+1][y+1];
            if (cell == white || cell == color) { return true; }
        }
        return false;
    }
}