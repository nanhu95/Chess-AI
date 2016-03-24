package chai;

import java.util.HashMap;
import java.util.Random;

import chesspresso.Chess;
import chesspresso.move.IllegalMoveException;
import chesspresso.move.Move;
import chesspresso.position.*;

public class MinimaxAI implements ChessAI{
	// random number generator
	private Random rand = new Random(0);
	// maximum search depth
	private final int MAXDEPTH = 5;
	// player that's maximizing
	private int maximizingPlayer;
	// transposition table
	HashMap<Long, Entry> tt;
	
	// the belows weights and position tables are taken from
	// https://chessprogramming.wikispaces.com/Simplified+evaluation+function
	// set weights for pieces
	private int p = 100;
	private int r = 500;
	private int n = 320;
	private int b = 330;
	private int q = 900;
	private int k = 20000;
	
	// tables of values for position
	private int[] PAWN_TABLE = {0,  0,  0,  0,  0,  0,  0,  0,
								50, 50, 50, 50, 50, 50, 50, 50,
								10, 10, 20, 30, 30, 20, 10, 10,
								5,  5, 10, 25, 25, 10,  5,  5,
								0,  0,  0, 20, 20,  0,  0,  0,
								5, -5,-10,  0,  0,-10, -5,  5,
								5, 10, 10,-20,-20, 10, 10,  5,
								0,  0,  0,  0,  0,  0,  0,  0};
	
	private int[] KNIGHT_TABLE = {-50,-40,-30,-30,-30,-30,-40,-50,
									-40,-20,  0,  0,  0,  0,-20,-40,
									-30,  0, 10, 15, 15, 10,  0,-30,
									-30,  5, 15, 20, 20, 15,  5,-30,
									-30,  0, 15, 20, 20, 15,  0,-30,
									-30,  5, 10, 15, 15, 10,  5,-30,
									-40,-20,  0,  5,  5,  0,-20,-40,
									-50,-40,-30,-30,-30,-30,-40,-50,};
	
	private int[] BISHOP_TABLE = {-20,-10,-10,-10,-10,-10,-10,-20,
									-10,  0,  0,  0,  0,  0,  0,-10,
									-10,  0,  5, 10, 10,  5,  0,-10,
									-10,  5,  5, 10, 10,  5,  5,-10,
									-10,  0, 10, 10, 10, 10,  0,-10,
									-10, 10, 10, 10, 10, 10, 10,-10,
									-10,  5,  0,  0,  0,  0,  5,-10,
									-20,-10,-10,-10,-10,-10,-10,-20,};
	
	private int[] ROOK_TABLE = {0,  0,  0,  0,  0,  0,  0,  0,
			  					5, 10, 10, 10, 10, 10, 10,  5,
			  					-5,  0,  0,  0,  0,  0,  0, -5,
			  					-5,  0,  0,  0,  0,  0,  0, -5,
			  					-5,  0,  0,  0,  0,  0,  0, -5,
			  					-5,  0,  0,  0,  0,  0,  0, -5,
			  					-5,  0,  0,  0,  0,  0,  0, -5,
			  					0,  0,  0,  5,  5,  0,  0,  0};
	
	private int[] QUEEN_TABLE = {-20,-10,-10, -5, -5,-10,-10,-20,
								-10,  0,  0,  0,  0,  0,  0,-10,
								-10,  0,  5,  5,  5,  5,  0,-10,
								-5,  0,  5,  5,  5,  5,  0, -5,
								0,  0,  5,  5,  5,  5,  0, -5,
								-10,  5,  5,  5,  5,  5,  0,-10,
								-10,  0,  5,  0,  0,  0,  0,-10,
								-20,-10,-10, -5, -5,-10,-10,-20};
// king tables are not in use	
//	private int[] KING_TABLE_INIT = {-30,-40,-40,-50,-50,-40,-40,-30,
//									-30,-40,-40,-50,-50,-40,-40,-30,
//									-30,-40,-40,-50,-50,-40,-40,-30,
//									-30,-40,-40,-50,-50,-40,-40,-30,
//									-20,-30,-30,-40,-40,-30,-30,-20,
//									-10,-20,-20,-20,-20,-20,-20,-10,
//									20, 20,  0,  0,  0,  0, 20, 20,
//									20, 30, 10,  0,  0, 10, 30, 20};
//	
//	private int[] KING_TABLE_END = {-50,-40,-30,-20,-20,-30,-40,-50,
//									-30,-20,-10,  0,  0,-10,-20,-30,
//									-30,-10, 20, 30, 30, 20,-10,-30,
//									-30,-10, 30, 40, 40, 30,-10,-30,
//									-30,-10, 30, 40, 40, 30,-10,-30,
//									-30,-10, 20, 30, 30, 20,-10,-30,
//									-30,-30,  0,  0,  0,  0,-30,-30,
//									-50,-30,-30,-30,-30,-30,-30,-50};
	
