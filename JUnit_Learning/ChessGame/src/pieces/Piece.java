package pieces;
/**
 * Provide a chess pawn with 2 different colors, white and black.
 * @author huangye
 *
 */
public class Piece {
	private String color;
	static final String DEFAULT_COLOR = "white";
	static final String SECOND_COLOR = "black";
	private String name;
	private static int count;
	
	public boolean isBlack(){
		String color = this.color.toLowerCase();
		return color.equals(SECOND_COLOR);
	}
	
	public boolean isWhite(){
		String color = this.color.toLowerCase();
		return color.equals(DEFAULT_COLOR);
	}
	
	public static void resetPieceCount(){
		count = 0;
	}
	
	public static int getPiecesCount(){
		return count;
	}
	
	private Piece(String color, String name){
		this.color = color.toLowerCase();
		if (this.color.equals(SECOND_COLOR)) {
			this.name = name.toLowerCase();
		}
		else
			this.name = name.toUpperCase();
		
	}
	
	public static Piece create(String color, String name){
		++count;
		return new Piece(color, name);
	}
	/**
	 * Without parameter, the color of pawn will be white by default.
	 */

	
	String getColor(){
		return color;
	}
	
	public char getPrintForm(){
		return this.name.charAt(0);
	}
}
