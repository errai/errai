package org.jboss.errai.ui.rebind.less;

import com.google.gwt.core.ext.*;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.MULTILINE;

/**
 * Converts a less resource to a css file using the java LessCompiler wrapper.
 * And adds deferred binding properties to the top of the sheet. So that you can
 * use <code>user.agent</code> in less be sure to change '.' into '_' because variables with
 * a '.' in the name are not valid in less.
 *
 * @author edewit@redhat.com
 */
public class LessConverter {
  private static final Pattern IMPORT_PATTERN = Pattern.compile("^(?!\\s*//\\s*)@import\\s+(url\\()?\\s*(\"|')(.+)\\s*(\"|')(\\))?\\s*;.*$", MULTILINE);
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
      String lessFile = parseLess(resource);
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

  private String parseLess(URL resource) throws IOException {
    String lessFile = resourceToString(resource);
    lessFile = resolveImports(resource, lessFile);
    lessFile = replaceVariables(lessFile);
    return lessFile;
  }

  private String resourceToString(URL resource) throws IOException {
    Scanner scanner = new Scanner(resource.openStream());
    StringBuilder lessFile = new StringBuilder();
    while (scanner.hasNextLine()) {
      lessFile.append(scanner.nextLine()).append('\n');
    }
    scanner.close();
    return lessFile.toString();
  }

  private String replaceVariables(String lessFile) {
    final Matcher matcher = LESS_VAR.matcher(lessFile);
    while (matcher.find()) {
      for (int i = 1; i < matcher.groupCount() + 1; i++) {
        final String lessVariable = matcher.group(i);
        final String value = evaluate(lessVariable.replace('_', '.'));
        if (value != null) {
          lessFile = "@" + lessVariable + ": \"" + value + "\";\n" + lessFile;
        }
      }
    }
    return lessFile;
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

  private String resolveImports(URL base, String content) throws IOException {
    Matcher importMatcher = IMPORT_PATTERN.matcher(content);
    while (importMatcher.find()) {
      String importedFile = importMatcher.group(3);
      importedFile = importedFile.matches(".*\\.(le?|c)ss$") ? importedFile : importedFile + ".less";
      boolean css = importedFile.matches(".*css$");
      if (!css) {
        String importedLess = parseLess(new URL(base, importedFile));
        // update content *and* matcher
        content = content.substring(0, importMatcher.start()) + importedLess + content.substring(importMatcher.end());
        importMatcher = IMPORT_PATTERN.matcher(content);
      }
    }

    return content;
  }

}
