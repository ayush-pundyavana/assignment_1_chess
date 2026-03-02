package chess;

import java.util.ArrayList;
import java.util.HashMap;

interface PieceFunctions {
    
    boolean CheckMove(int initRank, int initFile, int nextRank, int nextFile, ArrayList<ReturnPiece> pOB, Chess.Player color);

    void MakeMove(int initRank, int initFile, int nextRank, int nextFile, ArrayList<ReturnPiece> pOB);
                      
    default void UpdateLOS() {
        
    }

    default ReturnPiece FindPieceAt(int r,int f, ArrayList<ReturnPiece> pOB) {
        ReturnPiece.PieceFile pf = ReturnPiece.PieceFile.values()[f];

        for (ReturnPiece rp : pOB) {
            if (rp.pieceRank == r && rp.pieceFile == pf) {
                return rp;
            }
        }
        return null;
    }

    default boolean canAttack(int fromRank, int fromFile, int toRank, int toFile, ArrayList<ReturnPiece> pOB, Chess.Player color) {
        return CheckMove(fromRank, fromFile, toRank, toFile, pOB, color);
    }

    default boolean isSameColor(int rank, int file, Chess.Player color,ArrayList<ReturnPiece> pOB) {
        ReturnPiece piece = FindPieceAt(rank, file, pOB);
        if (piece == null) return false;
        boolean pieceIsWhite  = piece.pieceType.name().startsWith("W");
        boolean colorIsWhite  = (color == Chess.Player.white);
        return pieceIsWhite == colorIsWhite;
    }

    default boolean isPathClear(int initRank, int initFile, int nextRank, int nextFile, ArrayList<ReturnPiece> pOB) {
        int stepRank = Integer.signum(nextRank - initRank);
        int stepFile = Integer.signum(nextFile - initFile);
        int curRank  = initRank + stepRank;
        int curFile  = initFile + stepFile;

        while (curRank != nextRank || curFile != nextFile) {
            if (FindPieceAt(curRank, curFile, pOB) != null) return false;
            curRank += stepRank;
            curFile += stepFile;
        }
        return true;
    }

    default void removeAt(int rank, int file, ReturnPiece exclude, ArrayList<ReturnPiece> pOB) {
        ReturnPiece.PieceFile pf = ReturnPiece.PieceFile.values()[file];
        pOB.removeIf(p -> p.pieceRank == rank && p.pieceFile == pf && p != exclude);
    }

}

class King implements PieceFunctions {

    int file;
    int rank;
    Chess.Player color;
    ArrayList<HashMap<String, Boolean>> LOS;
    ArrayList<ReturnPiece> pOB;
    

    public King (int file, int rank, ArrayList<HashMap<String, Boolean>> LOS, ArrayList<ReturnPiece> pOB, Chess.Player color) {
        this.file = file; 
        this.rank = rank;
        this.color = color;
        this.LOS = LOS;
        this.pOB = pOB;
    }
    //castling flags
    public static boolean whiteKingMoved  = false;
    public static boolean whiteRookAMoved = false;
    public static boolean whiteRookHMoved = false;
    public static boolean blackKingMoved  = false;
    public static boolean blackRookAMoved = false;
    public static boolean blackRookHMoved = false;

    public static void reset() {
        whiteKingMoved  = false;
        whiteRookAMoved = false;
        whiteRookHMoved = false;
        blackKingMoved  = false;
        blackRookAMoved = false;
        blackRookHMoved = false;
    }
    
    public boolean CheckMove(int initRank, int initFile, int nextRank, int nextFile,ArrayList<ReturnPiece> pOB, Chess.Player color) {
        if (initRank == nextRank && initFile == nextFile) return false;

        int dr = Math.abs(nextRank - initRank);
        int df = Math.abs(nextFile - initFile);

        // Can't capture own piece
        if (isSameColor(nextRank, nextFile, color, pOB)) return false;

        // Normal one-square move
        if (dr <= 1 && df <= 1) return true;

        // Castling: king moves 2 squares horizontally
        if (dr == 0 && df == 2) {
            return canCastle(initRank, initFile, nextFile, pOB, color);
        }

        return false;
    }


