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
	
	Board() {
		for(int i=0; i<SPACENUM; i++) {
			for(int j=0; j<SPACENUM; j++) {
				board[i][j] = EMPTY;
			}
		}
	}
	
	Board(Board b) {
		for(int i=0; i<SPACENUM; i++) {
			for(int j=0; j<SPACENUM; j++) {
				board[i][j] = b.board[i][j];
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
	
	public void addToBoard(int row, int col, int value) {
		board[row][col] += value;
	}
	
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
	
	public void printBoard() {
		System.out.println();
		for(int i=0; i<200; i++)
			System.out.print("*");
		System.out.println();
		for(int row=0; row<SPACENUM; row++) {
			for(int col=0; col<SPACENUM; col++) {
				System.out.print(String.format("%7d", this.board[row][col]));
//				if(row == 10 && col == 8) System.out.print("<");
			}
			System.out.println();
			System.out.println();
		}
		for(int i=0; i<200; i++)
			System.out.print("*");
		System.out.println();
	}
	
	public ArrayList<Point> getChildMax(Board b) {
		System.out.println("[getChildMax]");
		b.printBoard();
		ArrayList<Point> tmpList = new ArrayList<Point>();
		int beforemax = Integer.MIN_VALUE;
		int max = Integer.MIN_VALUE;
		Point maxPoint = new Point();
		
		for(int row=0; row<SPACENUM; row++) {
			for(int col=0; col<SPACENUM; col++) {
				if(b.askBoard(row, col) != EMPTY) continue;
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
				all: for(int col=0; col<SPACENUM; col++) {
					if(b.askBoard(row, col) != EMPTY) continue;
					//tmpList안에 있는 좌표일 경우 continue
					for(Point tmpPoint : tmpList) {
						if(tmpPoint.x==row && tmpPoint.y ==col) {
							continue all;
						}
					}
					if (this.askBoard(row, col) <= beforemax && this.askBoard(row, col) > max) {
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
	
	public ArrayList<Point> getChildMin(Board b) {
		ArrayList<Point> tmpList = new ArrayList<Point>();
		int beforemin = Integer.MAX_VALUE;
		int min = Integer.MAX_VALUE;
		Point minPoint = new Point();
		
		for(int row=0; row<SPACENUM; row++) {
			for(int col=0; col<SPACENUM; col++) {
				if(b.askBoard(row, col) != EMPTY) continue;
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
					if(b.askBoard(row, col) != EMPTY) continue;
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
