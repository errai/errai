package org.jboss.errai.ioc.tests.rebind;

/**
 * Expected test results for the {@link LoopBuilderTest}
 *  
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface LoopBuilderTestResult {

    public static final String FOREACH_RESULT_STRING_IN_LIST = 
        "for (java.lang.String element : list) {" +
        "\n}";

    public static final String FOREACH_RESULT_STRING_IN_ARRAY_ONE_STATEMENT = 
        "for (java.lang.String element : list) {" +
            "\nnew java.lang.String();" + 
        "\n}";
    
    public static final String FOREACH_RESULT_OBJECT_IN_LIST_TWO_STATEMENTS = 
        "for (java.lang.Object element : list) {" +
            "\nnew java.lang.String();" + 
            "\nnew java.lang.Object();" + 
        "\n}";
    
    public static final String FOREACH_RESULT_OBJECT_IN_LIST = 
        "for (java.lang.Object element : list) {" +
        "\n}";
    
    public static final String FOREACH_RESULT_NESTED_STRING_IN_LIST = 
        "for (java.lang.String element : list) {" +
            "\nfor (java.lang.String anotherElement : anotherList) {" +
                "\nnew java.lang.Integer();" +
            "\n};" +
        "\n}";
    
    public static final String FOREACH_RESULT_KEYSET_LOOP = 
        "for (java.lang.Object key : map.keySet()) {" +
        "\n}";  
    
    public static final String FOREACH_RESULT_LITERAL_STRING_ARRAY = 
        "for (java.lang.String s : new java.lang.String[] {\"s1\", \"s2\"}) {" +
            "\ns.getBytes();" +
        "\n}";  
}