    private boolean canCastle(int fromRank, int fromFile, int toFile,ArrayList<ReturnPiece> pOB, Chess.Player color) {
        boolean white = (color == Chess.Player.white);

        if (white) {
            if (whiteKingMoved || fromRank != 1 || fromFile != 4) return false;
            Chess.Player opp = Chess.Player.black;
            if (toFile == 6) {  // king-side
                return !whiteRookHMoved
                    && FindPieceAt(1, 5, pOB) == null
                    && FindPieceAt(1, 6, pOB) == null
                    && !Chess.squareAttacked(1, 4, opp, pOB)
                    && !Chess.squareAttacked(1, 5, opp, pOB)
                    && !Chess.squareAttacked(1, 6, opp, pOB);
            }
            if (toFile == 2) {  // queen-side
                return !whiteRookAMoved
                    && FindPieceAt(1, 1, pOB) == null
                    && FindPieceAt(1, 2, pOB) == null
                    && FindPieceAt(1, 3, pOB) == null
                    && !Chess.squareAttacked(1, 4, opp, pOB)
                    && !Chess.squareAttacked(1, 3, opp, pOB)
                    && !Chess.squareAttacked(1, 2, opp, pOB);
            }
        } else {
            if (blackKingMoved || fromRank != 8 || fromFile != 4) return false;
            Chess.Player opp = Chess.Player.white;
            if (toFile == 6) {
                return !blackRookHMoved
                    && FindPieceAt(8, 5, pOB) == null
                    && FindPieceAt(8, 6, pOB) == null
                    && !Chess.squareAttacked(8, 4, opp, pOB)
                    && !Chess.squareAttacked(8, 5, opp, pOB)
                    && !Chess.squareAttacked(8, 6, opp, pOB);
            }
            if (toFile == 2) {
                return !blackRookAMoved
                    && FindPieceAt(8, 1, pOB) == null
                    && FindPieceAt(8, 2, pOB) == null
                    && FindPieceAt(8, 3, pOB) == null
                    && !Chess.squareAttacked(8, 4, opp, pOB)
                    && !Chess.squareAttacked(8, 3, opp, pOB)
                    && !Chess.squareAttacked(8, 2, opp, pOB);
            }
        }
        return false;
    }

    public void MakeMove(int initRank, int initFile, int nextRank, int nextFile,
                         ArrayList<ReturnPiece> pOB) {
        boolean white = (FindPieceAt(initRank, initFile, pOB).pieceType == ReturnPiece.PieceType.WK);

        // Slide the rook if castling
        if (Math.abs(nextFile - initFile) == 2) {
            int rank = white ? 1 : 8;
            if (nextFile == 6) {    // king-side: rook h→f
                ReturnPiece rook = FindPieceAt(rank, 7, pOB);
                if (rook != null) rook.pieceFile = ReturnPiece.PieceFile.values()[5];
            } else {                // queen-side: rook a→d
                ReturnPiece rook = FindPieceAt(rank, 0, pOB);
                if (rook != null) rook.pieceFile = ReturnPiece.PieceFile.values()[3];
            }
        }

        // Update flags
        if (white) whiteKingMoved = true;
        else        blackKingMoved = true;

        // Move the king
        ReturnPiece king = FindPieceAt(initRank, initFile, pOB);
        if (king == null) return;
        king.pieceRank = nextRank;
        king.pieceFile = ReturnPiece.PieceFile.values()[nextFile];
    }

    public boolean canAttack(int fromRank, int fromFile, int toRank, int toFile, ArrayList<ReturnPiece> pOB, Chess.Player color) {
        int dr = Math.abs(toRank - fromRank);
        int df = Math.abs(toFile - fromFile);
        return dr <= 1 && df <= 1 && !(dr == 0 && df == 0);
    }
}



class Queen implements PieceFunctions {
    int file;
    int rank;
    Chess.Player color;
    ArrayList<HashMap<String, Boolean>> LOS;
    ArrayList<ReturnPiece> pOB;
    

    public Queen (int file, int rank, ArrayList<HashMap<String, Boolean>> LOS, ArrayList<ReturnPiece> pOB, Chess.Player color) {
        this.file = file; 
        this.rank = rank;
        this.color = color;
        this.LOS = LOS;
        this.pOB = pOB;
    }
    
