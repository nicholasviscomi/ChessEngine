import java.awt.*;

@SuppressWarnings("SpellCheckingInspection")
public class Move {
    Piece piece;
    Point to;

    Move(Piece piece, Point to) {
        this.piece = piece;
        this.to = to;
    }
}
