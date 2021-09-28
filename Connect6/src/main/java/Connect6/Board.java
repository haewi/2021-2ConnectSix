package Connect6;

public class Board {
	int[][] board = new int[19][19];
	
	void Board() {
		for(int i=0; i<19; i++) {
			for(int j=0; j<19; j++) {
				board[i][j] = 0;
			}
		}
	}
	
	/*
	 * update board
	 * ask board
	 * get child
	 * calculate 가중치 
	 */
}
