import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.axis.client.Call;
import org.apache.axis.Constants;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.utils.Options;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import javax.xml.parsers.*;
import org.w3c.dom.*;

import de.schlund.pfixcore.example.webservices.*;


public class TypeTestClient {

	int threadNo;

	public TypeTestClient(int threadNo) {
		this.threadNo=threadNo;
	}

	public void startTest() {
		for(int i=0;i<threadNo;i++) {
			ClientThread ct=new ClientThread(i);
			ct.start();
		}
	}

	public static void main(String [] args) throws Exception {
		int threadNo=1;
		if(args.length==1) threadNo=Integer.parseInt(args[0]);
		TypeTestClient ttc=new TypeTestClient(threadNo);
		ttc.startTest();
	}

	public class ClientThread extends Thread {

		int no;

		ClientThread(int no) {
			this.no=no;
		}

		public void run() {
			try {
		System.out.println("START CLIENT "+no);
		long t1=System.currentTimeMillis();
		TypeTestService tts=new TypeTestServiceLocator();
		TypeTest tt=tts.getTypeTest();
		tt.echoInt(1);
		tt.echoIntArray(new int[] {1,2,3});
		tt.echoFloat(1f);
		tt.echoFloatArray(new float[] {1f,2f,3f});
		tt.echoDouble(1d);
		tt.echoDoubleArray(new double[] {1d,2d,3d});
		tt.echoString("a");
		tt.echoStringArray(new String[] {"a","b","c"});
		tt.echoStringMultiArray(new String[][] {{"a","b"},{"c","d","e"}});
		tt.echoDate(Calendar.getInstance());
		tt.echoDateArray(new Calendar[] {Calendar.getInstance(),Calendar.getInstance(),Calendar.getInstance()});
		tt.echoObject("testtext");
		tt.echoObjectArray(new Object[] {new Integer(34),"testtext",new Object[] {"a","b"}});	
		DataBean bean=new DataBean("bean",Calendar.getInstance(),1,1f);
		//bean.setName("bean");
		//bean.setDate(Calendar.getInstance());
		//bean.setIntVal(4);
		//bean.setFloatVal(4f);
		tt.echoDataBean(bean);
                DataBean bean2=new DataBean("bean2",Calendar.getInstance(),2,2f);
                //bean2.setName("bean2");
                //bean2.setDate(Calendar.getInstance());
                //bean2.setIntVal(3);
                //bean2.setFloatVal(3f);
		tt.echoDataBeanArray(new DataBean[] {bean,bean,bean2});
		//new TypeTestClient();
		Element elem=getDOMElement("test1");
		tt.echoElement(elem);
		Element elem2=getDOMElement("test2");
		tt.echoElementArray(new Element[] {elem,elem,elem2});
		HashMap map=new HashMap();
		map.put("stringkey","stringval");
		map.put(new Integer(1),new Float(1.1));
		map.put(bean,new int[] {1,2,3});
		tt.echoHashMap(map);
		long t2=System.currentTimeMillis();
		System.out.println("END CLIENT "+no+" ("+(t2-t1)+"ms)");

			} catch(Exception x) {
				System.err.println(x);
			}
	}

	}

	public static Element getDOMElement(String name) throws Exception {
			DocumentBuilderFactory dbf=DocumentBuilderFactory.newInstance();
			DocumentBuilder db=dbf.newDocumentBuilder();
			Document doc=db.newDocument();
			Element root=doc.createElement(name);
			doc.appendChild(root);
			Element elem=doc.createElement("foo");
			elem.setAttribute("grrr","llll");
			root.appendChild(elem);
			return root;
	}


}
