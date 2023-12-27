import java.awt.*;

@SuppressWarnings("SpellCheckingInspection")
public class Move {
    Piece piece;
    Point to;
    Point from;

    Move(Piece piece, Point from, Point to) {
        this.piece = piece;
        this.to = to;
        this.from = from;
    }
}
