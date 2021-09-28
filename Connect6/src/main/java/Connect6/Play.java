package Connect6;

public class Play {
	
	Board board;
	
	public static void main(String[] args) {
		// 현재 보드 가중치 계산 - Board
		// child 선정 depth 3로 AlphaBeta class 내의 함수 call - Play
		// 선정된 child 중 제일 높은 점수의 아이 위치로 결정 - Play
		// Board update - Play가 Board의 함수 이용
		// 결정된 위치를 API에 알려줌 - Play
		// API가 상대편 돌 위치 알려줌 - API
		// 그 위치로 보드 업데이트 - Play가 Board의 함수 이용
		
		Play play = new Play();
		play.board.calculateWeight();
	}
	
	void Play() {
		board = new Board();
	}
}
