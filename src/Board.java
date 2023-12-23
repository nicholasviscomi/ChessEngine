import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

@SuppressWarnings("SpellCheckingInspection")
public class Board extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    int square_width = 75, square_height = 75;
    Frame parent;
    private Graphics2D g2d;
    private Piece[][] board;
    private Point curr_click;
    Board(int parent_width, int parent_height, Frame parent) {
        setLocation((parent_width - square_width*8)/2, (parent_height-square_height*8)/2 - 10);
        setSize(square_width * 8, square_height * 8);
        setLayout(null);

        this.parent = parent;
        this.curr_click = null;

        init_board();

        addMouseListener(this);
        addMouseMotionListener(this);
        setVisible(true);
    }

    /*
    Given a 0-indexed rank and file number starting the count from top left
    will return the square coordinates as referred to in chess
    E.x. 0,0 --> a8
     */
    private String get_square_coord(int rank, int file) {
        String[] files = new String[] {"a", "b", "c", "d", "e", "f", "g", "h"};
        return String.format(files[file] + "%d", 8 - rank);
    }

    private void init_board() {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

        board = new Piece[8][8];
        int file = 0, rank = 0;
        for (char c: fen.toCharArray()) {
            if (c == '/') {
                file = 0; rank += 1;
                continue;
            }

            // CHAR IS A NUMBER => skip some squares
            if ((int) c >= 48 && (int) c <= 57) {
                int shift = (int) c - 48;

                if (shift == 8) {
                    file += 7;
                } else {
                    file += Math.max(shift, 1); // at minimum needs to move x over by 1
                }

                continue;
            }

            int color = -1;
            // UPPERCASE => white
            if ((int) c >= 65 && (int) c <= 90) { color = Piece.WHITE; }
            // LOWERCASE => black
            else if ((int) c >= 97 && (int) c <= 122) { color = Piece.BLACK; }

            Piece piece = new Piece(rank, file, c, color);
            board[rank][file] = piece;

            file += 1;
        }
        repaint();
    }

    private void set_gui_from_fen(String fen) {
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

    /*
    Given Piece.id parameter returns the image
     */
    private Image image_from_piece(char c) {
        String path = null;
        if ((int) c >= 65 && (int) c <= 90) {
            path = String.format("src/materials/white/%c.png", c);
        }
        // LOWERCASE => black
        else if ((int) c >= 97 && (int) c <= 122) {
            path = String.format("src/materials/black/%c.png", c);
        }

        if (path == null) { System.err.println("PATH WAS NULL: image_from_piece()"); }

        return Toolkit.getDefaultToolkit().getImage(path);
    }

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
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                    g2d.drawString(String.valueOf(8 - y), 3, y * square_height + 15);
                }
                if (y == 7) {
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                    g2d.drawString(files[x], x * square_width + square_width - 17, y * square_height + square_height - 5);
                }

            }

        }

        if (curr_click != null) {
            g2d.setColor(new Color(0x99BC6FFF, true));
            g2d.fillRect(curr_click.x * square_width, curr_click.y * square_height, square_width, square_height);
        }

        // Put pieces ont top of the board
        for (Piece[] rank : board) {
            for (Piece piece : rank) {
                if (piece == null) continue;
                g2d.drawImage(
                        image_from_piece(piece.id),
                        piece.file*square_width + 5, piece.rank*square_height + 5,
                        square_width - 10, square_height - 10,
                        this
                );
            }
        }
    }

    //TODO: initialize the board and have the paintcomponent just draw the state of the board
    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Handle clicking on a piece
        Point click = e.getPoint();
        Point trans_p = new Point(
                Math.max(Math.floorDiv(click.x, square_width), 0),
                Math.max(Math.floorDiv(click.y, square_height), 0)
        );

        // if there is no piece reset the square
        if (board[trans_p.y][trans_p.x] == null) {
            curr_click = null;
        } else {
            if (trans_p.equals(curr_click)) {
                // clicked on same piece --> clear the highlighted square
                curr_click = null;
            } else {
                // clicked on new piece --> update highlight
                curr_click = trans_p;
            }
        }

        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
