import junit.framework.TestCase;


public class CharacterTest extends TestCase {
	public void testWhitespace(){
		assertEquals(true,Character.isWhitespace('\n'));
		assertEquals(true,Character.isWhitespace('\t'));
		assertEquals(true,Character.isWhitespace('\f'));
	}
	
	public void testJavaIdentifier(){
		assertEquals(true, Character.isJavaIdentifierPart('2'));
		assertEquals(false, Character.isJavaIdentifierStart('2'));
	}

}
