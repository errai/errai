package org.jboss.errai.ui.rebind.less;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

import org.junit.Test;
import org.mockito.Matchers;

import com.google.gwt.core.ext.BadPropertyValueException;
import com.google.gwt.core.ext.PropertyOracle;
import com.google.gwt.core.ext.SelectionProperty;
import com.google.gwt.core.ext.TreeLogger;

/**
 * @author edewit@redhat.com
 */
public class LessConverterTest {

  @Test
  public void shouldConvertLessToNormalCss() throws Exception {
    // given
    final URL resource = getClass().getResource("/org/jboss/errai/package.less");
    final PropertyOracle oracle = noVariablesFoundOracle();

    // when
    final File css = new LessConverter(TreeLogger.NULL, oracle).convert(resource);

    // then
    assertNotNull(css);
    assertTrue(css.exists());

    String cssOutput = readFile(css);
    assertEquals("#header {  color: #4d926f;}", cssOutput);
  }

  @Test
  public void shouldThrowException() throws Exception {
    // given
    final PropertyOracle oracle = noVariablesFoundOracle();
    File lessError = File.createTempFile("error", ".less");
    final PrintWriter writer = new PrintWriter(lessError);
    writer.println(".header { color: @color; }");

    writer.close();

    // when
    try {
      new LessConverter(TreeLogger.NULL, oracle).convert(lessError.toURI().toURL());
      fail("exception should have been thrown, because color is not defined");
    } catch (IOException e) {

      // then
      assertTrue(e.getMessage().contains("compile"));
    }
  }

  @Test
  public void shouldConvertLessFilesWithinJars() throws Exception {
    // given
    final PropertyOracle oracle = noVariablesFoundOracle();
    final URL resource = getClass().getResource("/less.jar");
    final URL less = new URL("jar:" + resource + "!/org/jboss/errai/package.less");

    // when
    final File css = new LessConverter(TreeLogger.NULL, oracle).convert(less);

    // then
    final Scanner scanner = new Scanner(css);
    assertEquals("#header", scanner.next());
    scanner.close();
  }

  @Test
  public void shouldAppendPropertyOracleVaraiblesToStartOfLessFile() throws Exception {
    //given
    final URL resource = getClass().getResource("/user-agent.less");
    final PropertyOracle oracle = mock(PropertyOracle.class);
    final SelectionProperty property = mock(SelectionProperty.class);

    // when
    when(oracle.getSelectionProperty(Matchers.<TreeLogger>any(), eq("user.agent"))).thenReturn(property);
    when(property.getCurrentValue()).thenReturn("safari");

    when(oracle.getSelectionProperty(Matchers.<TreeLogger>any(), not(eq("user.agent"))))
            .thenThrow(new BadPropertyValueException(""));
    when(oracle.getConfigurationProperty(anyString())).thenThrow(new BadPropertyValueException(""));

    final File css = new LessConverter(TreeLogger.NULL, oracle).convert(resource);

    //then
    assertNotNull(css);
    assertEquals(".class1 {  background-color: black;}", readFile(css));
  }

  @Test
  public void shouldImportLessFiles() throws Exception {
    // given
    final URL resource = getClass().getResource("/import.less");
    final PropertyOracle oracle = mock(PropertyOracle.class);
    final SelectionProperty property = mock(SelectionProperty.class);

    // when
    when(oracle.getSelectionProperty(Matchers.<TreeLogger>any(), eq("user.agent"))).thenReturn(property);
    when(property.getCurrentValue()).thenReturn("mozilla");

    when(oracle.getSelectionProperty(Matchers.<TreeLogger>any(), not(eq("user.agent"))))
            .thenThrow(new BadPropertyValueException(""));
    when(oracle.getConfigurationProperty(anyString())).thenThrow(new BadPropertyValueException(""));

    final File css = new LessConverter(TreeLogger.NULL, oracle).convert(resource);

    // then
    assertEquals(".class1 {  background-color: white;}.theclass {  color: #808080;}", readFile(css));
  }

  @Test
  public void shouldImportMultipleLessFiles() throws Exception {
    // given
    final URL resource = getClass().getResource("/multipleImports.less");
    final PropertyOracle oracle = noVariablesFoundOracle();

    // when
    final File css = new LessConverter(TreeLogger.NULL, oracle).convert(resource);

    // then
    assertEquals(".imported1 {  color: #0000ff;}.imported2 {  color: #800080;}.main {  color: #0000ff;  background-color: #800080;}",
            readFile(css));
  }

  private PropertyOracle noVariablesFoundOracle() throws BadPropertyValueException {
    final PropertyOracle oracle = mock(PropertyOracle.class);
    when(oracle.getSelectionProperty(Matchers.<TreeLogger>any(), anyString()))
            .thenThrow(new BadPropertyValueException(""));
    when(oracle.getConfigurationProperty(anyString())).thenThrow(new BadPropertyValueException(""));
    return oracle;
  }

  private String readFile(File css) throws FileNotFoundException {
    StringBuilder buffer = new StringBuilder();
    final Scanner scanner = new Scanner(css);
    while (scanner.hasNextLine()) {
      buffer.append(scanner.nextLine());
    }
    scanner.close();
    return buffer.toString();
  }
}
