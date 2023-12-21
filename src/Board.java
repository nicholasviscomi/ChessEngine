import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

@SuppressWarnings("SpellCheckingInspection")
public class Board extends JPanel implements ActionListener {
    int square_width = 75, square_height = 75;
    Frame parent;
    private Graphics2D g2d;
    Board(int parent_width, int parent_height, Frame parent) {
        setLocation((parent_width - square_width*8)/2, (parent_height-square_height*8)/2 - 10);
        setSize(square_width * 8, square_height * 8);
        setLayout(null);

        this.parent = parent;


        setVisible(true);
    }

    //  rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR
    //  rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR
    private void set_from_fen(String fen) {
        int x = 0;
        int y = 0;
        for (char c: fen.toCharArray()) {
            if (c == '/') {
                // y ONLY gets incremented here because a backslash is the only thing denoting the
                // end of the rank in the FEN string
                x = 0; y += 1;
                continue;
            }

            // CHAR IS A NUMBER => skip some squares
            if ((int) c >= 48 && (int) c <= 57) {
                // 48 is the offset in the ASCII table)
                int shift = (int) c - 48;

                if (shift == 8) {
                    // if it's 8 it shouldn't wrap around. Backslash is what marks the end of the line
                    x += 7;
                } else {
                    x += Math.max(shift, 1); // at minimum needs to move x over by 1
                }

                continue;
            }


            String path = "";
            // UPPERCASE => white
            if ((int) c >= 65 && (int) c <= 90) {
                path = String.format("src/materials/white/%c.png", c);
            }
            // LOWERCASE => black
            else if ((int) c >= 97 && (int) c <= 122) {
                path = String.format("src/materials/black/%c.png", c);
            }

            Image piece = Toolkit.getDefaultToolkit().getImage(path);
            g2d.drawImage(
                    piece,
                    x*square_width + 5, y*square_height + 5, square_width - 10, square_height - 10,
                    null
            );

            // AGAIN, only backslashes denote the end of the rank
            x += 1;
        }
        repaint();
    }

    String curr_fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR";
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g2d = (Graphics2D) g;

        String[] files = new String[] {"A", "B", "C", "D", "E", "F", "G", "H"};
        Color light_square = new Color(231, 214, 185);
        Color dark_square = new Color(171, 138, 109);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if ((y + x) % 2 == 0) {
                    g2d.setColor(light_square);
                } else {
                    g2d.setColor(dark_square);
                }
                g2d.fillRect(x * square_width, y * square_height, square_width, square_height);

                // FLIP THE COLORS FOR THE TEXT
                if ((y + x) % 2 == 0) {
                    g2d.setColor(dark_square);
                } else {
                    g2d.setColor(light_square);
                }
                if (x == 0) {
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
                    g2d.drawString(String.valueOf(8 - y), 3, y * square_height + 20);
                }
                if (y == 7) {
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
                    g2d.drawString(files[x], x * square_width + square_width - 20, y * square_height + square_height - 3);
                }

            }

            set_from_fen(curr_fen);
        }

//        set_from_fen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
//        set_from_fen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR");
    }

    //TODO: initialize the board and have the paintcomponent just draw the state of the board
    @Override
    public void actionPerformed(ActionEvent e) {
        if (curr_fen.equals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR")) {
            curr_fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR";
        } else {
            curr_fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
        }
        repaint();
    }
}
