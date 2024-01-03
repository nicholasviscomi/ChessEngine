import java.util.HashMap;
import java.util.Map;

public class Engine {
    Board board;
    Engine(Board board) {
        this.board = board;
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
        double eval = 0;

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
}
