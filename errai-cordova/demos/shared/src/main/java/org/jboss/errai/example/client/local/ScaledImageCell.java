package org.jboss.errai.example.client.local;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

/**
 *
 */
public class ScaledImageCell extends AbstractCell<String> {
  interface Template extends SafeHtmlTemplates {
    @Template("<img src=\"{0}\" style=\"width:20px;height:20px;\"/>")  // 20*20 size
    SafeHtml img(SafeUri url);
  }

  private static Template template;

  /**
   * Construct a new ScaledImageCell.
   */
  public ScaledImageCell() {
    if (template == null) {
      template = GWT.create(Template.class);
    }
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    if (value != null) {
      sb.append(template.img(UriUtils.fromSafeConstant(value)));
    }
  }
}
