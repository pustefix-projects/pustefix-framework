package de.schlund.pfixcore.example;

import java.util.Date;
import java.util.Vector;

import org.apache.log4j.Category;
import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixxml.ResultDocument;

public class ContextDataImpl implements ContextResource, ContextData {
    
    Category CAT = Category.getInstance(this.getClass().getName());
    
    public void init(Context context) {
    }
   
    public void reset() {
    }

    public boolean needsData() {
        return false;
    }

    public void insertStatus(ResultDocument resdoc, Element node) throws Exception {
        resdoc.addTextChild(node,"text","üöäß");
        createStruct(resdoc,node,0,3,4);
    }
        
    public void createStruct(ResultDocument resdoc,Element node,int level,int no,int depth) throws Exception {   
        if(level>=depth) return;
        for(int i=0;i<no;i++) {
            Element elem=resdoc.createNode("data");
            elem.setAttribute("id",level+"_"+i);
            node.appendChild(elem);
            createStruct(resdoc,elem,level+1,no,depth);
        }
    }
    
    public String getData() {
        StringBuffer sb=new StringBuffer();
        createStruct(sb,0,3,4);
        return sb.toString();
    }
    
    public void createStruct(StringBuffer sb,int level,int no,int depth) {
        if(level>=depth) return;
        for(int i=0;i<no;i++) {
            sb.append("<data ");
            sb.append("id=\""+level+"_"+i+"\">");
            createStruct(sb,level+1,no,depth);
            sb.append("</data>");
        }
    }
    
    public String[] getDataArray() {
        int no=10;
        String[] data=new String[no];
        for(int i=0;i<no;i++) data[i]="data"+i;
        return data;
    }
    
    public DataBean getDataBean() {
        return new DataBean("test");
    }
    
    public ComplexData getComplexData() {
        Vector v=new Vector();
        v.add("abc");
        v.add(new Integer("123"));
        ComplexData leaf1=new ComplexData("leaf1",new Date(),1,(float)1);
        ComplexData leaf2=new ComplexData("leaf2",new Date(),2,(float)2);
        ComplexData root=new ComplexData("root",new Date(),0,(float)0);
        root.setChildren(new ComplexData[] {leaf1,leaf2});
        //leaf1.setParent(root);
        //leaf2.setParent(root);
        return root;
    }
    
}
