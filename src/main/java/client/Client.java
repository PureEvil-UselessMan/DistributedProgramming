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
    static String move;
    static MyJButton Buttons[][] = new MyJButton[10][10];
    static boolean Enabled[][] = new boolean[10][10];
    static final Color DARKBLUE = new Color(0, 0, 153);
    static final Color DARKRED = new Color(153, 0, 0);


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
            btn.setCoord(i/10, i%10);
            Buttons[i/10][i%10] = btn;
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (me == turn) {
                        if (count != 0) {
                            info += btn.getx() + btn.gety();
                            count--;
                            int x = Integer.parseInt(btn.getx());
                            int y = Integer.parseInt(btn.gety());
                            SetEnabled(btn);
                            if (me == Turn.BLUE) {
                                if (btn.getText().equals("")) {
                                    btn.setText("Y");
                                    btn.setBackground(Color.BLUE);
                                } else if (btn.getBackground() == Color.RED) {
                                    btn.setBackground(DARKBLUE);
                                }
                                if (!(x == 0 && y == 9)) {
                                    Enabled[x][y] = false;
                                }
                            } else if (me == Turn.RED) {
                                if (btn.getText().equals("")) {
                                    btn.setBackground(Color.RED);
                                    btn.setText("Y");
                                } else if (btn.getBackground() == Color.BLUE) {
                                    btn.setBackground(DARKRED);
                                }
                                if (!(x == 9 && y == 0)) {
                                    Enabled[x][y] = false;
                                }
                            }
                            CalcEnabledButtons();
                        }
                    }
                }

                private void SetEnabled(MyJButton btn) {
                    int x = Integer.parseInt(btn.getx());
                    int y = Integer.parseInt(btn.gety());
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

                private Color getEnemyColor() {
                    if (me == Turn.BLUE) {
                        return Color.RED;
                    } else if (me == Turn.RED) {
                        return Color.BLUE;
                    }
                    return null;
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
        CalcEnabledButtons();
        if (me == Turn.RED) Enabled[9][0] = true;

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
                CalcEnabledButtons();
            }
        }
        turn = whosTurn(in.readUTF());
        print("Winner is " + turn.toString());
        frame.setTitle(turn.toString() + " has win!");
        for (int i = 0; i < 10; i++)
            for (int j = 0; j < 10; j++)
                    Buttons[i][j].setEnabled(false);
    }

    private static void CalcEnabledButtons() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (Enabled[i][j]) {
                    Buttons[i][j].setEnabled(true);
                } else {
                    Buttons[i][j].setEnabled(false);
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

    private static void SetDisabledBlue(int x, int y) {
        if (x + 1 < 10) {
            if (Buttons[x+1][y].isEnabled() == true) {
                if (!isOptionBlue(x+1, y)) {
                    Enabled[x+1][y] = false;
                }
            }
        }
        if (x + 1 < 10 && y - 1 > -1) {
            if (Buttons[x+1][y-1].isEnabled() == true) {
                if (!isOptionBlue(x+1, y-1)) {
                    Enabled[x+1][y-1] = false;
                }
            }
        }
        if (y - 1 > -1) {
            if (Buttons[x][y-1].isEnabled() == true) {
                if (!isOptionBlue(x, y-1)) {
                    Enabled[x][y-1] = false;
                }
            }
        }
        if (x - 1 > -1 && y - 1 > -1) {
            if (Buttons[x-1][y-1].isEnabled() == true) {
                if (!isOptionBlue(x-1, y-1)) {
                    Enabled[x-1][y-1] = false;
                }
            }
        }
        if (x - 1 > -1) {
            if (Buttons[x-1][y].isEnabled() == true) {
                if (!isOptionBlue(x-1, y) && !(x-1 == 0 && y == 9)) {
                    Enabled[x-1][y] = false;
                }
            }
        }
        if (x - 1 > -1 && y + 1 < 10) {
            if (Buttons[x-1][y+1].isEnabled() == true) {
                if (!isOptionBlue(x-1, y+1) && !(x-1 == 0 && y+1 == 9)) {
                    Enabled[x-1][y+1] = false;
                }
            }
        }
        if (y + 1 < 10) {
            if (Buttons[x][y+1].isEnabled() == true) {
                if (!isOptionBlue(x, y+1) && !(x == 0 && y+1 == 9)) {
                    Enabled[x][y+1] = false;
                }
            }
        }
        if (x + 1 < 10 && y + 1 < 10) {
            if (Buttons[x+1][y+1].isEnabled() == true) {
                if (!isOptionBlue(x+1, y+1)) {
                    Enabled[x+1][y+1] = false;
                }
            }
        }
    }

    private static boolean isOptionBlue(int x, int y) {
        if (x + 1 < 10) {
            if (Buttons[x+1][y].getBackground() == Color.BLUE || Buttons[x+1][y].getBackground() == DARKBLUE) {
                return true;
            }
        }
        if (x + 1 < 10 && y - 1 > -1) {
            if (Buttons[x+1][y-1].getBackground() == Color.BLUE || Buttons[x+1][y-1].getBackground() == DARKBLUE) {
                return true;
            }
        }
        if (y - 1 > -1) {
            if (Buttons[x][y-1].getBackground() == Color.BLUE || Buttons[x][y-1].getBackground() == DARKBLUE) {
                return true;
            }
        }
        if (x - 1 > -1 && y - 1 > -1) {
            if (Buttons[x-1][y-1].getBackground() == Color.BLUE || Buttons[x-1][y-1].getBackground() == DARKBLUE) {
                return true;
            }
        }
        if (x - 1 > -1) {
            if (Buttons[x-1][y].getBackground() == Color.BLUE || Buttons[x-1][y].getBackground() == DARKBLUE) {
                return true;
            }
        }
        if (x - 1 > -1 && y + 1 < 10) {
            if (Buttons[x-1][y+1].getBackground() == Color.BLUE || Buttons[x-1][y+1].getBackground() == DARKBLUE) {
                return true;
            }
        }
        if (y + 1 < 10) {
            if (Buttons[x][y+1].getBackground() == Color.BLUE || Buttons[x][y+1].getBackground() == DARKBLUE) {
                return true;
            }
        }
        if (x + 1 < 10 && y + 1 < 10) {
            if (Buttons[x+1][y+1].getBackground() == Color.BLUE || Buttons[x+1][y+1].getBackground() == DARKBLUE) {
                return true;
            }
        }
        return false;
    }

    private static void SetDisabledRed(int x, int y) {
        if (x + 1 < 10) {
            if (Buttons[x+1][y].isEnabled() == true) {
                if (!isOptionRed(x+1, y)) {
                    Enabled[x+1][y] = false;
                }
            }
        }
        if (x + 1 < 10 && y - 1 > -1) {
            if (Buttons[x+1][y-1].isEnabled() == true) {
                if (!isOptionRed(x+1, y-1)) {
                    Enabled[x+1][y-1] = false;
                }
            }
        }
        if (y - 1 > -1) {
            if (Buttons[x][y-1].isEnabled() == true) {
                if (!isOptionRed(x, y-1)) {
                    Enabled[x][y-1] = false;
                }
            }
        }
        if (x - 1 > -1 && y - 1 > -1) {
            if (Buttons[x-1][y-1].isEnabled() == true) {
                if (!isOptionRed(x-1, y-1)) {
                    Enabled[x-1][y-1] = false;
                }
            }
        }
        if (x - 1 > -1) {
            if (Buttons[x-1][y].isEnabled() == true) {
                if (!isOptionRed(x-1, y) && !(x-1 == 9 && y == 0)) {
                    Enabled[x-1][y] = false;
                }
            }
        }
        if (x - 1 > -1 && y + 1 < 10) {
            if (Buttons[x-1][y+1].isEnabled() == true) {
                if (!isOptionRed(x-1, y+1) && !(x-1 == 9 && y+1 == 0)) {
                    Enabled[x-1][y+1] = false;
                }
            }
        }
        if (y + 1 < 10) {
            if (Buttons[x][y+1].isEnabled() == true) {
                if (!isOptionRed(x, y+1) && !(x == 9 && y+1 == 0)) {
                    Enabled[x][y+1] = false;
                }
            }
        }
        if (x + 1 < 10 && y + 1 < 10) {
            if (Buttons[x+1][y+1].isEnabled() == true) {
                if (!isOptionRed(x+1, y+1)) {
                    Enabled[x+1][y+1] = false;
                }
            }
        }
    }

    private static boolean isOptionRed(int x, int y) {
        if (x + 1 < 10) {
            if (Buttons[x+1][y].getBackground() == Color.RED || Buttons[x+1][y].getBackground() == DARKRED) {
                return true;
            }
        }
        if (x + 1 < 10 && y - 1 > -1) {
            if (Buttons[x+1][y-1].getBackground() == Color.RED || Buttons[x+1][y-1].getBackground() == DARKRED) {
                return true;
            }
        }
        if (y - 1 > -1) {
            if (Buttons[x][y-1].getBackground() == Color.RED || Buttons[x][y-1].getBackground() == DARKRED) {
                return true;
            }
        }
        if (x - 1 > -1 && y - 1 > -1) {
            if (Buttons[x-1][y-1].getBackground() == Color.RED || Buttons[x-1][y-1].getBackground() == DARKRED) {
                return true;
            }
        }
        if (x - 1 > -1) {
            if (Buttons[x-1][y].getBackground() == Color.RED || Buttons[x-1][y].getBackground() == DARKRED) {
                return true;
            }
        }
        if (x - 1 > -1 && y + 1 < 10) {
            if (Buttons[x-1][y+1].getBackground() == Color.RED || Buttons[x-1][y+1].getBackground() == DARKRED) {
                return true;
            }
        }
        if (y + 1 < 10) {
            if (Buttons[x][y+1].getBackground() == Color.RED || Buttons[x][y+1].getBackground() == DARKRED) {
                return true;
            }
        }
        if (x + 1 < 10 && y + 1 < 10) {
            if (Buttons[x+1][y+1].getBackground() == Color.RED || Buttons[x+1][y+1].getBackground() == DARKRED) {
                return true;
            }
        }
        return false;
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