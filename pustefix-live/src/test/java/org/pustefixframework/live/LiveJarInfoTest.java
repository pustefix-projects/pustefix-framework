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
        entry.groupId = "ggg";
        entry.artifactId = "aaa";
        entry.version = "vvv";
        entry.directories.add(new File(dir, "jar1"));
        entry.directories.add(new File(dir, "jar2"));
        liveJarInfo.getJarEntries().put(entry.getId(), entry);
        Entry warEntry = new Entry();
        warEntry.groupId = "gggggg";
        warEntry.artifactId = "aaaaaa";
        warEntry.version = "vvvvvv";
        warEntry.directories.add(new File(dir, "war"));
        liveJarInfo.getWarEntries().put(warEntry.getId(), warEntry);
        liveJarInfo.write();
        
        // load the live.xml again, assert it has the entry
        LiveJarInfo loadedLiveJarInfo = new LiveJarInfo(file);
        assertNotNull(loadedLiveJarInfo.getJarEntries());
        assertEquals(1, loadedLiveJarInfo.getJarEntries().size());
        Entry loadedEntry = loadedLiveJarInfo.getJarEntries().values().iterator().next();
        assertEquals("ggg", loadedEntry.groupId);
        assertEquals("aaa", loadedEntry.artifactId);
        assertEquals("vvv", loadedEntry.version);
        assertNotNull(loadedEntry.directories);
        assertEquals(2, loadedEntry.directories.size());
        assertTrue(loadedEntry.directories.contains(new File(dir, "jar1")));
        assertTrue(loadedEntry.directories.contains(new File(dir, "jar2")));

        assertNotNull(loadedLiveJarInfo.getWarEntries());
        assertEquals(1, loadedLiveJarInfo.getWarEntries().size());
        Entry loadedWarEntry = loadedLiveJarInfo.getWarEntries().values().iterator().next();
        assertEquals("gggggg", loadedWarEntry.groupId);
        assertEquals("aaaaaa", loadedWarEntry.artifactId);
        assertEquals("vvvvvv", loadedWarEntry.version);
        assertNotNull(loadedWarEntry);
        assertNotNull(loadedWarEntry.directories);
        assertEquals(1, loadedWarEntry.directories.size());
        assertTrue(loadedWarEntry.directories.contains(new File(dir, "war")));
    }
    
}
