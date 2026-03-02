//Names: Ayush Pundyavana (ap2735),  Michelle Thomas (mrt159)

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
		if (mod_move.substring(0,4).equals("resi")) {
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

		//Check geometric validity via the piece's CheckMove
		PieceFunctions piece = pieceFor(mover);
		if (!piece.CheckMove(fromRank, fromFile, toRank, toFile, game.piecesOnBoard, player)) {
			game.message = Message.ILLEGAL_MOVE;
			return game;
		}

		//Simulate move — reject if own king is left in check
		ArrayList<ReturnPiece> snapshot = deepCopy(game.piecesOnBoard);
		int savedEPFile = Pawn.enPassantFile;
		int savedEPRank = Pawn.enPassantRank;

		applyMove(mover, fromRank, fromFile, toRank, toFile, promoteTo);

		if (isInCheck(player, game.piecesOnBoard)) {
			//Restore board state and reject move
			game.piecesOnBoard = snapshot;
			Pawn.enPassantFile = savedEPFile;
			Pawn.enPassantRank = savedEPRank;
			game.message = Message.ILLEGAL_MOVE;
			return game;
		}

		updateRookFlags(fromRank, fromFile);

		//Switch to next player
		Player justMoved = player;
		player = (player == Player.white) ? Player.black : Player.white;

		//Draw is applied after move executes
		if (drawRequested) {
			game.message = Message.DRAW;
			return game;
		}

		boolean inCheck = isInCheck(player, game.piecesOnBoard);
		boolean noMoves = hasNoLegalMoves(player, game.piecesOnBoard);

		if (inCheck && noMoves) {
			if (justMoved == Player.white) {
				game.message = Message.CHECKMATE_WHITE_WINS;
			} else {
				game.message = Message.CHECKMATE_BLACK_WINS;
			}
		} else if (inCheck) {
			game.message = Message.CHECK;
		} else {
			game.message = null;
		}

		return game;	}
	

	//Helper method used in play()
	public static String[] Splitter(String input) {
		input = input.trim().replaceAll("\\s+", " ");
		String[] words = input.split(" ");

		return words;
	}

	
	/**
	 * This method should reset the game, and start from scratch.
	 */
	public static void start() {
		/* FILL IN THIS METHOD */

		//Create a new ReturnPlay object to use in play method
		game = new ReturnPlay();
		game.piecesOnBoard = newBoard();
		game.message = null;
		player = Player.white;

		King.reset();
		Pawn.reset();

	}

		//Executes a move unconditionally on the live board
	//CheckMove and in-check simulation must be done before calling this
	private static void applyMove(ReturnPiece mover,
								   int fromRank, int fromFile,
								   int toRank,   int toFile,
								   ReturnPiece.PieceType promoteTo) {

		//Delegate to piece — handles captures, en passant, castling rook slide
		PieceFunctions piece = pieceFor(mover);
		piece.MakeMove(fromRank, fromFile, toRank, toFile, game.piecesOnBoard);

		//Handle promotion after pawn has moved
		ReturnPiece arrived = FindPieceAt(toRank, toFile, game.piecesOnBoard);
		if (arrived != null) {
			if (arrived.pieceType == ReturnPiece.PieceType.WP && toRank == 8) {
				if (promoteTo != null) {
					arrived.pieceType = promoteTo;
				} else {
					arrived.pieceType = ReturnPiece.PieceType.WQ;
				}
			} else if (arrived.pieceType == ReturnPiece.PieceType.BP && toRank == 1) {
				if (promoteTo != null) {
					arrived.pieceType = promoteTo;
				} else {
					arrived.pieceType = ReturnPiece.PieceType.BQ;
				}
			}
		}
	}

	//Returns true if the given player's king is currently attacked by any opponent piece
	public static boolean isInCheck(Player player, ArrayList<ReturnPiece> pOB) {

		//Find the king
		ReturnPiece.PieceType kingType;
		if (player == Player.white) {
			kingType = ReturnPiece.PieceType.WK;
		} else {
			kingType = ReturnPiece.PieceType.BK;
		}

		ReturnPiece king = null;
		for (ReturnPiece p : pOB) {
			if (p.pieceType == kingType) {
				king = p;
				break;
			}
		}
		if (king == null) { return false; }

		int kingRank = king.pieceRank;
		int kingFile = king.pieceFile.ordinal();

		Player opponent;
		if (player == Player.white) {
			opponent = Player.black;
		} else {
			opponent = Player.white;
		}

		//Ask every opponent piece if it can attack the king's square
		for (ReturnPiece p : pOB) {
			if (isWhite(p) == (player == Player.white)) { continue; }  //skip own pieces
			PieceFunctions pf = pieceFor(p);
			if (pf.canAttack(p.pieceRank, p.pieceFile.ordinal(), kingRank, kingFile, pOB, opponent)) {
				return true;
			}
		}
		return false;
	}

	//Returns true if (rank, file) can be attacked by any piece belonging to byPlayer
	//Used by King.canCastle to verify castling squares are safe
	public static boolean squareAttacked(int rank, int file,
										  Player byPlayer,
										  ArrayList<ReturnPiece> pOB) {
		for (ReturnPiece p : pOB) {
			if (isWhite(p) != (byPlayer == Player.white)) { continue; }
			PieceFunctions pf = pieceFor(p);
			if (pf.canAttack(p.pieceRank, p.pieceFile.ordinal(), rank, file, pOB, byPlayer)) {
				return true;
			}
		}
		return false;
	}

	//Returns true if the given player has no move that keeps their king safe
	//Used to detect checkmate (inCheck + hasNoLegalMoves)
	private static boolean hasNoLegalMoves(Player player, ArrayList<ReturnPiece> pOB) {

		for (ReturnPiece p : new ArrayList<>(pOB)) {
			if (isWhite(p) != (player == Player.white)) { continue; }

			int pf = p.pieceFile.ordinal();
			int pr = p.pieceRank;

			for (int tf = 0; tf < 8; tf++) {
				for (int tr = 1; tr <= 8; tr++) {

					//Skip if piece can't move there geometrically
					if (!pieceFor(p).CheckMove(pr, pf, tr, tf, pOB, player)) { continue; }

					//Simulate the move
					ArrayList<ReturnPiece> snap = deepCopy(pOB);
					int savedEPFile = Pawn.enPassantFile;
					int savedEPRank = Pawn.enPassantRank;

					ArrayList<ReturnPiece> savedBoard = game.piecesOnBoard;
					game.piecesOnBoard = pOB;
					applyMove(p, pr, pf, tr, tf, null);
					boolean inCheck = isInCheck(player, pOB);

					//Restore everything
					game.piecesOnBoard = savedBoard;
					pOB.clear();
					pOB.addAll(snap);
					Pawn.enPassantFile = savedEPFile;
					Pawn.enPassantRank = savedEPRank;

					//Found at least one legal move — not checkmate
					if (!inCheck) { return false; }
				}
			}
		}
		return true;
	}

	//Strips castling rights from a rook that has moved off its starting square
	private static void updateRookFlags(int fromRank, int fromFile) {
		if (fromRank == 1 && fromFile == 0) { King.whiteRookAMoved = true; }
		if (fromRank == 1 && fromFile == 7) { King.whiteRookHMoved = true; }
		if (fromRank == 8 && fromFile == 0) { King.blackRookAMoved = true; }
		if (fromRank == 8 && fromFile == 7) { King.blackRookHMoved = true; }
	}

	

	//Parses promotion token into the correct PieceType for the current player
	private static ReturnPiece.PieceType parsePromotion(String token, Player player) {
		boolean w = (player == Player.white);
		switch (token.toUpperCase()) {
			case "Q": return w ? ReturnPiece.PieceType.WQ : ReturnPiece.PieceType.BQ;
			case "R": return w ? ReturnPiece.PieceType.WR : ReturnPiece.PieceType.BR;
			case "B": return w ? ReturnPiece.PieceType.WB : ReturnPiece.PieceType.BB;
			case "N": return w ? ReturnPiece.PieceType.WN : ReturnPiece.PieceType.BN;
			default:  return null;
		}
	}

	//Returns the piece at (rank, file), or null if the square is empty
	public static ReturnPiece FindPieceAt(int rank, int file, ArrayList<ReturnPiece> pOB) {
		ReturnPiece.PieceFile pf = ReturnPiece.PieceFile.values()[file];
		for (ReturnPiece p : pOB) {
			if (p.pieceRank == rank && p.pieceFile == pf) {
				return p;
			}
		}
		return null;
	}

	//Returns true if the piece belongs to white
	public static boolean isWhite(ReturnPiece p) {
		return p.pieceType.name().startsWith("W");
	}

	//Returns the right PieceFunctions implementation for a given ReturnPiece
	public static PieceFunctions pieceFor(ReturnPiece rp) {
		switch (rp.pieceType) {
			case WP: case BP: return new Pawn();
			case WR: case BR: return new Rook();
			case WN: case BN: return new Knight();
			case WB: case BB: return new Bishop();
			case WQ: case BQ: return new Queen();
			case WK: case BK: return new King();
			default: throw new IllegalArgumentException("Unknown type: " + rp.pieceType);
		}
	}

	//Creates an independent copy of the board for move simulation
	private static ArrayList<ReturnPiece> deepCopy(ArrayList<ReturnPiece> src) {
		ArrayList<ReturnPiece> copy = new ArrayList<>();
		for (ReturnPiece p : src) {
			ReturnPiece n = new ReturnPiece();
			n.pieceType = p.pieceType;
			n.pieceFile = p.pieceFile;
			n.pieceRank = p.pieceRank;
			copy.add(n);
		}
		return copy;
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

	