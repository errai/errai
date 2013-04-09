package org.jboss.errai.example.client.local;

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

    String[] colors = modelValue.split("-");
    Color color = new Color();
    try {
      color.setRGB(Integer.parseInt(colors[1]), Integer.parseInt(colors[2]), Integer.parseInt(colors[3]));
    } catch (Exception e) {
      throw new RuntimeException("invalid color numbers");
    }

    return color.getHex();
  }
}
