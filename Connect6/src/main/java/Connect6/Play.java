package Connect6;

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
//		play.weightBoard.printWeight();
		
		if(play.color == BLACK) {
			play.board.updateBoard("K10", play.color);
			play.updateWeight("K10", BLACK);
			String fromWhite = conSix.drawAndRead("K10");
			play.board.updateBoard(fromWhite, play.opponent);
			play.updateWeight(fromWhite, WHITE);
		} else {
			String fromBlack = conSix.drawAndRead("");
			play.board.updateBoard(fromBlack, play.opponent);
			play.updateWeight(fromBlack, BLACK);
		}
		
		while(true) {
			String draw = play.getPosition() + ":" + play.getPosition(); // getPosition에서 updateBoard, updateWeight 다함
			String read = conSix.drawAndRead(draw);
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
		}
		
		// 현재 보드 가중치 계산 - Board
		// child 선정 depth 3로 AlphaBeta class 내의 함수 call - Play
		// 선정된 child 중 제일 높은 점수의 아이 위치로 결정 - Play
		// Board update - Play가 Board의 함수 이용
		// 결정된 위치를 API에 알려줌 - Play
		// API가 상대편 돌 위치 알려줌 - API
		// 그 위치로 보드 업데이트 - Play가 Board의 함수 이용
		
	} // kim
	
	Play() {
		board = new Board();
		weightBoard = new Board();
	}
	
	private String getPosition() {
		/*
		 * 가중치 중에서 children 5개 선택 (getChild() 함수 실행)
		 * 각 child에 대해서 minimax 값 받아오기 (alphabeta method 실행)
		 * 가장 값이 높은 child 위치로 결정 (if문)
		 * 보드 update (board method 실행)
		 * 가중치 전체 update (updateWeight() 실행)
		 * */
		
		weightBoard.printWeight();
		ArrayList<Point> children = weightBoard.getChildMax(new Board(board));
//		System.out.println(children);
		int max = Integer.MIN_VALUE;
		Point nextChild = null;
		int tmp = 0;
		for(Point child : children) {
			if((tmp = AlphaBeta.miniMax(new Board(board), new Board(weightBoard), child, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, true, color)) > max) {
				max = tmp;
				nextChild = child;
			}
		}
		String nextCoord = toCoordinate(nextChild); 
		this.board.updateBoard(nextCoord, color);
		this.updateWeight(nextCoord, this.color);
//		this.weightBoard.printWeight();
		System.out.println(nextCoord);
		
		return nextCoord;
	} // kim
	

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

	private void updateWeight(String position, int colorInterest) { // 현재 보드 가중치 재계산 [보드 바뀌었어 가중치 update해]
		// 전체 보드에 대해서
		// 모든 6칸을 모든 방향으로 확인하여 그 6칸에 checkSix 리턴값 더하기
		/*
			모든 6칸에 대하여
				state 결정 (1-3. 2-4 등등) [filled-max_empty] 	=> checkSix
				그 state에 맞는 가중치를 계산						=> getScore
				max_empty에 해당하는 칸에 가중치 더하기				=> 여기서
		 * */
		if(colorInterest == this.opponent) {
//			for(int i=0; i<board.SPACENUM; i++) {
//				for(int j=0; j<board.SPACENUM; j++) {
//					checkSixOpponent(new Point(i, j), VERTICAL, this.opponent);
//					checkSixOpponent(new Point(i, j), HORIZONTAL, this.opponent);
//					checkSixOpponent(new Point(i, j), RIGHT_DIAGONAL, this.opponent);
//					checkSixOpponent(new Point(i, j), LEFT_DIAGONAL, this.opponent);
//					
//				}
//			}
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
			// TODO
		}
	} // seo
	
	private void checkSixColor(Point start, int direct, int colorInterest) {
		// 최대 가능성 있는 범위에 있는 내 돌 개수 - 최대 가능성 있는 개수
		//y == row
		
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
	} // seo

	/*
	 * 주어진 6칸에 대하여 state 결정
	 * return value: 가중치
	 * */
	private void checkSixOpponent(Point start, int direct, int colorInterest) {
		// 최대 가능성 있는 범위에 있는 내 돌 개수 - 최대 가능성 있는 개수
		//y == row
		
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
//			for (int i = 0; i < maxemptyCount;i++) {
//				weightBoard.addToBoard(maxPoint.x+i, maxPoint.y, getScoreOpponent(maxfillCount,maxemptyCount));
//			}
			for(int i=0; i<6; i++) {
				if(start.x+i<SPACENUM && (start.x+i < maxPoint.x || start.x+i > maxPoint.x+maxemptyCount-1)) { // 점수 더해주는 범위 밖
					if(weightBoard.askBoard(start.x+i, start.y) >= 100000) {
						weightBoard.addToBoard(start.x+i, start.y, -100000);
					}
				}
				else { // 점수 더해주는 범위 안
					weightBoard.addToBoard(start.x+i, start.y, getScoreOpponent(maxfillCount,maxemptyCount));
				}
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
	} // seo
	
	private void checkSixMinus(Point start, int direct, int colorInterest) {
		// 최대 가능성 있는 범위에 있는 내 돌 개수 - 최대 가능성 있는 개수
		//y == row
		
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

//			for (int i = 0; i < maxemptyCount;i++) {
//				weightBoard.addToBoard(maxPoint.x, maxPoint.y+i, getScoreOpponent(maxfillCount,maxemptyCount));
//			}
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
//			for (int i = 0; i < maxemptyCount;i++) {
//				weightBoard.addToBoard(maxPoint.x-i, maxPoint.y+i, getScoreOpponent(maxfillCount,maxemptyCount));
//			}
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
//			for (int i = 0; i < maxemptyCount;i++) {
//				weightBoard.addToBoard(maxPoint.x+i, maxPoint.y+i, getScoreOpponent(maxfillCount, maxemptyCount));
//			}
			for(int i=0; i<6; i++) {
				if(start.x+i <SPACENUM && start.y+i<SPACENUM && (start.y+i < maxPoint.y || start.y+i > maxPoint.y+maxemptyCount-1)) {
					if(weightBoard.askBoard(start.x+i, start.y+i) >= 100000) {
						weightBoard.addToBoard(start.x+i, start.y+i, -100000);
					}
				}
			}
			
		}
		
		return;
	} // seo
	
