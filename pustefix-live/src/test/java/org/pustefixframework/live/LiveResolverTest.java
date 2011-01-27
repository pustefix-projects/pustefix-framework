/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.pustefixframework.live;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.pustefixframework.live.Helper.APP1_BASE_DIR;
import static org.pustefixframework.live.Helper.WORKSPACE_BASE_DIR;
import static org.pustefixframework.live.Helper.createProjectLayout;
import static org.pustefixframework.live.Helper.createWorkspaceLive;

import java.io.File;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

public class LiveResolverTest {

    @Before
    public void setUp() throws Exception {
        Helper.cleanUp();
    }

    @Test
    public void testResolveLiveRootWebapp() throws Exception {
        createWorkspaceLive();
        createProjectLayout();

        LiveResolver liveResolver = new LiveResolver();
        String docroot = new File(APP1_BASE_DIR, "target" + File.separator + "app1").toString();

        // path must start with a slash
        try {
            liveResolver.resolveLiveRoot(docroot, "file.xml");
            fail("Expected IllegalArgumentException, path must start with a slash.");
        } catch (IllegalArgumentException e) {
            // expected
        }

        File liveRoot = liveResolver.resolveLiveRoot(docroot, File.separator + "file.xml");
        assertNotNull(liveRoot);

        // Make the test run under Windows
        File[] roots = File.listRoots();
        boolean rootEqualsLifeRoot = false;
        for (File root : roots) {
        	String rootPathToFile = root.toString() + "tmp" + File.separator + "app1" + File.separator + "src" + File.separator + "main" + File.separator + "webapp";
        	if (rootPathToFile.equals(liveRoot.toString())) {
        		rootEqualsLifeRoot = true;
        	}
        }
        assertTrue(rootEqualsLifeRoot);
    }

    /**
     * Assert no live docroot for the editor. The editor has its own pom.xml below target/editor/META-INF, no live
     * resources are specified for those coordinates.
     * @throws Exception
     */
    @Test
    public void testResolveLiveRootOfEditor() throws Exception {
        createWorkspaceLive();
        createProjectLayout();

        LiveResolver liveResolver = new LiveResolver();
        String docroot = new File(APP1_BASE_DIR, "target" + File.separator + "editor").toString();

        File liveRoot = liveResolver.resolveLiveRoot(docroot, File.separator + "file.xml");
        assertNull(liveRoot);
    }

    @Test
    public void testResolveLiveRootModule() throws Exception {
        createWorkspaceLive();
        createProjectLayout();

        LiveResolver liveResolver = new LiveResolver();
        String moduleJarPath = new File(WORKSPACE_BASE_DIR, "sample-module-A-0.14.4-SNAPSHOT.jar").toString();

        File liveRoot = liveResolver.resolveLiveRoot(moduleJarPath, File.separator + "file.xml");
        assertNotNull(liveRoot);
        
        // Make the test run under Windows
        File[] roots = File.listRoots();
        boolean rootEqualsLifeRoot = false;
        for (File root : roots) {
        	String rootPathToFile = root.toString() + "tmp" + File.separator + "sample" + File.separator + "src" + File.separator + "main" + File.separator + "resources";
        	if (rootPathToFile.equals(liveRoot.toString())) {
        		rootEqualsLifeRoot = true;
        	}
        }
        assertTrue(rootEqualsLifeRoot);
    }

    /**
     * If live.xml exists and it contain a &lt;war&gt; element this is used for resolving the live docroot. The webapp
     * group:artifact:version is retrieved from pom.xml.
     * @throws Exception
     */
    @Test
    public void testResolveLiveDocrootByLiveXml() throws Exception {
        createWorkspaceLive();
        createProjectLayout();

        LiveResolver liveResolver = new LiveResolver();
        String docroot = new File(APP1_BASE_DIR, "target" + File.separator + "app1").toString();

        URL liveDocroot = liveResolver.resolveLiveDocroot(docroot, File.separator + "path" + File.separator + "to" + File.separator + "file.xml");
        assertNotNull(liveDocroot);       
        assertEquals("/tmp/app1/src/main/webapp", liveDocroot.getFile());

        // non-existing docroot
        liveDocroot = liveResolver.resolveLiveDocroot(File.separator + "tmp", File.separator + "path" + File.separator + "to" + File.separator + "file.xml");
        assertNull(liveDocroot);
    }

    /**
     * Assert no live docroot for the editor. The editor has its own pom.xml below target/editor/META-INF, no live
     * resources are specified for those coordinates.
     * @throws Exception
     */
    @Test
    public void testResolveLiveDocrootOfEditorByLiveXml() throws Exception {
        createWorkspaceLive();
        createProjectLayout();

        LiveResolver liveResolver = new LiveResolver();
        String docroot = new File(APP1_BASE_DIR, "target" + File.separator + "editor").toString();

        // Make the test run under Windows
        File[] roots = File.listRoots();
        for (File root : roots) {
            URL liveDocroot = liveResolver.resolveLiveDocroot(docroot, root.toString() + "path" + File.separator + "to" + File.separator + "file.xml");
            assertNull(liveDocroot);
        }

    }

