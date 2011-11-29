package org.jboss.errai.marshalling.client.api;

import java.util.Map;
import java.util.HashMap;
import org.jboss.errai.marshalling.client.marshallers.ObjectMarshaller;
import org.jboss.errai.marshalling.client.marshallers.MapMarshaller;
import org.jboss.errai.marshalling.client.marshallers.BooleanMarshaller;
import org.jboss.errai.marshalling.client.marshallers.ListMarshaller;
import org.jboss.errai.marshalling.client.marshallers.LongMarshaller;
import org.jboss.errai.marshalling.client.marshallers.IntegerMarshaller;
import org.jboss.errai.marshalling.client.marshallers.StringMarshaller;
import org.jboss.errai.marshalling.client.marshallers.ShortMarshaller;
import org.jboss.errai.marshalling.client.marshallers.DateMarshaller;
import org.jboss.errai.marshalling.client.marshallers.FloatMarshaller;
import org.jboss.errai.marshalling.client.marshallers.SQLDateMarshaller;
import org.jboss.errai.marshalling.client.marshallers.DoubleMarshaller;
import org.jboss.errai.marshalling.client.marshallers.CharacterMarshaller;
import org.jboss.errai.marshalling.client.marshallers.SetMarshaller;
import org.jboss.errai.marshalling.client.marshallers.ByteMarshaller;
import java.util.List;
import com.google.gwt.json.client.JSONValue;
import java.util.EmptyStackException;
import org.jboss.errai.bus.client.tests.support.StudyTreeNodeContainer;
import org.jboss.errai.bus.client.tests.support.SType;
import org.jboss.errai.bus.client.tests.support.ClassWithNestedClass;
import org.jboss.errai.bus.client.tests.support.SimpleEntity;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import org.jboss.errai.bus.client.tests.support.Group;
import org.jboss.errai.bus.client.tests.support.TreeNodeContainer;
import java.io.UnsupportedEncodingException;
import org.jboss.errai.bus.client.tests.support.TestException;
import org.jboss.errai.bus.client.tests.support.User;
import org.jboss.errai.bus.client.tests.support.ClassWithNestedClass.Nested;
import com.google.gwt.json.client.JSONObject;
import org.jboss.errai.marshalling.client.util.MarshallUtil;
import org.jboss.errai.bus.client.tests.support.SType.Place;
import org.jboss.errai.bus.client.tests.support.AbstractEntity;

