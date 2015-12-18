/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ui.rebind.less;

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.util.DefaultTextOutput;
import com.google.gwt.dev.util.TextOutput;
import com.google.gwt.resources.css.*;
import com.google.gwt.resources.css.ast.Context;
import com.google.gwt.resources.css.ast.CssProperty;
import com.google.gwt.resources.css.ast.CssRule;
import com.google.gwt.resources.css.ast.CssStylesheet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Parses the css stylesheet and performs optimizations on it remove identical selectors and merge identical rules.
 * @author edewit@redhat.com
 */
public class StylesheetOptimizer {

  private final SelectorMinifyVisitor selectorMinifyVisitor = new SelectorMinifyVisitor();
  private final CssStylesheet stylesheet;

  public StylesheetOptimizer(File stylesheet) throws UnableToCompleteException {
    this.stylesheet = parse(stylesheet);
    minify();
  }

  private CssStylesheet parse(File compiled) throws UnableToCompleteException {
    final URL url;
    try {
      url = compiled.toURI().toURL();
    } catch (MalformedURLException e) {
      throw new UnableToCompleteException();
    }

    return GenerateCssAst.exec(TreeLogger.NULL, url);
  }

  private void minify() {
    (new SplitRulesVisitor()).accept(stylesheet);
    (new MergeIdenticalSelectorsVisitor()).accept(stylesheet);
    (new MergeRulesByContentVisitor()).accept(stylesheet);
    selectorMinifyVisitor.accept(stylesheet);
  }

  protected CssStylesheet getStylesheet() {
    return stylesheet;
  }

  public Map<String, String> getConvertedSelectors() {
    return selectorMinifyVisitor.getConvertedSelectors();
  }

  public String output() {
    final DefaultTextOutput defaultTextOutput = new DefaultTextOutput(false);
    new CssGeneration(defaultTextOutput).accept(stylesheet);

    return defaultTextOutput.toString();
  }

  /**
   * The CssGenerationVisitor will create a static css template and a list of substitutions this one will
   * output the css as is.
   */
  private static class CssGeneration extends CssGenerationVisitor {
    private final TextOutput out;
    private boolean needsOpenBrace;

    public CssGeneration(TextOutput out) {
      super(out);
      this.out = out;
    }

    @Override
    public boolean visit(CssRule x, Context ctx) {
      if (x.getProperties().isEmpty()) {
        return false;
      }

      needsOpenBrace = true;
      return super.visit(x, ctx);
    }

    @Override
    public boolean visit(CssProperty x, Context ctx) {
      if (needsOpenBrace) {
        openBrace();
        needsOpenBrace = false;
      }

      out.print(x.getName());

      colon();
      out.print(x.getValues().toCss());

      if (x.isImportant()) {
        important();
      }

      semi();

      return true;
    }

    private void colon() {
      spaceOpt();
      out.print(':');
      spaceOpt();
    }

    private void important() {
      out.print(" !important");
    }

    private void openBrace() {
      spaceOpt();
      out.print('{');
      out.newlineOpt();
      out.indentIn();
    }

    private void semi() {
      out.print(';');
      out.newlineOpt();
    }

    private void spaceOpt() {
      out.printOpt(' ');
    }
  }
}