    public boolean CheckMove (int nextRank, int nextFile) {
        int dr = Math.abs(rank - nextRank);
        int df = Math.abs(file - nextFile);

        if (rank == nextRank && file == nextFile) {return false;}  //same square
        if (!(rank==nextRank || file==nextFile || dr == df)) {return false;}  //not Horiz, Vertical, or Diag.}

        /*
        ArrayList<Boolean> lineOfView = new ArrayList<Boolean>();

        boolean isLeft  = nextRank == rank && nextFile < file;     lineOfView.add(isLeft);
        boolean isTopLeft = nextRank > rank && nextFile < file;    lineOfView.add(isTopLeft);
        boolean isUp    = nextRank > rank && nextFile == file;     lineOfView.add(isUp);
        boolean isTopRight= nextRank > rank && nextFile > file;    lineOfView.add(isTopRight);
        boolean isRight = nextRank == rank && nextFile > file;     lineOfView.add(isRight);
        boolean isBottomRight= nextRank < rank && nextFile > file; lineOfView.add(isBottomRight);
        boolean isDown  = nextRank < rank && nextFile == file;     lineOfView.add(isDown);
        boolean isBottomLeft = nextRank < rank && nextFile < file; lineOfView.add(isBottomLeft);
         */

        
        
        //Probing for position in each line of sight
        for (int i = 0; i < LOS.size(); i++) {
            //Getting the move  e.g  4,5 to "d5"
            char file_char = (char) ('a' + (nextFile-1));
            String move = "" + file_char + ("" + nextRank);

            //probing
            if (LOS.get(i).containsKey(move)) {
                if (LOS.get(i).get(move) == false) {
                    return true;
                } 
                
                ReturnPiece piece = FindPieceAt(nextRank, nextFile, this.pOB);
                boolean isSameColor = (piece.pieceType.name().startsWith("W") && color.name().startsWith("w")) || (piece.pieceType.name().startsWith("B") && color.name().startsWith("b"));
                boolean isDiffColor = !(isSameColor);
                if (isSameColor) { return false; }
                if (isDiffColor) { return true; }
            }
        }

        //Implement isKingSafe Here:


        return false;
        
        //checking
    }

    public void MakeMove (int nextRank, int nextFile) {
        ReturnPiece curr_piece = FindPieceAt(rank, file, pOB);
        char file_char = (char) ('a' + (nextFile-1));

        //Not taking another piece
        if (FindPieceAt(nextRank, nextFile, pOB) == null) {
            curr_piece.pieceRank = nextRank;
            curr_piece.pieceFile = ReturnPiece.PieceFile.values()[nextFile];
        }

        //Taking another piece Implementation Here:

    }

    
}

class Rook implements PieceFunctions {

    int file;
    int rank;
    Chess.Player color;
    ArrayList<HashMap<String, Boolean>> LOS;
    ArrayList<ReturnPiece> pOB;
    

    public Rook (int file, int rank, ArrayList<HashMap<String, Boolean>> LOS, ArrayList<ReturnPiece> pOB, Chess.Player color) {
        this.file = file; 
        this.rank = rank;
        this.color = color;
        this.LOS = LOS;
        this.pOB = pOB;
    }
    public boolean CheckMove(int initRank, int initFile, int nextRank, int nextFile,ArrayList<ReturnPiece> pOB, Chess.Player color) {

        if (initRank == nextRank && initFile == nextFile) return false;


        if (initRank != nextRank && initFile != nextFile) return false;

        if (isSameColor(nextRank, nextFile, color, pOB)) return false;

        // Path must be clear
        return isPathClear(initRank, initFile, nextRank, nextFile, pOB);
    } 
    public void MakeMove(int initRank, int initFile, int nextRank, int nextFile, ArrayList<ReturnPiece> pOB) {

        ReturnPiece rook = FindPieceAt(initRank, initFile, pOB);
        if (rook == null) return;

        // Remove any captured piece at destination
        removeAt(nextRank, nextFile, rook, pOB);

        // Move the rook
        rook.pieceRank = nextRank;
        rook.pieceFile = ReturnPiece.PieceFile.values()[nextFile];
    }
}

class Knight implements PieceFunctions {
    public boolean CheckMove (int initRank, int initFile, int rank, int file) {
        if (Math.abs(rank-initRank) == 2) {
            if (Math.abs(file-initFile) == 1) {
                return true;
            }
        }

        if (Math.abs(file-initFile) == 2) {
            if(Math.abs(rank-initRank) == 1) {
                return true;
            }
        }

        return false;
    }
}

