package chess;

import java.util.ArrayList;
import java.util.HashMap;

import chess.ReturnPlay.Message;

public class Chess {

        enum Player { white, black }
		static ReturnPlay game = new ReturnPlay();
		static Player player = Player.white;
		static ArrayList<ArrayList<HashMap<String, Boolean>>> pieceLOS = new ArrayList<>();
   
	/**
	 * Plays the next move for whichever player has the turn.
	 * 
	 * @param move String for next move, e.g. "a2 a3"
	 * 
	 * @return A ReturnPlay instance that contains the result of the move.
	 *         See the section "The Chess class" in the assignment description for details of
	 *         the contents of the returned ReturnPlay instance.
	 */
	public static ReturnPlay play(String move) {

		/* FILL IN THIS METHOD */
		String mod_move = move.strip();

		//Check for resignation
		if (mod_move.substring(0,1).equals("r")) {
			if (player == Player.white) {
				game.message = Message.RESIGN_BLACK_WINS;
			} else {
				game.message = Message.RESIGN_WHITE_WINS;
			}
			return game;
		}

		//Takes care of draws - implements later
		boolean drawRequested = false;
		if (mod_move.endsWith("draw?")) {
			drawRequested = true;
			mod_move = mod_move.substring(0, mod_move.length() - 5).trim();
		}

		//Parse the move string into tokens
		String[] tokens = Splitter(mod_move);
		int fromFile = tokens[0].charAt(0) - 'a';
		int fromRank = tokens[0].charAt(1) - '0';
		int toFile   = tokens[1].charAt(0) - 'a';
		int toRank   = tokens[1].charAt(1) - '0';

		//Promotion check - implements later
		ReturnPiece.PieceType promoteTo = null;
		if (tokens.length >= 3) {
			promoteTo = parsePromotion(tokens[2], player);   //CHECK THIS METHOD LATER
		}
		
		//Find piece being moved
		ReturnPiece mover = FindPieceAt(fromRank, fromFile, game.piecesOnBoard);

		//No piece there, or piece belongs to wrong player
		if (mover == null || isWhite(mover) != (player == Player.white)) {
			game.message = Message.ILLEGAL_MOVE;
			return game;
		}
		
		/* FOLLOWING LINE IS A PLACEHOLDER TO MAKE COMPILER HAPPY */
		/* WHEN YOU FILL IN THIS METHOD, YOU NEED TO RETURN A ReturnPlay OBJECT */
		return null;
	}
	

	//Helper method used in play()
	public static String[] Splitter(String input) {
		input = input.trim().replaceAll("\\s+", " ");
		String[] words = input.split(" ");

		return words;
	}

	//Helper method used in play() -> finds piece in certain location
	static ReturnPiece FindPieceAt(int r,int f, ArrayList<ReturnPiece> pOB) {
        ReturnPiece.PieceFile pf = ReturnPiece.PieceFile.values()[f];

        for (ReturnPiece rp : pOB) {
            if (rp.pieceRank == r && rp.pieceFile == pf) {
                return rp;
            }
        }
        return null;
    }
	
	/**
	 * This method should reset the game, and start from scratch.
	 */
	public static void start() {
		/* FILL IN THIS METHOD */

		//Create a new ReturnPlay object to use in play method
		game.piecesOnBoard = newBoard();
		game.message = null;
		player = Player.white;
	}

