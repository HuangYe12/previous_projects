package chess;

import java.util.*;

import pieces.Piece;
/**
 * Used to create a Chess Board.
 * @author huangye
 *
 */
public class Board {
	private ArrayList<Piece> firstRowPieces = new ArrayList<Piece>();
	private ArrayList<Piece> secondRowPieces = new ArrayList<Piece>();
	private ArrayList<Piece> seventhRowPieces = new ArrayList<Piece>();
	private ArrayList<Piece> eighthRowPieces = new ArrayList<Piece>();
	static final char PRINT_FORM_OF_WHITEPIECE = 'P';
	static final char PRINT_FORM_OF_BLACKPIECE = 'p';
	static final String EMPTY_ROW_PRINT_FORM = "........";
	
	
	void initialize(){
		Piece.resetPieceCount();
		for(int i=0; i<8; ++i){
			this.secondRowPieces.add(pieces.Piece.create("white", "pawn")); //("white", PRINT_FORM_OF_WHITEPAWN));
			this.seventhRowPieces.add(pieces.Piece.create("black", "pawn"));	
		}
		this.firstRowPieces.add(pieces.Piece.create("white", "rook"));
		this.firstRowPieces.add(pieces.Piece.create("white", "Nknight"));
		this.firstRowPieces.add(pieces.Piece.create("white", "bishop"));
		this.firstRowPieces.add(pieces.Piece.create("white", "queen"));
		this.firstRowPieces.add(pieces.Piece.create("white", "king"));
		this.firstRowPieces.add(pieces.Piece.create("white", "bishop"));
		this.firstRowPieces.add(pieces.Piece.create("white", "Nknight"));
		this.firstRowPieces.add(pieces.Piece.create("white", "rook"));
		
		this.eighthRowPieces.add(pieces.Piece.create("black", "rook"));
		this.eighthRowPieces.add(pieces.Piece.create("black", "Nknight"));
		this.eighthRowPieces.add(pieces.Piece.create("black", "bishop"));
		this.eighthRowPieces.add(pieces.Piece.create("black", "queen"));
		this.eighthRowPieces.add(pieces.Piece.create("black", "king"));
		this.eighthRowPieces.add(pieces.Piece.create("black", "bishop"));
		this.eighthRowPieces.add(pieces.Piece.create("black", "Nknight"));
		this.eighthRowPieces.add(pieces.Piece.create("black", "rook"));
	}
	
	int getNumberOfPieces(){
		return Piece.getPiecesCount();
	}
	
	String getRowPrintForm(int row){
		if(row == 1){
			StringBuilder buffer = new StringBuilder();
			for(Piece piece: firstRowPieces)
				buffer.append(piece.getPrintForm());
			return buffer.toString();
		}
		
		if(row == 2){
			StringBuilder buffer = new StringBuilder();
			for(Piece piece: secondRowPieces)
				buffer.append(piece.getPrintForm());
			return buffer.toString();
		}
		
		if(row == 7){
			StringBuilder buffer = new StringBuilder();
			for(Piece piece: seventhRowPieces)
				buffer.append(piece.getPrintForm());
			return buffer.toString();
		}
		
		if(row == 8){
			StringBuilder buffer = new StringBuilder();
			for(Piece piece: eighthRowPieces)
				buffer.append(piece.getPrintForm());
			return buffer.toString();
		}
		
		else
			return EMPTY_ROW_PRINT_FORM;
	}
	
	String getBoardPrintForm(){
		StringBuilder bufferBoard = new StringBuilder();
		for(int row = 1; row <= 8; ++row)
			bufferBoard.append(util.StringUtil.appendNewLine(getRowPrintForm(row)));
		
		return bufferBoard.toString();
	}
	
	void printBoard(){
		System.out.print(getBoardPrintForm());
	}
/*
	Pawn getPieces(int index){
		return pawns.get(index);
	}
*/	
}
