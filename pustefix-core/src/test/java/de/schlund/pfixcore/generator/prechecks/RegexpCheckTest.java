package de.schlund.pfixcore.generator.prechecks;

import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.oro.text.perl.Perl5Util;

public class RegexpCheckTest extends TestCase {
	
	private Perl5Util p5 = new Perl5Util();
	
	private String[] testValues = { "", " ", "\n", "abc", "a", "123", "en", "de", "en_GB", "de_DE", "ABC", "A"};
	
	private String[] testRegexps= { "/^(en|de)$/", "/[A-Z]/", "/.{1,2}/", "/^[a-zA-Z]*$/"};
	
	public void test() {
		 
		 String perlRegex = "/^(BEGINNER|ADVANCED|EXPERT)$/";
		 Pattern javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(perlRegex, javaPattern, "ADVANCED");
		 assertNoMatch(perlRegex, javaPattern, "XADVANCED");
		 assertMatches(perlRegex, javaPattern, testValues);
		 
		 perlRegex = "/.{3,}/";
		 javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(perlRegex, javaPattern, "abc");
		 assertMatch(perlRegex, javaPattern, "abcd");
		 assertNoMatch(perlRegex, javaPattern, "ab");
		 assertMatches(perlRegex, javaPattern, testValues);
		 
		 perlRegex = "/[^\n\r]*/";
		 javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(perlRegex, javaPattern, "foo");
		 assertMatch(perlRegex, javaPattern, "\n\n");
		 assertMatches(perlRegex, javaPattern, testValues);
		 
		 perlRegex = "/[0-9]+/";
		 javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(perlRegex, javaPattern, "123");
		 assertMatch(perlRegex, javaPattern, "abc123");
		 assertNoMatch(perlRegex, javaPattern, "abc");
		 assertMatches(perlRegex, javaPattern, testValues);
		 
		 perlRegex = "/^[0-9]+$/";
		 javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(perlRegex, javaPattern, "123");
		 assertNoMatch(perlRegex, javaPattern, "abc123");
		 assertNoMatch(perlRegex, javaPattern, "abc");
		 assertMatches(perlRegex, javaPattern, testValues);
	
		 perlRegex = "/^[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$/i";
		 javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(perlRegex, javaPattern, "a@b.de");
		 assertNoMatch(perlRegex, javaPattern, "a@b");
		 assertMatches(perlRegex, javaPattern, testValues);
		 
		 perlRegex = "/^https?:\\/\\/([a-z0-9@-]+\\.)*[a-z0-9]{2,}\\.[a-z]{2,5}[a-z0-9@\\/?_=;:~&-.]*$/i";
		 javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(perlRegex, javaPattern, "http://pustefixframework.org");
		 assertMatch(perlRegex, javaPattern, "http://pustefixframework.org/");
		 assertMatch(perlRegex, javaPattern, "https://pustefixframework.org?foo=bar");
		 assertNoMatch(perlRegex, javaPattern, "pustefixframework.org");
		 assertNoMatch(perlRegex, javaPattern, "http://pustefixframework");
		 assertMatches(perlRegex, javaPattern, testValues);
		 
		 for(String testRegexp: testRegexps) {
			 assertMatches(testRegexp, Pattern.compile(RegexpCheck.perl5ToJavaRegex(testRegexp)), testValues);
		 }
		 
	}

	public void assertMatch(String perlRegex, Pattern javaPattern, String value) {
		assertTrue(p5.match(perlRegex, value));
		assertTrue(javaPattern.matcher(value).find());
	}
	
	public void assertNoMatch(String perlRegex, Pattern javaPattern, String value) {
		assertFalse(p5.match(perlRegex, value));
		assertFalse(javaPattern.matcher(value).find());
	}
	
	public void assertMatches(String perlRegex, Pattern javaPattern, String[] testValues) {
		for(String value: testValues) {
			boolean matchPerl = p5.match(perlRegex, value);
			boolean matchJava = javaPattern.matcher(value).find();
			assertEquals("Checking regex '" + perlRegex + "' with value '" + value + "'", matchPerl, matchJava);
		}
	}

}
