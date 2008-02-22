/* Generated By:JavaCC: Do not edit this line. JSONParser.java */
package de.schlund.pfixcore.webservice.json.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import de.schlund.pfixcore.webservice.json.JSONArray;
import de.schlund.pfixcore.webservice.json.JSONObject;
import de.schlund.pfixcore.webservice.json.ParserUtils;

public class JSONParser implements JSONParserConstants {

        Object jsonValue;
        String memberName;

        public static void main(String[] args) {
                try {
                        String query=args[0];
                        query=load(new File(query),"UTF-8");
                        JSONParser parser=new JSONParser(new StringReader(query));
                        Object val=parser.getJSONValue();
                        System.out.println(ParserUtils.javaToJson(val));
                } catch(Exception x) {
                        x.printStackTrace();
                }
        }

        public Object getJSONValue() throws Exception {
        if(jsonValue==null) {
        value();
      }
      return jsonValue;
   }


    public static String load(File file,String encoding) throws IOException {
        FileInputStream fis=new FileInputStream(file);
        InputStreamReader reader=new InputStreamReader(fis,encoding);
        StringBuffer strBuf=new StringBuffer();
        char[] buffer=new char[4096];
        int i=0;
        try {
            while((i=reader.read(buffer))!=-1) strBuf.append(buffer,0,i);
        } finally {
            fis.close();
        }
        return strBuf.toString();
    }

  final public void value() throws ParseException {
        Token valueToken;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LCB:
      object();
      break;
    case LSB:
      array();
      break;
    case QSTR:
      valueToken = jj_consume_token(QSTR);
                             jsonValue=ParserUtils.jsonToJava(valueToken.image);
      break;
    case NUM:
      valueToken = jj_consume_token(NUM);
                             jsonValue=ParserUtils.parseNumber(valueToken.image);
      break;
    case TRUE:
      jj_consume_token(TRUE);
                   jsonValue=Boolean.TRUE;
      break;
    case FALSE:
      jj_consume_token(FALSE);
                    jsonValue=Boolean.FALSE;
      break;
    case NULL:
      jj_consume_token(NULL);
                   jsonValue=null;
      break;
    case DATE:
      valueToken = jj_consume_token(DATE);
                         jsonValue=ParserUtils.parseDate(valueToken.image);
      break;
    default:
      jj_la1[0] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void member() throws ParseException {
        Token memberNameToken;
    memberNameToken = jj_consume_token(QSTR);
    jj_consume_token(COL);
    value();
         memberName=ParserUtils.jsonToJava(memberNameToken.image);
  }

  final public void object() throws ParseException {
        JSONObject jsonObject;
         jsonObject=new JSONObject();
    jj_consume_token(LCB);
    member();
                        jsonObject.putMember(memberName,jsonValue);
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COM:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_1;
      }
      jj_consume_token(COM);
      member();
                                                                                       jsonObject.putMember(memberName,jsonValue);
    }
    jj_consume_token(RCB);
         jsonValue=jsonObject;
  }

  final public void array() throws ParseException {
        JSONArray jsonArray;
         jsonArray=new JSONArray();
    jj_consume_token(LSB);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case LCB:
    case LSB:
    case TRUE:
    case FALSE:
    case NULL:
    case QSTR:
    case NUM:
    case DATE:
      value();
                        jsonArray.add(jsonValue);
      label_2:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case COM:
          ;
          break;
        default:
          jj_la1[2] = jj_gen;
          break label_2;
        }
        jj_consume_token(COM);
        value();
                                                                    jsonArray.add(jsonValue);
      }
      break;
    default:
      jj_la1[3] = jj_gen;
      ;
    }
    jj_consume_token(RSB);
         jsonValue=jsonArray;
  }

  public JSONParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[4];
  static private int[] jj_la1_0;
  static {
      jj_la1_0();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x6f140,0x800,0x800,0x6f140,};
   }

  public JSONParser(java.io.InputStream stream) {
     this(stream, null);
  }
  public JSONParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new JSONParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 4; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 4; i++) jj_la1[i] = -1;
  }

  public JSONParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new JSONParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 4; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 4; i++) jj_la1[i] = -1;
  }

  public JSONParser(JSONParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 4; i++) jj_la1[i] = -1;
  }

  public void ReInit(JSONParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 4; i++) jj_la1[i] = -1;
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector<int[]> jj_expentries = new java.util.Vector<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[19];
    for (int i = 0; i < 19; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 4; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 19; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

}
