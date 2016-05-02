package de.schlund.pfixcore.generator.prechecks;

import java.util.regex.Pattern;

import junit.framework.TestCase;

public class RegexpCheckTest extends TestCase {

	public void test() {
		 
		 String perlRegex = "/^(BEGINNER|ADVANCED|EXPERT)$/";
		 Pattern javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(javaPattern, "ADVANCED");
		 assertNoMatch(javaPattern, "XADVANCED");
		 
		 perlRegex = "/.{3,}/";
		 javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(javaPattern, "abc");
		 assertMatch(javaPattern, "abcd");
		 assertNoMatch(javaPattern, "ab");
		 
		 perlRegex = "/[^\n\r]*/";
		 javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(javaPattern, "foo");
		 assertMatch(javaPattern, "\n\n");
		 
		 perlRegex = "/[0-9]+/";
		 javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(javaPattern, "123");
		 assertMatch(javaPattern, "abc123");
		 assertNoMatch(javaPattern, "abc");
		 
		 perlRegex = "/^[0-9]+$/";
		 javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(javaPattern, "123");
		 assertNoMatch(javaPattern, "abc123");
		 assertNoMatch(javaPattern, "abc");
	
		 perlRegex = "/^[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}$/i";
		 javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(javaPattern, "a@b.de");
		 assertNoMatch(javaPattern, "a@b");
		 
		 perlRegex = "/^https?:\\/\\/([a-z0-9@-]+\\.)*[a-z0-9]{2,}\\.[a-z]{2,5}[a-z0-9@\\/?_=;:~&-.]*$/i";
		 javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(javaPattern, "http://pustefixframework.org");
		 assertMatch(javaPattern, "http://pustefixframework.org/");
		 assertMatch(javaPattern, "https://pustefixframework.org?foo=bar");
		 assertNoMatch(javaPattern, "pustefixframework.org");
		 assertNoMatch(javaPattern, "http://pustefixframework");
		 
		 perlRegex = "/foo";
		 javaPattern = Pattern.compile(RegexpCheck.perl5ToJavaRegex(perlRegex));
		 assertMatch(javaPattern, "/foo");
		 assertNoMatch(javaPattern, "/bar");
		 
	}

	public void assertMatch(Pattern javaPattern, String value) {
		assertTrue(javaPattern.matcher(value).find());
	}
	
	public void assertNoMatch(Pattern javaPattern, String value) {
		assertFalse(javaPattern.matcher(value).find());
	}

}
