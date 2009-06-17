#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.internal;

import junit.framework.TestCase;

import ${package}.StringReverser;

public class StringReverserImplTest extends TestCase {

    public void testReverse() {
    	StringReverser reverser = new StringReverserImpl();
    	String str = "abc";
    	String expStr = "cba";
    	String resStr = reverser.reverse(str);
    	assertEquals(expStr, resStr);
    	resStr = reverser.reverse(resStr);
    	assertEquals(str, resStr);
    }

}
