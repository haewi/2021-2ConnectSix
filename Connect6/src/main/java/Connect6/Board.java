package Connect6;

import java.awt.Point;
import java.util.ArrayList;

public class Board {
	final static int EMPTY = 0;
	final static int RED = 1;
	final static int BLACK = 2;
	final static int WHITE = 3;
	final static int SPACENUM = 19;
	
	int[][] board = new int[SPACENUM][SPACENUM]; // [세로][가로]
	int[][] weight = new int[SPACENUM][SPACENUM];
	
	void Board() {
		for(int i=0; i<SPACENUM; i++) {
			for(int j=0; j<SPACENUM; j++) {
				board[i][j] = EMPTY;
			}
		}
	}
	
	// 돌 업데이트 파라미터 바꾸기
	public void updateBoard(int col, int row, int color) {
		board[col][row] = color;
	} // seo
	
	public int askBoard(int col, int row) {
		return board[col][row];
	} // kim
	
	/*
	 * update board
	 * ask board
	 * get child
	 * calculate 가중치 
	 */
}
