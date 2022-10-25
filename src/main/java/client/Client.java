package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

enum Turn { BLUE, RED }

public class Client {
    // Connection
    static String host = "localhost";
    static int port = 8080;
    static Socket socket = null;
    static DataOutputStream out;
    static DataInputStream in;
    static Turn turn;
    static Turn me;
    static int count;
    static String info;

/*     static String[][] table = new String[10][10];
    static boolean first_step = false;
    static boolean first_player = false;
    static int turn;*/

    public static void main(String[] args) throws Exception {
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        me = whoIAm(in.readUTF());
        whosTurn(in.readUTF());
        print(turn.toString());

        final JFrame frame = new JFrame();
        frame.setTitle((turn == Turn.BLUE) ? "You play for Blue ":"You play for Red ");
        Container container = frame.getContentPane();
        container.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new GridLayout(10, 10, 1, 1));
        if (me == turn) {
            count = 3;
            info = "";
        }
        for (int i = 0; i < 100; i++) {
            MyJButton btn = new MyJButton("");
            btn.setCoord(i%10, i);
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (turn == Turn.BLUE) {
                        if (count != 0) {
                            btn.setBackground(Color.BLUE);
                            count--;
                            info += btn.getx() + btn.gety();
                        }
                        if (count == 0) {
                            try {
                                out.writeUTF(info);
                            } catch (IOException e1) {
                                // TODO Auto-generated catch block
                                e1.printStackTrace();
                            }
                        }
                    } else if (turn == Turn.RED) {
                        if (count != 0) {
                            btn.setBackground(Color.RED);
                            count--;
                        // int num = btn.getNum();
                        // int first = num%10;
                        // int second = num/10%10;
                        // table[first][second] = btn.getText();

                        // String player_and_cell = "1" + Integer.toString(num);
                        // sendMessage(player_and_cell);
                        }
                    }
                }
            });
            panel.add(btn);
        }
        container.add(panel);
        container.setPreferredSize(new Dimension(500, 500));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    private static Turn whoIAm(String readUTF) {
        if (readUTF.equals("BLUE")) {
            return Turn.BLUE;
        } else if (readUTF.equals("RED")) {
            return Turn.RED;
        }
        return null;
    }

    static void whosTurn(String stringTurn) {
        if (stringTurn.equals("BLUE")) {
            turn = Turn.BLUE;
        } else if (stringTurn.equals("RED")) {
            turn = Turn.RED;
        }
    }
    static void print(String msg) {
        System.out.println(msg);
    }
    public static class MyJButton extends JButton {
        int x;
        int y;
        public void setCoord(int _x, int _y) {
            x = _x;
            y = _y;
        }
        public String getx() {
            return Integer.toString(x);
        }
        public String gety() {
            return Integer.toString(y);
        }
        public MyJButton(String text) {
            super(text);
        }
    }
}





        // Reconection System
        // while (true) {
        //     try {
        //         socket = new Socket(host, port);
        //     }
        //     catch(IOException i) {
        //         //
        //     }
        // }