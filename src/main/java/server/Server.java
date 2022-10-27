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
    static String move;
    public static void main( String[] args ) throws IOException {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            print("Server started");
            while (!isOpen()) {
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
                Field[0][9] = Paint.Blue;
                Field[9][0] = Paint.Red;
                while (!isOver()) {
                    move = "";
                    SendMove(outB, "NO");
                    SendMove(outR, "NO");
                    SendTurn(outB, turn.toString());
                    SendTurn(outR, turn.toString());
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
                SendMove(outB, "YES");
                SendMove(outR, "YES");
                nextTurn();
                SendTurn(outB, turn.toString());
                SendTurn(outR, turn.toString());
                print("Game Complete");
            }
            serverSocket.close();
        }
        catch(IOException e) {
            print("Listen socket" + e.getMessage());
        }
    }

    private static boolean isOpen() {
        return false;
    }

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
    static void ParseMove(String move) {
        for (int i = 0; i < move.length(); i+=2) {
            if (turn == Turn.BLUE) {
                int x = getNumeric(i);
                int y = getNumeric(i+1);
                if (Field[x][y] == Paint.White) {
                    Field[x][y] = Paint.Blue;
                } else if (Field[x][y] == Paint.Red) {
                    Field[x][y] = Paint.DeadBlue;
                }
            }
            if (turn == Turn.RED) {
                int x = getNumeric(i);
                int y = getNumeric(i+1);
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
    static boolean isOver() {
        int countblue = 0;
        int countdeadblue = 0;
        int countred = 0;
        int countdeadred = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (Field[i][j] == Paint.Blue || Field[i][j] == Paint.DeadBlue) {
                    countblue++;
                    if (!isOptionBlue(i,j)) {
                        countdeadblue++;
                    }
                } else if (Field[i][j] == Paint.Red || Field[i][j] == Paint.DeadRed) {
                    countred++;
                    if (!isOptionRed(i,j)) {
                        countdeadred++;
                    }
                }
            }
        }
        if (countblue == 0 ||
            countred == 0 ||
            countblue == countdeadblue ||
            countred == countdeadred) {
                return true;
        }
        return false;
    }
    private static boolean isOptionRed(int x, int y) {
        if (x + 1 < 10) { // x+1,y
            if (Field[x+1][y] == Paint.White || Field[x+1][y] == Paint.Blue) {
                return true;
            }
        }
        if (x + 1 < 10 && y - 1 > -1) { //x+1,y-1
            if (Field[x+1][y - 1] == Paint.White || Field[x+1][y - 1] == Paint.Blue) {
                return true;
            }
        }
        if (y - 1 > -1) { //y-1,x
            if (Field[x][y-1] == Paint.White || Field[x][y-1] == Paint.Blue) {
                return true;
            }
        }
        if (x - 1 > -1 && y - 1 > -1) {
            if (Field[x-1][y - 1] == Paint.White || Field[x-1][y-1] == Paint.Blue) {
                return true;
            }
        }
        if (x - 1 > -1) {
            if (Field[x-1][y] == Paint.White || Field[x-1][y] == Paint.Blue) {
                return true;
            }
        }
        if (x - 1 > -1 && y + 1 < 10) {
            if (Field[x-1][y+1] == Paint.White || Field[x-1][y+1] == Paint.Blue) {
                return true;
            }
        }
        if (y + 1 < 10) {
            if (Field[x][y+1] == Paint.White || Field[x][y+1] == Paint.Blue) {
                return true;
            }
        }
        if (x + 1 < 10 && y + 1 < 10) {
            if (Field[x+1][y+1] == Paint.White || Field[x+1][y+1] == Paint.Blue) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOptionBlue(int x, int y) {
        if (x + 1 < 10) { // x+1,y
            if (Field[x+1][y] == Paint.White || Field[x+1][y] == Paint.Red) {
                return true;
            }
        }
        if (x + 1 < 10 && y - 1 > -1) { //x+1,y-1
            if (Field[x+1][y - 1] == Paint.White || Field[x+1][y - 1] == Paint.Red) {
                return true;
            }
        }
        if (y - 1 > -1) { //y-1,x
            if (Field[x][y-1] == Paint.White || Field[x][y-1] == Paint.Red) {
                return true;
            }
        }
        if (x - 1 > -1 && y - 1 > -1) {
            if (Field[x-1][y - 1] == Paint.White || Field[x-1][y-1] == Paint.Red) {
                return true;
            }
        }
        if (x - 1 > -1) {
            if (Field[x-1][y] == Paint.White || Field[x-1][y] == Paint.Red) {
                return true;
            }
        }
        if (x - 1 > -1 && y + 1 < 10) {
            if (Field[x-1][y+1] == Paint.White || Field[x-1][y+1] == Paint.Red) {
                return true;
            }
        }
        if (y + 1 < 10) {
            if (Field[x][y+1] == Paint.White || Field[x][y+1] == Paint.Red) {
                return true;
            }
        }
        if (x + 1 < 10 && y + 1 < 10) {
            if (Field[x+1][y+1] == Paint.White || Field[x+1][y+1] == Paint.Red) {
                return true;
            }
        }
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


