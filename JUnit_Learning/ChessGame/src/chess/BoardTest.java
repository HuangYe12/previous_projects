package chess;
import junit.framework.TestCase;
import util.StringUtil;

public class BoardTest extends TestCase {
	private Board board;
	
	
	protected void setUp(){
		board = new Board();
	}
	
	public void testCreate(){
		board.initialize();
		assertEquals(32, board.getNumberOfPieces());
		String blankRank = StringUtil.appendNewLine("........");
		assertEquals(
				StringUtil.appendNewLine("RNBQKBNR") +
				StringUtil.appendNewLine("PPPPPPPP") +
				blankRank + blankRank + blankRank + blankRank +
				StringUtil.appendNewLine("pppppppp") +
				StringUtil.appendNewLine("rnbqkbnr"), board.getBoardPrintForm()); 
		
		board.printBoard();
	}
	
	

}
