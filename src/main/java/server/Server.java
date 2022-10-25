package server;

import java.net.*;
import java.util.Random;
import java.io.*;

enum Turn { BLUE, RED }
enum Paint { White, Blue, Red, DeadBlue, DeadRed }

public class Server
{
    static private int port = 8080;
    static Paint[][] Field;
    static Turn turn = Turn.values()[(new Random()).nextInt(2)];
    public static void main( String[] args ) throws IOException {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            print("Server started");
            while (!isOver()) {
                Socket newClient = serverSocket.accept();
                VirusWars player = new VirusWars(newClient, turn);
                nextTurn();
            }
            serverSocket.close();
        }
        catch(IOException e) {
            print("Listen socket" + e.getMessage());
        }

            // socketClientOne = server.accept();
            // inputone = new DataInputStream(socketClientOne.getInputStream());
            // outputone = new DataOutputStream(socketClientOne.getOutputStream());
            // turn(outputone, 1);
            // utils.print("1 client accepted");
            // socketClientTwo = server.accept();
            // inputtwo = new DataInputStream(socketClientTwo.getInputStream());
            // outputtwo = new DataOutputStream(socketClientTwo.getOutputStream());
            // utils.print("2 client accepted");
            // turn(outputtwo, 2);
        // server.close();
    }

    public static class VirusWars extends Thread {
        DataInputStream in;
        DataOutputStream out;
        Socket client;

        VirusWars(Socket _client, Turn turn) {
            try {
                client = _client;
                in = new DataInputStream(client.getInputStream());
                out = new DataOutputStream(client.getOutputStream());
                print(turn.toString());
                out.writeUTF(turn.toString());
                this.start();
            }
            catch(IOException e) {
                print("tcp.ClientConnection:" + e.getMessage());
            }
        }

        public void run() {
            Field = new Paint[10][10];
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    Field[i][j] = Paint.White;
                }
            }
            turn = Turn.BLUE;
            try {
                out.writeUTF(turn.toString());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            while (!isOver()) {
                String move = "";
                if (turn == Turn.BLUE) {
                    try {
                        move = in.readUTF();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    while (move.equals(""));
                    try {
                        out.writeUTF(move);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
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
