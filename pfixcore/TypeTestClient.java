import org.apache.axis.client.Call;
import org.apache.axis.Constants;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.axis.utils.Options;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

public class TypeTestClient {

	public static void main(String [] args) throws Exception {
		new TypeTestClient();
	}

	public TypeTestClient() throws Exception {
		System.out.println("echoInt(3) -> "+echoInt(3));
		System.out.println("echoIntArray(new int[] {1,2,3,4,5,6,7,8}) -> "+toString(echoIntArray(new int[] {1,2,3,4,5,6,7,8})));
		System.out.println("echoFloat(7.54f) -> "+echoFloat(7.54f));
		System.out.println("echoDouble(4.67d) -> "+echoDouble(4.67d));
	}	

	private String toString(int[] vals) {
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<vals.length;i++) sb.append(""+vals[i]+" ");
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

	public double echoDouble(double val) throws Exception {
		Call call=createCall();
		call.setOperationName("echoDouble");
		call.addParameter(new QName("urn:TypeTest","val"),XMLType.XSD_DOUBLE,ParameterMode.IN);
                call.setReturnType(XMLType.XSD_DOUBLE);
                Double ret=(Double)call.invoke(new Object[] {new Double(val)});
                return ret.doubleValue();
        }


}
