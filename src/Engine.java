import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
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
    Searches for any move where it can capture the most materials and takes it—— no holds barred
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
                    if (best_move == null || m.evaluation < best_move.evaluation) {
                        // check for less than because a more negative eval means black is winning
                        best_move = m;
                        equal_moves.clear(); // if there is a new better move, clear the equal moves and
                        // then add to it if there are any other moves that equal this
                        // new best move
                    }
                    if (best_move.evaluation == m.evaluation) {
                        equal_moves.add(m);
                    }

                    // now search white's potential responses
//                    Piece[][] test_board = this.board.test_move_piece(m.from, m.to);
//                    for (Piece[] test_row : test_board) {
//                        for (Piece test_piece : test_row) {
//                            if (test_piece == null || test_piece.color != side_to_move * -1) continue;
//
//                            ArrayList<Move> test_moves = this.board.get_legal_moves(test_piece, side_to_move * -1);
//                            for (Move test_move : test_moves) {
//                                if (best_move == null || test_move.evaluation > best_move.evaluation) {
//                                    best_move = m;
//                                }
//                            }
//                        }
//                    }

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

        Move best_move = null;
        ArrayList<Move> equal_moves = new ArrayList<>();
        for (Piece[] row : board) {
            for (Piece piece : row) {
                if (piece == null || piece.color != side_to_move) continue;

                ArrayList<Move> moves = this.board.get_legal_moves(piece, side_to_move);
                for (Move m : moves) {
                    if (best_move == null || m.evaluation < best_move.evaluation) {
                        // check for less than because a more negative eval means black is winning
                        best_move = m;
                        equal_moves.clear(); // if there is a new better move, clear the equal moves and
                                             // then add to it if there are any other moves that equal this
                                             // new best move
                    }
                    if (best_move.evaluation == m.evaluation) {
                        equal_moves.add(m);
                    }

                    // now search white's potential responses
//                    Piece[][] test_board = this.board.test_move_piece(m.from, m.to);
//                    for (Piece[] test_row : test_board) {
//                        for (Piece test_piece : test_row) {
//                            if (test_piece == null || test_piece.color != side_to_move * -1) continue;
//
//                            ArrayList<Move> test_moves = this.board.get_legal_moves(test_piece, side_to_move * -1);
//                            for (Move test_move : test_moves) {
//                                if (best_move == null || test_move.evaluation > best_move.evaluation) {
//                                    best_move = m;
//                                }
//                            }
//                        }
//                    }

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

    public double evaluate(Piece[][] board) {
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

    public void update_eval(double new_eval) {
        current_eval = new_eval;
        repaint();
    }
}
