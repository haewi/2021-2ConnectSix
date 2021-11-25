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
	}
	
	public void updateBoard(int row, int col, int value) {
		board[row][col] = value;
	}
	
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
	}
	
	public int askBoard(int row, int col) {
		return board[row][col];
	}
	
	public void printWeight() {
		for(int i=0; i<200; i++)
			System.out.print("*");
		System.out.println();
		
		System.out.print("      ");
		for(int i=0; i<SPACENUM; i++) {
			if(i > 7) {
				System.out.print(String.format("%7c", 'A'+i+1));
			} else {
				System.out.print(String.format("%7c", 'A'+i));
			}
		}
		System.out.println();
		for(int row=0; row<SPACENUM; row++) {
			System.out.print(String.format("%5d |", 19-row));
			for(int col=0; col<SPACENUM; col++) {
				System.out.print(String.format("%7d", this.board[row][col]));
			}
			System.out.println();
			System.out.println();
		}
		for(int i=0; i<200; i++)
			System.out.print("*");
		System.out.println();
		System.out.println();
	}
	
	public void printBoard() {
		System.out.println();
		for(int i=0; i<200; i++)
			System.out.print("*");
		System.out.println();
		
		for(int row=0; row<SPACENUM; row++) {
			for(int col=0; col<SPACENUM; col++) {
				switch (this.board[row][col]) {
				case EMPTY:
					System.out.print(" ");
					break;
				case RED:
					System.out.print("ㅁ");
					break;
				case WHITE:
					System.out.print("X");
					break;
				case BLACK:
					System.out.print("O");
					break;
				}
				System.out.print("|");
			}
			System.out.println();
		}
		
		for(int i=0; i<200; i++)
			System.out.print("*");
		System.out.println();
	}

	public ArrayList<Point> getChildMax(Board b) {
		ArrayList<String> tmpList = new ArrayList<String>();
		
		String next = "";
		for(int i=0; i<5; i++) {
			int max = Integer.MIN_VALUE;
			for(int row=0; row<SPACENUM; row++) {
				for(int col=0; col<SPACENUM; col++) {
					if(!tmpList.contains(toCoordinate(new Point(row, col))) && b.askBoard(row, col) == EMPTY) {
						if(max < this.askBoard(row, col)) {
							next = toCoordinate(new Point(row, col));
							max = this.askBoard(row, col);
						}
					}
				}
			}
			tmpList.add(next);
		}

		ArrayList<Point> arr = new ArrayList<Point>();
		
		for(String str : tmpList) {
			arr.add(toPoint(str));
		}
		
		return arr;
	}
	
	public ArrayList<Point> getChildMin(Board b) {
		ArrayList<String> tmpList = new ArrayList<String>();
		
		String next = "";
		for(int i=0; i<5; i++) {
			int min = Integer.MAX_VALUE;
			for(int row=0; row<SPACENUM; row++) {
				for(int col=0; col<SPACENUM; col++) {
					if(!tmpList.contains(toCoordinate(new Point(row, col)))&& b.askBoard(row, col) == EMPTY) {
						if(min > this.askBoard(row, col)) {
							next = toCoordinate(new Point(row, col));
							min = this.askBoard(row, col);
						}
					}
				}
			}
			tmpList.add(next);
		}
		
		ArrayList<Point> arr = new ArrayList<Point>();
		
		for(String str : tmpList) {
			arr.add(toPoint(str));
		}
		
		return arr;
	}
	
	private String toCoordinate(Point nextChild) {
		char alpha = (char) (nextChild.y + 'A');
		if(alpha >= 'I') alpha++;
		String num = "";
		if(19-nextChild.x < 10) {
			num += '0';
		}
		num += Integer.toString(19 - nextChild.x);
		
		return alpha + num;
	}
	
	private Point toPoint(String coord) {
		String upCoord = coord.toUpperCase();
		int row = 18 - ((upCoord.charAt(1)-'0')*10 + (upCoord.charAt(2)-'0') -1);
		int col = upCoord.charAt(0) - 'A';
		if(col > 7) col--;
		return new Point(row, col);
	}
}
