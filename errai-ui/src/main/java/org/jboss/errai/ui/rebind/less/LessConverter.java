package org.jboss.errai.ui.rebind.less;

import com.google.gwt.core.ext.*;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts a less resource to a css file using the java LessCompiler wrapper.
 * And adds deferred binding properties to the top of the sheet. So that you can
 * use <code>user.agent</code> in less be sure to change '.' into '_' because variables with
 * a '.' in the name are not valid in less.
 * 
 * @author edewit@redhat.com
 */
public class LessConverter {
  private static final Pattern LESS_VAR = Pattern.compile("@(\\w+)");

  private final TreeLogger logger;
  private final PropertyOracle oracle;

  public LessConverter(TreeLogger logger, PropertyOracle oracle) {
    this.logger = logger;
    this.oracle = oracle;
  }

  public File convert(URL resource) throws IOException {
    LessCompiler lessCompiler = new LessCompiler();
    try {
      String lessFile = readLessFile(resource);
      final String css = lessCompiler.compile(lessFile);
      return createCssFile(css);
    } catch (LessException e) {
      throw new IOException("specified less stylesheet could not be compiled to css", e);
    }
  }

  private File createCssFile(String css) throws IOException {
    final File compiled = File.createTempFile("compiled", ".css");
    PrintWriter writer = new PrintWriter(compiled);
    writer.print(css);
    writer.close();
    return compiled;
  }

  private String readLessFile(URL resource) throws IOException {
    Scanner scanner = new Scanner(resource.openStream());
    StringBuilder lessFile = new StringBuilder();
    while (scanner.hasNext()) {
      String line = scanner.nextLine();
      final Matcher matcher = LESS_VAR.matcher(line);
      if (matcher.find()) {
        findVariableInLine(matcher, lessFile);
      }
      lessFile.append(line).append('\n');

    }
    scanner.close();
    return lessFile.toString();
  }

  private void findVariableInLine(MatchResult result, StringBuilder lessFile) {
    for (int i = 1; i < result.groupCount() + 1; i++) {
      final String lessVariable = result.group(i);
      final String value = evaluate(lessVariable.replace('_', '.'));
      if (value != null) {
        lessFile.insert(0, "@" + lessVariable + ": \"" + value + "\";");
      }
    }
  }

  private String evaluate(String lessVariable) {
    try {
      SelectionProperty selProp = oracle.getSelectionProperty(logger, lessVariable);
      return selProp.getCurrentValue();
    } catch (BadPropertyValueException e) {
      try {
        ConfigurationProperty confProp = oracle.getConfigurationProperty(lessVariable);
        return confProp.getValues().get(0);
      } catch (BadPropertyValueException e1) {
        return null;
      }
    }
  }
}
