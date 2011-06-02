package org.jboss.errai.ioc.tests.rebind;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public interface IfBlockBuilderTestResult {

    public static final String EMPTY_IF_BLOCK_RESULT_NO_RHS = 
        "if (str.endsWith(\"abc\")) { }\n";

    public static final String EMPTY_IF_BLOCK_RESULT_LITERAL_RHS = 
        "if (n == 1) { }\n";
    
    public static final String IF_ELSE_BLOCK_RESULT_NO_RHS = 
        "if (str.endsWith(\"abc\")) { " +
            "java.lang.Integer n = 0;\n" +
        "} else {" +
            "\njava.lang.Integer n = 1;\n" +
        "}\n";

    public static final String IF_ELSE_BLOCK_RESULT_RHS = 
        "if (n > m) { " +
            "java.lang.Integer n = 0;\n" +
        "} else {" +
            "\njava.lang.Integer n = 1;\n" +
        "}\n";

    public static final String IF_ELSEIF_ELSE_BLOCK_RESULT_NO_RHS = 
        "if (s.endsWith(\"abc\")) {\n" +
            "n = 0;\n" +
        "} else if (s.startsWith(\"def\")) { " +
            "n = 1;\n" +
        "} else { " +
            "n = 2;\n" +
        "}\n";
    
    public static final String IF_ELSEIF_ELSE_BLOCK_RESULT_RHS =
        "if (n > m) {\n" +
            "n = 0;\n" +
        "} else if (m > n) { " +
            "n = 1;\n" +
        "} else { " +
            "n = 2;\n" +
        "}\n";
}
