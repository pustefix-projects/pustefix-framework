#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.internal;

import ${package}.StringReverser;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Local integration test (outside of OSGi).
 * @see StringReverserOsgiIntegrationTest for integration test inside OSGi.
 */
public class StringReverserIntegrationTest extends AbstractDependencyInjectionSpringContextTests {

	private StringReverser reverser;
	
	protected String[] getConfigLocations() {
	  return new String[] {"META-INF/spring/bundle-context.xml"};
	}
	
	public void setStringReverser(StringReverser reverser) {
	  this.reverser = reverser;
	}
	
	public void testReverse() {
	  assertEquals("cba", reverser.reverse("abc"));
	}

}
