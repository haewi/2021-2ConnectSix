import java.awt.Point;
import java.util.ArrayList;

public class Play {

	final static int EMPTY = 0;
	final static int RED = 1;
	final static int BLACK = 2;
	final static int WHITE = 3;
	final static int SPACENUM = 19;
	
	final static int VERTICAL = 4;
	final static int HORIZONTAL = 5;
	final static int RIGHT_DIAGONAL = 6;	// 오른쪽 위로
	final static int LEFT_DIAGONAL = 7;		// 오른쪽 아래로
	
	Board board;
	Board weightBoard;
	int color = -1;
	int opponent = -1;
	int count = 0;
	

	
	public static void main(String[] args) throws Exception {
		
		// 서버 연결
		if(args.length < 3) {
			System.err.println("Wrong input");
		}
		
		Play play = new Play();
		
		// Board 초기화 및 가중치 초기 계산
		play.board = new Board();
		play.weightBoard = new Board();
		
		String ip = args[0];
		int port = Integer.parseInt(args[1]);
		String color = args[2];
		
		if(color.toLowerCase().equals("black")) {
			play.color = BLACK;
			play.opponent = WHITE;
		} else {
			play.color = WHITE;
			play.opponent = BLACK;
		}
		

		// 적돌 받기
		ConnectSix conSix = new ConnectSix(ip, port, color);
		System.out.println("Red Stone positions are " + conSix.redStones);
		if(conSix.redStones != null) {
			play.board.updateBoard(conSix.redStones, RED);
			String[] reds = conSix.redStones.split(":");
			for(String red : reds)
				play.updateWeight(red, RED);
		}
		play.weightBoard.printWeight();
		
		if(play.color == BLACK) {
			play.board.updateBoard("K10", play.color);
			play.updateWeight("K10", BLACK);
			String fromWhite = conSix.drawAndRead("K10");
			play.count++;
			play.board.updateBoard(fromWhite, play.opponent);
			play.updateWeight(fromWhite, WHITE);
		} else {
			String fromBlack = conSix.drawAndRead("");
			play.count++;
			play.board.updateBoard(fromBlack, play.opponent);
			play.updateWeight(fromBlack, BLACK);
		}
		
		play.weightBoard.printWeight();
		
		while(true) {
			String draw = play.getPosition() + ":" + play.getPosition(); // getPosition에서 updateBoard, updateWeight 다함
			String read = conSix.drawAndRead(draw);
			play.count++;
			if(read.equals("TIE") || read.equals("WIN") || read.equals("LOSE")) {
				System.out.println("Game End: " + read);
				break;
			}
			play.board.updateBoard(read, play.opponent);
			String[] stones = read.split(":");
			for(String stone : stones)
				play.updateWeight(stone, play.opponent);
			System.out.println("white: " + read);
			play.weightBoard.printWeight();
			
			if(play.count == 5) {
				play.count = 0;
				play.weightBoard = new Board();
				play.updateWeight(null, -1);
				System.out.println("초기화!");
				play.weightBoard.printWeight();
			}
		}
	}
	
	Play() {
		board = new Board();
		weightBoard = new Board();
	}
	
