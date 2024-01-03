import java.awt.*;

public class Piece {
    public static final int WHITE = -1; // negative for searching legal moves purposes
    public static final int BLACK = 1;
    int rank, file;
    char id;

    // also functions as a way to determine which direction the pieces are moving on in terms
    // of the indices of the Piece[][] board variable
    int color;
    boolean has_not_moved;
    boolean open_to_en_passant = false;
    boolean has_castling_rights = false;

    Piece(Piece piece) {
        this.rank = piece.rank;
        this.file = piece.file;
        this.id = piece.id;
        this.color = piece.color;
        this.open_to_en_passant = piece.open_to_en_passant;
        this.has_not_moved = piece.has_not_moved;
        this.has_castling_rights = piece.has_castling_rights;
    }

    Piece(int rank, int file, char id, int color) {
        this.rank = rank;
        this.file = file;
        this.id = id;
        this.color = color;
        this.has_not_moved = true;
    }

    public void promote() {
        this.id = this.color == WHITE ? 'Q' : 'q';
    }

    public Image get_image(char c) {
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

    public char type() {
        return Character.toLowerCase(this.id);
    }
    public Point get_point() {
        return new Point(file, rank);
    }

    //NOTE: only moves the piece in code; the board must be updated for it to be seen
    public void move(Point target) {
        this.rank = target.y;
        this.file = target.x;
        this.has_not_moved = false;
    }
}