	// private Entry class for transposition table
	private class Entry {
		private int score;
		private int depth;
		private int type;
		// each entry has a score, a depth, and a type
		// type meaning whether exact, lower bound, or upper bound
		// exact is 0, lower is -1, upper is 1
		public Entry(int score, int depth, int type) {
			this.score = score;
			this.depth = depth;
			this.type = type;
		}
		// a few getter methods
		public int getScore() {
			return this.score;
		}
		public int getDepth() {
			return this.depth;
		}
		public int getType() {
			return this.type;
		}
	}
	
	public MinimaxAI(int maximizingPlayer) {
		this.maximizingPlayer = maximizingPlayer;
	}
	
	@Override
	public short getMove(Position position) {
		// initialize transposition table
		tt = new HashMap<Long, Entry>();
		// note that both regular minimax search and
		// alpha-beta pruning are included in this one file
		
		//return minimaxSearch(position, 0);
		return abPruning(position, 0);
	}
	
	// test if a position is terminal state
	public boolean isTerminalState(Position position) {
		// game should end if checkmate or stalemate
		return (position.isMate() || position.isStaleMate());
	}
	
	// check if search should cut off
	public boolean cutOffTest(Position position, int depth) {
		// cut off search if exceed maxdepth for reach terminal state
		return ((depth>=MAXDEPTH)||(isTerminalState(position)));
	}
	
	// generate a random utility for a position
	public int randUtility(Position position) {
		// if checkmate for a player, return max/min value
		if (position.isMate()) {
			// see whose turn it is
			if(position.getToPlay()==Chess.WHITE) {
				// give min value for white win
				// this is because black is maximizing
				return Integer.MIN_VALUE;
			}
			else {
				// give max value if white wins
				return Integer.MAX_VALUE;
			}
		}
		// if stalemate, return 0
		else if (position.isStaleMate()) {
			return 0;
		}
		// otherwise return a random utility that falls in btw min and max values
		else {
			return ((Integer.MIN_VALUE)/4+1)+rand.nextInt((Integer.MAX_VALUE)/4-(Integer.MIN_VALUE)/4);
		}
	}
	
