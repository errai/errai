package org.jboss.errai.example.client.local.util;

import com.google.gwt.dom.client.Style;
import net.auroris.ColorPicker.client.Color;
import org.jboss.errai.databinding.client.api.Converter;

/**
 * @author edewit@redhat.com
 */
public class ColorConverter implements Converter<String, String> {
  @Override
  public String toModelValue(String widgetValue) {

    Color color = new Color();
    try {
      color.setHex(widgetValue);
    } catch (Exception e) {
      throw new RuntimeException("could not parse colors", e);
    }
    return "project-" + color.getRed() + "-" + color.getGreen() + "-" + color.getBlue();
  }

  @Override
  public String toWidgetValue(String modelValue) {
    if (modelValue == null) {
      return "fff";
    }

    return getColor(modelValue).getHex();
  }

  private Color getColor(String modelValue) {
    String[] colors = modelValue != null ? modelValue.split("-") : new String[] {"", "255", "255", "255"};
    Color color = new Color();
    try {
      color.setRGB(Integer.parseInt(colors[1]), Integer.parseInt(colors[2]), Integer.parseInt(colors[3]));
    } catch (Exception e) {
      throw new RuntimeException("invalid color numbers");
    }
    return color;
  }

  public String getTextColor(Color color) {
    return calcBrightness(color) < 130 ? "#EEE" : "#222" ;
  }

  protected double calcBrightness(Color color) {
    return color.getRed() * 0.2126 + color.getGreen() * 0.7152 + color.getBlue() * 0.0722;
  }

  public void applyStyles(Style style, String color) {
    Color c = getColor(color);
    style.setBackgroundColor(c.getHex());
    style.setColor(getTextColor(c));
  }
}
