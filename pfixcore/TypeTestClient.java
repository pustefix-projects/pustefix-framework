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

	public static void main(String [] args) throws Exception {
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
		tt.echoStringMultiArray(new String[][] {{"a","b"},{"c","d"}});
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


	public TypeTestClient() throws Exception {
		System.out.println("echoInt(3) -> "+echoInt(3));
		System.out.println("echoIntArray(new int[] {1,2,3,4,5,6,7,8}) -> "+toString(echoIntArray(new int[] {1,2,3,4,5,6,7,8})));
		System.out.println("echoFloat(7.54f) -> "+echoFloat(7.54f));
		System.out.println("echoFloatArray(new float[] {1f,2f,3f,4f,5f,6f,7f,8f}) -> "+toString(echoFloatArray(new float[] {1f,2f,3f,4f,5f,6f,7f,8f})));
		System.out.println("echoDouble(4.67d) -> "+echoDouble(4.67d));
		System.out.println("echoDoubleArray(new double[] {1d,2d,3d,4d,5d,6d,7d,8d}) -> "+toString(echoDoubleArray(new double[] {1d,2d,3d,4d,5d,6d,7d,8d})));
		System.out.println("echoString(testtext) -> "+echoString("testtest"));
		System.out.println("echoStringArray(a,b,c,d,e,f,g,h) -> "+toString(echoStringArray(new String[] {"a","b","c","d","e","f","g","h"})));
	}	

	private String toString(int[] vals) {
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<vals.length;i++) sb.append(""+vals[i]+" ");
		return sb.toString();
	}

        private String toString(float[] vals) {
                StringBuffer sb=new StringBuffer();
                for(int i=0;i<vals.length;i++) sb.append(""+vals[i]+" ");
                return sb.toString();
        }
	
        private String toString(double[] vals) {
                StringBuffer sb=new StringBuffer();
                for(int i=0;i<vals.length;i++) sb.append(""+vals[i]+" ");
                return sb.toString();
        }

        private String toString(Object[] vals) {
                StringBuffer sb=new StringBuffer();
                for(int i=0;i<vals.length;i++) sb.append(""+vals[i].toString()+" ");
                return sb.toString();
        }



	private Call createCall() throws Exception {
		String endpoint="http://webservice.zap.ue.schlund.de/xml/webservice/TypeTest";
                Service service=new Service();
		Call call=(Call)service.createCall();
                call.setTargetEndpointAddress(endpoint);
		//call.setEncodingStyle(Constants.URI_LITERAL_ENC);
		return call;
	}

	public int echoInt(int val) throws Exception {
		Call call=createCall();
	        call.setOperationName("echoInt");
                call.addParameter(new QName("urn:TypeTest","val"),XMLType.XSD_INT,ParameterMode.IN);
                call.setReturnType(XMLType.XSD_INT);
                Integer ret=(Integer)call.invoke(new Object[] {new Integer(val)});
		return ret.intValue();
	}

        public int[] echoIntArray(int[] vals) throws Exception {
                Call call=createCall();
                call.setOperationName("echoIntArray");
                call.addParameter(new QName("urn:TypeTest","vals"),new QName("urn:TypeTest","ArrayOf_xsd_int"),ParameterMode.IN);
                call.setReturnType(new QName("urn:TypeTest","ArrayOf_xsd_int"));
		Integer[] objVals=new Integer[vals.length];
		for(int i=0;i<vals.length;i++) objVals[i]=new Integer(vals[i]);
                int[] retVals=(int[])call.invoke(new Object[] {objVals});
                return retVals;
        }

	public float echoFloat(float val) throws Exception {
                Call call=createCall();
                call.setOperationName("echoFloat");
                call.addParameter(new QName("urn:TypeTest","val"),XMLType.XSD_FLOAT,ParameterMode.IN);
                call.setReturnType(XMLType.XSD_FLOAT);
                Float ret=(Float)call.invoke(new Object[] {new Float(val)});
		return ret.floatValue();
	}

        public float[] echoFloatArray(float[] vals) throws Exception {
                Call call=createCall();
                call.setOperationName("echoFloatArray");
                call.addParameter(new QName("urn:TypeTest","vals"),new QName("urn:TypeTest","ArrayOf_xsd_float"),ParameterMode.IN);
                call.setReturnType(new QName("urn:TypeTest","ArrayOf_xsd_float"));
                Float[] objVals=new Float[vals.length];
                for(int i=0;i<vals.length;i++) objVals[i]=new Float(vals[i]);
                float[] retVals=(float[])call.invoke(new Object[] {objVals});
                return retVals;
        }

	public double echoDouble(double val) throws Exception {
		Call call=createCall();
		call.setOperationName("echoDouble");
		call.addParameter(new QName("urn:TypeTest","val"),XMLType.XSD_DOUBLE,ParameterMode.IN);
                call.setReturnType(XMLType.XSD_DOUBLE);
                Double ret=(Double)call.invoke(new Object[] {new Double(val)});
                return ret.doubleValue();
        }

        public double[] echoDoubleArray(double[] vals) throws Exception {
                Call call=createCall();
                call.setOperationName("echoDoubleArray");
                call.addParameter(new QName("urn:TypeTest","vals"),new QName("urn:TypeTest","ArrayOf_xsd_double"),ParameterMode.IN);
                call.setReturnType(new QName("urn:TypeTest","ArrayOf_xsd_double"));
                Double[] objVals=new Double[vals.length];
                for(int i=0;i<vals.length;i++) objVals[i]=new Double(vals[i]);
                double[] retVals=(double[])call.invoke(new Object[] {objVals});
                return retVals;
        }

        public String echoString(String val) throws Exception {
                Call call=createCall();
                call.setOperationName("echoString");
                call.addParameter(new QName("urn:TypeTest","val"),XMLType.XSD_STRING,ParameterMode.IN);
                call.setReturnType(XMLType.XSD_STRING);
                String ret=(String)call.invoke(new Object[] {val});
                return ret;
        }

        public String[] echoStringArray(String[] vals) throws Exception {
                Call call=createCall();
                call.setOperationName("echoStringArray");
                call.addParameter(new QName("urn:TypeTest","vals"),new QName("urn:TypeTest","ArrayOf_xsd_string"),ParameterMode.IN);
                call.setReturnType(new QName("urn:TypeTest","ArrayOf_xsd_string"));
                String[] retVals=(String[])call.invoke(new Object[] {vals});
                return retVals;
        }

        public Date echoDate(Date val) throws Exception {
                Call call=createCall();
                call.setOperationName("echoDate");
                call.addParameter(new QName("urn:TypeTest","val"),XMLType.XSD_DATE,ParameterMode.IN);
                call.setReturnType(XMLType.XSD_DATE);
                Date ret=(Date)call.invoke(new Object[] {val});
                return ret;
        }

        public Date[] echoDateArray(Date[] vals) throws Exception {
                Call call=createCall();
                call.setOperationName("echoDateArray");
                call.addParameter(new QName("urn:TypeTest","vals"),new QName("urn:TypeTest","ArrayOf_xsd_date"),ParameterMode.IN);
                call.setReturnType(new QName("urn:TypeTest","ArrayOf_xsd_date"));
                Date[] retVals=(Date[])call.invoke(new Object[] {vals});
                return retVals;
        }


}