//	/*
//	 * state에 따라 가중치 리턴 ex) 1-6, 0-3 (0~6)
//	 * return value: 가중치
//	 */
//	public int getScoreOpponent(int count, int maxEmpty) {
//		switch (count){
//		case 5:
//			switch (maxEmpty) {
//			case 6: // 5-6
//				return 100000; // winning position
//			}
//			break;
//		case 4:
//			switch (maxEmpty) {
//			case 5: // 4-5
//				return 26;
//			case 6:
//				return 100000; // winning position
//			}
//			break;
//		case 3:
//			switch (maxEmpty) {
//			case 4:
//				return 19;
//			case 5:
//				return 23;
//			case 6:
//				return 27;
//			}
//			break;
//		case 2:
//			switch (maxEmpty) {
//			case 3:
//				return 13;
//			case 4:
//				return 16;
//			case 5:
//				return 20;
//			case 6:
//				return 24;
//			}
//			break;
//		case 1:
//			switch (maxEmpty) {
//			case 2:
//				return 8;
//			case 3:
//				return 11;
//			case 4:
//				return 14;
//			case 5:
//				return 17;
//			case 6:
//				return 21;
//			}
//			break;
//		case 0:
//			switch (maxEmpty) {
//			case 1:
//				return 3;
//			case 2:
//				return 6;
//			case 3:
//				return 9;
//			case 4:
//				return 12;
//			case 5:
//				return 15;
//			case 6:
//				return 18;
//			}
//			break;
//		}
//		
//		return 0;	
//	} // 6칸 짜리 값 업데이트 // start - 시작 위치; direct - 방향 
//	// seo
//	
//	public int getScoreColor(int count, int maxEmpty) {
//		switch (count){
//		case 5:
//			switch (maxEmpty) {
//			case 6: // 5-6
//				return 100000; // winning position
//			}
//			break;
//		case 4:
//			switch (maxEmpty) {
//			case 5: // 4-5
//				return 26*3/4;
//			case 6:
//				return 100000; // winning position
//			}
//			break;
//		case 3:
//			switch (maxEmpty) {
//			case 4:
//				return 19*3/4;
//			case 5:
//				return 23*3/4;
//			case 6:
//				return 27*3/4;
//			}
//			break;
//		case 2:
//			switch (maxEmpty) {
//			case 3:
//				return 13*3/4;
//			case 4:
//				return 16*3/4;
//			case 5:
//				return 20*3/4;
//			case 6:
//				return 24*3/4;
//			}
//			break;
//		case 1:
//			switch (maxEmpty) {
//			case 2:
//				return 8*3/4;
//			case 3:
//				return 11*3/4;
//			case 4:
//				return 14*3/4;
//			case 5:
//				return 17*3/4;
//			case 6:
//				return 21*3/4;
//			}
//			break;
//		case 0:
//			switch (maxEmpty) {
//			case 1:
//				return 3*3/4;
//			case 2:
//				return 6*3/4;
//			case 3:
//				return 9*3/4;
//			case 4:
//				return 12*3/4;
//			case 5:
//				return 15*3/4;
//			case 6:
//				return 18*3/4;
//			}
//			break;
//		}
//		
//		return 0;	
//	} // 6칸 짜리 값 업데이트 // start - 시작 위치; direct - 방향 
//	// seo
	
	
	// -- 2 --
	
	public int getScoreOpponent(int count, int maxEmpty) {
		switch (count){
		case 5:
			switch (maxEmpty) {
			case 6: // 5-6
				return 100000; // winning position
			}
			break;
		case 4:
			switch (maxEmpty) {
			case 5: // 4-5
				return 27;
			case 6:
				return 100000; // winning position
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
				return 10000000; // winning position
			}
			break;
		case 4:
			switch (maxEmpty) {
			case 5: // 4-5
				return 27*3/4;
			case 6:
				return 10000000; // winning position
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