public class MarshallerFactoryImpl implements MarshallerFactory {
  private Map<String, Marshaller> marshallers = new HashMap<String, Marshaller>();
  private ObjectMarshaller java_lang_Object;
  private ObjectMarshaller arrayOf_java_lang_Object;
  private MapMarshaller java_util_Map;
  private MapMarshaller arrayOf_java_util_Map;
  private BooleanMarshaller java_lang_Boolean;
  private BooleanMarshaller arrayOf_java_lang_Boolean;
  private ListMarshaller java_util_List;
  private ListMarshaller arrayOf_java_util_List;
  private LongMarshaller java_lang_Long;
  private LongMarshaller arrayOf_java_lang_Long;
  private IntegerMarshaller java_lang_Integer;
  private IntegerMarshaller arrayOf_java_lang_Integer;
  private StringMarshaller java_lang_String;
  private StringMarshaller arrayOf_java_lang_String;
  private ShortMarshaller java_lang_Short;
  private ShortMarshaller arrayOf_java_lang_Short;
  private DateMarshaller java_util_Date;
  private DateMarshaller arrayOf_java_util_Date;
  private FloatMarshaller java_lang_Float;
  private FloatMarshaller arrayOf_java_lang_Float;
  private SQLDateMarshaller java_sql_Date;
  private SQLDateMarshaller arrayOf_java_sql_Date;
  private DoubleMarshaller java_lang_Double;
  private DoubleMarshaller arrayOf_java_lang_Double;
  private CharacterMarshaller java_lang_Character;
  private CharacterMarshaller arrayOf_java_lang_Character;
  private SetMarshaller java_util_Set;
  private SetMarshaller arrayOf_java_util_Set;
  private ByteMarshaller java_lang_Byte;
  private ByteMarshaller arrayOf_java_lang_Byte;
  private Marshaller<List, StackTraceElement[]> arrayOf_java_lang_StackTraceElement_D1;
  private Marshaller<JSONValue, Throwable> java_lang_Throwable;
  private Marshaller<JSONValue, EmptyStackException> java_util_EmptyStackException;
  private Marshaller<JSONValue, StudyTreeNodeContainer> org_jboss_errai_bus_client_tests_support_StudyTreeNodeContainer;
  private Marshaller<List, char[]> arrayOf_char_D1;
  private Marshaller<List, char[][]> arrayOf_char_D2;
  private Marshaller<List, SType[]> arrayOf_org_jboss_errai_bus_client_tests_support_SType_D1;
  private Marshaller<JSONValue, SType> org_jboss_errai_bus_client_tests_support_SType;
  private Marshaller<JSONValue, StringIndexOutOfBoundsException> java_lang_StringIndexOutOfBoundsException;
  private Marshaller<JSONValue, ClassWithNestedClass> org_jboss_errai_bus_client_tests_support_ClassWithNestedClass;
  private Marshaller<JSONValue, Exception> java_lang_Exception;
  private Marshaller<JSONValue, ClassCastException> java_lang_ClassCastException;
  private Marshaller<JSONValue, NullPointerException> java_lang_NullPointerException;
  private Marshaller<JSONValue, NegativeArraySizeException> java_lang_NegativeArraySizeException;
  private Marshaller<JSONValue, SimpleEntity> org_jboss_errai_bus_client_tests_support_SimpleEntity;
  private Marshaller<JSONValue, IOException> java_io_IOException;
  private Marshaller<JSONValue, AssertionError> java_lang_AssertionError;
  private Marshaller<JSONValue, RuntimeException> java_lang_RuntimeException;
  private Marshaller<JSONValue, ConcurrentModificationException> java_util_ConcurrentModificationException;
  private Marshaller<JSONValue, Group> org_jboss_errai_bus_client_tests_support_Group;
  private Marshaller<JSONValue, TreeNodeContainer> org_jboss_errai_bus_client_tests_support_TreeNodeContainer;
  private Marshaller<JSONValue, IllegalArgumentException> java_lang_IllegalArgumentException;
  private Marshaller<JSONValue, StackTraceElement> java_lang_StackTraceElement;
  private Marshaller<JSONValue, ArrayStoreException> java_lang_ArrayStoreException;
  private Marshaller<JSONValue, ArithmeticException> java_lang_ArithmeticException;
  private Marshaller<JSONValue, UnsupportedEncodingException> java_io_UnsupportedEncodingException;
  private Marshaller<JSONValue, IndexOutOfBoundsException> java_lang_IndexOutOfBoundsException;
  private Marshaller<JSONValue, UnsupportedOperationException> java_lang_UnsupportedOperationException;
  private Marshaller<JSONValue, NumberFormatException> java_lang_NumberFormatException;
  private Marshaller<JSONValue, TestException> org_jboss_errai_bus_client_tests_support_TestException;
  private Marshaller<JSONValue, User> org_jboss_errai_bus_client_tests_support_User;
  private Marshaller<JSONValue, Nested> org_jboss_errai_bus_client_tests_support_ClassWithNestedClass_Nested;
  private Marshaller<List, Object[]> arrayOf_java_lang_Object_D1;
  public MarshallerFactoryImpl() {
    java_lang_Object = new ObjectMarshaller();
    marshallers.put("java.lang.Object", java_lang_Object);
    java_util_Map = new MapMarshaller();
    marshallers.put("java.util.Map", java_util_Map);
    marshallers.put("java.util.AbstractMap", java_util_Map);
    marshallers.put("java.util.HashMap", java_util_Map);
    marshallers.put("java.util.LinkedHashMap", java_util_Map);
    java_lang_Boolean = new BooleanMarshaller();
    marshallers.put("java.lang.Boolean", java_lang_Boolean);
    java_util_List = new ListMarshaller();
    marshallers.put("java.util.List", java_util_List);
    marshallers.put("java.util.AbstractList", java_util_List);
    marshallers.put("java.util.ArrayList", java_util_List);
    marshallers.put("java.util.LinkedList", java_util_List);
    java_lang_Long = new LongMarshaller();
    marshallers.put("java.lang.Long", java_lang_Long);
    java_lang_Integer = new IntegerMarshaller();
    marshallers.put("java.lang.Integer", java_lang_Integer);
    java_lang_String = new StringMarshaller();
    marshallers.put("java.lang.String", java_lang_String);
    java_lang_Short = new ShortMarshaller();
    marshallers.put("java.lang.Short", java_lang_Short);
    java_util_Date = new DateMarshaller();
    marshallers.put("java.util.Date", java_util_Date);
    java_lang_Float = new FloatMarshaller();
    marshallers.put("java.lang.Float", java_lang_Float);
    java_sql_Date = new SQLDateMarshaller();
    marshallers.put("java.sql.Date", java_sql_Date);
    java_lang_Double = new DoubleMarshaller();
    marshallers.put("java.lang.Double", java_lang_Double);
    java_lang_Character = new CharacterMarshaller();
    marshallers.put("java.lang.Character", java_lang_Character);
    java_util_Set = new SetMarshaller();
    marshallers.put("java.util.Set", java_util_Set);
    marshallers.put("java.util.AbstractSet", java_util_Set);
    marshallers.put("java.util.HashSet", java_util_Set);
    marshallers.put("java.util.SortedSet", java_util_Set);
    marshallers.put("java.util.LinkedHashSet", java_util_Set);
    java_lang_Byte = new ByteMarshaller();
    marshallers.put("java.lang.Byte", java_lang_Byte);
    arrayOf_java_lang_StackTraceElement_D1 = new Marshaller<List, StackTraceElement[]>() {
      public Class getTypeHandled() {
        return StackTraceElement.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public StackTraceElement[] demarshall(List a0, MarshallingSession a1) {
        if (a0 == null) {
          return null;
        } else {
          return _demarshall1(a0, a1);
        }
      }

      public boolean handles(List a0) {
        return true;
      }

      public String marshall(StackTraceElement[] a0, MarshallingSession a1) {
        if (a0 == null) {
          return null;
        } else {
          return _marshall1(a0, a1);
        }
      }

      private StackTraceElement[] _demarshall1(List a0, MarshallingSession a1) {
        StackTraceElement[] newArray = new StackTraceElement[a0.size()];
        for (int i = 0; i < newArray.length; i++) {
          newArray[i] = (StackTraceElement) a0.get(i);
        }
        return newArray;
      }

      private String _marshall1(StackTraceElement[] a0, MarshallingSession a1) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < a0.length; i++) {
          if (i > 0) {
            sb.append(",");
          }
          sb.append(java_lang_StackTraceElement.marshall(a0[i], a1));
        }
        return sb.append("]").toString();
      }

    };
    marshallers.put("[Ljava.lang.StackTraceElement;", arrayOf_java_lang_StackTraceElement_D1);
    java_lang_Throwable = new Marshaller<JSONValue, Throwable>() {
      public Class getTypeHandled() {
        return Throwable.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public Throwable demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(Throwable.class, objId);
          }
          Throwable entity = new Throwable();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.Throwable", t);
        }
      }

      public String marshall(Throwable a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.Throwable\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.Throwable\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.Throwable", java_lang_Throwable);
    java_util_EmptyStackException = new Marshaller<JSONValue, EmptyStackException>() {
      public Class getTypeHandled() {
        return EmptyStackException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public EmptyStackException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(EmptyStackException.class, objId);
          }
          EmptyStackException entity = new EmptyStackException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.util.EmptyStackException", t);
        }
      }

      public String marshall(EmptyStackException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.util.EmptyStackException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.util.EmptyStackException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.util.EmptyStackException", java_util_EmptyStackException);
    org_jboss_errai_bus_client_tests_support_StudyTreeNodeContainer = new Marshaller<JSONValue, StudyTreeNodeContainer>() {
      public Class getTypeHandled() {
        return StudyTreeNodeContainer.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public StudyTreeNodeContainer demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(StudyTreeNodeContainer.class, objId);
          }
          StudyTreeNodeContainer entity = new StudyTreeNodeContainer();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("studyId")) && (obj.get("studyId").isNull() == null)) {
            entity.setStudyId(java_lang_Integer.demarshall(obj.get("studyId"), a1));
          }
          if ((obj.containsKey("nodeId")) && (obj.get("nodeId").isNull() == null)) {
            entity.setNodeId(java_lang_Integer.demarshall(obj.get("nodeId"), a1));
          }
          if ((obj.containsKey("nodeName")) && (obj.get("nodeName").isNull() == null)) {
            entity.setNodeName(java_lang_String.demarshall(obj.get("nodeName"), a1));
          }
          if ((obj.containsKey("parentNodeId")) && (obj.get("parentNodeId").isNull() == null)) {
            entity.setParentNodeId(java_lang_Integer.demarshall(obj.get("parentNodeId"), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: org.jboss.errai.bus.client.tests.support.StudyTreeNodeContainer", t);
        }
      }

      public String marshall(StudyTreeNodeContainer a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.StudyTreeNodeContainer\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.StudyTreeNodeContainer\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"studyId\" : ").append(java_lang_Integer.marshall(a0.getStudyId(), a1)).append(",").append("\"nodeId\" : ").append(java_lang_Integer.marshall(a0.getNodeId(), a1)).append(",").append("\"nodeName\" : ").append(java_lang_String.marshall(a0.getNodeName(), a1)).append(",").append("\"parentNodeId\" : ").append(java_lang_Integer.marshall(a0.getParentNodeId(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("org.jboss.errai.bus.client.tests.support.StudyTreeNodeContainer", org_jboss_errai_bus_client_tests_support_StudyTreeNodeContainer);
    arrayOf_char_D1 = new Marshaller<List, char[]>() {
      public Class getTypeHandled() {
        return char.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public char[] demarshall(List a0, MarshallingSession a1) {
        if (a0 == null) {
          return null;
        } else {
          return _demarshall1(a0, a1);
        }
      }

      public boolean handles(List a0) {
        return true;
      }

      public String marshall(char[] a0, MarshallingSession a1) {
        if (a0 == null) {
          return null;
        } else {
          return _marshall1(a0, a1);
        }
      }

      private char[] _demarshall1(List a0, MarshallingSession a1) {
        char[] newArray = new char[a0.size()];
        for (int i = 0; i < newArray.length; i++) {
          newArray[i] = (Character) a0.get(i);
        }
        return newArray;
      }

      private String _marshall1(char[] a0, MarshallingSession a1) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < a0.length; i++) {
          if (i > 0) {
            sb.append(",");
          }
          sb.append(java_lang_Character.marshall(a0[i], a1));
        }
        return sb.append("]").toString();
      }

    };
    marshallers.put("[C", arrayOf_char_D1);
    arrayOf_char_D2 = new Marshaller<List, char[][]>() {
      public Class getTypeHandled() {
        return char.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public char[][] demarshall(List a0, MarshallingSession a1) {
        if (a0 == null) {
          return null;
        } else {
          return _demarshall2(a0, a1);
        }
      }

      public boolean handles(List a0) {
        return true;
      }

      public String marshall(char[][] a0, MarshallingSession a1) {
        if (a0 == null) {
          return null;
        } else {
          return _marshall2(a0, a1);
        }
      }

      private char[][] _demarshall2(List a0, MarshallingSession a1) {
        char[][] newArray = new char[a0.size()][];
        for (int i = 0; i < newArray.length; i++) {
          newArray[i] = _demarshall1((List) a0.get(i), a1);
        }
        return newArray;
      }

      private String _marshall2(char[][] a0, MarshallingSession a1) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < a0.length; i++) {
          if (i > 0) {
            sb.append(",");
          }
          sb.append(_marshall1(a0[i], a1));
        }
        return sb.append("]").toString();
      }

      private char[] _demarshall1(List a0, MarshallingSession a1) {
        char[] newArray = new char[a0.size()];
        for (int i = 0; i < newArray.length; i++) {
          newArray[i] = (Character) a0.get(i);
        }
        return newArray;
      }

      private String _marshall1(char[] a0, MarshallingSession a1) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < a0.length; i++) {
          if (i > 0) {
            sb.append(",");
          }
          sb.append(java_lang_Character.marshall(a0[i], a1));
        }
        return sb.append("]").toString();
      }

    };
    marshallers.put("[[C", arrayOf_char_D2);
    arrayOf_org_jboss_errai_bus_client_tests_support_SType_D1 = new Marshaller<List, SType[]>() {
      public Class getTypeHandled() {
        return SType.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public SType[] demarshall(List a0, MarshallingSession a1) {
        if (a0 == null) {
          return null;
        } else {
          return _demarshall1(a0, a1);
        }
      }

      public boolean handles(List a0) {
        return true;
      }

      public String marshall(SType[] a0, MarshallingSession a1) {
        if (a0 == null) {
          return null;
        } else {
          return _marshall1(a0, a1);
        }
      }

      private SType[] _demarshall1(List a0, MarshallingSession a1) {
        SType[] newArray = new SType[a0.size()];
        for (int i = 0; i < newArray.length; i++) {
          newArray[i] = (SType) a0.get(i);
        }
        return newArray;
      }

      private String _marshall1(SType[] a0, MarshallingSession a1) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < a0.length; i++) {
          if (i > 0) {
            sb.append(",");
          }
          sb.append(org_jboss_errai_bus_client_tests_support_SType.marshall(a0[i], a1));
        }
        return sb.append("]").toString();
      }

    };
    marshallers.put("[Lorg.jboss.errai.bus.client.tests.support.SType;", arrayOf_org_jboss_errai_bus_client_tests_support_SType_D1);
    org_jboss_errai_bus_client_tests_support_SType = new Marshaller<JSONValue, SType>() {
      public Class getTypeHandled() {
        return SType.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public SType demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(SType.class, objId);
          }
          SType entity = new SType();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("fieldOne")) && (obj.get("fieldOne").isNull() == null)) {
            entity.setFieldOne(java_lang_String.demarshall(obj.get("fieldOne"), a1));
          }
          if ((obj.containsKey("fieldTwo")) && (obj.get("fieldTwo").isNull() == null)) {
            entity.setFieldTwo(java_lang_String.demarshall(obj.get("fieldTwo"), a1));
          }
          if ((obj.containsKey("startDate")) && (obj.get("startDate").isNull() == null)) {
            entity.setStartDate(java_util_Date.demarshall(obj.get("startDate"), a1));
          }
          if ((obj.containsKey("endDate")) && (obj.get("endDate").isNull() == null)) {
            entity.setEndDate(java_util_Date.demarshall(obj.get("endDate"), a1));
          }
          if ((obj.containsKey("active")) && (obj.get("active").isNull() == null)) {
            entity.setActive(java_lang_Boolean.demarshall(obj.get("active"), a1));
          }
          if ((obj.containsKey("listOfStypes")) && (obj.get("listOfStypes").isNull() == null)) {
            entity.setListOfStypes(java_util_List.demarshall(obj.get("listOfStypes"), a1));
          }
          if ((obj.containsKey("listOfDates")) && (obj.get("listOfDates").isNull() == null)) {
            entity.setListOfDates(java_util_List.demarshall(obj.get("listOfDates"), a1));
          }
          if ((obj.containsKey("mapofStypes")) && (obj.get("mapofStypes").isNull() == null)) {
            entity.setMapofStypes(java_util_Map.demarshall(obj.get("mapofStypes"), a1));
          }
          if ((obj.containsKey("sTypeToStype")) && (obj.get("sTypeToStype").isNull() == null)) {
            entity.setsTypeToStype(java_util_Map.demarshall(obj.get("sTypeToStype"), a1));
          }
          if ((obj.containsKey("place")) && (obj.get("place").isNull() == null)) {
            entity.setPlace(MarshallUtil.demarshalEnum(Place.class, obj.get("place").isObject(), "EnumStringValue"));
          }
          if ((obj.containsKey("longValue")) && (obj.get("longValue").isNull() == null)) {
            entity.setLongValue(java_lang_Long.demarshall(obj.get("longValue"), a1));
          }
          if ((obj.containsKey("intValue")) && (obj.get("intValue").isNull() == null)) {
            entity.setIntValue(java_lang_Integer.demarshall(obj.get("intValue"), a1));
          }
          if ((obj.containsKey("shortValue")) && (obj.get("shortValue").isNull() == null)) {
            entity.setShortValue(java_lang_Short.demarshall(obj.get("shortValue"), a1));
          }
          if ((obj.containsKey("doubleValue")) && (obj.get("doubleValue").isNull() == null)) {
            entity.setDoubleValue(java_lang_Double.demarshall(obj.get("doubleValue"), a1));
          }
          if ((obj.containsKey("floatValue")) && (obj.get("floatValue").isNull() == null)) {
            entity.setFloatValue(java_lang_Float.demarshall(obj.get("floatValue"), a1));
          }
          if ((obj.containsKey("byteValue")) && (obj.get("byteValue").isNull() == null)) {
            entity.setByteValue(java_lang_Byte.demarshall(obj.get("byteValue"), a1));
          }
          if ((obj.containsKey("charValue")) && (obj.get("charValue").isNull() == null)) {
            entity.setCharValue(java_lang_Character.demarshall(obj.get("charValue"), a1));
          }
          if ((obj.containsKey("charArray")) && (obj.get("charArray").isNull() == null)) {
            entity.setCharArray(arrayOf_char_D1.demarshall(java_util_List.demarshall(obj.get("charArray"), a1), a1));
          }
          if ((obj.containsKey("charArrayMulti")) && (obj.get("charArrayMulti").isNull() == null)) {
            entity.setCharArrayMulti(arrayOf_char_D2.demarshall(java_util_List.demarshall(obj.get("charArrayMulti"), a1), a1));
          }
          if ((obj.containsKey("sTypeArray")) && (obj.get("sTypeArray").isNull() == null)) {
            entity.setsTypeArray(arrayOf_org_jboss_errai_bus_client_tests_support_SType_D1.demarshall(java_util_List.demarshall(obj.get("sTypeArray"), a1), a1));
          }
          if ((obj.containsKey("superValue")) && (obj.get("superValue").isNull() == null)) {
            entity.setSuperValue(java_lang_String.demarshall(obj.get("superValue"), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: org.jboss.errai.bus.client.tests.support.SType", t);
        }
      }

      public String marshall(SType a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.SType\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.SType\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"fieldOne\" : ").append(java_lang_String.marshall(a0.getFieldOne(), a1)).append(",").append("\"fieldTwo\" : ").append(java_lang_String.marshall(a0.getFieldTwo(), a1)).append(",").append("\"startDate\" : ").append(java_util_Date.marshall(a0.getStartDate(), a1)).append(",").append("\"endDate\" : ").append(java_util_Date.marshall(a0.getEndDate(), a1)).append(",").append("\"active\" : ").append(java_lang_Boolean.marshall(a0.getActive(), a1)).append(",").append("\"listOfStypes\" : ").append(java_util_List.marshall(a0.getListOfStypes(), a1)).append(",").append("\"listOfDates\" : ").append(java_util_List.marshall(a0.getListOfDates(), a1)).append(",").append("\"mapofStypes\" : ").append(java_util_Map.marshall(a0.getMapofStypes(), a1)).append(",").append("\"sTypeToStype\" : ").append(java_util_Map.marshall(a0.getsTypeToStype(), a1)).append(",").append("\"place\" : ").append("{\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.SType$Place\",\"EnumStringValue\":\"").append(a0.getPlace().toString()).append("\"}").append(",").append("\"longValue\" : ").append(java_lang_Long.marshall(a0.getLongValue(), a1)).append(",").append("\"intValue\" : ").append(java_lang_Integer.marshall(a0.getIntValue(), a1)).append(",").append("\"shortValue\" : ").append(java_lang_Short.marshall(a0.getShortValue(), a1)).append(",").append("\"doubleValue\" : ").append(java_lang_Double.marshall(a0.getDoubleValue(), a1)).append(",").append("\"floatValue\" : ").append(java_lang_Float.marshall(a0.getFloatValue(), a1)).append(",").append("\"byteValue\" : ").append(java_lang_Byte.marshall(a0.getByteValue(), a1)).append(",").append("\"charValue\" : ").append(java_lang_Character.marshall(a0.getCharValue(), a1)).append(",").append("\"charArray\" : ").append(arrayOf_char_D1.marshall(a0.getCharArray(), a1)).append(",").append("\"charArrayMulti\" : ").append(arrayOf_char_D2.marshall(a0.getCharArrayMulti(), a1)).append(",").append("\"sTypeArray\" : ").append(arrayOf_org_jboss_errai_bus_client_tests_support_SType_D1.marshall(a0.getsTypeArray(), a1)).append(",").append("\"superValue\" : ").append(java_lang_String.marshall(a0.getSuperValue(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("org.jboss.errai.bus.client.tests.support.SType", org_jboss_errai_bus_client_tests_support_SType);
    java_lang_StringIndexOutOfBoundsException = new Marshaller<JSONValue, StringIndexOutOfBoundsException>() {
      public Class getTypeHandled() {
        return StringIndexOutOfBoundsException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public StringIndexOutOfBoundsException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(StringIndexOutOfBoundsException.class, objId);
          }
          StringIndexOutOfBoundsException entity = new StringIndexOutOfBoundsException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.StringIndexOutOfBoundsException", t);
        }
      }

      public String marshall(StringIndexOutOfBoundsException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.StringIndexOutOfBoundsException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.StringIndexOutOfBoundsException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.StringIndexOutOfBoundsException", java_lang_StringIndexOutOfBoundsException);
    org_jboss_errai_bus_client_tests_support_ClassWithNestedClass = new Marshaller<JSONValue, ClassWithNestedClass>() {
      public Class getTypeHandled() {
        return ClassWithNestedClass.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public ClassWithNestedClass demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(ClassWithNestedClass.class, objId);
          }
          ClassWithNestedClass entity = new ClassWithNestedClass();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("nested")) && (obj.get("nested").isNull() == null)) {
            entity.setNested(org_jboss_errai_bus_client_tests_support_ClassWithNestedClass_Nested.demarshall(obj.get("nested"), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: org.jboss.errai.bus.client.tests.support.ClassWithNestedClass", t);
        }
      }

      public String marshall(ClassWithNestedClass a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.ClassWithNestedClass\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.ClassWithNestedClass\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"nested\" : ").append(org_jboss_errai_bus_client_tests_support_ClassWithNestedClass_Nested.marshall(a0.getNested(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("org.jboss.errai.bus.client.tests.support.ClassWithNestedClass", org_jboss_errai_bus_client_tests_support_ClassWithNestedClass);
    java_lang_Exception = new Marshaller<JSONValue, Exception>() {
      public Class getTypeHandled() {
        return Exception.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public Exception demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(Exception.class, objId);
          }
          Exception entity = new Exception();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.Exception", t);
        }
      }

      public String marshall(Exception a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.Exception\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.Exception\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.Exception", java_lang_Exception);
    java_lang_ClassCastException = new Marshaller<JSONValue, ClassCastException>() {
      public Class getTypeHandled() {
        return ClassCastException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public ClassCastException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(ClassCastException.class, objId);
          }
          ClassCastException entity = new ClassCastException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.ClassCastException", t);
        }
      }

      public String marshall(ClassCastException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.ClassCastException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.ClassCastException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.ClassCastException", java_lang_ClassCastException);
    java_lang_NullPointerException = new Marshaller<JSONValue, NullPointerException>() {
      public Class getTypeHandled() {
        return NullPointerException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public NullPointerException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(NullPointerException.class, objId);
          }
          NullPointerException entity = new NullPointerException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.NullPointerException", t);
        }
      }

      public String marshall(NullPointerException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.NullPointerException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.NullPointerException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.NullPointerException", java_lang_NullPointerException);
    java_lang_NegativeArraySizeException = new Marshaller<JSONValue, NegativeArraySizeException>() {
      public Class getTypeHandled() {
        return NegativeArraySizeException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public NegativeArraySizeException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(NegativeArraySizeException.class, objId);
          }
          NegativeArraySizeException entity = new NegativeArraySizeException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.NegativeArraySizeException", t);
        }
      }

      public String marshall(NegativeArraySizeException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.NegativeArraySizeException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.NegativeArraySizeException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.NegativeArraySizeException", java_lang_NegativeArraySizeException);
    org_jboss_errai_bus_client_tests_support_SimpleEntity = new Marshaller<JSONValue, SimpleEntity>() {
      public Class getTypeHandled() {
        return SimpleEntity.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public SimpleEntity demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(SimpleEntity.class, objId);
          }
          SimpleEntity entity = new SimpleEntity();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("id")) && (obj.get("id").isNull() == null)) {
            entity.setId(java_lang_Long.demarshall(obj.get("id"), a1));
          }
          if ((obj.containsKey("active")) && (obj.get("active").isNull() == null)) {
            entity.setActive(java_lang_Boolean.demarshall(obj.get("active"), a1));
          }
          if ((obj.containsKey("login")) && (obj.get("login").isNull() == null)) {
            entity.setLogin(java_lang_String.demarshall(obj.get("login"), a1));
          }
          if ((obj.containsKey("password")) && (obj.get("password").isNull() == null)) {
            entity.setPassword(java_lang_String.demarshall(obj.get("password"), a1));
          }
          if ((obj.containsKey("number")) && (obj.get("number").isNull() == null)) {
            entity.setNumber(java_lang_String.demarshall(obj.get("number"), a1));
          }
          if ((obj.containsKey("selected")) && (obj.get("selected").isNull() == null)) {
            entity.setSelected(java_lang_Boolean.demarshall(obj.get("selected"), a1));
          }
          if ((obj.containsKey("deleted")) && (obj.get("deleted").isNull() == null)) {
            entity.setDeleted(java_lang_Boolean.demarshall(obj.get("deleted"), a1));
          }
          if ((obj.containsKey("createDate")) && (obj.get("createDate").isNull() == null)) {
            entity.setCreateDate(java_util_Date.demarshall(obj.get("createDate"), a1));
          }
          if ((obj.containsKey("lastModifyDate")) && (obj.get("lastModifyDate").isNull() == null)) {
            entity.setLastModifyDate(java_util_Date.demarshall(obj.get("lastModifyDate"), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: org.jboss.errai.bus.client.tests.support.SimpleEntity", t);
        }
      }

      public String marshall(SimpleEntity a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.SimpleEntity\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.SimpleEntity\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"id\" : ").append(java_lang_Long.marshall(a0.getId(), a1)).append(",").append("\"active\" : ").append(java_lang_Boolean.marshall(org_jboss_errai_bus_client_tests_support_SimpleEntity_active(a0), a1)).append(",").append("\"login\" : ").append(java_lang_String.marshall(a0.getLogin(), a1)).append(",").append("\"password\" : ").append(java_lang_String.marshall(a0.getPassword(), a1)).append(",").append("\"number\" : ").append(java_lang_String.marshall(a0.getNumber(), a1)).append(",").append("\"selected\" : ").append(java_lang_Boolean.marshall(org_jboss_errai_bus_client_tests_support_AbstractEntity_selected(a0), a1)).append(",").append("\"deleted\" : ").append(java_lang_Boolean.marshall(org_jboss_errai_bus_client_tests_support_AbstractEntity_deleted(a0), a1)).append(",").append("\"createDate\" : ").append(java_util_Date.marshall(a0.getCreateDate(), a1)).append(",").append("\"lastModifyDate\" : ").append(java_util_Date.marshall(a0.getLastModifyDate(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("org.jboss.errai.bus.client.tests.support.SimpleEntity", org_jboss_errai_bus_client_tests_support_SimpleEntity);
    java_io_IOException = new Marshaller<JSONValue, IOException>() {
      public Class getTypeHandled() {
        return IOException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public IOException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(IOException.class, objId);
          }
          IOException entity = new IOException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.io.IOException", t);
        }
      }

      public String marshall(IOException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.io.IOException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.io.IOException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.io.IOException", java_io_IOException);
    java_lang_AssertionError = new Marshaller<JSONValue, AssertionError>() {
      public Class getTypeHandled() {
        return AssertionError.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public AssertionError demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(AssertionError.class, objId);
          }
          AssertionError entity = new AssertionError();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.AssertionError", t);
        }
      }

      public String marshall(AssertionError a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.AssertionError\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.AssertionError\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.AssertionError", java_lang_AssertionError);
    java_lang_RuntimeException = new Marshaller<JSONValue, RuntimeException>() {
      public Class getTypeHandled() {
        return RuntimeException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public RuntimeException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(RuntimeException.class, objId);
          }
          RuntimeException entity = new RuntimeException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.RuntimeException", t);
        }
      }

      public String marshall(RuntimeException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.RuntimeException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.RuntimeException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.RuntimeException", java_lang_RuntimeException);
    java_util_ConcurrentModificationException = new Marshaller<JSONValue, ConcurrentModificationException>() {
      public Class getTypeHandled() {
        return ConcurrentModificationException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public ConcurrentModificationException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(ConcurrentModificationException.class, objId);
          }
          ConcurrentModificationException entity = new ConcurrentModificationException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.util.ConcurrentModificationException", t);
        }
      }

      public String marshall(ConcurrentModificationException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.util.ConcurrentModificationException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.util.ConcurrentModificationException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.util.ConcurrentModificationException", java_util_ConcurrentModificationException);
    org_jboss_errai_bus_client_tests_support_Group = new Marshaller<JSONValue, Group>() {
      public Class getTypeHandled() {
        return Group.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public Group demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(Group.class, objId);
          }
          Group entity = new Group();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("groupId")) && (obj.get("groupId").isNull() == null)) {
            entity.setGroupId(java_lang_Integer.demarshall(obj.get("groupId"), a1));
          }
          if ((obj.containsKey("name")) && (obj.get("name").isNull() == null)) {
            entity.setName(java_lang_String.demarshall(obj.get("name"), a1));
          }
          if ((obj.containsKey("usersInGroup")) && (obj.get("usersInGroup").isNull() == null)) {
            entity.setUsersInGroup(java_util_List.demarshall(obj.get("usersInGroup"), a1));
          }
          if ((obj.containsKey("subGroup")) && (obj.get("subGroup").isNull() == null)) {
            entity.setSubGroup(org_jboss_errai_bus_client_tests_support_Group.demarshall(obj.get("subGroup"), a1));
          }
          if ((obj.containsKey("groupUserMap")) && (obj.get("groupUserMap").isNull() == null)) {
            entity.setGroupUserMap(java_util_Map.demarshall(obj.get("groupUserMap"), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: org.jboss.errai.bus.client.tests.support.Group", t);
        }
      }

      public String marshall(Group a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.Group\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.Group\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"groupId\" : ").append(java_lang_Integer.marshall(a0.getGroupId(), a1)).append(",").append("\"name\" : ").append(java_lang_String.marshall(a0.getName(), a1)).append(",").append("\"usersInGroup\" : ").append(java_util_List.marshall(a0.getUsersInGroup(), a1)).append(",").append("\"subGroup\" : ").append(org_jboss_errai_bus_client_tests_support_Group.marshall(a0.getSubGroup(), a1)).append(",").append("\"groupUserMap\" : ").append(java_util_Map.marshall(a0.getGroupUserMap(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("org.jboss.errai.bus.client.tests.support.Group", org_jboss_errai_bus_client_tests_support_Group);
    org_jboss_errai_bus_client_tests_support_TreeNodeContainer = new Marshaller<JSONValue, TreeNodeContainer>() {
      public Class getTypeHandled() {
        return TreeNodeContainer.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public TreeNodeContainer demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(TreeNodeContainer.class, objId);
          }
          TreeNodeContainer entity = new TreeNodeContainer();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("nodeId")) && (obj.get("nodeId").isNull() == null)) {
            entity.setNodeId(java_lang_Integer.demarshall(obj.get("nodeId"), a1));
          }
          if ((obj.containsKey("nodeName")) && (obj.get("nodeName").isNull() == null)) {
            entity.setNodeName(java_lang_String.demarshall(obj.get("nodeName"), a1));
          }
          if ((obj.containsKey("parentNodeId")) && (obj.get("parentNodeId").isNull() == null)) {
            entity.setParentNodeId(java_lang_Integer.demarshall(obj.get("parentNodeId"), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: org.jboss.errai.bus.client.tests.support.TreeNodeContainer", t);
        }
      }

      public String marshall(TreeNodeContainer a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.TreeNodeContainer\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.TreeNodeContainer\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"nodeId\" : ").append(java_lang_Integer.marshall(a0.getNodeId(), a1)).append(",").append("\"nodeName\" : ").append(java_lang_String.marshall(a0.getNodeName(), a1)).append(",").append("\"parentNodeId\" : ").append(java_lang_Integer.marshall(a0.getParentNodeId(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("org.jboss.errai.bus.client.tests.support.TreeNodeContainer", org_jboss_errai_bus_client_tests_support_TreeNodeContainer);
    java_lang_IllegalArgumentException = new Marshaller<JSONValue, IllegalArgumentException>() {
      public Class getTypeHandled() {
        return IllegalArgumentException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public IllegalArgumentException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(IllegalArgumentException.class, objId);
          }
          IllegalArgumentException entity = new IllegalArgumentException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.IllegalArgumentException", t);
        }
      }

      public String marshall(IllegalArgumentException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.IllegalArgumentException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.IllegalArgumentException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.IllegalArgumentException", java_lang_IllegalArgumentException);
    java_lang_StackTraceElement = new Marshaller<JSONValue, StackTraceElement>() {
      public Class getTypeHandled() {
        return StackTraceElement.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public StackTraceElement demarshall(JSONValue a0, MarshallingSession a1) {
        JSONObject obj;
        if ((a0 == null) || (a0.isNull() != null)) {
          return null;
        } else {
          obj = a0.isObject();
        }
        String objId = obj.get("__ObjectID").isString().stringValue();
        if (a1.hasObjectHash(objId)) {
          return a1.getObject(StackTraceElement.class, objId);
        }
        StackTraceElement entity = new StackTraceElement(java_lang_String.demarshall(((JSONObject) a0).get("declaringClass"), a1), java_lang_String.demarshall(((JSONObject) a0).get("methodName"), a1), java_lang_String.demarshall(((JSONObject) a0).get("fileName"), a1), java_lang_Integer.demarshall(((JSONObject) a0).get("lineNumber"), a1));
        a1.recordObjectHash(objId, entity);
        return entity;
      }

      public String marshall(StackTraceElement a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        return null;
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.StackTraceElement", java_lang_StackTraceElement);
    java_lang_ArrayStoreException = new Marshaller<JSONValue, ArrayStoreException>() {
      public Class getTypeHandled() {
        return ArrayStoreException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public ArrayStoreException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(ArrayStoreException.class, objId);
          }
          ArrayStoreException entity = new ArrayStoreException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.ArrayStoreException", t);
        }
      }

      public String marshall(ArrayStoreException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.ArrayStoreException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.ArrayStoreException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.ArrayStoreException", java_lang_ArrayStoreException);
    java_lang_ArithmeticException = new Marshaller<JSONValue, ArithmeticException>() {
      public Class getTypeHandled() {
        return ArithmeticException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public ArithmeticException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(ArithmeticException.class, objId);
          }
          ArithmeticException entity = new ArithmeticException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.ArithmeticException", t);
        }
      }

      public String marshall(ArithmeticException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.ArithmeticException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.ArithmeticException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.ArithmeticException", java_lang_ArithmeticException);
    java_io_UnsupportedEncodingException = new Marshaller<JSONValue, UnsupportedEncodingException>() {
      public Class getTypeHandled() {
        return UnsupportedEncodingException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public UnsupportedEncodingException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(UnsupportedEncodingException.class, objId);
          }
          UnsupportedEncodingException entity = new UnsupportedEncodingException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.io.UnsupportedEncodingException", t);
        }
      }

      public String marshall(UnsupportedEncodingException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.io.UnsupportedEncodingException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.io.UnsupportedEncodingException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.io.UnsupportedEncodingException", java_io_UnsupportedEncodingException);
    java_lang_IndexOutOfBoundsException = new Marshaller<JSONValue, IndexOutOfBoundsException>() {
      public Class getTypeHandled() {
        return IndexOutOfBoundsException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public IndexOutOfBoundsException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(IndexOutOfBoundsException.class, objId);
          }
          IndexOutOfBoundsException entity = new IndexOutOfBoundsException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.IndexOutOfBoundsException", t);
        }
      }

      public String marshall(IndexOutOfBoundsException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.IndexOutOfBoundsException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.IndexOutOfBoundsException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.IndexOutOfBoundsException", java_lang_IndexOutOfBoundsException);
    java_lang_UnsupportedOperationException = new Marshaller<JSONValue, UnsupportedOperationException>() {
      public Class getTypeHandled() {
        return UnsupportedOperationException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public UnsupportedOperationException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(UnsupportedOperationException.class, objId);
          }
          UnsupportedOperationException entity = new UnsupportedOperationException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.UnsupportedOperationException", t);
        }
      }

      public String marshall(UnsupportedOperationException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.UnsupportedOperationException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.UnsupportedOperationException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.UnsupportedOperationException", java_lang_UnsupportedOperationException);
    java_lang_NumberFormatException = new Marshaller<JSONValue, NumberFormatException>() {
      public Class getTypeHandled() {
        return NumberFormatException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public NumberFormatException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(NumberFormatException.class, objId);
          }
          NumberFormatException entity = new NumberFormatException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: java.lang.NumberFormatException", t);
        }
      }

      public String marshall(NumberFormatException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.NumberFormatException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"java.lang.NumberFormatException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("java.lang.NumberFormatException", java_lang_NumberFormatException);
    org_jboss_errai_bus_client_tests_support_TestException = new Marshaller<JSONValue, TestException>() {
      public Class getTypeHandled() {
        return TestException.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public TestException demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(TestException.class, objId);
          }
          TestException entity = new TestException();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("detailMessage")) && (obj.get("detailMessage").isNull() == null)) {
            java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(obj.get("detailMessage"), a1));
          }
          if ((obj.containsKey("cause")) && (obj.get("cause").isNull() == null)) {
            java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(obj.get("cause"), a1));
          }
          if ((obj.containsKey("stackTrace")) && (obj.get("stackTrace").isNull() == null)) {
            entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(obj.get("stackTrace"), a1), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: org.jboss.errai.bus.client.tests.support.TestException", t);
        }
      }

      public String marshall(TestException a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.TestException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.TestException\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"detailMessage\" : ").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(",").append("\"cause\" : ").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(",").append("\"stackTrace\" : ").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("org.jboss.errai.bus.client.tests.support.TestException", org_jboss_errai_bus_client_tests_support_TestException);
    org_jboss_errai_bus_client_tests_support_User = new Marshaller<JSONValue, User>() {
      public Class getTypeHandled() {
        return User.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public User demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(User.class, objId);
          }
          User entity = new User();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("id")) && (obj.get("id").isNull() == null)) {
            entity.setId(java_lang_Integer.demarshall(obj.get("id"), a1));
          }
          if ((obj.containsKey("name")) && (obj.get("name").isNull() == null)) {
            entity.setName(java_lang_String.demarshall(obj.get("name"), a1));
          }
          if ((obj.containsKey("groups")) && (obj.get("groups").isNull() == null)) {
            entity.setGroups(java_util_List.demarshall(obj.get("groups"), a1));
          }
          if ((obj.containsKey("userStringMap")) && (obj.get("userStringMap").isNull() == null)) {
            entity.setUserStringMap(java_util_Map.demarshall(obj.get("userStringMap"), a1));
          }
          if ((obj.containsKey("userMapString")) && (obj.get("userMapString").isNull() == null)) {
            entity.setUserMapString(java_util_Map.demarshall(obj.get("userMapString"), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: org.jboss.errai.bus.client.tests.support.User", t);
        }
      }

      public String marshall(User a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.User\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.User\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"id\" : ").append(java_lang_Integer.marshall(a0.getId(), a1)).append(",").append("\"name\" : ").append(java_lang_String.marshall(a0.getName(), a1)).append(",").append("\"groups\" : ").append(java_util_List.marshall(a0.getGroups(), a1)).append(",").append("\"userStringMap\" : ").append(java_util_Map.marshall(a0.getUserStringMap(), a1)).append(",").append("\"userMapString\" : ").append(java_util_Map.marshall(a0.getUserMapString(), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("org.jboss.errai.bus.client.tests.support.User", org_jboss_errai_bus_client_tests_support_User);
    org_jboss_errai_bus_client_tests_support_ClassWithNestedClass_Nested = new Marshaller<JSONValue, Nested>() {
      public Class getTypeHandled() {
        return Nested.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public Nested demarshall(JSONValue a0, MarshallingSession a1) {
        try {
          JSONObject obj = a0.isObject();
          String objId = obj.get("__ObjectID").isString().stringValue();
          if (a1.hasObjectHash(objId)) {
            return a1.getObject(Nested.class, objId);
          }
          Nested entity = new Nested();
          a1.recordObjectHash(objId, entity);
          if ((obj.containsKey("field")) && (obj.get("field").isNull() == null)) {
            org_jboss_errai_bus_client_tests_support_ClassWithNestedClass$Nested_field(entity, java_lang_String.demarshall(obj.get("field"), a1));
          }
          return entity;
        } catch (Throwable t) {
          t.printStackTrace();
          throw new RuntimeException("error demarshalling entity: org.jboss.errai.bus.client.tests.support.ClassWithNestedClass$Nested", t);
        }
      }

      public String marshall(Nested a0, MarshallingSession a1) {
        if (a0 == null) {
          return "null";
        }
        String objId = String.valueOf(a0.hashCode());
        if (a1.hasObjectHash(objId)) {
          return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.ClassWithNestedClass$Nested\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"}").toString();
        }
        a1.recordObjectHash(objId, objId);
        return new StringBuilder().append("{").append("\"__EncodedType\":\"org.jboss.errai.bus.client.tests.support.ClassWithNestedClass$Nested\"").append(",").append("\"__ObjectID\":\"").append(objId).append("\"").append(",").append("\"field\" : ").append(java_lang_String.marshall(org_jboss_errai_bus_client_tests_support_ClassWithNestedClass$Nested_field(a0), a1)).append("}").toString();
      }

      public boolean handles(JSONValue a0) {
        return (a0.isObject() != null) && a0.isObject().get("__EncodedType").equals(this.getTypeHandled().getName());
      }


    };
    marshallers.put("org.jboss.errai.bus.client.tests.support.ClassWithNestedClass.Nested", org_jboss_errai_bus_client_tests_support_ClassWithNestedClass_Nested);
    arrayOf_java_lang_Object_D1 = new Marshaller<List, Object[]>() {
      public Class getTypeHandled() {
        return Object.class;
      }

      public String getEncodingType() {
        return "json";
      }

      public Object[] demarshall(List a0, MarshallingSession a1) {
        if (a0 == null) {
          return null;
        } else {
          return _demarshall1(a0, a1);
        }
      }

      public boolean handles(List a0) {
        return true;
      }

      public String marshall(Object[] a0, MarshallingSession a1) {
        if (a0 == null) {
          return null;
        } else {
          return _marshall1(a0, a1);
        }
      }

      private Object[] _demarshall1(List a0, MarshallingSession a1) {
        Object[] newArray = new Object[a0.size()];
        for (int i = 0; i < newArray.length; i++) {
          newArray[i] = a0.get(i);
        }
        return newArray;
      }

      private String _marshall1(Object[] a0, MarshallingSession a1) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < a0.length; i++) {
          if (i > 0) {
            sb.append(",");
          }
          sb.append(java_lang_Object.marshall(a0[i], a1));
        }
        return sb.append("]").toString();
      }

    };
    marshallers.put("[Ljava.lang.Object;", arrayOf_java_lang_Object_D1);
  }

  private native static void java_lang_Throwable_detailMessage(Throwable instance, String value) /*-{
    instance.@java.lang.Throwable::detailMessage = value;
  }-*/;

  private native static String java_lang_Throwable_detailMessage(Throwable instance) /*-{
    return instance.@java.lang.Throwable::detailMessage;
  }-*/;

  private native static void java_lang_Throwable_cause(Throwable instance, Throwable value) /*-{
    instance.@java.lang.Throwable::cause = value;
  }-*/;

  private native static Throwable java_lang_Throwable_cause(Throwable instance) /*-{
    return instance.@java.lang.Throwable::cause;
  }-*/;

  private native static void org_jboss_errai_bus_client_tests_support_SimpleEntity_active(SimpleEntity instance, boolean value) /*-{
    instance.@org.jboss.errai.bus.client.tests.support.SimpleEntity::active = value;
  }-*/;

  private native static boolean org_jboss_errai_bus_client_tests_support_SimpleEntity_active(SimpleEntity instance) /*-{
    return instance.@org.jboss.errai.bus.client.tests.support.SimpleEntity::active;
  }-*/;

  private native static void org_jboss_errai_bus_client_tests_support_AbstractEntity_selected(AbstractEntity instance, boolean value) /*-{
    instance.@org.jboss.errai.bus.client.tests.support.AbstractEntity::selected = value;
  }-*/;

  private native static boolean org_jboss_errai_bus_client_tests_support_AbstractEntity_selected(AbstractEntity instance) /*-{
    return instance.@org.jboss.errai.bus.client.tests.support.AbstractEntity::selected;
  }-*/;

  private native static void org_jboss_errai_bus_client_tests_support_AbstractEntity_deleted(AbstractEntity instance, boolean value) /*-{
    instance.@org.jboss.errai.bus.client.tests.support.AbstractEntity::deleted = value;
  }-*/;

  private native static boolean org_jboss_errai_bus_client_tests_support_AbstractEntity_deleted(AbstractEntity instance) /*-{
    return instance.@org.jboss.errai.bus.client.tests.support.AbstractEntity::deleted;
  }-*/;

  private native static void org_jboss_errai_bus_client_tests_support_ClassWithNestedClass$Nested_field(Nested instance, String value) /*-{
    instance.@org.jboss.errai.bus.client.tests.support.ClassWithNestedClass.Nested::field = value;
  }-*/;

  private native static String org_jboss_errai_bus_client_tests_support_ClassWithNestedClass$Nested_field(Nested instance) /*-{
    return instance.@org.jboss.errai.bus.client.tests.support.ClassWithNestedClass.Nested::field;
  }-*/;

  public Marshaller getMarshaller(String a0, String a1) {
    return marshallers.get(a1);
  }
}