	private String getPosition() {
		ArrayList<Point> children = weightBoard.getChildMax(new Board(board));
		int max = Integer.MIN_VALUE;
		Point nextChild = null;
		int tmp = 0;
		for(Point child : children) {
			if((tmp = AlphaBeta.miniMax(new Board(board), new Board(weightBoard), child, 10, Integer.MIN_VALUE, Integer.MAX_VALUE, true, color)) > max) {
				max = tmp;
				nextChild = child;
			}
		}
		String nextCoord = toCoordinate(nextChild); 
		this.board.updateBoard(nextCoord, color);
		this.updateWeight(nextCoord, this.color);
		System.out.println(nextCoord);
		this.weightBoard.printWeight();
		
		return nextCoord;
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

	private void updateWeight(String position, int colorInterest) {
		if(position == null) {
			// 전체보드 가중치 재계산
			for(int row=0; row<SPACENUM; row++) {
				for(int col=0; col<SPACENUM; col++) {
					if(this.board.askBoard(row, col) != EMPTY) {
						for(int i=0; i<6; i++) {
							if(row-i >= 0) {
								checkSixOpponent(new Point(row-i, col), VERTICAL, this.opponent);
								checkSixColor(new Point(row-i, col), VERTICAL, this.color);
							}
							// horizontal
							if(col-i >= 0) {
								checkSixOpponent(new Point(row, col-i), HORIZONTAL, this.opponent);
								checkSixColor(new Point(row, col-i), HORIZONTAL, this.color);
							}
							// right-diagonal
							if(col-i >= 0 && row+i < SPACENUM) {
								checkSixOpponent(new Point(row+i, col-i), RIGHT_DIAGONAL, this.opponent);
								checkSixColor(new Point(row+i, col-i), RIGHT_DIAGONAL, this.color);
							}
							// left-diagonal
							if(row-i >=0 && col-i >= 0) {
								checkSixOpponent(new Point(row-i, col-i), LEFT_DIAGONAL, this.opponent);
								checkSixColor(new Point(row-i, col-i), LEFT_DIAGONAL, this.color);
							}
						}
					}
				}
			}
		}
		else if(colorInterest == this.opponent) {
			Point point = toPoint(position);
			for(int i=0; i<6; i++) {
				// vertical
				if(point.x-i >= 0)
					checkSixOpponent(new Point(point.x-i, point.y), VERTICAL, this.opponent);
				// horizontal
				if(point.y-i >= 0)
					checkSixOpponent(new Point(point.x, point.y-i), HORIZONTAL, this.opponent);
				// right-diagonal
				if(point.y-i >= 0 && point.x+i < SPACENUM)
					checkSixOpponent(new Point(point.x+i, point.y-i), RIGHT_DIAGONAL, this.opponent);
				// left-diagonal
				if(point.x-i >=0 && point.y-i >= 0)
					checkSixOpponent(new Point(point.x-i, point.y-i), LEFT_DIAGONAL, this.opponent);
				
				// 상대방이 막아서 -1000_0000
				// vertical
				if(point.x-i >= 0)
					checkSixMinusColor(new Point(point.x-i, point.y), VERTICAL, this.color);
				// horizontal
				if(point.y-i >= 0)
					checkSixMinusColor(new Point(point.x, point.y-i), HORIZONTAL, this.color);
				// right-diagonal
				if(point.y-i >= 0 && point.x+i < SPACENUM)
					checkSixMinusColor(new Point(point.x+i, point.y-i), RIGHT_DIAGONAL, this.color);
				// left-diagonal
				if(point.x-i >=0 && point.y-i >= 0)
					checkSixMinusColor(new Point(point.x-i, point.y-i), LEFT_DIAGONAL, this.color);
			}
		}
		else if(colorInterest == this.color){
			Point point = toPoint(position);
			for(int i=0; i<6; i++) {
				// 내 돌에 대한 가중치 더하기
				// vertical
				if(point.x-i >= 0)
					checkSixColor(new Point(point.x-i, point.y), VERTICAL, this.color);
				// horizontal
				if(point.y-i >= 0)
					checkSixColor(new Point(point.x, point.y-i), HORIZONTAL, this.color);
				// right-diagonal
				if(point.y-i >= 0 && point.x+i < SPACENUM)
					checkSixColor(new Point(point.x+i, point.y-i), RIGHT_DIAGONAL, this.color);
				// left-diagonal
				if(point.x-i >=0 && point.y-i >= 0)
					checkSixColor(new Point(point.x-i, point.y-i), LEFT_DIAGONAL, this.color);
				
				// 내 돌로 인한 -100000 해주기
				// vertical
				if(point.x-i >= 0)
					checkSixMinus(new Point(point.x-i, point.y), VERTICAL, this.opponent);
				// horizontal
				if(point.y-i >= 0)
					checkSixMinus(new Point(point.x, point.y-i), HORIZONTAL, this.opponent);
				// right-diagonal
				if(point.y-i >= 0 && point.x+i < SPACENUM)
					checkSixMinus(new Point(point.x+i, point.y-i), RIGHT_DIAGONAL, this.opponent);
				// left-diagonal
				if(point.x-i >=0 && point.y-i >= 0)
					checkSixMinus(new Point(point.x-i, point.y-i), LEFT_DIAGONAL, this.opponent);
				
			}
		}
		else { // red
			int[] num = {0, -19, -17, -15, -13, -11};
			Point p = toPoint(position);
			for(int i=1; i<6; i++) {
				// vertical
				if(p.x+i < SPACENUM)
					this.weightBoard.addToBoard(p.x+i, p.y, num[i]);
				if(p.x-i >= 0)
					this.weightBoard.addToBoard(p.x-i, p.y, num[i]);
				
				// horizontal
				if(p.y+i < SPACENUM)
					this.weightBoard.addToBoard(p.x, p.y+i, num[i]);
				if(p.y-i >= 0)
					this.weightBoard.addToBoard(p.x, p.y-i, num[i]);

				if(p.x-i >= 0) {
					if(p.y+i < SPACENUM)
						this.weightBoard.addToBoard(p.x-i, p.y+i, num[i]); // right-diagonal
					if(p.y-i >= 0)
						this.weightBoard.addToBoard(p.x-i, p.y-i, num[i]); // left-diagonal
				}
				
				// left-diagonal
				if(p.x+i < SPACENUM) {
					if(p.y-i >= 0)
						this.weightBoard.addToBoard(p.x+i, p.y-i, num[i]); // right-diagonal
					if(p.y+i < SPACENUM)
						this.weightBoard.addToBoard(p.x+i, p.y+i, num[i]); // left-diagonal
				}
			}
		}
	}
	
	private void checkSixColor(Point start, int direct, int colorInterest) {
		// 최대 가능성 있는 범위에 있는 내 돌 개수 - 최대 가능성 있는 개수
		
		int otherColor=-1;
		if (colorInterest == BLACK) otherColor=WHITE;
		else if(colorInterest == WHITE) otherColor=BLACK;
		
		int emptyCount;
		int fillCount;
		Point checkPoint = new Point();
		
		Point maxPoint = new Point();//max empty start point
		int maxfillCount = 0;
		int maxemptyCount = 0;
		
		
		// 최대 가능성 있는 범위에 있는 내 돌 개수 - 최대 가능성 있는 개수
		if (direct == VERTICAL) {
			emptyCount = 0;
			fillCount = 0;
			maxfillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			
			for(int i=0; i<6; i++) {
					checkPoint.x = start.x+i;
					checkPoint.y = start.y;
					
					if(checkPoint.x<Board.SPACENUM&&checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount;
								maxPoint.y = checkPoint.y;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount+1;
								maxPoint.y = checkPoint.y;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
				
			}
			for (int i = 0; i < maxemptyCount;i++) {
				weightBoard.addToBoard(maxPoint.x+i, maxPoint.y, getScoreColor(maxfillCount,maxemptyCount));
			}
		}
		if (direct == HORIZONTAL) {
			emptyCount = 0;
			fillCount = 0;
			maxfillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x<Board.SPACENUM && checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount > maxemptyCount) {
								maxPoint.x = checkPoint.x;
								maxPoint.y = checkPoint.y-emptyCount;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) { // 마지막 6번째
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x;
								maxPoint.y = checkPoint.y-emptyCount+1;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
			}

			for (int i = 0; i < maxemptyCount;i++) {
				weightBoard.addToBoard(maxPoint.x, maxPoint.y+i, getScoreColor(maxfillCount,maxemptyCount));
			}
			
		}
		if (direct == RIGHT_DIAGONAL) { //lb to rt
			emptyCount = 0;
			fillCount = 0;
			maxfillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x-i;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x>=0 && checkPoint.x<Board.SPACENUM && checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x+emptyCount;
								maxPoint.y = checkPoint.y-emptyCount;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x+emptyCount-1;
								maxPoint.y = checkPoint.y-emptyCount+1;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
				
			}
			for (int i = 0; i < maxemptyCount;i++) {
				weightBoard.addToBoard(maxPoint.x-i, maxPoint.y+i, getScoreColor(maxfillCount,maxemptyCount));
			}
			
		}
		if (direct == LEFT_DIAGONAL) { //lt to rb
			emptyCount = 0;
			fillCount = 0;
			maxfillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x+i;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x<Board.SPACENUM&&checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount;
								maxPoint.y = checkPoint.y-emptyCount;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount+1;
								maxPoint.y = checkPoint.y-emptyCount+1;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
				
			}
			for (int i = 0; i < maxemptyCount;i++) {
				weightBoard.addToBoard(maxPoint.x+i, maxPoint.y+i, getScoreColor(maxfillCount, maxemptyCount));
			}
			
		}
		
