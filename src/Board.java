import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

@SuppressWarnings("SpellCheckingInspection")
public class Board extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    int square_width = 75, square_height = 75;
    Frame parent;
    private Graphics2D g2d;
    private Piece[][] board;
    private Point curr_click;
    private Piece selected_piece = null;
    private int side_to_move = Piece.WHITE;

    private final HashMap<Character, Point> piece_map = new HashMap<>();
    private final HashMap<Character, Point> test_piece_map = new HashMap<>();
    private final Engine engine;

    public int get_side_to_move() {
        return side_to_move;
    }
    Board(int parent_width, int parent_height, Frame parent, Engine engine) {
        setLocation((parent_width - square_width*8)/2, (parent_height-square_height*8)/2 - 10);
        setSize(square_width * 8, square_height * 8);
        setLayout(null);

        this.parent = parent;
        this.curr_click = null;
        this.engine = engine;

        init_board();

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setVisible(true);
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

            int color = 0;
            // UPPERCASE => white
            if ((int) c >= 65 && (int) c <= 90) { color = Piece.WHITE; }
            // LOWERCASE => black
            else if ((int) c >= 97 && (int) c <= 122) { color = Piece.BLACK; }

            Piece piece = new Piece(rank, file, c, color);
            board[rank][file] = piece;
            piece_map.put(c, piece.get_point());

            file += 1;
        }
        repaint();
    }

    private Piece piece_from_id(Piece[][] board, HashMap<Character, Point> piece_map, char id) {
        return board[piece_map.get(id).y][piece_map.get(id).x];
    }

    /*
    Exapnds out from the given king in every possible attacking direction. If a piece of the correct type
    is present, that means the king is being attacked
     */
    public boolean king_in_check(Piece[][] board, HashMap<Character, Point> map, int king_side) {
        /*
        Expand out from king in all possible directions to see if it's being attacked
        * Uses a given board (so this code can be used to check whether a move is legal)
         */

        Piece king = piece_from_id(board, map, king_side == Piece.WHITE ? 'K' : 'k');
        int y = king.rank;
        int x = king.file;

        int[] knight_dy =   {2,  2, 1, -1,  1, -1, -2, -2};
        int[] knight_dx =   {1, -1, 2,  2, -2, -2,  1, -1};
        int[] rook_dx   =   {1, -1, 0,  0};
        int[] rook_dy   =   {0,  0, 1, -1};
        int[] bishop_dx =   {1,  1, -1, -1};
        int[] bishop_dy =   {1, -1,  1, -1};
        int[] pawn_dx   =   {1, -1};
        int[] pawn_dy   =   {1,  1};

        int[][] all_dy = {
                knight_dy, rook_dy, bishop_dy, pawn_dy
        };
        int[][] all_dx = {
                knight_dx, rook_dx, bishop_dx, pawn_dx
        };

        for (int i = 0; i < all_dy.length; i++) {
            int [] dys = all_dy[i], dxs = all_dx[i];
            for (int j = 0; j < dys.length; j++) {
                int dx = dxs[j];
                int dy = dys[j];

                int scale = 1;
                while (true) {
                    //target x and y
                    int ty = y + (dy * king.color * scale);
                    int tx = x + (dx * king.color * scale); // multiply by color to search the correct direction

                    // if off the screen break
                    if (ty < 0 || ty > 7 || tx < 0 || tx > 7) break;

                    Piece target = board[ty][tx];
                    if (target != null) { // square is open
                        if (target.color == (king.color * -1)) { // opposite color piece
                            switch (i) {
                                case 0 -> {
                                    // there is an opposite colored knight attacking the king
                                    if (
                                            target.type() == 'n'
                                    ) return true;
                                }
                                case 1 -> {
                                    // opposite colored rook or queen attacking the king
                                    if (
                                            target.type() == 'r' || target.type() == 'q'
                                    ) return true;
                                }
                                case 2 -> {
                                    // opposite colored bishop or queen attacking the king
                                    if (
                                            target.type() == 'b' || target.type() == 'q'
                                    ) return true;
                                }
                                case 3 -> {
                                    // pawn attacking the king
                                    if (
                                            target.type() == 'p'
                                    ) return true;
                                }
                            }
                        }

                        // hit a piece --> no more searching this direction
                        break;
                    }

                    if (i == 0 || i == 3)
                        break; // looking at knight or pawn moves; don't reiterate

                    scale += 1;
                }
            }


        }

        return false;
    }

    /*
    - Given the moves a piece can make (as described by the dx and dy params) it returns the legal moves
    - If early exit is false, it will search the entire board for the given direction vectors
        - making it true allows support for knights, who have more limited mobility
     */
    public ArrayList<Move> get_piece_moves(int[] piece_dx, int[] piece_dy, Piece piece, boolean early_exit) {
        if (piece == null) return null;

        int y = piece.rank;
        int x = piece.file;
        ArrayList<Move> moves = new ArrayList<>();
        boolean in_check = king_in_check(board, piece_map, side_to_move);

        for (int i = 0; i < piece_dx.length; i++) {
            int dx = piece_dx[i];
            int dy = piece_dy[i];

            int scale = 1;
            while (true) {
                int ty = y + (dy * piece.color * scale);
                int tx = x + (dx * piece.color * scale); // multiply by color to search the correct direction
                if (ty < 0 || ty > 7 || tx < 0 || tx > 7) break;
                Point tpoint = new Point(tx, ty);
                Piece target = board[ty][tx];

                Piece[][] test_board = test_move_piece(piece.get_point(), tpoint);
                double move_eval = engine.evaluate(test_board);

                boolean king_in_check_from_move = king_in_check(
                            test_board,
                            test_piece_map,
                            side_to_move
                );

                /*
                possible moves are open squares or captures:
                - it is legal if the king is not in check after the move
                 */
                if (target == null || target.color == piece.color * -1){
                    if (!king_in_check_from_move) {
                        moves.add(new Move(piece, piece.get_point(), tpoint, move_eval));
                    }

                    if (in_check) {
                        if (piece.type() != 'n') break;
                    }
                }

                // once you hit a piece in a direction, you're done
                if (target != null) { break; }
                if (early_exit) break;

                scale += 1;
            }

        }

        return moves;
    }

    public ArrayList<Move> get_legal_moves(Piece piece, int side_to_move) {
        ArrayList<Move> moves = new ArrayList<>();

        boolean in_check = king_in_check(board, piece_map, side_to_move);

        int x = piece.file, y = piece.rank;
        switch (piece.id) {
            // loop through directions for each piece type
            case 'p', 'P' -> {
                int[] pawn_attacks_dx = {1, -1};
                int[] pawn_attacks_dy = {1, 1};
                // to find en passant: when dx==0 and dy==2, look left and righ to see
                for (int i = 0; i < pawn_attacks_dy.length; i++) {
                    int dx = pawn_attacks_dx[i];
                    int dy = pawn_attacks_dy[i];
                    // multiply by color to search the correct direction
                    //target x and y coordinates
                    int ty = y + (dy * piece.color);
                    int tx = x + (dx * piece.color);
                    if (ty < 0 || ty > 7 || tx < 0 || tx > 7) continue;
                    Point tpoint = new Point(tx, ty);
                    Piece[][] test_board = test_move_piece(piece.get_point(), tpoint);
                    double move_eval = engine.evaluate(test_board);

                    Piece target = board[ty][tx];


                    if (target != null && target.color == (piece.color * -1)) { // piece is capturable
                        if (in_check) {
                            // if king is in check, only add moves which stop it
                            boolean stops_check = !king_in_check(
                                    test_board,
                                    test_piece_map,
                                    side_to_move
                            );
                            if (stops_check) {
                                moves.add(new Move(piece, piece.get_point(), tpoint, move_eval));
                            }
                        } else {
                            // if king is not in check, add moves which don't put him in check
                            if (!king_in_check(test_board, test_piece_map, side_to_move)) {
                                moves.add(new Move(piece, piece.get_point(), tpoint, move_eval));
                            }
                        }

                    }
                }

                int[] ep_dx = {-1, 1};
                for (int dx : ep_dx) {
                    // multiply by color to search the correct direction
                    //target x and y coordinates
                    // ty = y
                    int tx = x + (dx * piece.color);
                    if (y < 0 || y > 7 || tx < 0 || tx > 7) continue;
                    Piece target = board[y][tx]; // pawn side by side other pawn

                    // forward 1, over 1
                    Point tpoint = new Point(x + (dx * piece.color), y + piece.color);
                    Piece[][] test_board = test_move_piece(piece.get_point(), tpoint);
                    double move_eval = engine.evaluate(test_board);

                    if (target != null && target.color == (piece.color * -1)) { // capturable piece
                        if (target.open_to_en_passant && target.type() == 'p') {
                            if (in_check) {
                                boolean stops_check = !king_in_check(
                                        test_board,
                                        test_piece_map,
                                        side_to_move
                                );
                                if (stops_check) {
                                    moves.add(new Move(piece, piece.get_point(), tpoint, board[y][tx], move_eval));
                                }
                            } else {
                                // doesn't put king in check
                                // run this last because it's the most computationally intensive
                                if (!king_in_check(test_board, test_piece_map, side_to_move)) {
                                    moves.add(
                                            new Move(piece, piece.get_point(), tpoint, board[y][tx], move_eval)
                                    );
                                }
                            }

                        }
                    }
                }

                // move one forward if there are no pieces
                if (board[y + piece.color][x] == null) {
                    Point tpoint = new Point(x, y + piece.color);
                    Piece[][] test_board = test_move_piece(piece.get_point(), tpoint);
                    double move_eval = engine.evaluate(test_board);

                    if (in_check) {
                        boolean stops_check = !king_in_check(
                                test_move_piece(piece.get_point(), tpoint),
                                test_piece_map,
                                side_to_move
                        );
                        if (stops_check) {
                            moves.add(new Move(piece, piece.get_point(), tpoint, move_eval));
                        }
                    } else {
                        if (!king_in_check(test_move_piece(piece.get_point(), tpoint), test_piece_map, side_to_move)) {
                            moves.add(new Move(piece, piece.get_point(), tpoint, move_eval));
                        }
                    }

                    // double push on first move AND must be nothing blocking
                    if (piece.has_not_moved && board[y + (2 * piece.color)][x] == null) {
                        tpoint = new Point(x, y + (2 * piece.color));
                        if (in_check) {
                            boolean stops_check = !king_in_check(
                                    test_move_piece(piece.get_point(), tpoint),
                                    test_piece_map,
                                    side_to_move
                            );
                            if (stops_check) {
                                moves.add(new Move(piece, piece.get_point(), tpoint, move_eval));
                            }
                        } else {
                            if (!king_in_check(test_move_piece(piece.get_point(), tpoint), test_piece_map, side_to_move)) {
                                moves.add(new Move(piece, piece.get_point(), tpoint, move_eval));
                            }
                        }
                    }
                }

            }
            case 'k', 'K' -> {
                int[] king_dx = {0, 1, 0, -1, 1, 1, -1, -1};
                int[] king_dy = {1, 0, -1, 0, 1, -1, 1, -1};
                for (int i = 0; i < king_dx.length; i++) {
                    int dx = king_dx[i];
                    int dy = king_dy[i];
                    // multiply by color to search the correct direction
                    //target x and y coordinates
                    int ty = y + (dy * piece.color);
                    int tx = x + (dx * piece.color);
                    if (ty < 0 || ty > 7 || tx < 0 || tx > 7) continue;
                    Point tpoint = new Point(tx, ty);
                    Piece[][] test_board = test_move_piece(piece.get_point(), tpoint);
                    double move_eval = engine.evaluate(test_board);

                    Piece target = board[ty][tx];
                    if (target == null || target.color == (piece.color * -1)) { // square is open or can capture it
                        // and move does NOT put king in check
                        if (!king_in_check(test_board, test_piece_map, side_to_move)) {
                            moves.add(new Move(piece, piece.get_point(), tpoint, move_eval));
                        }
                    }
                }

                // Castling
                /*
                If (
                 king has not moved
                 && (rooks in corner && have not moved)
                 && no pieces in between
                 && not in check
                 && not in check by castling
                ) { castle() }
                 */

                if (!in_check && piece.has_not_moved) {
                    int x_ = 0;
                    while (x_ <= 7) {
                        Piece p = board[piece.rank][x_];
                        if (p != null) {
                            // if the piece is not == (either rook or king)
                            // parantheses are cruicial!
                            if (!(p.type() == 'r' || p.type() == 'k')) {
                                // if there are any non-rook or king pieces on the back rank, castling not available
                                // on this side --> check the other one
                                if (x_ < piece.file) {
                                    x_ = piece.file + 1;
                                    continue;
                                } else { break; }

                            } else  {
                                if (!p.has_not_moved) {
                                    // only rooks and kings, but the rooks have already moved, so castling unavailable
                                    if (x_ < piece.file) {
                                        x_ = piece.file + 1;
                                        continue;
                                    } else { break; }
                                }
                            }
                        }


                        if (x_ >= 2 && x <= 6 && x_ != 4) {
                            // these x coordinatres are all the possible places where the king will move
                            // --> must check whether moving there will put him in check (if so, invalid)
                            Point tpoint = new Point(x_, piece.rank);
                            if (king_in_check(test_move_piece(piece.get_point(), tpoint), test_piece_map, side_to_move)) {
                                if (x_ < piece.file) {
                                    x_ = piece.file + 1;
                                    continue;
                                } else { break; }
                            }
                        }

                        x_++;

                        // after checking the rook on the last file, add the move if everything is ok
                        // x_ was incremented so it will be 8 if everything is valid, and not 7 (ends the while loop)
                        if (x_ == piece.file || x_ == 8) {
                            // if either of these points are reached, that side castling is eligible
                            int dir = x_ == piece.file ? -1 : 1;
                            Point tpoint = new Point(piece.file + 2*dir, piece.rank);
                            double move_eval = engine.evaluate(test_move_piece(piece.get_point(), tpoint));
                            moves.add(new Move(piece, piece.get_point(), tpoint, move_eval));
                        }
                    }
                }

            }
            case 'n', 'N' -> {
                int[] knight_dy = {2, 2, 1, -1, 1, -1, -2, -2};
                int[] knight_dx = {1, -1, 2, 2, -2, -2, 1, -1};
                moves.addAll(get_piece_moves(knight_dx, knight_dy, piece, true));
            }
            case 'r', 'R' -> {
                int[] rook_dx = {1, -1, 0, 0};
                int[] rook_dy = {0, 0, 1, -1};
                moves.addAll(get_piece_moves(rook_dx, rook_dy, piece, false));
            }
            case 'b', 'B' -> {
                int[] bishop_dx = {1,  1, -1, -1};
                int[] bishop_dy = {1, -1,  1, -1};
                moves.addAll(get_piece_moves(bishop_dx, bishop_dy, piece, false));
            }
            case 'q', 'Q' -> {
                int[] rook_dx = {1, -1, 0, 0};
                int[] rook_dy = {0, 0, 1, -1};
                int[] bishop_dx = {1,  1, -1, -1};
                int[] bishop_dy = {1, -1,  1, -1};

                moves.addAll(get_piece_moves(rook_dx, rook_dy, piece, false));
                moves.addAll(get_piece_moves(bishop_dx, bishop_dy, piece, false));
            }
            default -> {}
        }

        return moves;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g2d = (Graphics2D) g;

        String[] files = new String[] {"A", "B", "C", "D", "E", "F", "G", "H"};
        Color light_square = new Color(231, 214, 185);
        Color dark_square = new Color(115, 109, 171);
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
                    g2d.drawString(files[x], x * square_width + square_width - 12, y * square_height + square_height - 5);
                }

            }

        }

        if (curr_click != null) {
            g2d.setColor(light_square);
            g2d.fillRect(curr_click.x * square_width, curr_click.y * square_height, square_width, square_height);
            g2d.setColor(new Color(0x7CFF3131, true));
            g2d.fillRect(curr_click.x * square_width, curr_click.y * square_height, square_width, square_height);
        }

        for (Piece[] rank : board) {
            for (Piece piece : rank) {
                if (piece == null) continue;
                g2d.drawImage(
                        piece.get_image(piece.id),
                        piece.file*square_width + 5, piece.rank*square_height + 5,
                        square_width - 10, square_height - 10,
                        this
                );
            }
        }

        if (selected_piece != null) {
            ArrayList<Move> legal_moves = get_legal_moves(selected_piece, side_to_move);
            for (Move move : legal_moves) {
                if (move == null) break;

//                    g2d.setColor(new Color(0x99BC6FFF, true));
//                    g2d.fillRect(
//                            move.x * square_width, move.y * square_height,
//                            square_width, square_height
//                    );

                // draw dot on legal moves
                g2d.setColor(new Color(0x7CFF3131, true));
                g2d.setStroke(new BasicStroke(1));
                // black dot over sqaure to move to
                g2d.fillOval(
                        move.to.x*square_width + square_width/2 - 10,
                        move.to.y*square_height + square_height/2 - 10,
                        20, 20
                );
            }
        }

        // Put pieces ont top of the board
    }

    private Piece[][] deep_copy_board() {
        Piece[][] copy_board = new Piece[8][8];

        for (int i = 0; i < board.length; i++) {
            Piece[] row = board[i];
            for (int j = 0; j < row.length; j++) {
                if (row[j] == null) {
                    copy_board[i][j] = null;
                } else {
                    Piece p = new Piece(row[j]);
                    copy_board[i][j] = p;
                    test_piece_map.put(p.id, p.get_point());
                }
            }
        }
        return copy_board;
    }

    public Piece[][] test_move_piece(Point from, Point to) {

        Piece[][] copy_board = deep_copy_board();

        copy_board[to.y][to.x] = copy_board[from.y][from.x];
        copy_board[from.y][from.x] = null;

        copy_board[to.y][to.x].move(to);
        test_piece_map.replace(copy_board[to.y][to.x].id, to);
        return copy_board;
    }

    public void move_piece_in_place(Move move) {
        Point to = move.to;
        Point from = move.from;

        board[to.y][to.x] = board[from.y][from.x];
        board[from.y][from.x] = null;

        board[to.y][to.x].move(to);
        piece_map.replace(board[to.y][to.x].id, to);

        if (move.captured != null) {
            Point p = move.captured.get_point();
            board[p.y][p.x] = null;
        }

        // no matter what, remove en passant rights
        for (Piece[] pieces : board) {
            for (Piece piece : pieces) {
                if (piece != null) {
                    piece.open_to_en_passant = false;
                }
            }
        }

        // only give en passant rights to the one piece which just moved
        if (move.piece.type() == 'p') {
            if (Math.abs(move.to.y - move.from.y) == 2) {
                // pawn moved forward twice
                board[move.to.y][move.to.x].open_to_en_passant = true;
            }

            if (move.to.y == 0 || move.to.y == 7) {
                move.piece.promote();
            }
        }

        // handle castling
        if (move.piece.type() == 'k') {
            if (Math.abs(move.to.x - move.from.x) == 2) {
                // king was already moved above --> move the rook
                if (move.to.x < move.from.x) {
                    // queen side
                    Piece qs_rook = board[move.piece.rank][0];
                    move_piece_in_place(
                            new Move(
                                    qs_rook,
                                    qs_rook.get_point(),
                                    new Point(3, move.piece.rank)
                            )
                    );
                } else {
                    // king side
                    Piece ks_rook = board[move.piece.rank][7];
                    move_piece_in_place(
                            new Move(ks_rook, ks_rook.get_point(),
                                    new Point(5, move.piece.rank)
                            )
                    );
                }
            }
        }

        engine.update_eval(move.evaluation);
    }

    public void play_sound(String path) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                    Objects.requireNonNull(this.getClass().getResource(path))
            );
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Handle clicking on a piece
        Point click = e.getPoint();
        Point trans_p = new Point(
                Math.max(Math.floorDiv(click.x, square_width), 0),
                Math.max(Math.floorDiv(click.y, square_height), 0)
        );

        // check to see if a move should be made
        if (selected_piece != null) {
            for (Move move : get_legal_moves(selected_piece, side_to_move)) {
                if (trans_p.equals(move.to)) {

                    // need to specify here that the opposite king should be searched and not the side_to_move king
                    if (king_in_check(test_move_piece(move.from, move.to), test_piece_map, side_to_move * -1)) {
                        play_sound("materials/audio/check.wav");
                    } else if (board[move.to.y][move.to.x] != null) {
                        // piece is being captured
                        play_sound("materials/audio/piece-capture.wav");

                    } else {
                        // regular move
                        play_sound("materials/audio/piece-move.wav");
                    }

                    move_piece_in_place(move);

                    side_to_move *= -1;
                    // Random move generation
//                    Move random_move = engine.random_piece_move(board);
                    Move random_move = engine.get_greedy_capture_move(board, side_to_move);
                    if (random_move != null) {
                        move_piece_in_place(random_move);
                    }

                    curr_click = null;
                    selected_piece = null;

                    side_to_move *= -1;

                    repaint();
                    return;
                }
            }
        }

        Piece target = board[trans_p.y][trans_p.x];
        // if there is no piece reset the square
        if (target == null || target.color != side_to_move) {
            curr_click = null;
            selected_piece = null;
        } else {
            if (trans_p.equals(curr_click)) {
                // clicked on same piece --> clear the highlighted square
                curr_click = null;
                selected_piece = null;
            } else {
                // clicked on new piece --> update highlight
                curr_click = trans_p;
                selected_piece = board[trans_p.y][trans_p.x];
            }
        }

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println("asdfksadf");
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT -> {
                System.out.println("left arrow press");
            }
            case KeyEvent.VK_RIGHT-> {
                System.out.println("right arrow press");
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        System.out.println("pouwehounmacs");
    }

    @Override
    public void mouseClicked(MouseEvent e) {

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

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
