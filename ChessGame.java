package chai;

import chesspresso.Chess;
import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.Position;

public class ChessGame {

	public Position position;

	public int rows = 8;
	public int columns = 8;

	public ChessGame() {
		position = new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		
		// these are some test positions
		//position = new Position("r5k1/p3Qpbp/2p3p1/1p6/q3bN2/6PP/PP3P2/K2RR3 b - - 0 1"); //black wins in 3
		//position = new Position("8/8/8/8/8/k1B5/BN6/K7 w - - 0 1"); //white wins in 6
		//position = new Position("8/4B2p/7n/4pqpk/P2n1b2/R3P2P/2r3P1/1Q3R1K b - - 0 1"); //black wins in 3
		//position = new Position("r2qk2r/pp6/2pbp3/2Pp1p2/3PBPp1/4PRp1/PP1BQ1P1/4R1K1 b kq - 0 20"); //black wins in 4
		//position = new Position("3q2k1/1pp2pp1/7p/5b2/1P6/2Q2P2/r5PP/2BrRK1R b - - 2 25"); //black wins in 3
		//position = new Position("2kr3r/1bbp1p2/p3pp2/1p4q1/4P3/PNN1PBP1/1PP3KP/1R1Q1R2 b - - 0 1"); //black wins in 3
	}

	public int getStone(int col, int row) {
		return position.getStone(Chess.coorToSqi(col, row));
	}
	
	public boolean squareOccupied(int sqi) {
		return position.getStone(sqi) != 0;
		
	}

	public boolean legalMove(short move) {
		
		for(short m: position.getAllMoves()) {
			if(m == move) return true;
		}
		System.out.println(java.util.Arrays.toString(position.getAllMoves()));
		System.out.println(move);
		return false;
	
	}

	// find a move from the list of legal moves from fromSqi to toSqi
	// return 0 if none available
	public short findMove(int fromSqi, int toSqi) {
		
		for(short move: position.getAllMoves()) {
			if(Move.getFromSqi(move) == fromSqi && 
					Move.getToSqi(move) == toSqi) return move;
		}
		return 0;
	}
	
	public void doMove(short move) {
		try {

			System.out.println("making move " + move);

			position.doMove(move);
			System.out.println(position);
		} catch (IllegalMoveException e) {
			System.out.println("illegal move!");
		}
	}

	public static void main(String[] args) {
		System.out.println();

		// Create a starting position using "Forsyth–Edwards Notation". (See
		// Wikipedia.)
		Position position = new Position(
				"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
		

		System.out.println(position);

	}
	
	

}
