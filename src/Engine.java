import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Engine extends JPanel {
    int square_width = 75, square_height = 75;
    double current_eval = 0.0;
    Engine(int parent_width, int parent_height) {
        setLocation((parent_width - square_width*8)/2 - 40, (parent_height-square_height*8)/2 - 10);
        setSize(30, square_height*8);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.BLACK);
        int black_height = square_height * 4 - ((int)current_eval * 20);
        g2d.fillRect(
                0, 0,
                30, black_height
        );


        g2d.setColor(Color.WHITE);
        g2d.fillRect(
                0, black_height,
                30, (square_height*8) - black_height
        );

        setVisible(true);
    }

    Map<Character, Integer> piece_values = Map.of(
            'p', 1,
            'b', 3,
            'n', 3,
            'r', 5,
            'q', 9,
            'k', 0
    );

    public Move search(Piece[][] board) {
        for (Piece[] row: board) {
            for (Piece piece : row) {

            }
        }

        return null;
    }

    public double evaluate(Piece[][] board) {
        double eval;

        int white_material = 0, black_material = 0;
        for (Piece[] row : board) {
            for (Piece piece : row) {
                if (piece == null) continue;

                if (piece.color == Piece.WHITE) {
                    white_material += piece_values.get(piece.type());
                } else {
                    black_material += piece_values.get(piece.type());
                }
            }
        }

        eval = white_material - black_material;

        return eval;
    }

    public void update_eval(double new_eval) {
        current_eval = new_eval;
        repaint();
    }
}
