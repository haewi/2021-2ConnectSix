package Connect6;

import java.awt.Point;
import java.util.ArrayList;

public class AlphaBeta {
	public static int miniMax(Board board, Point position, int depth, int alpha, int beta, boolean maximizingPlayer) { // kim
		if(depth == 0) {
			return static evaluation of position;
		}
		
		board.updateBoard(position.x, position.y);
		ArrayList<Point> children = board.getChild(board);
		
		if(maximizingPlayer) {
			int maxEval = Integer.MIN_VALUE;
			for(Point child : children) {
				int eval = miniMax(board, child, depth-1, alpha, beta, false);
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
			for(Point child : children) {
				int eval = miniMax(board, child, depth-1, alpha, beta, true);
				minEval = Integer.min(minEval, eval);
				beta = Integer.min(beta, eval);
				if(beta <= alpha) {
					break;
				}
				return minEval;
			}
		}
	}
}
