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
	
	public void updateBoard(int x, int y) {} // seo
	
	public int askBoard(int col, int row) {
		return board[col][row];
	} // kim
	
	public ArrayList<Point> getChild(Board board) {
		return null;
	} // seo
	
	public void calculateWeight() {
		
	} // 가로 세로 우대각선 좌대각선 전부 계산 시키는 함수
	public int getScore(int start, int direct) {
		return 0;
	} // 6칸 짜리 값 업데이트 // start - 시작 위치; direct - 방향 
	
	/*
	 * update board
	 * ask board
	 * get child
	 * calculate 가중치 
	 */
}
