import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class Engine extends JPanel {
    int square_width = 75, square_height = 75;
    double current_eval = 0.0;
    Board board = null;
    Frame parent;
    Engine(int parent_width, int parent_height, Frame parent) {
        setLocation((parent_width - square_width*8)/2 - 40, (parent_height-square_height*8)/2 - 10);
        setSize(30, square_height*8);
        this.parent = parent;
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

    int[][] pawntable = {
            {0,  0,  0,   0,   0,  0,  0, 0},
            {5, 10,  10, -20, -20,  10, 10, 5},
            {5, -5, -10,   0,   0, -10, -5, 5},
            {0,  0,   0,  20,  20,   0,  0, 0},
            {5,  5,  10,  25,  25,  10,  5, 5},
            {10, 10, 20, 30, 30, 20, 10, 10},
            {50, 50, 50, 50, 50, 50, 50, 50},
            {1000, 1000, 1000, 1000, 1000, 1000, 1000, 1000}
    };

    int[][] knightstable = {
            {-50, -40, -30, -30, -30, -30, -40, -50,},
            {-40, -20, 0, 5, 5, 0, -20, -40,},
            {-30, 5, 10, 15, 15, 10, 5, -30,},
            {-30, 0, 15, 20, 20, 15, 0, -30,},
            {-30, 5, 15, 20, 20, 15, 5, -30,},
            {-30, 0, 10, 15, 15, 10, 0, -30,},
            {-40, -20, 0, 0, 0, 0, -20, -40,},
            {-50, -40, -30, -30, -30, -30, -40, -50},
    };
    int[][] bishopstable = {
            {-20, -10, -10, -10, -10, -10, -10, -20,},
            {-10, 5, 0, 0, 0, 0, 5, -10,},
            {-10, 10, 10, 10, 10, 10, 10, -10,},
            {-10, 0, 10, 10, 10, 10, 0, -10,},
            {-10, 5, 5, 10, 10, 5, 5, -10,},
            {-10, 0, 5, 10, 10, 5, 0, -10,},
            {-10, 0, 0, 0, 0, 0, 0, -10,},
            {-20, -10, -10, -10, -10, -10, -10, -20},
    };
    int[][] rookstable = {
            {0, 0, 0, 5, 5, 0, 0, 0,},
            {-5, 0, 0, 0, 0, 0, 0, -5,},
            {-5, 0, 0, 0, 0, 0, 0, -5,},
            {-5, 0, 0, 0, 0, 0, 0, -5,},
            {-5, 0, 0, 0, 0, 0, 0, -5,},
            {-5, 0, 0, 0, 0, 0, 0, -5,},
            {5, 10, 10, 10, 10, 10, 10, 5,},
            {0, 0, 0, 0, 0, 0, 0, 0},
    };
    int[][] queenstable = {
            {-20, -10, -10, -5, -5, -10, -10, -20,},
            {-10, 0, 0, 0, 0, 0, 0, -10,},
            {-10, 5, 5, 5, 5, 5, 0, -10,},
            {0, 0, 5, 5, 5, 5, 0, -5,},
            {-5, 0, 5, 5, 5, 5, 0, -5,},
            {-10, 0, 5, 5, 5, 5, 0, -10,},
            {-10, 0, 0, 0, 0, 0, 0, -10,},
            {-20, -10, -10, -5, -5, -10, -10, -20},
    };
    int[][] kingstable = {
            {20, 30, 10, 0, 0, 10, 30, 20,},
            {20, 20, 0, 0, 0, 0, 20, 20,},
            {-10, -20, -20, -20, -20, -20, -20, -10,},
            {-20, -30, -30, -40, -40, -30, -30, -20,},
            {-30, -40, -40, -50, -50, -40, -40, -30,},
            {-30, -40, -40, -50, -50, -40, -40, -30,},
            {-30, -40, -40, -50, -50, -40, -40, -30,},
            {-30, -40, -40, -50, -50, -40, -40, -30}
    };
    Map<Character, int[][]> eval_tables = Map.of(
            'k', kingstable,
            'q', queenstable,
            'r', rookstable,
            'b', bishopstable,
            'n', knightstable,
            'p', pawntable
    );

    public Move get_random_piece_move(Piece[][] board, int side_to_move) {
        if (this.board == null) this.board = parent.get_board();

        ArrayList<Move> all_legal_moves = new ArrayList<>();

        for (Piece[] row : board) {
            for (Piece piece : row) {
                if (piece == null || piece.color != this.board.get_side_to_move()) continue;
                all_legal_moves.addAll(this.board.get_legal_moves(piece, side_to_move));
            }
        }

        if (all_legal_moves.size() == 0) return null;

        return all_legal_moves.get((int) (Math.random() * all_legal_moves.size()));
    }

    /*
    Gets best move according to the evaluation function. Since that just counts material, this
    searches for a move for black where it can capture the most material—— no holds barred.
    Will lose any piece for material
     */
    public Move get_greedy_capture_move(Piece[][] board, int side_to_move) {
        if (this.board == null) this.board = parent.get_board();

        Move best_move = null;
        ArrayList<Move> equal_moves = new ArrayList<>();
        for (Piece[] row : board) {
            for (Piece piece : row) {
                if (piece == null || piece.color != side_to_move) continue;

                ArrayList<Move> moves = this.board.get_legal_moves(piece, side_to_move);
                for (Move m : moves) {
                    Piece[][] test_board = this.board.test_move_piece(m.from, m.to);
                    double curr_eval = material_eval(test_board);
                    if (side_to_move == Piece.BLACK) {
                        // black wants more negative evaluations
                        if (best_move == null || curr_eval < best_move.evaluation) {
                            // check for less than because a more negative eval means black is winning
                            best_move = m;
                            equal_moves.clear(); // if there is a new better move, clear the equal moves and
                            // then add to it if there are any other moves that equal this
                            // new best move
                        }
                    }
                    if (side_to_move == Piece.WHITE) {
                        // white wants more positive evaluations
                        if (best_move == null || m.evaluation > best_move.evaluation) {
                            // check for less than because a more negative eval means black is winning
                            best_move = m;
                            equal_moves.clear(); // if there is a new better move, clear the equal moves and
                            // then add to it if there are any other moves that equal this
                            // new best move
                        }
                    }
                    if (best_move != null && best_move.evaluation == m.evaluation) {
                        equal_moves.add(m);
                    }

                }
            }
        }

        // if all the moves are equal (e.x. there are multiple ways to capture a piece)
        // pick a random one
        if (best_move != null && equal_moves.size() > 0) {
            if (best_move.evaluation == equal_moves.get(0).evaluation) {
                return equal_moves.get( (int) (Math.random()*equal_moves.size()));
            }
        }

        return best_move;
    }

    public Move get_greedy_move_with_tables(Piece[][] board, int side_to_move) {
        if (this.board == null) this.board = parent.get_board();

        Move best_move = null;
        ArrayList<Move> equal_moves = new ArrayList<>();
        for (Piece[] row : board) {
            for (Piece piece : row) {
                if (piece == null || piece.color != side_to_move) continue;

                ArrayList<Move> moves = this.board.get_legal_moves(piece, side_to_move);
                for (Move m : moves) {
                    Piece[][] test_board = this.board.test_move_piece(m.from, m.to);
                    int check_bonus = this.board.king_in_check(test_board, this.board.get_test_piece_map(), side_to_move*-1) ? 1 : 0;
                    double curr_eval = material_eval(test_board) + (double) eval_tables.get(piece.type())[m.to.y][m.to.x]/100 + check_bonus;
                    if (side_to_move == Piece.BLACK) {
                        // black wants more negative evaluations
                        if (best_move == null || curr_eval < best_move.evaluation) {
                            // check for less than because a more negative eval means black is winning
                            best_move = m;
                            equal_moves.clear(); // if there is a new better move, clear the equal moves and
                            // then add to it if there are any other moves that equal this
                            // new best move
                        }
                    }
                    if (side_to_move == Piece.WHITE) {
                        // white wants more positive evaluations
                        if (best_move == null || m.evaluation > best_move.evaluation) {
                            // check for less than because a more negative eval means black is winning
                            best_move = m;
                            equal_moves.clear(); // if there is a new better move, clear the equal moves and
                            // then add to it if there are any other moves that equal this
                            // new best move
                        }
                    }
                    if (best_move != null && best_move.evaluation - m.evaluation < 1) {
                        equal_moves.add(m);
                    }

                }
            }
        }

        // if all the moves are equal (e.x. there are multiple ways to capture a piece)
        // pick a random one
        if (best_move != null && equal_moves.size() > 0) {
            if (best_move.evaluation == equal_moves.get(0).evaluation) {
                return equal_moves.get( (int) (Math.random()*equal_moves.size()));
            }
        }

        return best_move;
    }

    public Move search(Piece[][] board, int side_to_move) {
        if (this.board == null) this.board = parent.get_board();



        return null;
    }

    public double material_eval(Piece[][] board) {
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

    public double evaluate(Piece[][] board) {
        return material_eval(board);
    }

    public void update_eval(double new_eval) {
        current_eval = new_eval;
        repaint();
    }
}
