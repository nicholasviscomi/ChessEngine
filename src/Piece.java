public class Piece {
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int rank, file;
    char id;
    int color;

    Piece(int rank, int file, char id, int color) {
        this.rank = rank;
        this.file = file;
        this.id = id;
        this.color = color;
    }
}
