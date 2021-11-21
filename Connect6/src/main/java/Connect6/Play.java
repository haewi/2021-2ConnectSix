package Connect6;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

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
	int color = 0;
	int opponent = 0;
	
	public static void main(String[] args) throws Exception {
		
		// 서버 연결
		if(args.length < 3) {
			System.err.println("Wrong input");
		}
		
		Play play = new Play();
		
		// Board 초기화 및 가중치 초기 계산
		play.board = new Board();
		play.weightBoard = new Board();
		play.updateWeight();
		
		String ip = args[0];
		int port = Integer.parseInt(args[1]);
		String color = args[2];

		// 적돌 받기
		ConnectSix conSix = new ConnectSix(ip, port, color);
		System.out.println("Red Stone positions are " + conSix.redStones);
		play.board.updateBoard(conSix.redStones, RED);
		play.updateWeight();
		
		if(color.toLowerCase().equals("black")) {
			play.color = BLACK;
			play.opponent = WHITE;
			play.board.updateBoard("K10", play.color);
			play.updateWeight();
			String first = conSix.drawAndRead("K10");
			play.board.updateBoard(first, play.opponent);
			play.updateWeight();
		} else {
			play.color = WHITE;
			play.opponent = BLACK;
			String first = conSix.drawAndRead("");
			play.board.updateBoard(first, play.opponent);
			play.updateWeight();
		}
		
		while(true) {
			String draw = play.getPosition() + ":" + play.getPosition();
//			play.board.updateBoard(draw, play.color); // getPosition에서 할 듯
			String read = conSix.drawAndRead(draw);
			if(read.equals("TIE") || read.equals("WIN") || read.equals("LOSE")) {
				System.out.println("Game End: " + read);
				break;
			}
			play.board.updateBoard(read, play.opponent);
			play.updateWeight();
		}
		
		// 현재 보드 가중치 계산 - Board
		// child 선정 depth 3로 AlphaBeta class 내의 함수 call - Play
		// 선정된 child 중 제일 높은 점수의 아이 위치로 결정 - Play
		// Board update - Play가 Board의 함수 이용
		// 결정된 위치를 API에 알려줌 - Play
		// API가 상대편 돌 위치 알려줌 - API
		// 그 위치로 보드 업데이트 - Play가 Board의 함수 이용
		
	} // kim
	
	void Play() {
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
		
		ArrayList<Point> children = weightBoard.getChildMax();
		int max = Integer.MIN_VALUE;
		Point nextChild = null;
		int tmp = 0;
		for(Point child : children) {
			if((tmp = AlphaBeta.miniMax(weightBoard, child, 3, Integer.MIN_VALUE, Integer.MAX_VALUE, true, color)) > max) {
				max = tmp;
				nextChild = child;
			}
		}
		String nextCoord = toCoordinate(nextChild); 
		this.board.updateBoard(nextCoord, color);
		this.updateWeight();
		
		return nextCoord;
	} // kim
	

	private String toCoordinate(Point nextChild) {
		char alpha = (char) (nextChild.x + 'A');
		if(alpha >= 'I') alpha++;
		String num = "";
		if(nextChild.y < 10) {
			num += '0';
		}
		num += Integer.toString(nextChild.y);
		
		return alpha + num;
	}

	private void updateWeight() { // 현재 보드 가중치 재계산 [보드 바뀌었어 가중치 update해]
		// 전체 보드에 대해서
		// 모든 6칸을 모든 방향으로 확인하여 그 6칸에 checkSix 리턴값 더하기
		/*
			모든 6칸에 대하여
				state 결정 (1-3. 2-4 등등) [filled-max_empty] 	=> checkSix
				그 state에 맞는 가중치를 계산						=> getScore
				max_empty에 해당하는 칸에 가중치 더하기				=> 여기서
		 * */
		for(int i=0; i<board.SPACENUM; i++) {
			for(int j=0; j<board.SPACENUM; j++) {
				checkSix(new Point(i, j), VERTICAL);
				checkSix(new Point(i, j), HORIZONTAL);
				checkSix(new Point(i, j), RIGHT_DIAGONAL);
				checkSix(new Point(i, j), LEFT_DIAGONAL);
				
			}
		}
	} // seo

	/*
	 * 주어진 6칸에 대하여 state 결정
	 * return value: 가중치
	 * */
	private void checkSix(Point start, int direct) {
		// 최대 가능성 있는 범위에 있는 내 돌 개수 - 최대 가능성 있는 개수
		//y == row
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
			
			checkPoint = start;
			maxPoint = start; 
			
			for(int i=0; i<6; i++) {
					checkPoint.x = start.x+i;
					checkPoint.y = start.y;
					
					if(checkPoint.x<board.SPACENUM&&checkPoint.y<board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==color) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==color) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==opponent) {
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
								maxPoint.x = checkPoint.x-emptyCount;
								maxPoint.y = checkPoint.y;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						continue;
					}
				
			}
			for (int i = 0; i < maxemptyCount;i++) {
				weightBoard.updateBoard(maxPoint.x+i, maxPoint.y, getScore(maxfillCount,maxemptyCount));
			}
			
		}
		if (direct == HORIZONTAL) {
			emptyCount = 0;
			fillCount = 0;
			maxfillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = start;
			maxPoint = start; 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x<board.SPACENUM&&checkPoint.y<board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==color) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==color) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==opponent) {
							if (emptyCount>maxemptyCount) {
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
								maxPoint.y = checkPoint.y-emptyCount;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						continue;
					}
				
			}
			for (int i = 0; i < maxemptyCount;i++) {
				System.out.println("x: " + maxPoint.x + " y: " + (maxPoint.y+i));
				weightBoard.updateBoard(maxPoint.x, maxPoint.y+i, getScore(maxfillCount,maxemptyCount));
			}
			
		}
		if (direct == RIGHT_DIAGONAL) { //lb to rt
			emptyCount = 0;
			fillCount = 0;
			maxfillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = start;
			maxPoint = start; 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x-i;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x>0 && checkPoint.x<board.SPACENUM && checkPoint.y<board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==color) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==color) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==opponent) {
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
								maxPoint.x = checkPoint.x+emptyCount;
								maxPoint.y = checkPoint.y-emptyCount;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						continue;
					}
				
			}
			for (int i = 0; i < maxemptyCount;i++) {
				weightBoard.updateBoard(maxPoint.x-i, maxPoint.y+i, getScore(maxfillCount,maxemptyCount));
			}
			
		}
		if (direct == LEFT_DIAGONAL) { //lt to rb
			emptyCount = 0;
			fillCount = 0;
			maxfillCount = 0;
			maxemptyCount = 0;
			
			checkPoint = start;
			maxPoint = start; 
			
			for(int i=0;i<6;i++) {
					checkPoint.x = start.x+i;
					checkPoint.y = start.y+i;
					
					if(checkPoint.x<board.SPACENUM&&checkPoint.y<board.SPACENUM) {
						if(board.askBoard(checkPoint.x, checkPoint.y)==EMPTY || board.askBoard(checkPoint.x, checkPoint.y)==color) {
							emptyCount++;
							if(board.askBoard(checkPoint.x, checkPoint.y)==color) {
								fillCount++;
							}
						}
						if(board.askBoard(checkPoint.x, checkPoint.y)==opponent) {
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
								maxPoint.x = checkPoint.x-emptyCount;
								maxPoint.y = checkPoint.y-emptyCount;
								maxfillCount = fillCount;
								maxemptyCount = emptyCount;
							}
							emptyCount = 0;
							fillCount =0;
						}
					}else {
						continue;
					}
				
			}
			for (int i = 0; i < maxemptyCount;i++) {
				weightBoard.updateBoard(maxPoint.x+i, maxPoint.y+i, getScore(maxfillCount, maxemptyCount));
			}
			
		}
		
		return;
	} // seo
	
	/*
	 * state에 따라 가중치 리턴 ex) 1-6, 0-3 (0~6)
	 * return value: 가중치
	 */
	public int getScore(int count, int maxEmpty) {
		switch (count){
		case 5:
			switch (maxEmpty) {
			case 6: // 5-6
				return 10000; // winning position
			}
			break;
		case 4:
			switch (maxEmpty) {
			case 5: // 4-5
				return 26;
			case 6:
				return 10000; // winning position
			}
			break;
		case 3:
			switch (maxEmpty) {
			case 4:
				return 19;
			case 5:
				return 23;
			case 6:
				return 27;
			}
			break;
		case 2:
			switch (maxEmpty) {
			case 3:
				return 13;
			case 4:
				return 16;
			case 5:
				return 20;
			case 6:
				return 24;
			}
			break;
		case 1:
			switch (maxEmpty) {
			case 2:
				return 8;
			case 3:
				return 11;
			case 4:
				return 14;
			case 5:
				return 17;
			case 6:
				return 21;
			}
			break;
		case 0:
			switch (maxEmpty) {
			case 1:
				return 3;
			case 2:
				return 6;
			case 3:
				return 9;
			case 4:
				return 12;
			case 5:
				return 15;
			case 6:
				return 18;
			}
			break;
		}
		
		return 0;	
	} // 6칸 짜리 값 업데이트 // start - 시작 위치; direct - 방향 
	// seo
	
}
