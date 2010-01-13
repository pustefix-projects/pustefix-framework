package org.pustefixframework.live;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.pustefixframework.live.LiveJarInfo.Entry;


public class LiveJarInfoTest {

    @Test
    public void testWrite() throws Exception {
        
        String dir = getClass().getResource("/").getFile();
        File file = new File(dir, "live.xml");
        file.delete();
        
        // assert the live.xml is empty 
        LiveJarInfo liveJarInfo = new LiveJarInfo(file);
        assertNotNull(liveJarInfo.getJarEntries());
        assertEquals(0, liveJarInfo.getJarEntries().size());
        
        // add an entry and write it
        Entry entry = new Entry();
        entry.setGroupId("ggg");
        entry.setArtifactId("aaa");
        entry.setVersion("vvv");
        entry.getDirectories().add(new File(dir, "jar1"));
        entry.getDirectories().add(new File(dir, "jar2"));
        liveJarInfo.getJarEntries().put(entry.getId(), entry);
        Entry warEntry = new Entry();
        warEntry.setGroupId("gggggg");
        warEntry.setArtifactId("aaaaaa");
        warEntry.setVersion("vvvvvv");
        warEntry.getDirectories().add(new File(dir, "war"));
        liveJarInfo.getWarEntries().put(warEntry.getId(), warEntry);
        liveJarInfo.write();
        
        // load the live.xml again, assert it has the entry
        LiveJarInfo loadedLiveJarInfo = new LiveJarInfo(file);
        assertNotNull(loadedLiveJarInfo.getJarEntries());
        assertEquals(1, loadedLiveJarInfo.getJarEntries().size());
        Entry loadedEntry = loadedLiveJarInfo.getJarEntries().values().iterator().next();
        assertEquals("ggg", loadedEntry.getGroupId());
        assertEquals("aaa", loadedEntry.getArtifactId());
        assertEquals("vvv", loadedEntry.getVersion());
        assertNotNull(loadedEntry.getDirectories());
        assertEquals(2, loadedEntry.getDirectories().size());
        assertTrue(loadedEntry.getDirectories().contains(new File(dir, "jar1")));
        assertTrue(loadedEntry.getDirectories().contains(new File(dir, "jar2")));

        assertNotNull(loadedLiveJarInfo.getWarEntries());
        assertEquals(1, loadedLiveJarInfo.getWarEntries().size());
        Entry loadedWarEntry = loadedLiveJarInfo.getWarEntries().values().iterator().next();
        assertEquals("gggggg", loadedWarEntry.getGroupId());
        assertEquals("aaaaaa", loadedWarEntry.getArtifactId());
        assertEquals("vvvvvv", loadedWarEntry.getVersion());
        assertNotNull(loadedWarEntry);
        assertNotNull(loadedWarEntry.getDirectories());
        assertEquals(1, loadedWarEntry.getDirectories().size());
        assertTrue(loadedWarEntry.getDirectories().contains(new File(dir, "war")));
    }
    
}
