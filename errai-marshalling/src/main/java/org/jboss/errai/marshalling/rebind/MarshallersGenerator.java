package org.jboss.errai.marshalling.rebind;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

import java.io.PrintWriter;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallersGenerator extends Generator {
  /**
   * Simple name of class to be generated
   */
  private String className = null;

  /**
   * Package name of class to be generated
   */
  private String packageName = null;
  private TypeOracle typeOracle;
  private String modulePackage;

  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName) throws UnableToCompleteException {
    try {
      typeOracle = context.getTypeOracle();

      JClassType classType = typeOracle.getType(typeName);
      packageName = classType.getPackage().getName();
      className = classType.getSimpleSourceName() + "Impl";

      logger.log(TreeLogger.INFO, "Generating Marshallers Bootstrapper...");

      // Generate class source code
      generateMarshallerBootstrapper(logger, context);
    }
    catch (Throwable e) {
      // record sendNowWith logger that Map generation threw an exception
      e.printStackTrace();
      logger.log(TreeLogger.ERROR, "Error generating marshallers", e);
    }

    System.out.println("**FIN**");

    // return the fully qualified name of the class generated
    return packageName + "." + className;
  }

  public void generateMarshallerBootstrapper(TreeLogger logger, GeneratorContext context) {
    PrintWriter printWriter = context.tryCreate(logger, packageName, className);
   // printWriter.write(new MarshallerGeneratorFactory().generate(packageName, className));
    printWriter.write("package org.jboss.errai.marshalling.client.api;\n" +
            "\n" +
            "import org.jboss.errai.marshalling.client.api.MarshallerFactory;\n" +
            "import java.util.Map;\n" +
            "import org.jboss.errai.marshalling.client.api.Marshaller;\n" +
            "import java.util.HashMap;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.ObjectMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.MapMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.BooleanMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.ListMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.LongMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.IntegerMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.StringMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.ShortMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.DateMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.FloatMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.SQLDateMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.DoubleMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.CharacterMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.SetMarshaller;\n" +
            "import org.jboss.errai.marshalling.client.marshallers.ByteMarshaller;\n" +
            "import com.google.gwt.json.client.JSONValue;\n" +
            "import org.jboss.errai.bus.client.tests.support.Group;\n" +
            "import org.jboss.errai.bus.client.tests.support.User;\n" +
            "import java.util.List;\n" +
            "import org.jboss.errai.bus.client.tests.support.SType;\n" +
            "import org.jboss.errai.bus.client.tests.support.TestException;\n" +
            "import org.jboss.errai.bus.client.tests.support.TreeNodeContainer;\n" +
            "import org.jboss.errai.bus.client.tests.support.StudyTreeNodeContainer;\n" +
            "import org.jboss.errai.bus.client.tests.support.SimpleEntity;\n" +
            "import org.jboss.errai.marshalling.client.api.MarshallingSession;\n" +
            "import org.jboss.errai.marshalling.client.util.MarshallUtil;\n" +
            "import com.google.gwt.json.client.JSONObject;\n" +
            "import org.jboss.errai.bus.client.tests.support.SType.Place;\n" +
            "import org.jboss.errai.marshalling.client.api.MarshallerFactoryImpl;\n" +
            "import org.jboss.errai.bus.client.tests.support.AbstractEntity;\n" +
            "\n" +
            "public class MarshallerFactoryImpl implements MarshallerFactory {\n" +
            "  private Map<String, Marshaller> marshallers = new HashMap<String, Marshaller>();\n" +
            "  private ObjectMarshaller java_lang_Object;\n" +
            "  private ObjectMarshaller arrayOf_java_lang_Object;\n" +
            "  private MapMarshaller java_util_Map;\n" +
            "  private MapMarshaller arrayOf_java_util_Map;\n" +
            "  private BooleanMarshaller java_lang_Boolean;\n" +
            "  private BooleanMarshaller arrayOf_java_lang_Boolean;\n" +
            "  private ListMarshaller java_util_List;\n" +
            "  private ListMarshaller arrayOf_java_util_List;\n" +
            "  private LongMarshaller java_lang_Long;\n" +
            "  private LongMarshaller arrayOf_java_lang_Long;\n" +
            "  private IntegerMarshaller java_lang_Integer;\n" +
            "  private IntegerMarshaller arrayOf_java_lang_Integer;\n" +
            "  private StringMarshaller java_lang_String;\n" +
            "  private StringMarshaller arrayOf_java_lang_String;\n" +
            "  private ShortMarshaller java_lang_Short;\n" +
            "  private ShortMarshaller arrayOf_java_lang_Short;\n" +
            "  private DateMarshaller java_util_Date;\n" +
            "  private DateMarshaller arrayOf_java_util_Date;\n" +
            "  private FloatMarshaller java_lang_Float;\n" +
            "  private FloatMarshaller arrayOf_java_lang_Float;\n" +
            "  private SQLDateMarshaller java_sql_Date;\n" +
            "  private SQLDateMarshaller arrayOf_java_sql_Date;\n" +
            "  private DoubleMarshaller java_lang_Double;\n" +
            "  private DoubleMarshaller arrayOf_java_lang_Double;\n" +
            "  private CharacterMarshaller java_lang_Character;\n" +
            "  private CharacterMarshaller arrayOf_java_lang_Character;\n" +
            "  private SetMarshaller java_util_Set;\n" +
            "  private SetMarshaller arrayOf_java_util_Set;\n" +
            "  private ByteMarshaller java_lang_Byte;\n" +
            "  private ByteMarshaller arrayOf_java_lang_Byte;\n" +
            "  private Marshaller<JSONValue, Group> org_jboss_errai_bus_client_tests_support_Group;\n" +
            "  private Marshaller<JSONValue, User> org_jboss_errai_bus_client_tests_support_User;\n" +
            "  private Marshaller<JSONValue, StackTraceElement> java_lang_StackTraceElement;\n" +
            "  private Marshaller<List, char[]> arrayOf_char_D1;\n" +
            "  private Marshaller<List, char[][]> arrayOf_char_D2;\n" +
            "  private Marshaller<List, SType[]> arrayOf_org_jboss_errai_bus_client_tests_support_SType_D1;\n" +
            "  private Marshaller<JSONValue, SType> org_jboss_errai_bus_client_tests_support_SType;\n" +
            "  private Marshaller<List, StackTraceElement[]> arrayOf_java_lang_StackTraceElement_D1;\n" +
            "  private Marshaller<JSONValue, TestException> org_jboss_errai_bus_client_tests_support_TestException;\n" +
            "  private Marshaller<JSONValue, TreeNodeContainer> org_jboss_errai_bus_client_tests_support_TreeNodeContainer;\n" +
            "  private Marshaller<JSONValue, Exception> java_lang_Exception;\n" +
            "  private Marshaller<JSONValue, StudyTreeNodeContainer> org_jboss_errai_bus_client_tests_support_StudyTreeNodeContainer;\n" +
            "  private Marshaller<JSONValue, RuntimeException> java_lang_RuntimeException;\n" +
            "  private Marshaller<JSONValue, Throwable> java_lang_Throwable;\n" +
            "  private Marshaller<JSONValue, SimpleEntity> org_jboss_errai_bus_client_tests_support_SimpleEntity;\n" +
            "  private Marshaller<List, Object[]> arrayOf_java_lang_Object_D1;\n" +
            "  public MarshallerFactoryImpl() {\n" +
            "    java_lang_Object = new ObjectMarshaller();\n" +
            "    marshallers.put(\"java.lang.Object\", java_lang_Object);\n" +
            "    java_util_Map = new MapMarshaller();\n" +
            "    marshallers.put(\"java.util.Map\", java_util_Map);\n" +
            "    marshallers.put(\"java.util.AbstractMap\", java_util_Map);\n" +
            "    marshallers.put(\"java.util.HashMap\", java_util_Map);\n" +
            "    marshallers.put(\"java.util.LinkedHashMap\", java_util_Map);\n" +
            "    java_lang_Boolean = new BooleanMarshaller();\n" +
            "    marshallers.put(\"java.lang.Boolean\", java_lang_Boolean);\n" +
            "    java_util_List = new ListMarshaller();\n" +
            "    marshallers.put(\"java.util.List\", java_util_List);\n" +
            "    marshallers.put(\"java.util.AbstractList\", java_util_List);\n" +
            "    marshallers.put(\"java.util.ArrayList\", java_util_List);\n" +
            "    marshallers.put(\"java.util.LinkedList\", java_util_List);\n" +
            "    java_lang_Long = new LongMarshaller();\n" +
            "    marshallers.put(\"java.lang.Long\", java_lang_Long);\n" +
            "    java_lang_Integer = new IntegerMarshaller();\n" +
            "    marshallers.put(\"java.lang.Integer\", java_lang_Integer);\n" +
            "    java_lang_String = new StringMarshaller();\n" +
            "    marshallers.put(\"java.lang.String\", java_lang_String);\n" +
            "    java_lang_Short = new ShortMarshaller();\n" +
            "    marshallers.put(\"java.lang.Short\", java_lang_Short);\n" +
            "    java_util_Date = new DateMarshaller();\n" +
            "    marshallers.put(\"java.util.Date\", java_util_Date);\n" +
            "    java_lang_Float = new FloatMarshaller();\n" +
            "    marshallers.put(\"java.lang.Float\", java_lang_Float);\n" +
            "    java_sql_Date = new SQLDateMarshaller();\n" +
            "    marshallers.put(\"java.sql.Date\", java_sql_Date);\n" +
            "    java_lang_Double = new DoubleMarshaller();\n" +
            "    marshallers.put(\"java.lang.Double\", java_lang_Double);\n" +
            "    java_lang_Character = new CharacterMarshaller();\n" +
            "    marshallers.put(\"java.lang.Character\", java_lang_Character);\n" +
            "    java_util_Set = new SetMarshaller();\n" +
            "    marshallers.put(\"java.util.Set\", java_util_Set);\n" +
            "    marshallers.put(\"java.util.AbstractSet\", java_util_Set);\n" +
            "    marshallers.put(\"java.util.HashSet\", java_util_Set);\n" +
            "    marshallers.put(\"java.util.SortedSet\", java_util_Set);\n" +
            "    marshallers.put(\"java.util.LinkedHashSet\", java_util_Set);\n" +
            "    java_lang_Byte = new ByteMarshaller();\n" +
            "    marshallers.put(\"java.lang.Byte\", java_lang_Byte);\n" +
            "    org_jboss_errai_bus_client_tests_support_Group = new Marshaller<JSONValue, Group>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return Group.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public Group demarshall(JSONValue a0, MarshallingSession a1) {\n" +
            "        try {\n" +
            "          if ((a0 == null) || (a0.isNull() != null)) {\n" +
            "            return null;\n" +
            "          }\n" +
            "          Group entity = new Group();\n" +
            "          entity.setGroupId(java_lang_Integer.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"groupId\"), a1));\n" +
            "          entity.setName(java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"name\"), a1));\n" +
            "          entity.setUsersInGroup(java_util_List.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"usersInGroup\"), a1));\n" +
            "          entity.setSubGroup(org_jboss_errai_bus_client_tests_support_Group.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"subGroup\"), a1));\n" +
            "          entity.setGroupUserMap(java_util_Map.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"groupUserMap\"), a1));\n" +
            "          return entity;\n" +
            "        } catch (Throwable t) {\n" +
            "          t.printStackTrace();\n" +
            "          return null;\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(Group a0, MarshallingSession a1) {\n" +
            "        return new StringBuilder().append(\"{\").append(\"\\\"__EncodedType\\\":\\\"org.jboss.errai.bus.client.tests.support.Group\\\"\").append(\",\").append(\"\\\"__ObjectID\\\"\").append(\":\").append(a0.hashCode()).append(\",\").append(\"\\\"groupId\\\" : \").append(java_lang_Integer.marshall(a0.getGroupId(), a1)).append(\",\").append(\"\\\"name\\\" : \").append(java_lang_String.marshall(a0.getName(), a1)).append(\",\").append(\"\\\"usersInGroup\\\" : \").append(java_util_List.marshall(a0.getUsersInGroup(), a1)).append(\",\").append(\"\\\"subGroup\\\" : \").append(org_jboss_errai_bus_client_tests_support_Group.marshall(a0.getSubGroup(), a1)).append(\",\").append(\"\\\"groupUserMap\\\" : \").append(java_util_Map.marshall(a0.getGroupUserMap(), a1)).append(\"}\").toString();\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(JSONValue a0) {\n" +
            "        return (a0.isObject() != null) && a0.isObject().get(\"__EncodedType\").equals(this.getTypeHandled().getName());\n" +
            "      }\n" +
            "\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"org.jboss.errai.bus.client.tests.support.Group\", org_jboss_errai_bus_client_tests_support_Group);\n" +
            "    org_jboss_errai_bus_client_tests_support_User = new Marshaller<JSONValue, User>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return User.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public User demarshall(JSONValue a0, MarshallingSession a1) {\n" +
            "        try {\n" +
            "          if ((a0 == null) || (a0.isNull() != null)) {\n" +
            "            return null;\n" +
            "          }\n" +
            "          User entity = new User();\n" +
            "          entity.setId(java_lang_Integer.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"id\"), a1));\n" +
            "          entity.setName(java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"name\"), a1));\n" +
            "          entity.setGroups(java_util_List.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"groups\"), a1));\n" +
            "          entity.setUserStringMap(java_util_Map.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"userStringMap\"), a1));\n" +
            "          entity.setUserMapString(java_util_Map.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"userMapString\"), a1));\n" +
            "          return entity;\n" +
            "        } catch (Throwable t) {\n" +
            "          t.printStackTrace();\n" +
            "          return null;\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(User a0, MarshallingSession a1) {\n" +
            "        return new StringBuilder().append(\"{\").append(\"\\\"__EncodedType\\\":\\\"org.jboss.errai.bus.client.tests.support.User\\\"\").append(\",\").append(\"\\\"__ObjectID\\\"\").append(\":\").append(a0.hashCode()).append(\",\").append(\"\\\"id\\\" : \").append(java_lang_Integer.marshall(a0.getId(), a1)).append(\",\").append(\"\\\"name\\\" : \").append(java_lang_String.marshall(a0.getName(), a1)).append(\",\").append(\"\\\"groups\\\" : \").append(java_util_List.marshall(a0.getGroups(), a1)).append(\",\").append(\"\\\"userStringMap\\\" : \").append(java_util_Map.marshall(a0.getUserStringMap(), a1)).append(\",\").append(\"\\\"userMapString\\\" : \").append(java_util_Map.marshall(a0.getUserMapString(), a1)).append(\"}\").toString();\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(JSONValue a0) {\n" +
            "        return (a0.isObject() != null) && a0.isObject().get(\"__EncodedType\").equals(this.getTypeHandled().getName());\n" +
            "      }\n" +
            "\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"org.jboss.errai.bus.client.tests.support.User\", org_jboss_errai_bus_client_tests_support_User);\n" +
            "    java_lang_StackTraceElement = new Marshaller<JSONValue, StackTraceElement>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return StackTraceElement.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public StackTraceElement demarshall(JSONValue a0, MarshallingSession a1) {\n" +
            "        return new StackTraceElement(java_lang_String.demarshall(((JSONObject) a0).get(\"declaringClass\"), a1), java_lang_String.demarshall(((JSONObject) a0).get(\"methodName\"), a1), java_lang_String.demarshall(((JSONObject) a0).get(\"fileName\"), a1), java_lang_Integer.demarshall(((JSONObject) a0).get(\"lineNumber\"), a1));\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(StackTraceElement a0, MarshallingSession a1) {\n" +
            "        return null;\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(JSONValue a0) {\n" +
            "        return (a0.isObject() != null) && a0.isObject().get(\"__EncodedType\").equals(this.getTypeHandled().getName());\n" +
            "      }\n" +
            "\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"java.lang.StackTraceElement\", java_lang_StackTraceElement);\n" +
            "    arrayOf_char_D1 = new Marshaller<List, char[]>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return char.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public char[] demarshall(List a0, MarshallingSession a1) {\n" +
            "        if (a0 == null) {\n" +
            "          return null;\n" +
            "        } else {\n" +
            "          return _demarshall1(a0, a1);\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(List a0) {\n" +
            "        return true;\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(char[] a0, MarshallingSession a1) {\n" +
            "        if (a0 == null) {\n" +
            "          return null;\n" +
            "        } else {\n" +
            "          return _marshall1(a0, a1);\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      private char[] _demarshall1(List a0, MarshallingSession a1) {\n" +
            "        char[] newArray = new char[a0.size()];\n" +
            "        for (int i = 0; i < newArray.length; i++) {\n" +
            "          newArray[i] = ((String) a0.get(i)).charAt(0);\n" +
            "        }\n" +
            "        return newArray;\n" +
            "      }\n" +
            "\n" +
            "      private String _marshall1(char[] a0, MarshallingSession a1) {\n" +
            "        StringBuilder sb = new StringBuilder(\"[\");\n" +
            "        for (int i = 0; i < a0.length; i++) {\n" +
            "          if (i > 0) {\n" +
            "            sb.append(\",\");\n" +
            "          }\n" +
            "          sb.append(java_lang_Character.marshall(a0[i], a1));\n" +
            "        }\n" +
            "        return sb.append(\"]\").toString();\n" +
            "      }\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"[C\", arrayOf_char_D1);\n" +
            "    arrayOf_char_D2 = new Marshaller<List, char[][]>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return char.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public char[][] demarshall(List a0, MarshallingSession a1) {\n" +
            "        if (a0 == null) {\n" +
            "          return null;\n" +
            "        } else {\n" +
            "          return _demarshall2(a0, a1);\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(List a0) {\n" +
            "        return true;\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(char[][] a0, MarshallingSession a1) {\n" +
            "        if (a0 == null) {\n" +
            "          return null;\n" +
            "        } else {\n" +
            "          return _marshall2(a0, a1);\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      private char[][] _demarshall2(List a0, MarshallingSession a1) {\n" +
            "        char[][] newArray = new char[a0.size()][];\n" +
            "        for (int i = 0; i < newArray.length; i++) {\n" +
            "          newArray[i] = _demarshall1((List) a0.get(i), a1);\n" +
            "        }\n" +
            "        return newArray;\n" +
            "      }\n" +
            "\n" +
            "      private String _marshall2(char[][] a0, MarshallingSession a1) {\n" +
            "        StringBuilder sb = new StringBuilder(\"[\");\n" +
            "        for (int i = 0; i < a0.length; i++) {\n" +
            "          if (i > 0) {\n" +
            "            sb.append(\",\");\n" +
            "          }\n" +
            "          sb.append(_marshall1(a0[i], a1));\n" +
            "        }\n" +
            "        return sb.append(\"]\").toString();\n" +
            "      }\n" +
            "\n" +
            "      private char[] _demarshall1(List a0, MarshallingSession a1) {\n" +
            "        char[] newArray = new char[a0.size()];\n" +
            "        for (int i = 0; i < newArray.length; i++) {\n" +
            "          newArray[i] = ((String) a0.get(i)).charAt(0);\n" +
            "        }\n" +
            "        return newArray;\n" +
            "      }\n" +
            "\n" +
            "      private String _marshall1(char[] a0, MarshallingSession a1) {\n" +
            "        StringBuilder sb = new StringBuilder(\"[\");\n" +
            "        for (int i = 0; i < a0.length; i++) {\n" +
            "          if (i > 0) {\n" +
            "            sb.append(\",\");\n" +
            "          }\n" +
            "          sb.append(java_lang_Character.marshall(a0[i], a1));\n" +
            "        }\n" +
            "        return sb.append(\"]\").toString();\n" +
            "      }\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"[[C\", arrayOf_char_D2);\n" +
            "    arrayOf_org_jboss_errai_bus_client_tests_support_SType_D1 = new Marshaller<List, SType[]>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return SType.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public SType[] demarshall(List a0, MarshallingSession a1) {\n" +
            "        if (a0 == null) {\n" +
            "          return null;\n" +
            "        } else {\n" +
            "          return _demarshall1(a0, a1);\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(List a0) {\n" +
            "        return true;\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(SType[] a0, MarshallingSession a1) {\n" +
            "        if (a0 == null) {\n" +
            "          return null;\n" +
            "        } else {\n" +
            "          return _marshall1(a0, a1);\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      private SType[] _demarshall1(List a0, MarshallingSession a1) {\n" +
            "        SType[] newArray = new SType[a0.size()];\n" +
            "        for (int i = 0; i < newArray.length; i++) {\n" +
            "          newArray[i] = (SType) a0.get(i);\n" +
            "        }\n" +
            "        return newArray;\n" +
            "      }\n" +
            "\n" +
            "      private String _marshall1(SType[] a0, MarshallingSession a1) {\n" +
            "        StringBuilder sb = new StringBuilder(\"[\");\n" +
            "        for (int i = 0; i < a0.length; i++) {\n" +
            "          if (i > 0) {\n" +
            "            sb.append(\",\");\n" +
            "          }\n" +
            "          sb.append(org_jboss_errai_bus_client_tests_support_SType.marshall(a0[i], a1));\n" +
            "        }\n" +
            "        return sb.append(\"]\").toString();\n" +
            "      }\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"[Lorg.jboss.errai.bus.client.tests.support.SType;\", arrayOf_org_jboss_errai_bus_client_tests_support_SType_D1);\n" +
            "    org_jboss_errai_bus_client_tests_support_SType = new Marshaller<JSONValue, SType>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return SType.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public SType demarshall(JSONValue a0, MarshallingSession a1) {\n" +
            "        try {\n" +
            "          if ((a0 == null) || (a0.isNull() != null)) {\n" +
            "            return null;\n" +
            "          }\n" +
            "          SType entity = new SType();\n" +
            "          entity.setFieldOne(java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"fieldOne\"), a1));\n" +
            "          entity.setFieldTwo(java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"fieldTwo\"), a1));\n" +
            "          entity.setStartDate(java_util_Date.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"startDate\"), a1));\n" +
            "          entity.setEndDate(java_util_Date.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"endDate\"), a1));\n" +
            "          entity.setActive(java_lang_Boolean.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"active\"), a1));\n" +
            "          entity.setListOfStypes(java_util_List.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"listOfStypes\"), a1));\n" +
            "          entity.setListOfDates(java_util_List.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"listOfDates\"), a1));\n" +
            "          entity.setMapofStypes(java_util_Map.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"mapofStypes\"), a1));\n" +
            "          entity.setsTypeToStype(java_util_Map.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"sTypeToStype\"), a1));\n" +
            "          entity.setPlace(Enum.valueOf(Place.class, MarshallUtil.nullSafe_JSONObject(a0, \"place\").isObject().get(\"EnumStringValue\").isString().stringValue()));\n" +
            "          entity.setLongValue(java_lang_Long.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"longValue\"), a1));\n" +
            "          entity.setIntValue(java_lang_Integer.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"intValue\"), a1));\n" +
            "          entity.setShortValue(java_lang_Short.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"shortValue\"), a1));\n" +
            "          entity.setDoubleValue(java_lang_Double.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"doubleValue\"), a1));\n" +
            "          entity.setFloatValue(java_lang_Float.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"floatValue\"), a1));\n" +
            "          entity.setByteValue(java_lang_Byte.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"byteValue\"), a1));\n" +
            "          entity.setCharValue(java_lang_Character.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"charValue\"), a1));\n" +
            "          entity.setCharArray(arrayOf_char_D1.demarshall(java_util_List.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"charArray\"), a1), a1));\n" +
            "          entity.setCharArrayMulti(arrayOf_char_D2.demarshall(java_util_List.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"charArrayMulti\"), a1), a1));\n" +
            "          entity.setsTypeArray(arrayOf_org_jboss_errai_bus_client_tests_support_SType_D1.demarshall(java_util_List.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"sTypeArray\"), a1), a1));\n" +
            "          entity.setSuperValue(java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"superValue\"), a1));\n" +
            "          return entity;\n" +
            "        } catch (Throwable t) {\n" +
            "          t.printStackTrace();\n" +
            "          return null;\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(SType a0, MarshallingSession a1) {\n" +
            "        return new StringBuilder().append(\"{\").append(\"\\\"__EncodedType\\\":\\\"org.jboss.errai.bus.client.tests.support.SType\\\"\").append(\",\").append(\"\\\"__ObjectID\\\"\").append(\":\").append(a0.hashCode()).append(\",\").append(\"\\\"fieldOne\\\" : \").append(java_lang_String.marshall(a0.getFieldOne(), a1)).append(\",\").append(\"\\\"fieldTwo\\\" : \").append(java_lang_String.marshall(a0.getFieldTwo(), a1)).append(\",\").append(\"\\\"startDate\\\" : \").append(java_util_Date.marshall(a0.getStartDate(), a1)).append(\",\").append(\"\\\"endDate\\\" : \").append(java_util_Date.marshall(a0.getEndDate(), a1)).append(\",\").append(\"\\\"active\\\" : \").append(java_lang_Boolean.marshall(a0.getActive(), a1)).append(\",\").append(\"\\\"listOfStypes\\\" : \").append(java_util_List.marshall(a0.getListOfStypes(), a1)).append(\",\").append(\"\\\"listOfDates\\\" : \").append(java_util_List.marshall(a0.getListOfDates(), a1)).append(\",\").append(\"\\\"mapofStypes\\\" : \").append(java_util_Map.marshall(a0.getMapofStypes(), a1)).append(\",\").append(\"\\\"sTypeToStype\\\" : \").append(java_util_Map.marshall(a0.getsTypeToStype(), a1)).append(\",\").append(\"\\\"place\\\" : \").append(\"{\\\"__EncodedType\\\":\\\"org.jboss.errai.bus.client.tests.support.SType$Place\\\",\\\"EnumStringValue\\\":\\\"\").append(a0.getPlace().toString()).append(\"\\\"}\").append(\",\").append(\"\\\"longValue\\\" : \").append(java_lang_Long.marshall(a0.getLongValue(), a1)).append(\",\").append(\"\\\"intValue\\\" : \").append(java_lang_Integer.marshall(a0.getIntValue(), a1)).append(\",\").append(\"\\\"shortValue\\\" : \").append(java_lang_Short.marshall(a0.getShortValue(), a1)).append(\",\").append(\"\\\"doubleValue\\\" : \").append(java_lang_Double.marshall(a0.getDoubleValue(), a1)).append(\",\").append(\"\\\"floatValue\\\" : \").append(java_lang_Float.marshall(a0.getFloatValue(), a1)).append(\",\").append(\"\\\"byteValue\\\" : \").append(java_lang_Byte.marshall(a0.getByteValue(), a1)).append(\",\").append(\"\\\"charValue\\\" : \").append(java_lang_Character.marshall(a0.getCharValue(), a1)).append(\",\").append(\"\\\"charArray\\\" : \").append(arrayOf_char_D1.marshall(a0.getCharArray(), a1)).append(\",\").append(\"\\\"charArrayMulti\\\" : \").append(arrayOf_char_D2.marshall(a0.getCharArrayMulti(), a1)).append(\",\").append(\"\\\"sTypeArray\\\" : \").append(arrayOf_org_jboss_errai_bus_client_tests_support_SType_D1.marshall(a0.getsTypeArray(), a1)).append(\",\").append(\"\\\"superValue\\\" : \").append(java_lang_String.marshall(a0.getSuperValue(), a1)).append(\"}\").toString();\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(JSONValue a0) {\n" +
            "        return (a0.isObject() != null) && a0.isObject().get(\"__EncodedType\").equals(this.getTypeHandled().getName());\n" +
            "      }\n" +
            "\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"org.jboss.errai.bus.client.tests.support.SType\", org_jboss_errai_bus_client_tests_support_SType);\n" +
            "    arrayOf_java_lang_StackTraceElement_D1 = new Marshaller<List, StackTraceElement[]>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return StackTraceElement.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public StackTraceElement[] demarshall(List a0, MarshallingSession a1) {\n" +
            "        if (a0 == null) {\n" +
            "          return null;\n" +
            "        } else {\n" +
            "          return _demarshall1(a0, a1);\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(List a0) {\n" +
            "        return true;\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(StackTraceElement[] a0, MarshallingSession a1) {\n" +
            "        if (a0 == null) {\n" +
            "          return null;\n" +
            "        } else {\n" +
            "          return _marshall1(a0, a1);\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      private StackTraceElement[] _demarshall1(List a0, MarshallingSession a1) {\n" +
            "        StackTraceElement[] newArray = new StackTraceElement[a0.size()];\n" +
            "        for (int i = 0; i < newArray.length; i++) {\n" +
            "          newArray[i] = (StackTraceElement) a0.get(i);\n" +
            "        }\n" +
            "        return newArray;\n" +
            "      }\n" +
            "\n" +
            "      private String _marshall1(StackTraceElement[] a0, MarshallingSession a1) {\n" +
            "        StringBuilder sb = new StringBuilder(\"[\");\n" +
            "        for (int i = 0; i < a0.length; i++) {\n" +
            "          if (i > 0) {\n" +
            "            sb.append(\",\");\n" +
            "          }\n" +
            "          sb.append(java_lang_StackTraceElement.marshall(a0[i], a1));\n" +
            "        }\n" +
            "        return sb.append(\"]\").toString();\n" +
            "      }\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"[Ljava.lang.StackTraceElement;\", arrayOf_java_lang_StackTraceElement_D1);\n" +
            "    org_jboss_errai_bus_client_tests_support_TestException = new Marshaller<JSONValue, TestException>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return TestException.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public TestException demarshall(JSONValue a0, MarshallingSession a1) {\n" +
            "        try {\n" +
            "          if ((a0 == null) || (a0.isNull() != null)) {\n" +
            "            return null;\n" +
            "          }\n" +
            "          TestException entity = new TestException();\n" +
            "          java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"detailMessage\"), a1));\n" +
            "          java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"cause\"), a1));\n" +
            "          entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"stackTrace\"), a1), a1));\n" +
            "          return entity;\n" +
            "        } catch (Throwable t) {\n" +
            "          t.printStackTrace();\n" +
            "          return null;\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(TestException a0, MarshallingSession a1) {\n" +
            "        return new StringBuilder().append(\"{\").append(\"\\\"__EncodedType\\\":\\\"org.jboss.errai.bus.client.tests.support.TestException\\\"\").append(\",\").append(\"\\\"__ObjectID\\\"\").append(\":\").append(a0.hashCode()).append(\",\").append(\"\\\"detailMessage\\\" : \").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(\",\").append(\"\\\"cause\\\" : \").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(\",\").append(\"\\\"stackTrace\\\" : \").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append(\"}\").toString();\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(JSONValue a0) {\n" +
            "        return (a0.isObject() != null) && a0.isObject().get(\"__EncodedType\").equals(this.getTypeHandled().getName());\n" +
            "      }\n" +
            "\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"org.jboss.errai.bus.client.tests.support.TestException\", org_jboss_errai_bus_client_tests_support_TestException);\n" +
            "    org_jboss_errai_bus_client_tests_support_TreeNodeContainer = new Marshaller<JSONValue, TreeNodeContainer>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return TreeNodeContainer.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public TreeNodeContainer demarshall(JSONValue a0, MarshallingSession a1) {\n" +
            "        try {\n" +
            "          if ((a0 == null) || (a0.isNull() != null)) {\n" +
            "            return null;\n" +
            "          }\n" +
            "          TreeNodeContainer entity = new TreeNodeContainer();\n" +
            "          entity.setNodeId(java_lang_Integer.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"nodeId\"), a1));\n" +
            "          entity.setNodeName(java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"nodeName\"), a1));\n" +
            "          entity.setParentNodeId(java_lang_Integer.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"parentNodeId\"), a1));\n" +
            "          return entity;\n" +
            "        } catch (Throwable t) {\n" +
            "          t.printStackTrace();\n" +
            "          return null;\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(TreeNodeContainer a0, MarshallingSession a1) {\n" +
            "        return new StringBuilder().append(\"{\").append(\"\\\"__EncodedType\\\":\\\"org.jboss.errai.bus.client.tests.support.TreeNodeContainer\\\"\").append(\",\").append(\"\\\"__ObjectID\\\"\").append(\":\").append(a0.hashCode()).append(\",\").append(\"\\\"nodeId\\\" : \").append(java_lang_Integer.marshall(a0.getNodeId(), a1)).append(\",\").append(\"\\\"nodeName\\\" : \").append(java_lang_String.marshall(a0.getNodeName(), a1)).append(\",\").append(\"\\\"parentNodeId\\\" : \").append(java_lang_Integer.marshall(a0.getParentNodeId(), a1)).append(\"}\").toString();\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(JSONValue a0) {\n" +
            "        return (a0.isObject() != null) && a0.isObject().get(\"__EncodedType\").equals(this.getTypeHandled().getName());\n" +
            "      }\n" +
            "\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"org.jboss.errai.bus.client.tests.support.TreeNodeContainer\", org_jboss_errai_bus_client_tests_support_TreeNodeContainer);\n" +
            "    java_lang_Exception = new Marshaller<JSONValue, Exception>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return Exception.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public Exception demarshall(JSONValue a0, MarshallingSession a1) {\n" +
            "        try {\n" +
            "          if ((a0 == null) || (a0.isNull() != null)) {\n" +
            "            return null;\n" +
            "          }\n" +
            "          Exception entity = new Exception();\n" +
            "          java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"detailMessage\"), a1));\n" +
            "          java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"cause\"), a1));\n" +
            "          entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"stackTrace\"), a1), a1));\n" +
            "          return entity;\n" +
            "        } catch (Throwable t) {\n" +
            "          t.printStackTrace();\n" +
            "          return null;\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(Exception a0, MarshallingSession a1) {\n" +
            "        return new StringBuilder().append(\"{\").append(\"\\\"__EncodedType\\\":\\\"java.lang.Exception\\\"\").append(\",\").append(\"\\\"__ObjectID\\\"\").append(\":\").append(a0.hashCode()).append(\",\").append(\"\\\"detailMessage\\\" : \").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(\",\").append(\"\\\"cause\\\" : \").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(\",\").append(\"\\\"stackTrace\\\" : \").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append(\"}\").toString();\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(JSONValue a0) {\n" +
            "        return (a0.isObject() != null) && a0.isObject().get(\"__EncodedType\").equals(this.getTypeHandled().getName());\n" +
            "      }\n" +
            "\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"java.lang.Exception\", java_lang_Exception);\n" +
            "    org_jboss_errai_bus_client_tests_support_StudyTreeNodeContainer = new Marshaller<JSONValue, StudyTreeNodeContainer>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return StudyTreeNodeContainer.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public StudyTreeNodeContainer demarshall(JSONValue a0, MarshallingSession a1) {\n" +
            "        try {\n" +
            "          if ((a0 == null) || (a0.isNull() != null)) {\n" +
            "            return null;\n" +
            "          }\n" +
            "          StudyTreeNodeContainer entity = new StudyTreeNodeContainer();\n" +
            "          entity.setStudyId(java_lang_Integer.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"studyId\"), a1));\n" +
            "          entity.setNodeId(java_lang_Integer.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"nodeId\"), a1));\n" +
            "          entity.setNodeName(java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"nodeName\"), a1));\n" +
            "          entity.setParentNodeId(java_lang_Integer.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"parentNodeId\"), a1));\n" +
            "          return entity;\n" +
            "        } catch (Throwable t) {\n" +
            "          t.printStackTrace();\n" +
            "          return null;\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(StudyTreeNodeContainer a0, MarshallingSession a1) {\n" +
            "        return new StringBuilder().append(\"{\").append(\"\\\"__EncodedType\\\":\\\"org.jboss.errai.bus.client.tests.support.StudyTreeNodeContainer\\\"\").append(\",\").append(\"\\\"__ObjectID\\\"\").append(\":\").append(a0.hashCode()).append(\",\").append(\"\\\"studyId\\\" : \").append(java_lang_Integer.marshall(a0.getStudyId(), a1)).append(\",\").append(\"\\\"nodeId\\\" : \").append(java_lang_Integer.marshall(a0.getNodeId(), a1)).append(\",\").append(\"\\\"nodeName\\\" : \").append(java_lang_String.marshall(a0.getNodeName(), a1)).append(\",\").append(\"\\\"parentNodeId\\\" : \").append(java_lang_Integer.marshall(a0.getParentNodeId(), a1)).append(\"}\").toString();\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(JSONValue a0) {\n" +
            "        return (a0.isObject() != null) && a0.isObject().get(\"__EncodedType\").equals(this.getTypeHandled().getName());\n" +
            "      }\n" +
            "\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"org.jboss.errai.bus.client.tests.support.StudyTreeNodeContainer\", org_jboss_errai_bus_client_tests_support_StudyTreeNodeContainer);\n" +
            "    java_lang_RuntimeException = new Marshaller<JSONValue, RuntimeException>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return RuntimeException.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public RuntimeException demarshall(JSONValue a0, MarshallingSession a1) {\n" +
            "        try {\n" +
            "          if ((a0 == null) || (a0.isNull() != null)) {\n" +
            "            return null;\n" +
            "          }\n" +
            "          RuntimeException entity = new RuntimeException();\n" +
            "          java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"detailMessage\"), a1));\n" +
            "          java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"cause\"), a1));\n" +
            "          entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"stackTrace\"), a1), a1));\n" +
            "          return entity;\n" +
            "        } catch (Throwable t) {\n" +
            "          t.printStackTrace();\n" +
            "          return null;\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(RuntimeException a0, MarshallingSession a1) {\n" +
            "        return new StringBuilder().append(\"{\").append(\"\\\"__EncodedType\\\":\\\"java.lang.RuntimeException\\\"\").append(\",\").append(\"\\\"__ObjectID\\\"\").append(\":\").append(a0.hashCode()).append(\",\").append(\"\\\"detailMessage\\\" : \").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(\",\").append(\"\\\"cause\\\" : \").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(\",\").append(\"\\\"stackTrace\\\" : \").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append(\"}\").toString();\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(JSONValue a0) {\n" +
            "        return (a0.isObject() != null) && a0.isObject().get(\"__EncodedType\").equals(this.getTypeHandled().getName());\n" +
            "      }\n" +
            "\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"java.lang.RuntimeException\", java_lang_RuntimeException);\n" +
            "    java_lang_Throwable = new Marshaller<JSONValue, Throwable>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return Throwable.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public Throwable demarshall(JSONValue a0, MarshallingSession a1) {\n" +
            "        try {\n" +
            "          if ((a0 == null) || (a0.isNull() != null)) {\n" +
            "            return null;\n" +
            "          }\n" +
            "          Throwable entity = new Throwable();\n" +
            "          java_lang_Throwable_detailMessage(entity, java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"detailMessage\"), a1));\n" +
            "          java_lang_Throwable_cause(entity, java_lang_Throwable.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"cause\"), a1));\n" +
            "          entity.setStackTrace(arrayOf_java_lang_StackTraceElement_D1.demarshall(java_util_List.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"stackTrace\"), a1), a1));\n" +
            "          return entity;\n" +
            "        } catch (Throwable t) {\n" +
            "          t.printStackTrace();\n" +
            "          return null;\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(Throwable a0, MarshallingSession a1) {\n" +
            "        return new StringBuilder().append(\"{\").append(\"\\\"__EncodedType\\\":\\\"java.lang.Throwable\\\"\").append(\",\").append(\"\\\"__ObjectID\\\"\").append(\":\").append(a0.hashCode()).append(\",\").append(\"\\\"detailMessage\\\" : \").append(java_lang_String.marshall(java_lang_Throwable_detailMessage(a0), a1)).append(\",\").append(\"\\\"cause\\\" : \").append(java_lang_Throwable.marshall(a0.getCause(), a1)).append(\",\").append(\"\\\"stackTrace\\\" : \").append(arrayOf_java_lang_StackTraceElement_D1.marshall(a0.getStackTrace(), a1)).append(\"}\").toString();\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(JSONValue a0) {\n" +
            "        return (a0.isObject() != null) && a0.isObject().get(\"__EncodedType\").equals(this.getTypeHandled().getName());\n" +
            "      }\n" +
            "\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"java.lang.Throwable\", java_lang_Throwable);\n" +
            "    org_jboss_errai_bus_client_tests_support_SimpleEntity = new Marshaller<JSONValue, SimpleEntity>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return SimpleEntity.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public SimpleEntity demarshall(JSONValue a0, MarshallingSession a1) {\n" +
            "        try {\n" +
            "          if ((a0 == null) || (a0.isNull() != null)) {\n" +
            "            return null;\n" +
            "          }\n" +
            "          SimpleEntity entity = new SimpleEntity();\n" +
            "          entity.setId(java_lang_Long.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"id\"), a1));\n" +
            "          entity.setActive(java_lang_Boolean.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"active\"), a1));\n" +
            "          entity.setLogin(java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"login\"), a1));\n" +
            "          entity.setPassword(java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"password\"), a1));\n" +
            "          entity.setNumber(java_lang_String.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"number\"), a1));\n" +
            "          entity.setSelected(java_lang_Boolean.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"selected\"), a1));\n" +
            "          entity.setDeleted(java_lang_Boolean.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"deleted\"), a1));\n" +
            "          entity.setCreateDate(java_util_Date.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"createDate\"), a1));\n" +
            "          entity.setLastModifyDate(java_util_Date.demarshall(MarshallUtil.nullSafe_JSONObject(a0, \"lastModifyDate\"), a1));\n" +
            "          return entity;\n" +
            "        } catch (Throwable t) {\n" +
            "          t.printStackTrace();\n" +
            "          return null;\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(SimpleEntity a0, MarshallingSession a1) {\n" +
            "        return new StringBuilder().append(\"{\").append(\"\\\"__EncodedType\\\":\\\"org.jboss.errai.bus.client.tests.support.SimpleEntity\\\"\").append(\",\").append(\"\\\"__ObjectID\\\"\").append(\":\").append(a0.hashCode()).append(\",\").append(\"\\\"id\\\" : \").append(java_lang_Long.marshall(a0.getId(), a1)).append(\",\").append(\"\\\"active\\\" : \").append(java_lang_Boolean.marshall(org_jboss_errai_bus_client_tests_support_SimpleEntity_active(a0), a1)).append(\",\").append(\"\\\"login\\\" : \").append(java_lang_String.marshall(a0.getLogin(), a1)).append(\",\").append(\"\\\"password\\\" : \").append(java_lang_String.marshall(a0.getPassword(), a1)).append(\",\").append(\"\\\"number\\\" : \").append(java_lang_String.marshall(a0.getNumber(), a1)).append(\",\").append(\"\\\"selected\\\" : \").append(java_lang_Boolean.marshall(org_jboss_errai_bus_client_tests_support_AbstractEntity_selected(a0), a1)).append(\",\").append(\"\\\"deleted\\\" : \").append(java_lang_Boolean.marshall(org_jboss_errai_bus_client_tests_support_AbstractEntity_deleted(a0), a1)).append(\",\").append(\"\\\"createDate\\\" : \").append(java_util_Date.marshall(a0.getCreateDate(), a1)).append(\",\").append(\"\\\"lastModifyDate\\\" : \").append(java_util_Date.marshall(a0.getLastModifyDate(), a1)).append(\"}\").toString();\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(JSONValue a0) {\n" +
            "        return (a0.isObject() != null) && a0.isObject().get(\"__EncodedType\").equals(this.getTypeHandled().getName());\n" +
            "      }\n" +
            "\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"org.jboss.errai.bus.client.tests.support.SimpleEntity\", org_jboss_errai_bus_client_tests_support_SimpleEntity);\n" +
            "    arrayOf_java_lang_Object_D1 = new Marshaller<List, Object[]>() {\n" +
            "      public Class getTypeHandled() {\n" +
            "        return Object.class;\n" +
            "      }\n" +
            "\n" +
            "      public String getEncodingType() {\n" +
            "        return \"json\";\n" +
            "      }\n" +
            "\n" +
            "      public Object[] demarshall(List a0, MarshallingSession a1) {\n" +
            "        if (a0 == null) {\n" +
            "          return null;\n" +
            "        } else {\n" +
            "          return _demarshall1(a0, a1);\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      public boolean handles(List a0) {\n" +
            "        return true;\n" +
            "      }\n" +
            "\n" +
            "      public String marshall(Object[] a0, MarshallingSession a1) {\n" +
            "        if (a0 == null) {\n" +
            "          return null;\n" +
            "        } else {\n" +
            "          return _marshall1(a0, a1);\n" +
            "        }\n" +
            "      }\n" +
            "\n" +
            "      private Object[] _demarshall1(List a0, MarshallingSession a1) {\n" +
            "        Object[] newArray = new Object[a0.size()];\n" +
            "        for (int i = 0; i < newArray.length; i++) {\n" +
            "          newArray[i] = a0.get(i);\n" +
            "        }\n" +
            "        return newArray;\n" +
            "      }\n" +
            "\n" +
            "      private String _marshall1(Object[] a0, MarshallingSession a1) {\n" +
            "        StringBuilder sb = new StringBuilder(\"[\");\n" +
            "        for (int i = 0; i < a0.length; i++) {\n" +
            "          if (i > 0) {\n" +
            "            sb.append(\",\");\n" +
            "          }\n" +
            "          sb.append(java_lang_Object.marshall(a0[i], a1));\n" +
            "        }\n" +
            "        return sb.append(\"]\").toString();\n" +
            "      }\n" +
            "\n" +
            "    };\n" +
            "    marshallers.put(\"[Ljava.lang.Object;\", arrayOf_java_lang_Object_D1);\n" +
            "  }\n" +
            "\n" +
            "  private native static void java_lang_Throwable_detailMessage(Throwable instance, String value) /*-{\n" +
            "    instance.@java.lang.Throwable::detailMessage = value;\n" +
            "  }-*/;\n" +
            "\n" +
            "  private native static String java_lang_Throwable_detailMessage(Throwable instance) /*-{\n" +
            "    return instance.@java.lang.Throwable::detailMessage;\n" +
            "  }-*/;\n" +
            "\n" +
            "  private native static void java_lang_Throwable_cause(Throwable instance, Throwable value) /*-{\n" +
            "    instance.@java.lang.Throwable::cause = value;\n" +
            "  }-*/;\n" +
            "\n" +
            "  private native static Throwable java_lang_Throwable_cause(Throwable instance) /*-{\n" +
            "    return instance.@java.lang.Throwable::cause;\n" +
            "  }-*/;\n" +
            "\n" +
            "  private native static void org_jboss_errai_bus_client_tests_support_SimpleEntity_active(SimpleEntity instance, boolean value) /*-{\n" +
            "    instance.@org.jboss.errai.bus.client.tests.support.SimpleEntity::active = value;\n" +
            "  }-*/;\n" +
            "\n" +
            "  private native static boolean org_jboss_errai_bus_client_tests_support_SimpleEntity_active(SimpleEntity instance) /*-{\n" +
            "    return instance.@org.jboss.errai.bus.client.tests.support.SimpleEntity::active;\n" +
            "  }-*/;\n" +
            "\n" +
            "  private native static void org_jboss_errai_bus_client_tests_support_AbstractEntity_selected(AbstractEntity instance, boolean value) /*-{\n" +
            "    instance.@org.jboss.errai.bus.client.tests.support.AbstractEntity::selected = value;\n" +
            "  }-*/;\n" +
            "\n" +
            "  private native static boolean org_jboss_errai_bus_client_tests_support_AbstractEntity_selected(AbstractEntity instance) /*-{\n" +
            "    return instance.@org.jboss.errai.bus.client.tests.support.AbstractEntity::selected;\n" +
            "  }-*/;\n" +
            "\n" +
            "  private native static void org_jboss_errai_bus_client_tests_support_AbstractEntity_deleted(AbstractEntity instance, boolean value) /*-{\n" +
            "    instance.@org.jboss.errai.bus.client.tests.support.AbstractEntity::deleted = value;\n" +
            "  }-*/;\n" +
            "\n" +
            "  private native static boolean org_jboss_errai_bus_client_tests_support_AbstractEntity_deleted(AbstractEntity instance) /*-{\n" +
            "    return instance.@org.jboss.errai.bus.client.tests.support.AbstractEntity::deleted;\n" +
            "  }-*/;\n" +
            "\n" +
            "  public Marshaller getMarshaller(String a0, String a1) {\n" +
            "    return marshallers.get(a1);\n" +
            "  }\n" +
            "}");

    context.commit(logger, printWriter);
  }
}
