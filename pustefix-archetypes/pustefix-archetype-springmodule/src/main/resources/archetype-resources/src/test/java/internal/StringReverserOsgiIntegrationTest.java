#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.internal;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.osgi.test.AbstractConfigurableBundleCreatorTests;

/**
 * OSGi integration test (inside OSGi).
 * @see AbstractConfigurableBundleCreatorTests
 */
public class StringReverserOsgiIntegrationTest extends AbstractConfigurableBundleCreatorTests {

	protected String[] getConfigLocations() {
	  return new String[] {"META-INF/spring/*.xml"};
	}
	
	public void testOsgiBundleContext() {
		assertNotNull(bundleContext);
	}
	
	@Override
	protected Resource getTestingFrameworkBundlesConfiguration() {
		return new InputStreamResource(getClass().getClassLoader().getResourceAsStream("boot-bundles.properties"));
	}

}
