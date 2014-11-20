package org.jboss.errai.forge.ui.command;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TemplateWriter {
  
  private final String templateResourcePath;
  private final Map<String, String> translations = new HashMap<String, String>();

  public TemplateWriter(final String templateResourcePath, final String... placeholders) {
    this.templateResourcePath = templateResourcePath;
    for (final String placeholder : placeholders) {
      translations.put(placeholder, null);
    }
  }
  
  public TemplateWriter set(final String placeholder, final String value) {
    translations.put(placeholder, value);
    return this;
  }
  
  public void writeTemplate(final File outputFile) throws IOException {
    createFileAndParentDirectories(outputFile);
    final StringBuilder template = loadTemplate();
    fillInPlaceholders(template);
    writeTemplateToOutputFile(template, outputFile);
  }

  private void createFileAndParentDirectories(final File outputFile) throws IOException {
    outputFile.getParentFile().mkdirs();
    outputFile.createNewFile();
  }

  private void fillInPlaceholders(final StringBuilder template) {
    for (final Entry<String, String> entry : translations.entrySet()) {
      if (entry.getValue() == null)
        throw new IllegalStateException("There is not translations for the key " + entry.getKey());
      
      replacePlaceholder(template, entry.getKey(), entry.getValue());
    }
  }

  private void replacePlaceholder(final StringBuilder testBuilder, final String placeholder, final String replacement) {
    int indexOfPlaceholder = 0;
    while ((indexOfPlaceholder = testBuilder.indexOf(placeholder, indexOfPlaceholder)) > -1) {
      final int start = indexOfPlaceholder;
      final int end = indexOfPlaceholder + placeholder.length();
      
      testBuilder.replace(start, end, replacement);
    }
  }

  private void writeTemplateToOutputFile(final StringBuilder template, final File outputFile) throws IOException {
    try (final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile))) {
      writer.write(template.toString());
    }
  }

  private StringBuilder loadTemplate() throws IOException {
    try (final InputStreamReader templateReader = new InputStreamReader(Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(templateResourcePath))) {
      final StringBuilder retVal = new StringBuilder();
      final char[] chars = new char[256];
      int read;

      while ((read = templateReader.read(chars)) > -1) {
        retVal.append(chars, 0, read);
      }

      return retVal;
    }
  }

}
