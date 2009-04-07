/*
 * Created on May 24, 2004
 *
 */
package de.schlund.pfixxml.targets;

import java.io.File;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import de.schlund.pfixxml.XMLException;
import de.schlund.pfixxml.config.GlobalConfig;
import de.schlund.pfixxml.config.GlobalConfigurator;
import de.schlund.pfixxml.resources.FileResource;
import de.schlund.pfixxml.resources.ResourceUtil;
import de.schlund.pfixxml.util.Xml;

public class TargetGeneratorTest extends TestCase {

    private static final File DOCROOT = GlobalConfig.guessDocroot().getAbsoluteFile();

    static {
        if(GlobalConfig.getDocroot()==null) GlobalConfigurator.setDocroot(DOCROOT.getAbsolutePath());
    }

    @Override
    protected void tearDown() throws Exception {
        TargetGeneratorFactory.getInstance().reset();
    }
    
    public void testEmpty() throws Exception {

        TargetGenerator gen;

        gen = create("<make project='foo' lang='bar'/>");
        assertEquals(0, gen.getAllTargets().size());
        assertEquals("foo", gen.getName());
        assertEquals("bar", gen.getLanguage());
        assertNotNull(gen.getDisccachedir().toURI().getPath());
    }

    public void testTarget() throws Exception {
        TargetGenerator gen;

        gen = create("<make project='foo' lang='bar'>" + "  <navigation/> " + "  <target name='master.xsl' type='xsl'>" + "    <depxml name='core/xsl/master.xsl'/>"
                + "    <depxsl name='core/xsl/customizemaster.xsl'/>" + "  </target>" + "</make>");
        assertEquals(3, gen.getAllTargets().size());
        // TODO: more tests
    }

    public void testXmlMissing() throws Exception {
        createInvalid("<make project='foo' lang='bar'>" + "  <target name='master.xsl' type='xsl'>" + "    <depxsl name='core/xsl/customizemaster.xsl'/>" + "  </target>"
                + "</make>", "without [depxml]");
    }

    public void testXslMissing() throws Exception {
        createInvalid("<make project='foo' lang='bar'>" + "  <target name='master.xsl' type='xsl'>" + "    <depxml name='core/xsl/master.xsl'/>" + "  </target>" + "</make>",
                "without [depxsl]");
    }

    public void testDocrootNoLongerAllowed() throws Exception {
        createInvalid("<make project='foo' lang='bar'>" + "  <target name='master.xsl' type='xsl'>" + "    <depxml name='core/xsl/master.xsl'/>"
                + "    <depxsl name='core/xsl/customizemaster.xsl'/>" + "    <param name='docroot' value='foo'/>" + "  </target>" + "</make>",
                "The docroot parameter is no longer allowed");
    }

    public void createInvalid(String str, String msg) throws Exception {
        String full;

        try {
            create(str);
            fail("'" + str + "' does not raise exception with '" + msg + "'");
        } catch (XMLException e) {
            full = e.getMessage();
            if (full.indexOf(msg) != -1) {
                // ok
            } else {
                fail("exception message '" + full + "' does not contain '" + msg + "'");
            }
        }
    }

    private TargetGenerator create(String str) throws Exception {
        TargetGenerator gen;
        File file;

        Document doc = Xml.parseStringMutable(str);
        file = File.createTempFile("depend", ".xml", new File("target"));
        file.deleteOnExit();
        Xml.serialize(doc, file, true, true);
        gen = new TargetGenerator(ResourceUtil.getFileResource(file.toURI()));
        return gen;
    }

    public void testConcurrency() throws Exception {
        File cacheDir = new File("target/.cache");
        File tmpCacheDir = new File("target/.cache_tmp");
        if (cacheDir.exists()) cacheDir.renameTo(tmpCacheDir);
        try {
            FileResource res = ResourceUtil.getFileResource(new File(GlobalConfig.guessDocroot(), "WEB-INF/depend.xml").toURI());
            TargetGenerator generator = new TargetGenerator(res);
            generator.setIsGetModTimeMaybeUpdateSkipped(true);
            TreeSet<Target> topTargets = generator.getPageTargetTree().getToplevelTargets();
            final Target[] targets = new Target[topTargets.size()];
            Iterator<Target> it = topTargets.iterator();
            for (int i = 0; i < targets.length; i++)
                targets[i] = (Target) it.next();
            int threadNo = 50;
            final int requestNo = 10;
            Thread[] threads = new Thread[threadNo];
            for (int i = 0; i < threadNo; i++) {
                final long seed = i;
                final int max = targets.length;
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        Random random = new Random(System.currentTimeMillis() / (seed + 1));
                        for (int j = 0; j < requestNo; j++) {
                            int ind = random.nextInt(max);
                            try {
                                Object obj = targets[ind].getValue();
                                assertNotNull(obj);
                            } catch (TargetGenerationException x) {
                                throw new RuntimeException("Error", x);
                            }
                        }
                    }
                };
                threads[i] = thread;
                thread.start();
            }
            for (int i = 0; i < threadNo; i++)
                threads[i].join();
        } finally {
            if (cacheDir.exists()) delete(cacheDir);
            if (tmpCacheDir.exists()) tmpCacheDir.renameTo(cacheDir);
        }
    }

    private static boolean delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                delete(files[i]);
            }
        }
        return file.delete();
    }

    public static void main(String[] args) throws Exception {
        new TargetGeneratorTest().testConcurrency();
    }

}
