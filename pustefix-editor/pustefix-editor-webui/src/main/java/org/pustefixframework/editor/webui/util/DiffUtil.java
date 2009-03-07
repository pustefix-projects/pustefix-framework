/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.pustefixframework.editor.webui.util;

import java.util.ArrayList;
import java.util.List;

import org.pustefixframework.editor.webui.util.DiffUtil.ComparedLine.CompareStatus;


public class DiffUtil {
    public static class ComparedLine {
        private String version1 = null;
        private String version2 = null;
        private CompareStatus status = null;
        
        public ComparedLine(String v1, String v2, CompareStatus status) {
            this.version1 = v1;
            this.version2 = v2;
            this.status = status;
        }
        
        public String getVersion1() {
            return version1;
        }
        
        public String getVersion2() {
            return version2;
        }
        
        public CompareStatus getStatus() {
            return status;
        }
        
        public static enum CompareStatus {
            NOCHANGE, DELETED, INSERTED, CONFLICT
        }
    }
    /**
     * This method compares two documents line by line. If a line is only present
     * in one version, the result for the other version is <code>null</code>.
     * 
     * @param text1
     *            The first version of the text
     * @param text2
     *            The second version of the text
     * @return An array of compared lines
     */
    public static ComparedLine[] compare(String text1, String text2) {
        String[] arrLines0 = text1.split("(\r\n)|(\n)|(\r)");
        String[] arrLines1 = text2.split("(\r\n)|(\n)|(\r)");
        
        ArrayList<String> lines0 = new ArrayList<String>();
        ArrayList<String> lines1 = new ArrayList<String>();
        for (int i = 0; i < arrLines0.length; i++) {
            lines0.add(arrLines0[i]);
        }
        for (int i = 0; i < arrLines1.length; i++) {
            lines1.add(arrLines1[i]);
        }
        
        List<Diff.Block> blocks = Diff.diff(lines0, lines1);
        
        ArrayList<ComparedLine> out = new ArrayList<ComparedLine>();
        
        for (Diff.Block block: blocks) {
            if (block.status == Diff.BlockStatus.COMMON) {
                for (int i = block.startA; i < (block.startA + block.lengthA); i++) {
                    out.add(new ComparedLine(lines0.get(i), lines0.get(i), CompareStatus.NOCHANGE));
                }
            } else if (block.status == Diff.BlockStatus.ONLY_LEFT) {
                for (int i = block.startA; i < (block.startA + block.lengthA); i++) {
                    out.add(new ComparedLine(lines0.get(i), null, CompareStatus.DELETED));
                }
            } else if (block.status == Diff.BlockStatus.ONLY_RIGHT) {
                for (int i = block.startB; i < (block.startB + block.lengthB); i++) {
                    out.add(new ComparedLine(null, lines1.get(i), CompareStatus.INSERTED));
                }
            } else if (block.status == Diff.BlockStatus.CONFLICT) {
                // Conflict
                int i = 0, j = 0;
                for (i = block.startA, j = block.startB; i < (block.startA + block.lengthA) && j < (block.startB + block.lengthB); i++, j++) {
                    out.add(new ComparedLine(lines0.get(i), lines1.get(j), CompareStatus.CONFLICT));
                }
                // Add lines if left version is longer
                for (; i < (block.startA + block.lengthA); i++) {
                    out.add(new ComparedLine(lines0.get(i), null, CompareStatus.CONFLICT));
                }
                // Add lines if right version is longer
                for (; j < (block.startB + block.lengthB); j++) {
                    out.add(new ComparedLine(null, lines1.get(j), CompareStatus.CONFLICT));
                }
            }
        }
        
        return out.toArray(new ComparedLine[out.size()]);
    }
    
    /**
     * This method merges to documents line by line. For each difference found,
     * a hint is included in the output, thus allowing the user to decide which
     * changes to keep.
     * 
     * @param text1
     *            The first version of the text
     * @param text2
     *            The second version of the text
     * @return Merged text containing both versions and hints
     */
    public static String merge(String text1, String text2) {
        String[] arrLines0 = text1.split("(\r\n)|(\n)|(\r)");
        String[] arrLines1 = text2.split("(\r\n)|(\n)|(\r)");
        
        ArrayList<String> lines0 = new ArrayList<String>();
        ArrayList<String> lines1 = new ArrayList<String>();
        for (int i = 0; i < arrLines0.length; i++) {
            lines0.add(arrLines0[i]);
        }
        for (int i = 0; i < arrLines1.length; i++) {
            lines1.add(arrLines1[i]);
        }
        
        List<Diff.Block> blocks = Diff.diff(lines0, lines1);
        StringBuffer output = new StringBuffer();
        
        for (Diff.Block block: blocks) {
            if (block.status == Diff.BlockStatus.COMMON) {
                for (int i = block.startA; i < (block.startA + block.lengthA); i++) {
                    output.append(lines0.get(i));
                    output.append('\n');
                }
            } else if (block.status == Diff.BlockStatus.ONLY_LEFT) {
                output.append("<<<<<< Only in your version:\n");
                for (int i = block.startA; i < (block.startA + block.lengthA); i++) {
                    output.append(lines0.get(i));
                    output.append('\n');
                }
                output.append(">>>>>>\n");
            } else if (block.status == Diff.BlockStatus.ONLY_RIGHT) {
                output.append("<<<<<< Only in other version:\n");
                for (int i = block.startB; i < (block.startB + block.lengthB); i++) {
                    output.append(lines1.get(i));
                    output.append('\n');
                }
                output.append(">>>>>>\n");
            } else if (block.status == Diff.BlockStatus.CONFLICT) {
                output.append("<<<<<< Only in your version:\n");
                for (int i = block.startA; i < (block.startA + block.lengthA); i++) {
                    output.append(lines0.get(i));
                    output.append('\n');
                }
                output.append("====== Only in other version:\n");
                for (int i = block.startB; i < (block.startB + block.lengthB); i++) {
                    output.append(lines1.get(i));
                    output.append('\n');
                }
                output.append(">>>>>>\n");
            }
        }
        
        return output.toString();
    }
}
