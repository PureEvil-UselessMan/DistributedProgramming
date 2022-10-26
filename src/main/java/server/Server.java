package server;

import java.net.*;
import java.io.*;

enum Turn { BLUE, RED }
enum Paint { White, Blue, Red, DeadBlue, DeadRed }

public class Server
{
    static private int port = 8080;
    static Paint[][] Field;
    static Turn turn; // = Turn.values()[(new Random()).nextInt(2)];
    static Socket playerBlue;
    static Socket playerRed;
    static DataInputStream inB;
    static DataOutputStream outB;
    static DataInputStream inR;
    static DataOutputStream outR;
    public static void main( String[] args ) throws IOException {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            print("Server started");
            while (!isOver()) {
                playerBlue = serverSocket.accept();
                inB = new DataInputStream(playerBlue.getInputStream());
                outB = new DataOutputStream(playerBlue.getOutputStream());
                playerRed = serverSocket.accept();
                inR = new DataInputStream(playerRed.getInputStream());
                outR = new DataOutputStream(playerRed.getOutputStream());
                SendTurn(outB, (Turn.BLUE).toString());
                SendTurn(outR, (Turn.RED).toString());

                turn = Turn.BLUE;
                Field = new Paint[10][10];
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        Field[i][j] = Paint.White;
                    }
                }
                while (!isOver()) {
                    String move = "";
                    SendTurn(outB, turn.toString());
                    SendTurn(outR, turn.toString());
                    if (turn == Turn.BLUE) {
                        move = ReadMove(inB);
                        SendMove(outR, move);
                        nextTurn();
                    } else if (turn == Turn.RED) {
                        move = ReadMove(inR);
                        SendMove(outB, move);
                        nextTurn();
                    }
                }
            }
            serverSocket.close();
        }
        catch(IOException e) {
            print("Listen socket" + e.getMessage());
        }
    }

    // public static class VirusWars extends Thread {
    //     static DataInputStream in;
    //     static DataOutputStream out;
    //     static Socket client;
    //     Turn me;

    //     VirusWars(Socket _client, Turn _me) {
    //         try {
    //             client = _client;
    //             me = _me;
    //             in = new DataInputStream(client.getInputStream());
    //             out = new DataOutputStream(client.getOutputStream());
    //             print(me.toString());
    //             out.writeUTF(me.toString());
    //             this.start();
    //         }
    //         catch(IOException e) {
    //             print("tcp.ClientConnection:" + e.getMessage());
    //         }
    //     }

    //     public void run() {
    //         Field = new Paint[10][10];
    //         for (int i = 0; i < 10; i++) {
    //             for (int j = 0; j < 10; j++) {
    //                 Field[i][j] = Paint.White;
    //             }
    //         }
    //         turn = Turn.BLUE;
    //         while (!isOver()) {
    //             String move = "";
    //             SendTurn(turn.toString());
    //             if (turn == me) {
    //                 move = ReadMove();
    //                 SendMove(move);
    //                 nextTurn();
    //             } else {
    //                 while (me != turn);
    //             }
    //         }
    //     }
    //     private void SendMove(String move) {
    //         if (client == clients[0]) {
    //            try {
    //             (new DataOutputStream(clients[1].getOutputStream())).writeUTF(move);
    //             } catch (IOException e) {
    //                 e.printStackTrace();
    //             };
    //         } else if (client == clients[1]) {
    //             try {
    //              (new DataOutputStream(clients[0].getOutputStream())).writeUTF(move);
    //              } catch (IOException e) {
    //                  e.printStackTrace();
    //              };
    //          }
    //     }


    private static void SendMove(DataOutputStream out, String move) {
        try {
            out.writeUTF(move);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    static private String ReadMove(DataInputStream in) {
            try {
                return in.readUTF();
            } catch (IOException e) {
                return null;
            }
        }
    static void SendTurn(DataOutputStream out ,String _turn) {
        try {
            out.writeUTF(_turn);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    static void parseMove(String move) {

    }
    static boolean isOver() {
        return false;
    }
    static void print(String msg) {
        System.out.println(msg);
    }
    static void nextTurn() {
        if (turn == Turn.BLUE) {
            turn = Turn.RED;
        }
        else {
            turn = Turn.BLUE;
        }
    }
}