	//Helper method used in start()  -->  for piecesOnBoard (Arraylist w/ returnable/printable pieces)
	public static ArrayList<ReturnPiece> newBoard() {
		ArrayList<ReturnPiece> board = new ArrayList<>();
		ReturnPiece.PieceFile[] files = ReturnPiece.PieceFile.values();
		
		//White pieces - rooks
		ReturnPiece rook = new ReturnPiece();
		rook.pieceType = ReturnPiece.PieceType.WR;
		rook.pieceFile = files[0];
		rook.pieceRank = 1;
		board.add(rook);  //First rook - left corner

		rook = new ReturnPiece();
		rook.pieceType = ReturnPiece.PieceType.WR;
		rook.pieceFile = files[7];
		rook.pieceRank = 1;
		board.add(rook);  //Second rook - right corner

		//White pieces - knights
		ReturnPiece knight = new ReturnPiece();
		knight.pieceType = ReturnPiece.PieceType.WN;
		knight.pieceFile = files[1];
		knight.pieceRank = 1;
		board.add(knight);   //first knight - left

		knight = new ReturnPiece();
		knight.pieceType = ReturnPiece.PieceType.WN;
		knight.pieceFile = files[6];
		knight.pieceRank = 1;
		board.add(knight);   //second knight - right

		//White pieces - bishops
		ReturnPiece bishop = new ReturnPiece();
		bishop.pieceType = ReturnPiece.PieceType.WB;
		bishop.pieceFile = files[2];
		bishop.pieceRank = 1;
		board.add(bishop);   //first bishop - left

		bishop = new ReturnPiece();
		bishop.pieceType = ReturnPiece.PieceType.WB;
		bishop.pieceFile = files[5];
		bishop.pieceRank = 1;
		board.add(bishop);   //second bishop - right
		
		//White pieces - Queen
		ReturnPiece q = new ReturnPiece();
		q.pieceType = ReturnPiece.PieceType.WQ;
		q.pieceFile = files[3];
		q.pieceRank = 1;
		board.add(q);

		//White pieces - King
		ReturnPiece k = new ReturnPiece();
		k.pieceType = ReturnPiece.PieceType.WK;
		k.pieceFile = files[4];
		k.pieceRank = 1;
		board.add(k);

		//White pieces - Pawns
		for (int i=0; i<8; i++) {
			ReturnPiece pawn = new ReturnPiece();
			pawn.pieceType = ReturnPiece.PieceType.WP;
			pawn.pieceRank = 2;
			pawn.pieceFile = files[i];
			board.add(pawn);
		}


		//Black pieces - rooks
		rook = new ReturnPiece();
		rook.pieceType = ReturnPiece.PieceType.BR;
		rook.pieceFile = files[0];
		rook.pieceRank = 8;
		board.add(rook);  //First rook - left corner

		rook = new ReturnPiece();
		rook.pieceType = ReturnPiece.PieceType.BR;
		rook.pieceFile = files[7];
		rook.pieceRank = 8;
		board.add(rook);  //Second rook - right corner

		//Black pieces - knights
		knight = new ReturnPiece();
		knight.pieceType = ReturnPiece.PieceType.BN;
		knight.pieceFile = files[1];
		knight.pieceRank = 8;
		board.add(knight);   //first knight - left

		knight = new ReturnPiece();
		knight.pieceType = ReturnPiece.PieceType.BN;
		knight.pieceFile = files[6];
		knight.pieceRank = 8;
		board.add(knight);   //second knight - right

		//Black pieces - bishops
		bishop = new ReturnPiece();
		bishop.pieceType = ReturnPiece.PieceType.BB;
		bishop.pieceFile = files[2];
		bishop.pieceRank = 8;
		board.add(bishop);   //first bishop - left

		bishop = new ReturnPiece();
		bishop.pieceType = ReturnPiece.PieceType.BB;
		bishop.pieceFile = files[5];
		bishop.pieceRank = 8;
		board.add(bishop);   //second bishop - right
		
		//Black pieces - Queen
		q = new ReturnPiece();
		q.pieceType = ReturnPiece.PieceType.BQ;
		q.pieceFile = files[3];
		q.pieceRank = 8;
		board.add(q);

		//Black pieces - King
		k = new ReturnPiece();
		k.pieceType = ReturnPiece.PieceType.BK;
		k.pieceFile = files[4];
		k.pieceRank = 8;
		board.add(k);

		//Black pieces - Pawns
		for (int i=0; i<8; i++) {
			ReturnPiece pawn = new ReturnPiece();
			pawn.pieceType = ReturnPiece.PieceType.BP;
			pawn.pieceRank = 7;
			pawn.pieceFile = files[i];
			board.add(pawn);
		}

		return board;
	}

}

	