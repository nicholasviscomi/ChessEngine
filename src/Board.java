import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressWarnings("SpellCheckingInspection")
public class Board extends JPanel implements MouseListener, MouseMotionListener {
    int square_width = 75, square_height = 75;
    Frame parent;
    private Graphics2D g2d;
    private Piece[][] board;
    private Point curr_click;
    private Piece selected_piece = null;
    private int side_to_move = Piece.WHITE;

    private HashMap<Character, Point> piece_map = new HashMap<>();
    private HashMap<Character, Point> test_piece_map = new HashMap<>();

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

    private void init_board() {
        String fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
//        String fen = "rnbqkbnr/ppp1pppp/4P3/8/8/3p4/PPPP1PPP/RNBQKBNR";

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

    public boolean king_in_check(Piece[][] board, HashMap<Character, Point> map) {
        /*
        Expand out from king in all possible directions to see if it's being attacked
        * Uses a given board (so this code can be used to check whether a move is legal)
         */

        Piece king = piece_from_id(board, map, side_to_move == Piece.WHITE ? 'K' : 'k');
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
                knight_dy, rook_dy, bishop_dy, pawn_dx
        };
        int[][] all_dx = {
                knight_dx, rook_dx, bishop_dx, pawn_dy
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
                                            Character.toLowerCase(target.id) == 'n'
                                    ) return true;
                                }
                                case 1 -> {
                                    // opposite colored rook or queen attacking the king
                                    if (
                                            Character.toLowerCase(target.id) == 'r' ||
                                            Character.toLowerCase(target.id) == 'q'
                                    ) return true;
                                }
                                case 2 -> {
                                    // opposite colored bishop or queen attacking the king
                                    if (
                                            Character.toLowerCase(target.id) == 'b' ||
                                            Character.toLowerCase(target.id) == 'q'
                                    ) return true;
                                }
                                case 3 -> {
                                    // pawn attacking the king
                                    if (
                                            Character.toLowerCase(target.id) == 'p'
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
    public ArrayList<Point> get_piece_moves(int[] piece_dx, int[] piece_dy, Piece piece, boolean early_exit) {
        int y = piece.rank;
        int x = piece.file;
        ArrayList<Point> moves = new ArrayList<>();
        boolean in_check = king_in_check(board, piece_map);

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


                boolean stops_check = false;
                if (in_check) {
                    stops_check = !king_in_check(
                            test_move_piece(piece.get_point(), tpoint),
                            test_piece_map
                    );
                }


                if (target == null) { // square is open
                    if (in_check) {
                        if (stops_check) {
                            moves.add(tpoint);
                            // only a knight can block a check in multiple ways
                            if (Character.toLowerCase(piece.id) != 'n') break;
                        }
                    } else {
                        moves.add(tpoint);
                    }
                } else {
                    if (target.color == (piece.color * -1)) {
                        if (in_check) {
                            if (stops_check) {
                                moves.add(tpoint);
                                if (Character.toLowerCase(piece.id) != 'n') break;
                            }
                        }

                        // ensure this move doesn't put king in check
                        if (!king_in_check(test_move_piece(piece.get_point(), tpoint), test_piece_map)) {
                            moves.add(tpoint);
                        }
                    }
                    // hit a piece --> no more searching this direction
                    break;
                }


                if (early_exit) break;

                scale += 1;
            }

        }

        return moves;
    }

    public ArrayList<Point> get_legal_moves(Piece piece) {
        ArrayList<Point> moves = new ArrayList<>(); // maximum possible legal moves is 218 is some very obscure position

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

                    Piece target = board[ty][tx];
                    if (target != null && target.color == (piece.color * -1)) {
                        if (!king_in_check(test_move_piece(piece.get_point(), tpoint), test_piece_map)) {
                            moves.add(tpoint);
                        }
                    }
                }

                // move one forward if there are no pieces
                if (board[y + piece.color][x] == null) {
                    Point tpoint = new Point(x, y + piece.color);
                    if (!king_in_check(test_move_piece(piece.get_point(), tpoint), test_piece_map)) {
                        moves.add(tpoint);
                    }
                    // double push on first move AND must be nothing blocking
                    if (piece.has_not_moved && board[y + (2 * piece.color)][x] == null) {
                        tpoint = new Point(x, y + (2 * piece.color));
                        if (!king_in_check(test_move_piece(piece.get_point(), tpoint), test_piece_map)) {
                            moves.add(tpoint);
                        }
                    }
                }

            }
            case 'n', 'N' -> {
                int[] knight_dy = {2, 2, 1, -1, 1, -1, -2, -2};
                int[] knight_dx = {1, -1, 2, 2, -2, -2, 1, -1};
                moves.addAll(get_piece_moves(knight_dx, knight_dy, piece, true));
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

                    Piece target = board[ty][tx];
                    if (target == null || target.color == (piece.color * -1)) { // square is open or can capture it
                        // and move does NOT put king in check
                        if (!king_in_check(test_move_piece(piece.get_point(), tpoint), test_piece_map)) {
                            moves.add(tpoint);
                        }
                    }
                }
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
                    g2d.drawString(files[x], x * square_width + square_width - 12, y * square_height + square_height - 5);
                }

            }

        }

        //TODO: move this code to mouseclicked. This needs to be drawing from a global legal moves variable
        //TODO: this way mouseclicked will have access to the legal moves and it will be able to know if the
        //TODO: the user just clicked on one of the legal moves
        if (curr_click != null) {
            g2d.setColor(new Color(0x99BC6FFF, true));
            g2d.fillRect(curr_click.x * square_width, curr_click.y * square_height, square_width, square_height);

            if (selected_piece != null) {
                ArrayList<Point> legal_moves = get_legal_moves(selected_piece);
                for (Point move : legal_moves) {
                    if (move == null) break;
                    g2d.setColor(new Color(0x99BC6FFF, true));
                    g2d.fillRect(
                            move.x * square_width, move.y * square_height,
                            square_width, square_height
                    );

                    g2d.setColor(new Color(0xCB000000, true));
                    g2d.setStroke(new BasicStroke(1));
                    // black dot over sqaure to move to
                    g2d.fillOval(
                            move.x*square_width + square_width/2 - 10,
                            move.y*square_height + square_height/2 - 10,
                            20, 20
                    );
                }
            }
        }

        // Put pieces ont top of the board
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
    }

    public void move_piece_in_place(Point from, Point to) {
        board[to.y][to.x] = board[from.y][from.x];
        board[from.y][from.x] = null;

        board[to.y][to.x].move(to);
        piece_map.replace(board[to.y][to.x].id, to);
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

    private void print_board() {
        for (Piece[] rank : board) {
            for (Piece piece : rank) {
                if (piece == null) {
                    System.out.print("x");
                }
                else {
                    System.out.print(piece.id);
                }
            }
            System.out.println();
        }
        System.out.println();
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
            for (Point move : get_legal_moves(selected_piece)) {
                if (trans_p.equals(move)) {
                    move_piece_in_place(selected_piece.get_point(), move);

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
}