		return;
	}

	private void checkSixOpponent(Point start, int direct, int colorInterest) {
		// 최대 가능성 있는 범위에 있는 내 돌 개수 - 최대 가능성 있는 개수
		
		int otherColor=-1;
		if (colorInterest == BLACK) otherColor=WHITE;
		else if(colorInterest == WHITE) otherColor=BLACK;
		
		int emptyCount;
		int fillCount;
		Point checkPoint = new Point();
		
		Point maxPoint = new Point();//max empty start point
		int maxfillCount = 0;
		int maxemptyCount = 0;
		
		
		// 최대 가능성 있는 범위에 있는 내 돌 개수 - 최대 가능성 있는 개수
		if (direct == VERTICAL) {
			emptyCount = 0;
			fillCount = 0;
			maxfillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			
			for(int i=0; i<6; i++) {
					checkPoint.x = start.x+i;
					checkPoint.y = start.y;
					
					if(checkPoint.x<Board.SPACENUM&&checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount;
								maxPoint.y = checkPoint.y;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount+1;
								maxPoint.y = checkPoint.y;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
				
			}
			for (int i = 0; i < maxemptyCount;i++) {
				weightBoard.addToBoard(maxPoint.x+i, maxPoint.y, getScoreOpponent(maxfillCount,maxemptyCount));
			}
		}
		if (direct == HORIZONTAL) {
			emptyCount = 0;
			fillCount = 0;
			maxfillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x<Board.SPACENUM && checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount > maxemptyCount) {
								maxPoint.x = checkPoint.x;
								maxPoint.y = checkPoint.y-emptyCount;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) { // 마지막 6번째
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x;
								maxPoint.y = checkPoint.y-emptyCount+1;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
			}

			for (int i = 0; i < maxemptyCount;i++) {
				weightBoard.addToBoard(maxPoint.x, maxPoint.y+i, getScoreOpponent(maxfillCount,maxemptyCount));
			}
			
		}
		if (direct == RIGHT_DIAGONAL) { //lb to rt
			emptyCount = 0;
			fillCount = 0;
			maxfillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x-i;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x>=0 && checkPoint.x<Board.SPACENUM && checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x+emptyCount;
								maxPoint.y = checkPoint.y-emptyCount;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x+emptyCount-1;
								maxPoint.y = checkPoint.y-emptyCount+1;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
				
			}
			for (int i = 0; i < maxemptyCount;i++) {
				weightBoard.addToBoard(maxPoint.x-i, maxPoint.y+i, getScoreOpponent(maxfillCount,maxemptyCount));
			}
			
			
		}
		if (direct == LEFT_DIAGONAL) { //lt to rb
			emptyCount = 0;
			fillCount = 0;
			maxfillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x+i;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x<Board.SPACENUM&&checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount;
								maxPoint.y = checkPoint.y-emptyCount;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount+1;
								maxPoint.y = checkPoint.y-emptyCount+1;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
				
			}
			for (int i = 0; i < maxemptyCount;i++) {
				weightBoard.addToBoard(maxPoint.x+i, maxPoint.y+i, getScoreOpponent(maxfillCount, maxemptyCount));
			}
			
		}
		
		return;
	}
	
	private void checkSixMinus(Point start, int direct, int colorInterest) {
		// 최대 가능성 있는 범위에 있는 내 돌 개수 - 최대 가능성 있는 개수
		
		int otherColor=-1;
		if (colorInterest == BLACK) otherColor=WHITE;
		else if(colorInterest == WHITE) otherColor=BLACK;
		
		int emptyCount;
		int fillCount;
		Point checkPoint = new Point();
		
		Point maxPoint = new Point();//max empty start point
		int maxemptyCount = 0;
		
		
		// 최대 가능성 있는 범위에 있는 내 돌 개수 - 최대 가능성 있는 개수
		if (direct == VERTICAL) {
			emptyCount = 0;
			fillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			
			for(int i=0; i<6; i++) {
					checkPoint.x = start.x+i;
					checkPoint.y = start.y;
					
					if(checkPoint.x<Board.SPACENUM&&checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount;
								maxPoint.y = checkPoint.y;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount+1;
								maxPoint.y = checkPoint.y;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
				
			}
//			for (int i = 0; i < maxemptyCount;i++) {
//				weightBoard.addToBoard(maxPoint.x+i, maxPoint.y, getScoreOpponent(maxfillCount,maxemptyCount));
//			}
			for(int i=0; i<6; i++) {
				if(start.x+i<SPACENUM && (start.x+i < maxPoint.x || start.x+i > maxPoint.x+maxemptyCount-1)) { // 점수 더해주는 범위 밖
					if(weightBoard.askBoard(start.x+i, start.y) >= 100000) {
						weightBoard.addToBoard(start.x+i, start.y, -100000);
					}
				}
			}
		}
		if (direct == HORIZONTAL) {
			emptyCount = 0;
			fillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x<Board.SPACENUM && checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount > maxemptyCount) {
								maxPoint.x = checkPoint.x;
								maxPoint.y = checkPoint.y-emptyCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) { // 마지막 6번째
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x;
								maxPoint.y = checkPoint.y-emptyCount+1;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
			}

			for(int i=0; i<6; i++) {
				if(start.y+i<SPACENUM && (start.y+i < maxPoint.y || start.y+i > maxPoint.y+maxemptyCount-1)) {
					if(weightBoard.askBoard(start.x, start.y+i) >= 100000) {
						weightBoard.addToBoard(start.x, start.y+i, -100000);
					}
				}
			}
			
		}
		if (direct == RIGHT_DIAGONAL) { //lb to rt
			emptyCount = 0;
			fillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x-i;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x>=0 && checkPoint.x<Board.SPACENUM && checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x+emptyCount;
								maxPoint.y = checkPoint.y-emptyCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x+emptyCount-1;
								maxPoint.y = checkPoint.y-emptyCount+1;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
				
			}
			for(int i=0; i<6; i++) {
				if(start.x-i >=0 && start.y+i<SPACENUM && (start.y+i < maxPoint.y || start.y+i > maxPoint.y+maxemptyCount-1)) {
					if(weightBoard.askBoard(start.x-i, start.y+i) >= 100000) {
						weightBoard.addToBoard(start.x-i, start.y+i, -100000);
					}
				}
			}
			
		}
		if (direct == LEFT_DIAGONAL) { //lt to rb
			emptyCount = 0;
			fillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x+i;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x<Board.SPACENUM&&checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount;
								maxPoint.y = checkPoint.y-emptyCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount+1;
								maxPoint.y = checkPoint.y-emptyCount+1;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
				
			}
			for(int i=0; i<6; i++) {
				if(start.x+i <SPACENUM && start.y+i<SPACENUM && (start.y+i < maxPoint.y || start.y+i > maxPoint.y+maxemptyCount-1)) {
					if(weightBoard.askBoard(start.x+i, start.y+i) >= 100000) {
						weightBoard.addToBoard(start.x+i, start.y+i, -100000);
					}
				}
			}
			
		}
		
		return;
	}
	
	private void checkSixMinusColor(Point start, int direct, int colorInterest) {
		// 최대 가능성 있는 범위에 있는 내 돌 개수 - 최대 가능성 있는 개수
		
		int otherColor=-1;
		if (colorInterest == BLACK) otherColor=WHITE;
		else if(colorInterest == WHITE) otherColor=BLACK;
		
		int emptyCount;
		int fillCount;
		Point checkPoint = new Point();
		
		Point maxPoint = new Point();//max empty start point
		int maxemptyCount = 0;
		
		// 최대 가능성 있는 범위에 있는 내 돌 개수 - 최대 가능성 있는 개수
		if (direct == VERTICAL) {
			emptyCount = 0;
			fillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			
			for(int i=0; i<6; i++) {
					checkPoint.x = start.x+i;
					checkPoint.y = start.y;
					
					if(checkPoint.x<Board.SPACENUM&&checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount;
								maxPoint.y = checkPoint.y;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount+1;
								maxPoint.y = checkPoint.y;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
				
			}
			for(int i=0; i<6; i++) {
				if(start.x+i<SPACENUM && (start.x+i < maxPoint.x || start.x+i > maxPoint.x+maxemptyCount-1)) { // 점수 더해주는 범위 밖
					if(weightBoard.askBoard(start.x+i, start.y) >= 1000_0000) {
						weightBoard.addToBoard(start.x+i, start.y, -1000_0000);
					}
				}
			}
		}
		if (direct == HORIZONTAL) {
			emptyCount = 0;
			fillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x<Board.SPACENUM && checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount > maxemptyCount) {
								maxPoint.x = checkPoint.x;
								maxPoint.y = checkPoint.y-emptyCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) { // 마지막 6번째
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x;
								maxPoint.y = checkPoint.y-emptyCount+1;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
			}

			for(int i=0; i<6; i++) {
				if(start.y+i<SPACENUM && (start.y+i < maxPoint.y || start.y+i > maxPoint.y+maxemptyCount-1)) {
					if(weightBoard.askBoard(start.x, start.y+i) >= 1000_0000) {
						weightBoard.addToBoard(start.x, start.y+i, -1000_0000);
					}
				}
			}
			
		}
		if (direct == RIGHT_DIAGONAL) { //lb to rt
			emptyCount = 0;
			fillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x-i;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x>=0 && checkPoint.x<Board.SPACENUM && checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x+emptyCount;
								maxPoint.y = checkPoint.y-emptyCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x+emptyCount-1;
								maxPoint.y = checkPoint.y-emptyCount+1;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
				
			}
			for(int i=0; i<6; i++) {
				if(start.x-i >=0 && start.y+i<SPACENUM && (start.y+i < maxPoint.y || start.y+i > maxPoint.y+maxemptyCount-1)) {
					if(weightBoard.askBoard(start.x-i, start.y+i) >= 1000_0000) {
						weightBoard.addToBoard(start.x-i, start.y+i, -1000_0000);
					}
				}
			}
			
		}
		if (direct == LEFT_DIAGONAL) { //lt to rb
			emptyCount = 0;
			fillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = new Point(start);
			maxPoint = new Point(start); 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x+i;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x<Board.SPACENUM&&checkPoint.y<Board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==colorInterest) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==otherColor || board.askBoard(checkPoint.x, checkPoint.y)==RED) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount;
								maxPoint.y = checkPoint.y-emptyCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
						if(i==5 && emptyCount!=0) {
							if (emptyCount>maxemptyCount) {
								maxPoint.x = checkPoint.x-emptyCount+1;
								maxPoint.y = checkPoint.y-emptyCount+1;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						return;
					}
				
			}
			for(int i=0; i<6; i++) {
				if(start.x+i <SPACENUM && start.y+i<SPACENUM && (start.y+i < maxPoint.y || start.y+i > maxPoint.y+maxemptyCount-1)) {
					if(weightBoard.askBoard(start.x+i, start.y+i) >= 1000_0000) {
						weightBoard.addToBoard(start.x+i, start.y+i, -1000_0000);
					}
				}
			}
			
		}
		
		return;
	}
	
	public int getScoreOpponent(int count, int maxEmpty) {
		switch (count){
		case 5:
			switch (maxEmpty) {
			case 6: // 5-6
				return 10_0000; // winning position
			}
			break;
		case 4:
			switch (maxEmpty) {
			case 5: // 4-5
				return 27;
			case 6:
				return 10_0000; // winning position
			}
			break;
		case 3:
			switch (maxEmpty) {
			case 4:
				return 21;
			case 5:
				return 24;
			case 6:
				return 26;
			}
			break;
		case 2:
			switch (maxEmpty) {
			case 3:
				return 15;
			case 4:
				return 18;
			case 5:
				return 20;
			case 6:
				return 23;
			}
			break;
		case 1:
			switch (maxEmpty) {
			case 2:
				return 9;
			case 3:
				return 12;
			case 4:
				return 14;
			case 5:
				return 17;
			case 6:
				return 19;
			}
			break;
		case 0:
			switch (maxEmpty) {
			case 1:
				return 3;
			case 2:
				return 6;
			case 3:
				return 8;
			case 4:
				return 11;
			case 5:
				return 13;
			case 6:
				return 16;
			}
			break;
		}
		
		return 0;	
	} // 6칸 짜리 값 업데이트 // start - 시작 위치; direct - 방향 
	
	public int getScoreColor(int count, int maxEmpty) {
		switch (count){
		case 5:
			switch (maxEmpty) {
			case 6: // 5-6
				return 1000_0000; // winning position
			}
			break;
		case 4:
			switch (maxEmpty) {
			case 5: // 4-5
				return 27*3/4;
			case 6:
				return 1000_0000; // winning position
			}
			break;
		case 3:
			switch (maxEmpty) {
			case 4:
				return 21*3/4;
			case 5:
				return 24*3/4;
			case 6:
				return 26*3/4;
			}
			break;
		case 2:
			switch (maxEmpty) {
			case 3:
				return 15*3/4;
			case 4:
				return 18*3/4;
			case 5:
				return 20*3/4;
			case 6:
				return 23*3/4;
			}
			break;
		case 1:
			switch (maxEmpty) {
			case 2:
				return 9*3/4;
			case 3:
				return 12*3/4;
			case 4:
				return 14*3/4;
			case 5:
				return 17*3/4;
			case 6:
				return 19*3/4;
			}
			break;
		case 0:
			switch (maxEmpty) {
			case 1:
				return 3*3/4;
			case 2:
				return 6*3/4;
			case 3:
				return 8*3/4;
			case 4:
				return 11*3/4;
			case 5:
				return 13*3/4;
			case 6:
				return 16*3/4;
			}
			break;
		}
		
		return 0;	
	} // 6칸 짜리 값 업데이트 // start - 시작 위치; direct - 방향 
}