    /**
     * If no live.xml exists or it doesn't contain an &lt;war&gt; element and "target" is the docroot then
     * src/main/webapp is used as default live docroot. This is to support tomcat:run-war.
     * @throws Exception
     */
    @Test
    public void testResolveDefaultLiveDocroot() throws Exception {
        createProjectLayout();

        LiveResolver liveResolver = new LiveResolver();
        String docroot = new File(APP1_BASE_DIR, "target" + File.separator + "app1").toString();

        URL liveDocroot = liveResolver.resolveLiveDocroot(docroot, File.separator + "file.xml");
        assertNotNull(liveDocroot);

        assertEquals(new File(APP1_BASE_DIR, "src" + File.separator + "main" + File.separator + "webapp").toString() + File.separator, liveDocroot.getFile());

        // the default mechanism doesn't check if file exists in src/main/webapp
        liveDocroot = liveResolver.resolveLiveDocroot(docroot, File.separator + "no-such-file.xml");
        assertNotNull(liveDocroot);

        // the default mechanism uses default exclusions
        liveDocroot = liveResolver.resolveLiveDocroot(docroot, File.separator + "core" + File.separator + "file.xml");
        assertNull(liveDocroot);
    }

    /**
     * If no live.xml exists or it doesn't contain an &lt;war&gt; element and "src/main/webapp" is the docroot then
     * target is used as fallback docroot. This is to support tomcat:run.
     * @throws Exception
     */
    @Test
    public void testResolveFallbackDocroot() throws Exception {
        createProjectLayout();

        LiveResolver liveResolver = new LiveResolver();
        String docroot = new File(APP1_BASE_DIR, "src" + File.separator + "main" + File.separator + "webapp").toString();

        // the fallback mechanism uses default inclusions, "/core/" is included
        URL liveDocroot = liveResolver.resolveLiveDocroot(docroot, File.separator + "core" + File.separator + "file.xml");
        assertNotNull(liveDocroot);
        String pathToCompare = APP1_BASE_DIR.toURI().toURL().toString() + "target/app1/";
        assertEquals(pathToCompare, liveDocroot.toString());

        // the fallback mechanism doesn't check if file exists
        liveDocroot = liveResolver.resolveLiveDocroot(docroot, File.separator + "core" + File.separator + "no-such-file.xml");
        assertNotNull(liveDocroot);
        assertEquals(pathToCompare, liveDocroot.toString());

        // the fallback mechanism uses default inclusions, "/" is not included
        liveDocroot = liveResolver.resolveLiveDocroot(docroot, File.separator + "file.xml");
        assertNull(liveDocroot);
    }

    /**
     * The current implementation expects a format ".../groupId+artifactId+version.jar!..." to resolve the live module
     * root by jar name. Don't know why...
     * @throws Exception
     */
    @Test
    public void testResolveLiveModuleRootByJarName() throws Exception {
        createWorkspaceLive();

        LiveResolver liveResolver = new LiveResolver();
        URL liveModuleRoot = liveResolver.resolveLiveModuleRoot(new URL("jar:file:/tmp/a.b.c+mod1+0.1.jar!/"),
                "/file.xml");
        assertNotNull(liveModuleRoot);
        
        // Make the test run under Windows
        File[] roots = File.listRoots();
        boolean rootEqualsliveModuleRoot = false;
        for (File root : roots) {
        	String urlPathToFile = root.toURI().toURL() + "tmp/mod1/src/main/resources";
        	if (urlPathToFile.equals(liveModuleRoot.toString())) {
        		rootEqualsliveModuleRoot = true;
        	}
        }
        assertTrue(rootEqualsliveModuleRoot);
    }

    /**
     * GroupId and version are extracted from META-INF/MANIFEST.MF and artifactId is extracted from jar file name.
     * @throws Exception
     */
    @Test
    public void testResolveLiveModuleRootByManifest() throws Exception {
        createWorkspaceLive();

        LiveResolver liveResolver = new LiveResolver();
        String path = new File(WORKSPACE_BASE_DIR, "sample-module-A-0.14.4-SNAPSHOT.jar").toURI().toURL().toString();
        URL jarUrl = new URL("jar:" + path + "!/");
        URL liveModuleRoot = liveResolver.resolveLiveModuleRoot(jarUrl, "/file.xml");
        assertNotNull(liveModuleRoot);

        // Make the test run under Windows
        File[] roots = File.listRoots();
        boolean rootEqualsliveModuleRoot = false;
        for (File root : roots) {
        	String urlPathToFile = root.toURI().toURL() + "tmp/sample/src/main/resources";
        	if (urlPathToFile.equals(liveModuleRoot.toString())) {
        		rootEqualsliveModuleRoot = true;
        	}
        }
        assertTrue(rootEqualsliveModuleRoot);
    }

}
