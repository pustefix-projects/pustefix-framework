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

package org.pustefixframework.editor.webui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class Diff {
    private static class Match implements Comparable<Match> {
        private int posA;
        private int posB;
        
        public Match(int a, int b) {
            posA = a;
            posB = b;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof Match) {
                Match m = (Match) o;
                return (m.posA == posA && m.posB == posB);
            }
            return false;
        }

        @Override
        public int hashCode() {
            assert false : "hashCode not supported";
            return 0;
        }
        
        public int compareTo(Match m) {
            if (posA < m.posA) {
                return -1;
            } else if (posA > m.posA) {
                return 1;
            } else {
                if (posB < m.posB) {
                    return -1;
                } else if (posB > m.posB) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
    
    private static class MatchSequence {
        private List<Match> matches = new ArrayList<Match>();
        
        public MatchSequence(Match m) {
            this.matches.add(m);
        }
        
        public MatchSequence(MatchSequence ms, Match m) {
            this.matches.addAll(ms.matches);
            this.matches.add(m);
        }
        
        public boolean canAppend(Match m) {
            Match last = matches.get(matches.size() - 1);
            return (last.posA < m.posA && last.posB < m.posB);
        }
        
        public int getLength() {
            return this.matches.size();
        }
        
        public int getLastPosA() {
            return this.matches.get(this.matches.size() - 1).posA;
        }
        
        public int getLastPosB() {
            return this.matches.get(this.matches.size() - 1).posB;
        }
    }
    
    private static class MatchSequenceSizeComparator implements Comparator<MatchSequence> {

        public int compare(MatchSequence o1, MatchSequence o2) {
            if (o1.matches.size() > o2.matches.size()) {
                return -1;
            } else if (o1.matches.size() < o2.matches.size()) {
                return 1;
            } else {
                return 0;
            }
        }
        
    }
    
    public static class Block {
        public int startA;
        public int lengthA;
        public int startB;
        public int lengthB;
        public BlockStatus status; 
    }
    
    public enum BlockStatus {
        COMMON, ONLY_LEFT, ONLY_RIGHT, CONFLICT
    }
    
    private static MatchSequence canDispense(MatchSequence ms1, MatchSequence ms2) {
        if (ms1.getLength() <= ms2.getLength() && ms1.getLastPosA() >= ms2.getLastPosA() && ms1.getLastPosB() >= ms2.getLastPosB()) {
            return ms1;
        } else if (ms1.getLength() >= ms2.getLength() && ms1.getLastPosA() <= ms2.getLastPosA() && ms1.getLastPosB() <= ms2.getLastPosB()) {
            return ms2;
        } else {
            return null;
        }
    }
    
    public static List<Block> diff(List<?> a, List<?> b) {
        // Create hashmap for faster comparison
        HashMap<Object, Collection<Integer>> hashmap = new HashMap<Object, Collection<Integer>>();
        for (int i = 0; i < b.size(); i++) {
            Object o = b.get(i);
            if (hashmap.containsKey(o)) {
                hashmap.get(o).add(i);
            } else {
                HashSet<Integer> set = new HashSet<Integer>();
                set.add(i);
                hashmap.put(o, set);
            }
        }
        
        // Create a list of all matches
        TreeSet<Match> matches = new TreeSet<Match>();
        for (int i = 0; i < a.size(); i++) {
            Collection<Integer> targets = hashmap.get(a.get(i));
            if (targets != null) {
                for (int t: targets) {
                    matches.add(new Match(i, t));
                }
            }
        }
        
        // Build up all possible (and useful) sequences
        // The sequences are ordered by size, thus finally
        // the first sequence will be the longest one.
        TreeSet<MatchSequence> matchSeqs = new TreeSet<MatchSequence>(new MatchSequenceSizeComparator());
        for (Match m: matches) {
            boolean appended = false;
            for (MatchSequence ms: matchSeqs) {
                // Look for the first (longest possible)
                // sequence the match can be appended
                if (ms.canAppend(m)) {
                    appended = true;
                    MatchSequence msNew = new MatchSequence(ms, m);
                    // Check whether there is already a "better"
                    // sequence. While doing this, remove "worse"
                    // sequences.
                    for (Iterator<MatchSequence> i = matchSeqs.iterator(); i.hasNext();) {
                        MatchSequence msk = i.next();
                        MatchSequence dispensable = canDispense(msNew, msk);
                        if (dispensable == msk) {
                            i.remove();
                        } else if (dispensable == msNew) {
                            msNew = null;
                            break;
                        }
                    }
                    if (msNew != null) {
                        matchSeqs.add(msNew);
                    }
                    break;
                }
            }
            // If the match has not been appended to any sequence, create
            // a new sequence containing only the match
            if (!appended) {
                matchSeqs.add(new MatchSequence(m));
            }
        }
        
        // If there is not a single match, both versions
        // are completely different
        if (matchSeqs.size() < 1) {
            ArrayList<Block> blocks = new ArrayList<Block>();
            Block block = new Block();
            block.startA = 0;
            block.startB = 0;
            block.lengthA = a.size();
            block.lengthB = b.size();
            block.status = BlockStatus.CONFLICT;
            blocks.add(block);
            return blocks;
        }

        // The longest common sub-sequence (or at least one of them
        // if there are several) is the first one in the sorted set.
        MatchSequence lcs = matchSeqs.first();
        int startA = 0;
        int startB = 0;
        int nextA = 0;
        int nextB = 0;
        ArrayList<Block> blocks = new ArrayList<Block>();
        for (Match m: lcs.matches) {
            if (m.posA != nextA || m.posB != nextB) {
                // Save the last matching block
                Block x = new Block();
                x.startA = startA;
                x.startB = startB;
                x.lengthA = nextA - startA;
                x.lengthB = nextB - startB;
                x.status = BlockStatus.COMMON;
                if (x.lengthA > 0 && x.lengthB > 0) {
                    blocks.add(x);
                }
            }
            
            if (m.posA > nextA && m.posB == nextB) {
                // There is at least one line, that is only present in
                // A version.
                Block x = new Block();
                x.startA = nextA;
                x.lengthA = m.posA - nextA;
                x.startB = -1;
                x.lengthB = 0;
                x.status = BlockStatus.ONLY_LEFT;
                blocks.add(x);
                startA = m.posA;
                startB = m.posB;
                nextA = m.posA + 1;
                nextB++;
            } else if (m.posA == nextA && m.posB > nextB) {
                // There is at least one line, that is only present in
                // B version.
                Block x = new Block();
                x.startA = -1;
                x.lengthA = 0;
                x.startB = nextB;
                x.lengthB = m.posB - nextB;
                x.status = BlockStatus.ONLY_RIGHT;
                blocks.add(x);
                startA = m.posA;
                startB = m.posB;
                nextA++;
                nextB = m.posB + 1;
            } else if (m.posA > nextA && m.posB > nextB) {
                // There are lines in both versions which are not matching
                Block x = new Block();
                x.startA = nextA;
                x.lengthA = m.posA - nextA;
                x.startB = nextB;
                x.lengthB = m.posB - nextB;
                x.status = BlockStatus.CONFLICT;
                blocks.add(x);
                startA = m.posA;
                startB = m.posB;
                nextA = m.posA + 1;
                nextB = m.posB + 1;
            } else {
                // Matching lines
                nextA++;
                nextB++;
            }
        }
        
        // Finally save matching block that has not yet been saved
        Block x = new Block();
        x.startA = startA;
        x.startB = startB;
        x.lengthA = nextA - startA;
        x.lengthB = nextB - startB;
        x.status = BlockStatus.COMMON;
        if (x.lengthA > 0 && x.lengthB > 0) {
            blocks.add(x);
        }
        
        // There might still be lines that are only present in only the left
        // or the right version - check for them
        if (nextA < a.size() && nextB < b.size()) {
            // Conflict
            x = new Block();
            x.startA = nextA;
            x.startB = nextB;
            x.lengthA = a.size() - nextA;
            x.lengthB = b.size() - nextB;
            x.status = BlockStatus.CONFLICT;
            blocks.add(x);
        } else if (nextA < a.size()) {
            // Only in left version
            x = new Block();
            x.startA = nextA;
            x.startB = -1;
            x.lengthA = a.size() - nextA;
            x.lengthB = 0;
            x.status = BlockStatus.ONLY_LEFT;
            blocks.add(x);
        } else if (nextB < b.size()) {
            // Only in right version
            x = new Block();
            x.startA = -1;
            x.startB = nextB;
            x.lengthA = 0;
            x.lengthB = b.size() - nextB;
            x.status = BlockStatus.ONLY_RIGHT;
            blocks.add(x);
        }
        
        return blocks;
    }
}
