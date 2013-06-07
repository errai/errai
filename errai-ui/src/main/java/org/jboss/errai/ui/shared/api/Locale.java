package org.jboss.errai.ui.shared.api;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * @author edewit@redhat.com
 */
@Bindable
@Portable
public class Locale {
  private String locale;
  private String label;

  public Locale() {
  }

  public Locale(@MapsTo("locale") String locale, @MapsTo("label") String label) {
    this.locale = locale;
    this.label = label;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}
