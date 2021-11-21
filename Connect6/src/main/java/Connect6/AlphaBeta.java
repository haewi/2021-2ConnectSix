package Connect6;

import java.awt.Point;
import java.util.ArrayList;

public class AlphaBeta {
	
//	final static int EMPTY = 0;
//	final static int RED = 1;
	final static int BLACK = 2;
	final static int WHITE = 3;
	
	public static int miniMax(Board weightBoard, Point position, int depth, int alpha, int beta, boolean maximizingPlayer, int now) { // kim
		if(depth == 0) {
			return weightBoard.askBoard(position.x, position.y);
		}
		
		int next = -1;
		if(now == BLACK) next = WHITE;
		else next = BLACK;
		
		weightBoard.updateBoard(position.x, position.y, now);
		
		if(maximizingPlayer) {
			int maxEval = Integer.MIN_VALUE;
			ArrayList<Point> children = weightBoard.getChildMax();
			for(Point child : children) {
				int eval = miniMax(weightBoard, child, depth-1, alpha, beta, false, next);
				maxEval = Integer.max(alpha, eval);
				alpha = Integer.max(alpha, eval);
				if(beta <= alpha) {
					break;
				}
			}
			return maxEval;
		}
		else {
			int minEval = Integer.MAX_VALUE;
			ArrayList<Point> children = weightBoard.getChildMin();
			for(Point child : children) {
				int eval = miniMax(weightBoard, child, depth-1, alpha, beta, true, next);
				minEval = Integer.min(minEval, eval);
				beta = Integer.min(beta, eval);
				if(beta <= alpha) {
					break;
				}
			}
			return minEval;
		}
	}
}
