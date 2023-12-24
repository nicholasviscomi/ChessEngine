public class Pawn extends Piece {
    // flag to be set when a pawn moves two squares
    public boolean is_open_to_en_passant = false;
    public boolean been_promoted = false;

    Pawn(int rank, int file, char id, int color) {
        super(rank, file, id, color);
    }

}
