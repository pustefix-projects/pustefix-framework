package de.schlund.pfixcore.example.webservices;

import java.util.Random;

import org.w3c.dom.Element;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.ContextResource;
import de.schlund.pfixxml.ResultDocument;


public class ContextDataImpl implements ContextResource, ContextData {

    String data;
    String[] dataArray;
    
    public void init(Context context) {
    }
   
    public void reset() {
    }

    public boolean needsData() {
        return false;
    }

    public void insertStatus(ResultDocument resdoc,Element node) throws Exception {
    }
    
    public String exchangeData(String data,int strSize) throws Exception {
        this.data=data;
        if(strSize<0) strSize=0;
        return generateString(strSize);
    }
    
    public String[] exchangeDataArray(String[] data,int arrSize,int strSize) throws Exception {
        this.dataArray=data;
        if(arrSize<0) arrSize=0;
        if(strSize<0) strSize=0;
        String[] ret=new String[arrSize];
        for(int i=0;i<arrSize;i++) ret[i]=generateString(strSize);
        return ret;
    }
   
    
    final static Random random=new Random();

    public static String generateString(int length) {
        StringBuffer sb=new StringBuffer();
        int val=0;
        for(int i=0;i<length;i++) {
            while(!((val>47 && val<58)||(val>64 && val<91)||(val>96 && val<123)))  {
                val=random.nextInt(123); 
            }
            sb.append((char)val);
            val=0;
        }
        return sb.toString();
    }

}
