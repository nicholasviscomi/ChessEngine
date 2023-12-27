import java.awt.*;

@SuppressWarnings("SpellCheckingInspection")
public class Move {
    Piece piece;
    Piece captured = null;
    Point to;
    Point from;

    Move(Piece piece, Point from, Point to) {
        this.piece = piece;
        this.to = to;
        this.from = from;
    }

    // Need this for en passant, where the move is different from the peice being captured
    Move(Piece piece, Point from, Point to, Piece captured) {
        this.piece = piece;
        this.to = to;
        this.from = from;
        this.captured = captured;
    }
}