class Bishop implements PieceFunctions {
    public boolean CheckMove (int initRank, int initFile, int rank, int file) {
        if (initRank - rank == initFile - file) {return true;}  //Lower Left Diag.}
        if (initRank - rank == initFile + file) {return true;}  //Lower Right Diag.
        if (initRank + rank == initFile - file) {return true;}  //Upper Left Diag.
        if (initRank + rank == initFile + file) {return true;}  //Upper Right Diag.
        return false;
    }
}

class Pawn implements PieceFunctions {

    int file;
    int rank;
    Chess.Player color;
    ArrayList<HashMap<String, Boolean>> LOS;
    ArrayList<ReturnPiece> pOB;
    

    public Pawn (int file, int rank, ArrayList<HashMap<String, Boolean>> LOS, ArrayList<ReturnPiece> pOB, Chess.Player color) {
        this.file = file; 
        this.rank = rank;
        this.color = color;
        this.LOS = LOS;
        this.pOB = pOB;
    }
    public static int enPassantFile = -1;
    public static int enPassantRank = -1;

    public static void reset() {
        enPassantFile = -1;
        enPassantRank = -1;
    }

    public boolean CheckMove(int initRank, int initFile, int nextRank, int nextFile, ArrayList<ReturnPiece> pOB, Chess.Player color) {

        if (initRank == nextRank && initFile == nextFile) return false;

        int dr        = nextRank - initRank;  // signed: +1 = up, -1 = down
        int df        = Math.abs(nextFile - initFile);
        boolean white = (color == Chess.Player.white);
        int direction = white ? 1 : -1;       // white moves up, black moves down
        int startRank = white ? 2 : 7;        // rank pawns start on

        ReturnPiece target = FindPieceAt(nextRank, nextFile, pOB);

        // One step forward into empty square
        if (df == 0 && dr == direction && target == null) {
            return true;
        }

        // Two steps forward from starting rank 
        if (df == 0 && dr == 2 * direction && initRank == startRank
                && target == null
                && FindPieceAt(initRank + direction, initFile, pOB) == null) {
            return true;
        }

        // Diagonal capture
        if (df == 1 && dr == direction
                && target != null
                && !isSameColor(nextRank, nextFile, color, pOB)) {
            return true;
        }

        // En passant 
        if (df == 1 && dr == direction
                && nextFile == enPassantFile
                && nextRank == enPassantRank) {
            return true;
        }

        return false;
    }


    public void MakeMove(int initRank, int initFile, int nextRank, int nextFile,
                         ArrayList<ReturnPiece> pOB) {

        ReturnPiece pawn = FindPieceAt(initRank, initFile, pOB);
        if (pawn == null) return;

        boolean white = (pawn.pieceType == ReturnPiece.PieceType.WP);

        // ── en passant capture 
        if (nextFile == enPassantFile && nextRank == enPassantRank) {
            int capturedRank = white ? nextRank - 1 : nextRank + 1;
            final int capFile = nextFile;
            pOB.removeIf(p -> p.pieceRank == capturedRank && p.pieceFile == ReturnPiece.PieceFile.values()[capFile]);
        }

        
        if (white && initRank == 2 && nextRank == 4) {
            enPassantFile = nextFile;
            enPassantRank = 3;           // the square white skipped over
        } else if (!white && initRank == 7 && nextRank == 5) {
            enPassantFile = nextFile;
            enPassantRank = 6;           // the square black skipped over
        } else {
            enPassantFile = -1;
            enPassantRank = -1;
        }

        removeAt(nextRank, nextFile, pawn, pOB);

        pawn.pieceRank = nextRank;
        pawn.pieceFile = ReturnPiece.PieceFile.values()[nextFile];
    }


    public boolean canAttack(int fromRank, int fromFile, int toRank, int toFile, ArrayList<ReturnPiece> pOB, Chess.Player color) {
        int dr        = toRank - fromRank;
        int df        = Math.abs(toFile - fromFile);
        int direction = (color == Chess.Player.white) ? 1 : -1;
        return df == 1 && dr == direction;
    }

}