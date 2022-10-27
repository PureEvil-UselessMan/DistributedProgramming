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

    // game variables
    static Turn me;
    static Turn turn;
    static int count;
    static String info;
    static String move;
    static MyJButton Buttons[][] = new MyJButton[10][10];
    static boolean Enabled[][] = new boolean[10][10];

    public static void main(String[] args) throws Exception {
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());
        me = whosTurn(in.readUTF());
        print("I am " + me.toString());

        final JFrame frame = new JFrame();
        frame.setTitle((me == Turn.BLUE) ? "You play for Blue ":"You play for Red");
        Container container = frame.getContentPane();
        container.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setLayout(new GridLayout(10, 10, 1, 1));
        for (int i = 0; i < 100; i++) {
            MyJButton btn = new MyJButton("");
            btn.setEnabled(false);
            btn.setForeground(Color.WHITE);
            Buttons[i/10][i%10] = btn;
            btn.setCoord(i/10, i%10);
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (me == turn) {
                        if (count != 0) {
                            count--;
                            info += btn.getx() + btn.gety();
                            if (me == Turn.BLUE) {
                                btn.setBackground(Color.BLUE);
                                if (btn.getText().equals("")) btn.setText("Y");
                            } else if (me == Turn.RED) {
                                btn.setBackground(Color.RED);
                                if (btn.getText().equals("")) btn.setText("Y");
                            }
                            SetEnabled(btn);
                            CalcEnabledButtons();
                        }
                    }
                }

                private void SetEnabled(MyJButton btn) {
                    int x = Integer.parseInt(btn.getx());
                    int y = Integer.parseInt(btn.gety());
                    if (x + 1 < 10)
                        Enabled[x+1][y] = true;
                    if (x + 1 < 10 && y - 1 > -1)
                        Enabled[x+1][y-1] = true;
                    if ( y - 1 > -1)
                        Enabled[x][y-1] = true;
                    if (x - 1 > -1 && y - 1 > -1)
                        Enabled[x-1][y-1] = true;
                    if (x - 1 > -1)
                        Enabled[x-1][y] = true;
                    if (x - 1 > -1 && y + 1 < 10)
                        Enabled[x-1][y+1] = true;
                    if (y + 1 < 10)
                        Enabled[x][y+1] = true;
                    if (x + 1 < 10 && y + 1 < 10)
                        Enabled[x+1][y+1] = true;
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

        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                Enabled[i][j] = false;
        if (me == Turn.BLUE) Enabled[0][9] = true;
        if (me == Turn.RED) Enabled[9][0] = true;
        CalcEnabledButtons();

        while(!isOver()) {
            turn = whosTurn(in.readUTF());
            print("Now turn " + turn.toString());
            if (me == turn) {
                count = 3;
                info = "";
                frame.setTitle("Your turn. Points left: " + count);
                while(count == 3) { print ("Wait move"); }
                frame.setTitle("Your turn. Points left: " + count);
                while(count == 2) { print ("Wait move"); }
                frame.setTitle("Your turn. Points left: " + count);
                while(count == 1) { print ("Wait move"); }
                out.writeUTF(info);
                print("move " + me.toString() + " " + move);
            } else {
                print("Wait info of move my enemy " + me.toString());
                frame.setTitle("Enemy turn");
                info = in.readUTF();
                print("Get info of move " + info);
                ParseMove();
            }
        }
        turn = whosTurn(in.readUTF());
        print("Winner is " + turn.toString());
        frame.setTitle(turn.toString() + " has win!");
    }

    private static void CalcEnabledButtons() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (Enabled[i][j]) {
                    Buttons[i][j].setEnabled(true);
                }
            }
        }
    }

    private static boolean isOver() {
        try {
            String isover = in.readUTF();
            if (isover.equals("NO")) {
                return false;
            } else if (isover.equals("YES")) {
                return true;
            }
            return true;
        } catch (IOException e) {
            return true;
        }
    }

    private static void ParseMove() {
        for (int i = 0; i < info.length(); i+=2) {
            if (me == Turn.BLUE) {
                Buttons[Character.getNumericValue(info.charAt(i))][Character.getNumericValue(info.charAt(i+1))].setBackground(Color.RED);
                Buttons[Character.getNumericValue(info.charAt(i))][Character.getNumericValue(info.charAt(i+1))].setText("E");
            } else if (me == Turn.RED) {
                Buttons[Character.getNumericValue(info.charAt(i))][Character.getNumericValue(info.charAt(i+1))].setBackground(Color.BLUE);
                Buttons[Character.getNumericValue(info.charAt(i))][Character.getNumericValue(info.charAt(i+1))].setText("E");
            }
        }
    }

    private static Turn whosTurn(String readUTF) {
        if (readUTF.equals("BLUE")) {
            return Turn.BLUE;
        } else if (readUTF.equals("RED")) {
            return Turn.RED;
        }
        return null;
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