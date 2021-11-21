package Connect6;

import java.awt.Point;
import java.util.ArrayList;

public class Play {

	final static int EMPTY = 0;
	final static int RED = 1;
	final static int BLACK = 2;
	final static int WHITE = 3;
	
	Board board;
	Board weightBoard;
	int color = 0;
	int opponent = 0;
	
	public static void main(String[] args) throws Exception {
		
		Play play = new Play();
		
		// Board 초기화 및 가중치 초기 계산
		play.board = new Board();
		play.calculateWeight();
		
		// 서버 연결
		if(args.length < 3) {
			System.err.println("Wrong input");
		}
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
		
		ArrayList<Point> children = this.getChild();
		int max = Integer.MIN_VALUE;
		Point nextChild = null;
		int tmp = 0;
		for(Point child : children) {
			if((tmp = miniMax(this.board, child, ...)) > max) {
				max = tmp;
				nextChild = child;
			}
		}
		String nextCoord = toCoordinate(nextChild); 
		this.board.updateBoard(nextCoord);
		this.updateWeight();
		
		return nextCoord;
	} // kim
	
	private ArrayList<Point> getChild(Board board) { // parameter 지워도 될 듯
		return null;
	} // seo
	
	public void calculateWeight() {
		// 이거,,, 적돌 전에 하는거라 딱히 안해도 되려나..?
		// 엥 에초에 updateWeight이랑 다른 점이..?
	} // 가로 세로 우대각선 좌대각선 전부 계산 시키는 함수
	// kim
	
	public int getScore(int start, int direct) {
		return 0;	
	} // 6칸 짜리 값 업데이트 // start - 시작 위치; direct - 방향 
	// seo
	
	private void updateWeight() { // 현재 보드 가중치 재계산 [보드 바뀌었어 가중치 update해]
		// 전체 보드에 대해서
		// 모든 6칸을 모든 방향으로 확인하여 그 6칸에 checkSix 리턴값 더하기
	} // seo
	
	/*
	 * 주어진 6칸을 보고 state를 결정하여 가중치 리턴 ex) 1-6, 0-3 (0~6)
	 * return value: 판정 경과에 따른 가중치
	 * */
	private int checkSix() {
		// 최대 가능성 있는 범위에 있는 내 돌 개수 - 최대 가능성 있는 개수
		return 0;
	} // seo
}
