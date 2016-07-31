

import junit.framework.TestSuite;

public class AllTests {
	public static TestSuite suite(){
		TestSuite suite = new TestSuite();
		suite.addTestSuite(chess.BoardTest.class);
		suite.addTestSuite(pieces.PieceTest.class);
		suite.addTestSuite(CharacterTest.class);
		return suite;
	}
}
