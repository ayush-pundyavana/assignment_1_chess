package chess;

import java.util.ArrayList;
import java.util.HashMap;

interface PieceFunctions {
    
    boolean CheckMove(int initRank, int initFile, int nextRank, int nextFile, ArrayList<ReturnPiece> pOB, Chess.Player color);
    void MakeMove(int initRank, int initFile, int nextRank, int nextFile, ArrayList<ReturnPiece> pOB);        
    default void UpdateLOS() { }
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

    //default good for queens, rooks, bishops
    default boolean isPathClear(int initRank, int initFile, int nextRank, int nextFile, ArrayList<ReturnPiece> pOB) {
        int stepRank = Integer.signum(nextRank - initRank);
        int stepFile = Integer.signum(nextFile - initFile);
        int curRank  = initRank + stepRank;
        int curFile  = initFile + stepFile;

        while (curRank != nextRank || curFile != nextFile) {
            //ADD CASE WHERE WE ARE TO REMOVE OPPONENT PIECE
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
    private int file;
    private int rank;

    public King (int file, int rank) {
        this.file = file; 
        this.rank = rank;
    }


    public boolean CheckMove (int initRank, int initFile, int rank, int file) {
        return true;
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
    int file;
    int rank;
    Chess.Player color;
    ArrayList<HashMap<String, Boolean>> LOS;
    ArrayList<ReturnPiece> pOB;
    
    public Knight (int file, int rank, ArrayList<HashMap<String, Boolean>> LOS, ArrayList<ReturnPiece> pOB, Chess.Player color) {
        this.file = file;
        this.rank = rank;
        this.color = color;
        this.LOS = LOS;
        this.pOB = pOB;
    }
    
    public boolean CheckMove (int nextRank, int nextFile) {
        //check for knight type of move
        if (Math.abs(nextRank-rank) != 2 || Math.abs(nextFile-file) != 1) { return false; }

        //Check for collision
        if (FindPieceAt(nextRank, nextFile, pOB) == null) { return true; }

        boolean sameColor = isSameColor(nextRank, nextFile, color, pOB);
        
        if (sameColor) {return false;}
        return true;
    }

    public void MakeMove(int nextRank, int nextFile) {
        if (CheckMove(nextRank, nextFile) == false) {return;}

        //Move piece (no collision)
        ReturnPiece temp = FindPieceAt(nextRank, nextFile, pOB);
        ReturnPiece currPiece = FindPieceAt(rank, file, pOB);
        if (temp == null) { 
            
            currPiece.pieceRank = nextRank;
            currPiece.pieceFile = ReturnPiece.PieceFile.values()[nextFile];
        }

        //Move piece (collision)
        removeAt(nextRank, nextFile, null, pOB);
        currPiece.pieceRank = nextRank;
        currPiece.pieceFile = ReturnPiece.PieceFile.values()[nextFile];
    }
}

class Bishop implements PieceFunctions {
    int file;
    int rank;
    Chess.Player color;
    ArrayList<HashMap<String, Boolean>> LOS;
    ArrayList<ReturnPiece> pOB;
    
    public Bishop (int file, int rank, ArrayList<HashMap<String, Boolean>> LOS, ArrayList<ReturnPiece> pOB, Chess.Player color) {
        this.file = file;
        this.rank = rank;
        this.color = color;
        this.LOS = LOS;
        this.pOB = pOB;
    }
    
    public boolean CheckMove (int nextRank, int nextFile) {
        //Check for calling itself
        if (rank == nextRank && file == nextFile) {return false;}
        if (Math.abs(nextRank - rank) != Math.abs(nextFile - file)) {return false;}

        return isPathClear(rank, file, nextRank, nextFile, pOB);
    }

    public void MakeMove(int nextRank, int nextFile) {
        if (!(CheckMove(nextRank, nextFile))) { return; }

        //Move piece (no collision)
        ReturnPiece temp = FindPieceAt(nextRank, nextFile, pOB);
        ReturnPiece currPiece = FindPieceAt(rank, file, pOB);
        if (temp == null) {   
            currPiece.pieceRank = nextRank;
            currPiece.pieceFile = ReturnPiece.PieceFile.values()[nextFile];
        }

        //Move piece (collision)
        removeAt(nextRank, nextFile, null, pOB);
        currPiece.pieceRank = nextRank;
        currPiece.pieceFile = ReturnPiece.PieceFile.values()[nextFile];
    }
}

class Pawn implements PieceFunctions {
    
}