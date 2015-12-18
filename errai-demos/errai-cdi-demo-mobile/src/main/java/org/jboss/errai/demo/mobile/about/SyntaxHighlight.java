/*
 * Copyright (C) 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.demo.mobile.about;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.Ostermiller.Syntax.ToHTML;
import com.Ostermiller.bte.CompileException;

/**
 * A frontend for the ostermiller syntax hightlighter that processes all .java files in the given
 * input directory.
 * 
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class SyntaxHighlight {

  private final ToHTML toHTML = new ToHTML();
  private final File sourceBaseDir;
  private final File targetBaseDir;

  public static void main(String[] args) throws IOException, InvocationTargetException, CompileException {
    if (args.length != 2) {
      throw new IllegalArgumentException(
                "SyntaxHighlight requires exactly two arguments: source directory and target directory");
    }
    if (args[0] == null) {
      throw new IllegalArgumentException("Null source directory argument not allowed");
    }
    if (args[1] == null) {
      throw new IllegalArgumentException("Null target directory argument not allowed");
    }
    new SyntaxHighlight(new File(args[0]), new File(args[1])).run();
  }

  public SyntaxHighlight(File sourceBaseDir, File targetBaseDir) {
    toHTML.addIgnoreStyle("whitespace");
    this.sourceBaseDir = sourceBaseDir;
    this.targetBaseDir = targetBaseDir;
  }

  public void run() throws IOException, InvocationTargetException, CompileException {
    if (!sourceBaseDir.isDirectory()) {
      throw new IllegalArgumentException("Source base dir \"" + sourceBaseDir + "\" is not an existing directory");
    }
    targetBaseDir.mkdirs();
    recursivelyProcess(sourceBaseDir);
  }

  private void recursivelyProcess(File currentDirectory) throws IOException, InvocationTargetException,
      CompileException {
    File[] files = currentDirectory.listFiles();
    for (File f : files) {
      if (f.isFile() && f.getName().endsWith(".java")) {
        File targetFile = targetFileFor(f, ".html");
        writeIndividualFile(f, targetFile);
      }

      if (f.isDirectory()) {
        recursivelyProcess(f);
      }
    }
  }

  private File targetFileFor(File f, String newExtension) {
    String relativeSource = relativizeSourcePath(f);
    return new File(targetBaseDir, relativeSource + newExtension);
  }

  /**
   * Returns the path of the given file relative to {@link #sourceBaseDir}.
   * 
   * @param f
   *          The file to get the relative path for. Must be under {@link #sourceBaseDir}.
   * @return The path of f relative to {@link #sourceBaseDir}.
   * @throws IllegalArgumentException
   *           If the given file is not to be found under sourceBaseDir.
   */
  protected String relativizeSourcePath(File f) {
    String absoluteSourceBase = sourceBaseDir.getAbsolutePath();
    if (!absoluteSourceBase.endsWith(System.getProperty("file.separator"))) {
      absoluteSourceBase = absoluteSourceBase + System.getProperty("file.separator");
    }
    String absoluteSource = f.getAbsolutePath();
    if (!absoluteSource.startsWith(absoluteSource)) {
      throw new IllegalArgumentException("I don't know how to relativize \"" + absoluteSource + "\" against \""
                + absoluteSourceBase + "\".");
    }
    String relativeSource = absoluteSource.substring(absoluteSourceBase.length());
    return relativeSource;
  }

  /**
   * Returns the relative pathname from f to a stylesheet with the given name located in
   * {@link #targetBaseDir}.
   * 
   * @param f
   * @param stylesheetName
   * @return
   */
  private String getStylesheetRelativeLocation(File f, String stylesheetName) {
    char pathSepChar = System.getProperty("file.separator").charAt(0);
    String relativeSource = relativizeSourcePath(f);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < relativeSource.length(); i++) {
      if (relativeSource.charAt(i) == pathSepChar) {
        sb.append("../");
      }
    }
    sb.append(stylesheetName);
    return sb.toString();
  }

  private void writeIndividualFile(File source, File target) throws IOException, InvocationTargetException,
      CompileException {
    target.getParentFile().mkdirs();
    toHTML.setStyleSheet(getStylesheetRelativeLocation(source, "syntax.css"));
    toHTML.setOutput(target);
    toHTML.setExtFromFileName(source.getName());
    toHTML.setDocNameFromFileName(source.getName());
    toHTML.setInput(source);
    toHTML.setTitle(source.getName());
    toHTML.writeFullHTML();
  }
}
