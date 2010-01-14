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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.pustefixframework.live.Helper.FALLBACK_LIFE_XML;
import static org.pustefixframework.live.Helper.FALLBACK_LIVE_XML;
import static org.pustefixframework.live.Helper.WORKSPACE_BASE_DIR;
import static org.pustefixframework.live.Helper.WORKSPACE_LIVE_XML;
import static org.pustefixframework.live.Helper.createFallBackLife;
import static org.pustefixframework.live.Helper.createFallBackLive;
import static org.pustefixframework.live.Helper.createWorkspaceLive;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.pustefixframework.live.LiveJarInfo.Entry;

public class LiveJarInfoTest {

    @Before
    public void setUp() throws Exception {
        Helper.cleanUp();
    }

    @Test
    public void testInitNoLiveNoFallback() throws Exception {
        // fallback is life.xml, but does not exist
        LiveJarInfo liveJarInfo = new LiveJarInfo();
        assertNotNull(liveJarInfo.getLiveFile());
        assertEquals(FALLBACK_LIFE_XML, liveJarInfo.getLiveFile());
        assertFalse(liveJarInfo.getLiveFile().exists());

        // as file does not exist there are no jar an war entries
        assertFalse(liveJarInfo.hasJarEntries());
        assertFalse(liveJarInfo.hasWarEntries());
    }

    @Test
    public void testInitNoLiveButFallbackLife() throws Exception {
        createFallBackLife();

        // fallback is life.xml, but does not exist
        LiveJarInfo liveJarInfo = new LiveJarInfo();
        assertNotNull(liveJarInfo.getLiveFile());
        assertEquals(FALLBACK_LIFE_XML, liveJarInfo.getLiveFile());
        assertTrue(liveJarInfo.getLiveFile().exists());

        // check jar entry was read
        assertTrue(liveJarInfo.hasJarEntries());
        assertEquals(1, liveJarInfo.getJarEntries().size());
        assertFalse(liveJarInfo.hasWarEntries());
    }

    @Test
    public void testInitNoLiveButFallbackLive() throws Exception {
        createFallBackLive();
        createFallBackLife();

        // fallback is life.xml, but does not exist
        LiveJarInfo liveJarInfo = new LiveJarInfo();
        assertNotNull(liveJarInfo.getLiveFile());
        assertEquals(FALLBACK_LIVE_XML, liveJarInfo.getLiveFile());
        assertTrue(liveJarInfo.getLiveFile().exists());

        // check jar entry was read
        assertFalse(liveJarInfo.hasJarEntries());
        assertTrue(liveJarInfo.hasWarEntries());
        assertEquals(1, liveJarInfo.getWarEntries().size());
    }

    @Test
    public void testInitLive() throws Exception {
        createWorkspaceLive();
        createFallBackLive();
        createFallBackLife();

        LiveJarInfo liveJarInfo = new LiveJarInfo();
        assertNotNull(liveJarInfo.getLiveFile());
        assertEquals(WORKSPACE_LIVE_XML, liveJarInfo.getLiveFile());
        assertTrue(liveJarInfo.getLiveFile().exists());

        // check war and jar entries were read
        assertTrue(liveJarInfo.hasJarEntries());
        assertEquals(2, liveJarInfo.getJarEntries().size());
        assertTrue(liveJarInfo.hasWarEntries());
        assertEquals(1, liveJarInfo.getWarEntries().size());
    }

    @Test
    public void testWrite() throws Exception {
        File dir = WORKSPACE_BASE_DIR;
        File file = WORKSPACE_LIVE_XML;

        // assert the live.xml is empty
        LiveJarInfo liveJarInfo = new LiveJarInfo(file);
        assertNotNull(liveJarInfo.getJarEntries());
        assertEquals(0, liveJarInfo.getJarEntries().size());

        // add an jar an war entry and write to file
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

        // load live.xml again, assert jar and war entries were read
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