	// generate a more useful utility for a position
	public int evalUtility(Position position) {
		// return the evaluation provided by Chesspresso
		// set to negative because of Chesspresso assuming white is maximizing
		//if (maximizingPlayer==Chess.WHITE) return position.getMaterial();
		//return -position.getMaterial();
		
		// set up initial numbers of all pieces
		int wp = 0, wr = 0, wn = 0, wb = 0, wq = 0, wk = 0;
		int bp = 0, br = 0, bn = 0, bb = 0, bq = 0, bk = 0;
		int value = 0;
		// give value for check or checkmate moves
		if(position.isMate()) value+=1000;
		if(position.isCheck()) value+=100;
		// go through all the squares, get what piece is on them
		// we know there are 64 squares
		// also get the position value
		for (int i=0; i<64; i++) {
			int st = position.getStone(i);
			switch(st) {
				case Chess.WHITE_PAWN: wp++; value+=PAWN_TABLE[i]; break;
				case Chess.WHITE_ROOK: wr++; value+=ROOK_TABLE[i]; break;
				case Chess.WHITE_KNIGHT: wn++; value+=KNIGHT_TABLE[i]; break;
				case Chess.WHITE_BISHOP: wb++; value+=BISHOP_TABLE[i]; break;
				case Chess.WHITE_QUEEN: wq++; value+=QUEEN_TABLE[i]; break;
				case Chess.WHITE_KING: wk++; break;
				case Chess.BLACK_PAWN: bp++; value+=PAWN_TABLE[63-i]; break;
				case Chess.BLACK_ROOK: br++; value+=ROOK_TABLE[63-i]; break;
				case Chess.BLACK_KNIGHT: bn++; value+=KNIGHT_TABLE[63-i]; break;
				case Chess.BLACK_BISHOP: bb++; value+=BISHOP_TABLE[63-i]; break;
				case Chess.BLACK_QUEEN: bq++; value+=QUEEN_TABLE[63-i]; break;
				case Chess.BLACK_KING: bk++; break;
			}
		}
		// take material heuristic
		if (maximizingPlayer==Chess.WHITE) {
			value+=wp*p+wr*r+wn*n+wb*b+wq*q+wk*k-bp*p-br*r-bn*n-bb*b-bq*q-bk*k;
			return value;
		}
		value+=bp*p+br*r+bn*n+bb*b+bq*q+bk*k-wp*p-wr*r-wn*n-wb*b-wq*q-wk*k;
		return value;
	}
	
	
	/*--------------------------MINIMAX SEARCH------------------------*/
	// depth-limited minimax search
	public short minimaxSearch(Position position, int depth) {
		// store index of move with greatest utility
		int maxIndex = 0;
		// store actual value of greatest utility
		int maxUtil = Integer.MIN_VALUE;
		// get all possible moves
		short [] moves = position.getAllMoves();
		for(int i=0; i<moves.length; i++) {
			// do the move
			try {
				position.doMove(moves[i]);
			} catch (IllegalMoveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// get utility from search
			int util = minValue(position,depth+1);
			// check if we get a new maximum
			if (util > maxUtil) {
				maxUtil = util;
				maxIndex = i;
			}
			// undo the move
			position.undoMove();
		}
		// return move that gives max utility
		
		System.out.println("Utility is "+maxUtil);
		System.out.println("Move to make is "+moves[maxIndex]);
		
		return moves[maxIndex];
	}
	
	// method for max-value
	public int maxValue(Position position, int depth) {
		// if we need to cut off search return utility
		if(cutOffTest(position, depth)) {
			
			return evalUtility(position);
		}
		// int to store utility, initially set to min
		int utility;
		utility = Integer.MIN_VALUE;
		// go through all possible values
		for(short move:position.getAllMoves()) {
			try {
				position.doMove(move);
			} catch (IllegalMoveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// want utility to be larger of current utility
			// and the minValue of the position
			utility = Integer.max(utility,minValue(position, depth+1));

			//System.out.println("Utility of maxValue is "+utility);

			position.undoMove();
		}
		return utility;
	}
	
	// method for min-value
	public int minValue(Position position, int depth) {
		// if we need to cut off search return utility
		if(cutOffTest(position, depth)) {
			
			//System.out.println("Cutting off search");
			
			return evalUtility(position);
		}
		// int to store utility, set initally to max
		int utility;
		utility = Integer.MAX_VALUE;
		// go through all possible moves
		for(short move:position.getAllMoves()) {
			try {
				position.doMove(move);
			} catch (IllegalMoveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// want utility to be smaller of current utility
			// and the maxValue of the position
			utility = Integer.min(utility,maxValue(position, depth+1));
			
			//System.out.println("Utility of minValue is "+utility);
			
			position.undoMove();
		}
		return utility;
	}
	
	
	
	
	/*---------------------------ALPHA-BETA PRUNING----------------------------*/
	
	// alpha-beta pruning
	public short abPruning(Position position, int depth) {
		// find the utility
		int utility = maxValue(position, Integer.MIN_VALUE, Integer.MAX_VALUE, depth);
		
		System.out.println("Utility is "+utility);
		
		// go through all moves
		for (short move:position.getAllMoves()) {
			try {
				position.doMove(move);
			} catch (IllegalMoveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// return the move with the same utility
			if (minValue(position,Integer.MIN_VALUE,Integer.MAX_VALUE,depth+1)==utility) {
				
				System.out.println("Move to make is "+move);
				
				position.undoMove();
				return move;
			}
			position.undoMove();
		}
		
		System.out.println("No move.");
		
		return Move.NO_MOVE;
		
	}
	
	// overload maxValue method for alpha-beta pruning
	public int maxValue(Position position, int alpha, int beta, int depth) {
		// if we need to cut off search return utility
		if(cutOffTest(position, depth)) {
			// below is code to get and add scores to the transposition table
			// fell free to comment out this section of code to run 
			// alpha-beta pruning without the table
			// check if position is in transposition table
			if(tt.containsKey(position.getHashCode())&&tt.get(position.getHashCode()).getDepth()>depth) {
				Entry entry = tt.get(position.getHashCode());
				// if score is exact of beta<=alpha, return directly
				if(beta<=alpha||entry.getType()==0)
					return entry.getScore();
				// if score is lower bound and score is at least beta, return score
				else if (entry.getType()==-1&&entry.getScore()>=beta) {
					return entry.getScore();
				}
				// if score is upper bound and score is at most beta, return score
				else if (entry.getType()==1&&entry.getScore()<=alpha) {
					return entry.getScore();
				}
			}
			// add scores to tt
			int score = evalUtility(position);
			// if score no more than alpha, it's upper bound
			if (score<=alpha) {
				tt.put(position.getHashCode(), new Entry(score, depth, 1));
			}
			// if score at least beta, it's lower bound 
			else if (score>=beta) {
				tt.put(position.getHashCode(), new Entry(score, depth, -1));
			}
			// otherwise score is exact
			else {
				tt.put(position.getHashCode(), new Entry(score, depth, 0));
			}
			return score;
		}
		// set utility to min value
		int utility = Integer.MIN_VALUE;
		// go through all possible moves
		for(short move:position.getAllMoves()) {
			try {
				position.doMove(move);
			} catch (IllegalMoveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			utility = Integer.max(utility, minValue(position, alpha, beta, depth+1));
			// undo move
			position.undoMove();
			// if utility is >= beta, return utility
			if (utility >= beta) return utility;
			// set alpha
			alpha = Integer.max(alpha, utility);	
		}
		return utility;
	}
	
	// overload minValue method for alpha-beta pruning
	public int minValue(Position position, int alpha, int beta, int depth) {
		// if we need to cut off search return utility
		if(cutOffTest(position, depth)) {
			// below is code to get and add scores to the transposition table
			// fell free to comment out this section of code to run 
			// alpha-beta pruning without the table
			// check if position is in transposition table
			if(tt.containsKey(position.getHashCode())&&tt.get(position.getHashCode()).getDepth()>depth) {
				Entry entry = tt.get(position.getHashCode());
				// if score is exact of beta<=alpha, return directly
				if(beta<=alpha||entry.getType()==0)
					return entry.getScore();
				// if score is lower bound and score is at least beta, return score
				else if (entry.getType()==-1&&entry.getScore()>=beta) {
					return entry.getScore();
				}
				// if score is upper bound and score is at most beta, return score
				else if (entry.getType()==1&&entry.getScore()<=alpha) {
					return entry.getScore();
				}
			}
			// add scores to tt
			int score = evalUtility(position);
			// if score no more than alpha, it's upper bound
			if (score<=alpha) {
				tt.put(position.getHashCode(), new Entry(score, depth, 1));
			}
			// if score at least beta, it's lower bound 
			else if (score>=beta) {
				tt.put(position.getHashCode(), new Entry(score, depth, -1));
			}
			// otherwise score is exact
			else {
				tt.put(position.getHashCode(), new Entry(score, depth, 0));
			}
			return score;
		}
		// set utility to max value
		int utility = Integer.MAX_VALUE;
		// go through all possible moves
		for(short move:position.getAllMoves()) {
			try {
				position.doMove(move);
			} catch (IllegalMoveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			utility = Integer.min(utility, maxValue(position, alpha, beta, depth+1));
			// undo move
			position.undoMove();
			// if utility <= alpha return utility
			if (utility <= alpha) return utility;
			// set beta
			beta = Integer.min(beta, utility);	
		}
		return utility;
	}
	
	
}
