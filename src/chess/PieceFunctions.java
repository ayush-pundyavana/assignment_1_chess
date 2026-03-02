package chess;

import java.util.ArrayList;
import java.util.HashMap;

interface PieceFunctions {
    
    boolean CheckMove();
    void MakeMove();
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

    public MakeMove (int nextRank, int nextFile) {

    }

    
}

class Rook implements PieceFunctions {
    public boolean CheckMove (int initRank, int initFile, int rank, int file) {
        if (initRank==rank || initFile==file) {return true;}  //Horiz. & Vertical}
        return false;
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
    
}