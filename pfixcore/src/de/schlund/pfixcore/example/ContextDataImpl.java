package de.schlund.pfixcore.example;

import java.util.Calendar;
import java.util.Vector;

import org.apache.log4j.Category;
import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixxml.ResultDocument;

import de.schlund.pfixcore.example.webservices.*;

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
        int no=250;
        String[] data=new String[no];
        for(int i=0;i<no;i++) data[i]="data"+i;
        return data;
    }
    
    public DataBean getDataBean() {
        return new DataBean("test",Calendar.getInstance(),123,new float[] {123f,456f});
    }
    
    public ComplexDataBean getComplexDataBean() {
        Vector v=new Vector();
        v.add("abc");
        v.add(new Integer("123"));
        ComplexDataBean leaf1=new ComplexDataBean("leaf1",Calendar.getInstance(),1,1f);
        ComplexDataBean leaf2=new ComplexDataBean("leaf2",Calendar.getInstance(),2,2f);
        ComplexDataBean root=new ComplexDataBean("root",Calendar.getInstance(),0,0f);
        root.setChildren(new ComplexDataBean[] {leaf1,leaf2});
        leaf1.setParent(root);
        leaf2.setParent(root);
        return root;
    }
    
}
