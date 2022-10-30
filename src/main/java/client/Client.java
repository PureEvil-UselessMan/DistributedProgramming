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
import static javax.swing.JOptionPane.showMessageDialog;

enum Turn { BLUE, RED };


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
    static Button Buttons[][] = new Button[10][10];
    static boolean Enabled[][] = new boolean[10][10];
    static final int ROWS = 10;
    static final int COLUMNS = 10;
    static final Color DARKBLUE = new Color(0, 0, 153);
    static final Color DARKRED = new Color(153, 0, 0);


    public static void main(String[] args) throws Exception {
        // Connect
        socket = new Socket(host, port);
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(socket.getInputStream());

        // Turn
        me = whosTurn(in.readUTF());
        System.out.println("I am " + me.toString());

        // Interface
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        if (me == Turn.BLUE) { showMessageDialog(null, "You play for Blue"); }
        else if (me == Turn.RED) { showMessageDialog(null, "You play for Red"); }
        Container container = frame.getContentPane();
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(ROWS, COLUMNS, 0, 0));
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMNS; j++) {
                Button btn = new Button(i, j);
                Buttons[i][j] = btn;
                panel.add(btn);
            }
        }
        container.add(panel);
        container.setPreferredSize(new Dimension(600, 600));
        frame.pack();
        frame.setVisible(true);

        // Preparations
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                Enabled[i][j] = false;
        if (me == Turn.BLUE) Enabled[0][9] = true;
        CalcEnabledButtons();
        if (me == Turn.RED) Enabled[9][0] = true;

        // GamePlay
        while(!isOver()) {
            turn = whosTurn(in.readUTF());
            System.out.println("Now turn is " + turn.toString());
            if (me == turn) {
                count = 3;
                info = "";
                frame.setTitle("Your turn. Points left: " + count);
                while(count == 3) { System.out.println ("Wait move"); }
                frame.setTitle("Your turn. Points left: " + count);
                while(count == 2) { System.out.println ("Wait move"); }
                frame.setTitle("Your turn. Points left: " + count);
                while(count == 1) { System.out.println ("Wait move"); }
                out.writeUTF(info);
            } else {
                System.out.println("Wait enemy move");
                frame.setTitle("Enemy turn");
                info = in.readUTF();
                System.out.println("Enemy move: " + info);
                ParseMove();
                CalcEnabledButtons();
            }
        }

        // Ending
        turn = whosTurn(in.readUTF());
        System.out.println("Winner is " + turn.toString());
        frame.setTitle(turn.toString() + " has won!");
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                    Buttons[i][j].setEnabled(false);
        if (me == turn) { showMessageDialog(null, "You won. Grats!"); }
        else if (me != turn) { showMessageDialog(null, "You lose. Another time will be better!"); }
    }
    private static Turn whosTurn(String readUTF) {
        if (readUTF.equals("BLUE")) {
            return Turn.BLUE;
        } else if (readUTF.equals("RED")) {
            return Turn.RED;
        }
        return null;
    }
    public static class Button extends JButton implements ActionListener {
        int x;
        int y;
        public Button(int _x, int _y) {
            super("");
            this.setEnabled(false);
            this.setForeground(Color.WHITE);
            this.addActionListener(this);
            x = _x;
            y = _y;
        }
        public int getx() {
            return x;
        }
        public int gety() {
            return y;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (me == turn) {
                if (count != 0) {
                    info += Integer.toString(x) + Integer.toString(y);
                    SetEnabled();
                    if (me == Turn.BLUE) {
                        if (this.getText().equals("")) {
                            this.setText("Y");
                            this.setBackground(Color.BLUE);
                        } else if (this.getBackground() == Color.RED) {
                            this.setBackground(DARKBLUE);
                        }
                        if (!(x == 0 && y == 9)) {
                            Enabled[x][y] = false;
                        }
                    } else if (me == Turn.RED) {
                        if (this.getText().equals("")) {
                            this.setBackground(Color.RED);
                            this.setText("Y");
                        } else if (this.getBackground() == Color.BLUE) {
                            this.setBackground(DARKRED);
                        }
                        if (!(x == 9 && y == 0)) {
                            Enabled[x][y] = false;
                        }
                    }
                    count--;
                    CalcEnabledButtons();
                }
            }
        }
        private void SetEnabled() {
            if (x + 1 < 10)
                if (Buttons[x+1][y].getText().equals("") || Buttons[x+1][y].getBackground() == getEnemyColor())
                    Enabled[x+1][y] = true;
            if (x + 1 < 10 && y - 1 > -1)
                if (Buttons[x+1][y-1].getText().equals("") || Buttons[x+1][y-1].getBackground() == getEnemyColor())
                    Enabled[x+1][y-1] = true;
            if ( y - 1 > -1)
                if (Buttons[x][y-1].getText().equals("") || Buttons[x][y-1].getBackground() == getEnemyColor())
                    Enabled[x][y-1] = true;
            if (x - 1 > -1 && y - 1 > -1)
                if (Buttons[x-1][y-1].getText().equals("") || Buttons[x-1][y-1].getBackground() == getEnemyColor())
                Enabled[x-1][y-1] = true;
            if (x - 1 > -1)
                if (Buttons[x-1][y].getText().equals("") || Buttons[x-1][y].getBackground() == getEnemyColor())
                Enabled[x-1][y] = true;
            if (x - 1 > -1 && y + 1 < 10)
                if (Buttons[x-1][y+1].getText().equals("") || Buttons[x-1][y+1].getBackground() == getEnemyColor())
                Enabled[x-1][y+1] = true;
            if (y + 1 < 10)
                if (Buttons[x][y+1].getText().equals("") || Buttons[x][y+1].getBackground() == getEnemyColor())
                Enabled[x][y+1] = true;
            if (x + 1 < 10 && y + 1 < 10)
                if (Buttons[x+1][y+1].getText().equals("") || Buttons[x+1][y+1].getBackground() == getEnemyColor())
                Enabled[x+1][y+1] = true;
        }
    }
    private static Color getEnemyColor() {
        if (me == Turn.BLUE) {
            return Color.RED;
        } else if (me == Turn.RED) {
            return Color.BLUE;
        }
        return null;
    }
    private static Color getMyColor() {
        if (me == Turn.BLUE) {
            return Color.BLUE;
        } else if (me == Turn.RED) {
            return Color.RED;
        }
        return null;
    }
    private static Color getMyDarkColor() {
        if (me == Turn.BLUE) {
            return DARKBLUE;
        } else if (me == Turn.RED) {
            return DARKRED;
        }
        return null;
    }
    private static void CalcEnabledButtons() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (Enabled[i][j]) { Buttons[i][j].setEnabled(true); }
                else { Buttons[i][j].setEnabled(false); }
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
            int x = Character.getNumericValue(info.charAt(i));
            int y = Character.getNumericValue(info.charAt(i+1));
            Color backColor = Buttons[x][y].getBackground();
            String text = Buttons[x][y].getText();
            if (me == Turn.BLUE) {
                if (text.equals("")) {
                    Buttons[x][y].setBackground(Color.RED);
                    Buttons[x][y].setText("E");
                } else if (backColor == Color.BLUE) {
                    Buttons[x][y].setBackground(DARKRED);
                    SetDisabledBlue(x, y);
                    CalcEnabledButtons();
                }
            } else if (me == Turn.RED) {
                if (text.equals("")) {
                    Buttons[x][y].setBackground(Color.BLUE);
                    Buttons[x][y].setText("E");
                } else if (backColor == Color.RED) {
                    Buttons[x][y].setBackground(DARKBLUE);
                    SetDisabledRed(x, y);
                    CalcEnabledButtons();
                }
            }
        }
    }
    private static boolean isOption(int x, int y, Color myColor, Color myDarkColor) {
        Color backColor;
        if (x + 1 < ROWS) {
            backColor = Buttons[x+1][y].getBackground();
            if (backColor == myColor || backColor == myDarkColor) { return true; }
        }
        if (x + 1 < ROWS && y - 1 > -1) {
            backColor = Buttons[x+1][y-1].getBackground();
            if (backColor == myColor || backColor == myDarkColor) { return true; }
        }
        if (y - 1 > -1) {
            backColor = Buttons[x][y-1].getBackground();
            if (backColor == myColor || backColor == myDarkColor) { return true; }
        }
        if (x - 1 > -1 && y - 1 > -1) {
            backColor = Buttons[x-1][y-1].getBackground();
            if (backColor == myColor || backColor == myDarkColor) { return true; }
        }
        if (x - 1 > -1) {
            backColor = Buttons[x-1][y].getBackground();
            if (backColor == myColor || backColor == myDarkColor) { return true; }
        }
        if (x - 1 > -1 && y + 1 < COLUMNS) {
            backColor = Buttons[x-1][y+1].getBackground();
            if (backColor == myColor || backColor == myDarkColor) { return true; }
        }
        if (y + 1 < COLUMNS) {
            backColor = Buttons[x][y+1].getBackground();
            if (backColor == myColor || backColor == myDarkColor) { return true; }
        }
        if (x + 1 < ROWS && y + 1 < COLUMNS) {
            backColor = Buttons[x+1][y+1].getBackground();
            if (backColor == myColor || backColor == myDarkColor) { return true; }
        }
        return false;
    }
    private static void SetDisabledBlue(int x, int y) {
        Color myColor = getMyColor();
        Color myDarkColor = getMyDarkColor();
        if (x + 1 < ROWS) {
            if (Buttons[x+1][y].isEnabled() == true) {
                if (!isOption(x+1, y, myColor, myDarkColor)) {
                    Enabled[x+1][y] = false;
                }
            }
        }
        if (x + 1 < ROWS && y - 1 > -1) {
            if (Buttons[x+1][y-1].isEnabled() == true) {
                if (!isOption(x+1, y-1, myColor, myDarkColor)) {
                    Enabled[x+1][y-1] = false;
                }
            }
        }
        if (y - 1 > -1) {
            if (Buttons[x][y-1].isEnabled() == true) {
                if (!isOption(x, y-1, myColor, myDarkColor)) {
                    Enabled[x][y-1] = false;
                }
            }
        }
        if (x - 1 > -1 && y - 1 > -1) {
            if (Buttons[x-1][y-1].isEnabled() == true) {
                if (!isOption(x-1, y-1, myColor, myDarkColor)) {
                    Enabled[x-1][y-1] = false;
                }
            }
        }
        if (x - 1 > -1) {
            if (Buttons[x-1][y].isEnabled() == true) {
                if (!isOption(x-1, y, myColor, myDarkColor) && !(x-1 == 0 && y == COLUMNS-1)) {
                    Enabled[x-1][y] = false;
                }
            }
        }
        if (x - 1 > -1 && y + 1 < COLUMNS) {
            if (Buttons[x-1][y+1].isEnabled() == true) {
                if (!isOption(x-1, y+1, myColor, myDarkColor) && !(x-1 == 0 && y+1 == COLUMNS-1)) {
                    Enabled[x-1][y+1] = false;
                }
            }
        }
        if (y + 1 < COLUMNS) {
            if (Buttons[x][y+1].isEnabled() == true) {
                if (!isOption(x, y+1, myColor, myDarkColor) && !(x == 0 && y+1 == COLUMNS-1)) {
                    Enabled[x][y+1] = false;
                }
            }
        }
        if (x + 1 < ROWS && y + 1 < COLUMNS) {
            if (Buttons[x+1][y+1].isEnabled() == true) {
                if (!isOption(x+1, y+1, myColor, myDarkColor)) {
                    Enabled[x+1][y+1] = false;
                }
            }
        }
    }
    private static void SetDisabledRed(int x, int y) {
        Color myColor = getMyColor();
        Color myDarkColor = getMyDarkColor();
        if (x + 1 < ROWS) {
            if (Buttons[x+1][y].isEnabled() == true) {
                if (!isOption(x+1, y, myColor, myDarkColor) && !(x+1 == ROWS-1 && y == 0)) {
                    Enabled[x+1][y] = false;
                }
            }
        }
        if (x + 1 < ROWS && y - 1 > -1) {
            if (Buttons[x+1][y-1].isEnabled() == true) {
                if (!isOption(x+1, y-1, myColor, myDarkColor) && !(x+1 == ROWS-1 && y-1 == 0)) {
                    Enabled[x+1][y-1] = false;
                }
            }
        }
        if (y - 1 > -1) {
            if (Buttons[x][y-1].isEnabled() == true) {
                if (!isOption(x, y-1, myColor, myDarkColor) && !(x == ROWS-1 && y-1 == 0)) {
                    Enabled[x][y-1] = false;
                }
            }
        }
        if (x - 1 > -1 && y - 1 > -1) {
            if (Buttons[x-1][y-1].isEnabled() == true) {
                if (!isOption(x-1, y-1, myColor, myDarkColor)) {
                    Enabled[x-1][y-1] = false;
                }
            }
        }
        if (x - 1 > -1) {
            if (Buttons[x-1][y].isEnabled() == true) {
                if (!isOption(x-1, y, myColor, myDarkColor)) {
                    Enabled[x-1][y] = false;
                }
            }
        }
        if (x - 1 > -1 && y + 1 < COLUMNS) {
            if (Buttons[x-1][y+1].isEnabled() == true) {
                if (!isOption(x-1, y+1, myColor, myDarkColor)) {
                    Enabled[x-1][y+1] = false;
                }
            }
        }
        if (y + 1 < COLUMNS) {
            if (Buttons[x][y+1].isEnabled() == true) {
                if (!isOption(x, y+1, myColor, myDarkColor)) {
                    Enabled[x][y+1] = false;
                }
            }
        }
        if (x + 1 < ROWS && y + 1 < COLUMNS) {
            if (Buttons[x+1][y+1].isEnabled() == true) {
                if (!isOption(x+1, y+1, myColor, myDarkColor)) {
                    Enabled[x+1][y+1] = false;
                }
            }
        }
    }
}