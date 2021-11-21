package Connect6;

import java.awt.Point;
import java.util.ArrayList;

public class Board {
	final static int EMPTY = 0;
	final static int RED = 1;
	final static int BLACK = 2;
	final static int WHITE = 3;
	final static int SPACENUM = 19;
	
	int[][] board = new int[SPACENUM][SPACENUM]; // [세로][가로] [row][col]
	int[][] weight = new int[SPACENUM][SPACENUM];
	
	void Board() {
		for(int i=0; i<SPACENUM; i++) {
			for(int j=0; j<SPACENUM; j++) {
				board[i][j] = EMPTY;
			}
		}
	}
	
	public void updateBoard(String positions, int value) {
		String[] stones = positions.split(":");
		for(String stone : stones) {
			int col = stone.toUpperCase().charAt(0) - 'A';
			if(col > 'I' - 'A') {
				col--;
			}
			
			int row = 18 - ((stone.toUpperCase().charAt(1)-'0')*10 + (stone.toUpperCase().charAt(2)-'0'-1));
			
			board[row][col] = value;
		}
	} // seo
	
	public void updateBoard(int row, int col, int value) {
		board[row][col] = value;
	} // seo
	
	public int askBoard(String position) {
		int col = position.toUpperCase().charAt(0) - 'A';
		if(col > 'I' - 'A') {
			col--;
		}
		
		int row = 18 - ((position.toUpperCase().charAt(1)-'0')*10 + (position.toUpperCase().charAt(2)-'0'-1));
		
		return board[row][col];
	} // kim
	

	public int askBoard(int row, int col) {
		return board[row][col];
	} // kim
	
//	public static void main(String[] args) {
//		Board b = new Board();
//		b.updateBoard("E07:F10:K11", RED);
//		b.updateBoard("D02:T19", BLACK);
//		b.updateBoard("A01:H13", WHITE);
//		b.printBoard();
//		System.out.println("T19 has to be black(2) > " + b.askBoard("T19"));
//		System.out.println("E07 has to be red(1) > " + b.askBoard("E07"));
//		System.out.println("A01 has to be white(3) > " + b.askBoard("A01"));
//		System.out.println("H13 has to be white(3) > " + b.askBoard("H13"));
//	}
	
	public void printBoard() {
		for(int row=0; row<SPACENUM; row++) {
			for(int col=0; col<SPACENUM; col++) {
				System.out.print(this.board[row][col] + " ");
			}
			System.out.println();
		}
	}
	
	public ArrayList<Point> getChildMax() {
		ArrayList<Point> tmpList = new ArrayList<Point>();
		int beforemax = Integer.MIN_VALUE;
		int max = Integer.MIN_VALUE;
		Point maxPoint = new Point();
		
		for(int row=0; row<SPACENUM; row++) {
			for(int col=0; col<SPACENUM; col++) {
				if (this.askBoard(row, col) > max) {
					max = this.askBoard(row, col);
					maxPoint.x = row;
					maxPoint.y = col;
				}
			}
		}
		tmpList.add(maxPoint);
		beforemax = max;
		max = Integer.MIN_VALUE;
		
		for (int k = 0; k<4 ; k++) {
			for(int row=0; row<SPACENUM; row++) {
				for(int col=0; col<SPACENUM; col++) {
//					this.updateBoard(row, col, EMPTY);
					if (this.askBoard(row, col) <= beforemax && this.askBoard(row, col) > max) {
						//tmpList안에 있는 좌표일 경우 continue
						for(Point tmpPoint : tmpList) {
							if(tmpPoint.x==row && tmpPoint.y ==col) {
								continue;
							}
						}
						max = this.askBoard(row, col);
						maxPoint.x = row;
						maxPoint.y = col;
					}
				}
			}
			tmpList.add(maxPoint);
			beforemax = max;
		}
		return tmpList;
	} // seo
	
	public ArrayList<Point> getChildMin() {
		ArrayList<Point> tmpList = new ArrayList<Point>();
		int beforemin = Integer.MAX_VALUE;
		int min = Integer.MAX_VALUE;
		Point minPoint = new Point();
		
		for(int row=0; row<SPACENUM; row++) {
			for(int col=0; col<SPACENUM; col++) {
				if (this.askBoard(row, col) < min) {
					min = this.askBoard(row, col);
					minPoint.x = row;
					minPoint.y = col;
				}
			}
		}
		tmpList.add(minPoint);
		beforemin = min;
		min = Integer.MIN_VALUE;
		
		for (int k = 0; k<4 ; k++) {
			for(int row=0; row<SPACENUM; row++) {
				for(int col=0; col<SPACENUM; col++) {
//					this.updateBoard(row, col, EMPTY);
					if (this.askBoard(row, col) >= beforemin && this.askBoard(row, col) < min) {
						//tmpList안에 있는 좌표일 경우 continue
						for(Point tmpPoint : tmpList) {
							if(tmpPoint.x==row && tmpPoint.y ==col) {
								continue;
							}
						}
						min = this.askBoard(row, col);
						minPoint.x = row;
						minPoint.y = col;
					}
				}
			}
			tmpList.add(minPoint);
			beforemin = min;
		}
		return tmpList;
	} // seo
	
	/*
	 * update board
	 * ask board
	 * get child
	 * calculate 가중치 
	 */
}
