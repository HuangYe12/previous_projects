package pieces;
import junit.framework.TestCase;


public class PieceTest extends TestCase {
	public void testCreate(){
		final String pawnDefaultColor = "white";
		Piece firstPawn = Piece.create(pawnDefaultColor,"pawn");
		assertEquals(pawnDefaultColor, firstPawn.getColor());
		assertFalse(firstPawn.isBlack());
		assertTrue(firstPawn.isWhite());
		
		final String pawnSecondColor = "black";
		Piece secondPawn = Piece.create(pawnSecondColor,"pawn");
		assertEquals(pawnSecondColor, secondPawn.getColor());
		assertTrue(secondPawn.isBlack());
		assertFalse(secondPawn.isWhite());
		
		
		final char printFormForWhite = 'P';
		final char printFormForBlack = 'p';
		assertEquals(printFormForWhite, firstPawn.getPrintForm());
		assertEquals(printFormForBlack, secondPawn.getPrintForm());
	}

}
