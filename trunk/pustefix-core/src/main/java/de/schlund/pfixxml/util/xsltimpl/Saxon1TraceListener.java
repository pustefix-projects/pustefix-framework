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

package de.schlund.pfixxml.util.xsltimpl;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.icl.saxon.Context;
import com.icl.saxon.Mode;
import com.icl.saxon.NodeHandler;
import com.icl.saxon.om.Navigator;
import com.icl.saxon.om.NodeInfo;
import com.icl.saxon.trace.TraceListener;

/**
 * @author mleidig@schlund.de
 */
public class Saxon1TraceListener implements TraceListener {

   private final static Logger LOG=Logger.getLogger(Saxon1TraceListener.class);
    
   public enum Format {VERBOSE,COMPRESSED};
   
   private final static String FORMATNAME="SaxonTraceDump";
   private final static String FORMATVERSION="1";
   
   private Format format=Format.VERBOSE;
   private Writer writer;
   private Map<String,Integer> namePool=new HashMap<String,Integer>();
   private String currentIndent="";
   private String indent=" ";
   
   public Saxon1TraceListener(Format format, Writer writer) {
      this.format=format;
      this.writer=writer;
   }
   
   public void enter(NodeInfo node, Context context) {
      //Format: callback|systemid|line|path|mode
       try {
          if (node.getNodeType()==NodeInfo.ELEMENT) {
             if(format==Format.VERBOSE) {
                writer.write(currentIndent);
                writer.write("(");
                writer.write(getShortSystemId(node)+"|");
                writer.write(node.getLineNumber()+"|");
                writer.write(Navigator.getPath(node)+"|");
                String mode=getShortMode(context);
                writer.write((mode==null?"":mode)+"|\n");     
                currentIndent+=indent;
             } else {
                writer.write("(");
                writer.write(getShortSystemId(node)+"|");
                writer.write(node.getLineNumber()+"|");
                writer.write(getShortPath(Navigator.getPath(node))+"|");
                String mode=getShortMode(context);
                writer.write((mode==null?"":mode)+"|\n");     
             }
          }
       } catch(IOException x) {
           LOG.error("Can't write trace.",x);
       }
   }
   
   public void leave(NodeInfo node, Context context) {
      //Format: callback
       try {
          if (node.getNodeType()==NodeInfo.ELEMENT) {
             if(format==Format.VERBOSE) {
                currentIndent=currentIndent.substring(0,currentIndent.length()-indent.length());
                writer.write(currentIndent);
                writer.write(")\n");
             } else {
                writer.write(")\n");
             }
          }
       } catch(IOException x) {
           LOG.error("Can't write trace.",x);
       }
   }
   
   public void enterSource(NodeHandler handler, Context context) {
      //Format: callback|systemid|path|line|mode
       try {
          NodeInfo node = context.getContextNodeInfo();
          if(format==Format.VERBOSE) {
             writer.write(currentIndent);
             writer.write("[");
             writer.write(getShortSystemId(node)+"|");
             writer.write(node.getLineNumber()+"|");
             writer.write(Navigator.getPath(node)+"|");
             String mode=getShortMode(context);
             writer.write((mode==null?"":mode)+"|\n");
             currentIndent+=indent;
          } else {
             writer.write("[");
             writer.write(getShortSystemId(node)+"|");
             writer.write(node.getLineNumber()+"|");
             writer.write(getShortPath(Navigator.getPath(node))+"|");
             String mode=getShortMode(context);
             writer.write((mode==null?"":mode)+"|\n");
          }
       } catch(IOException x) {
           LOG.error("Can't write trace.",x);
       }
   }
   
   public void leaveSource(NodeHandler handler, Context context) {
      //Format: callback
       try {
          if(format==Format.VERBOSE) {
             currentIndent=currentIndent.substring(0,currentIndent.length()-indent.length());
             writer.write(currentIndent);
             writer.write("]\n");
          } else {
             writer.write("]\n");
          }
       } catch(IOException x) {
           LOG.error("Can't write trace.",x);
       }
   }
   
   public void open() {
       try {
          writer.write(FORMATNAME+FORMATVERSION+"\n");
          writer.write(format.toString()+"\n");
       } catch(IOException x) {
           LOG.error("Can't write trace.",x);
       }
   }
   
   public void close() {
      if(format==Format.COMPRESSED) serializePool();
      try {
         writer.close();
      } catch(IOException x) {
         LOG.error("Can't write trace.",x);
     }
   }
   
   public void toplevel(NodeInfo arg0) {
   }
   
   private String getShortMode(Context context) {
      String modeName="";
      Mode mode = context.getMode();
      if (mode!=null && mode.getNameCode()!=-1) {
         modeName=context.getController().getNamePool().getDisplayName(mode.getNameCode());
         if(format==Format.COMPRESSED) {
            Integer value=namePool.get(modeName);
            if(value==null) {
               value=namePool.size();
               namePool.put(modeName, value);
            }
            modeName=value.toString();
         }
      }
      return modeName;
   }
   
   private String getShortPath(String path) {
      StringBuilder sb=new StringBuilder();
      String[] components=path.split("/");
      for(String component:components) {
         if(component.length()>0) {
            int ind=component.indexOf('[');
            String name=component.substring(0,ind);
            String pred=component.substring(ind);
            Integer value=namePool.get(name);
            if(value==null) {
               value=namePool.size();
               namePool.put(name, value);
            }
            sb.append("/"+value.toString()+pred);
         }
      }
      return sb.toString();
   }
   
   private String getShortSystemId(NodeInfo node) {
      String systemId=node.getSystemId();
      if(format==Format.VERBOSE) {
         int ind=systemId.lastIndexOf('/');
         if(ind>0) systemId=systemId.substring(ind+1);
      } else {
         Integer value=namePool.get(systemId);
         if(value==null) {
            value=namePool.size();
            namePool.put(systemId, value);
         }
         systemId=value.toString();
      }
      return systemId;
   }
   
   private void serializePool() {
      try {
         for(String key:namePool.keySet()) {
            Integer value=namePool.get(key);
            writer.write(value.toString()+":"+key+"\n");
         }
      } catch(IOException x) {
         LOG.error("Can't write trace.",x);
     }
   }
   
}
