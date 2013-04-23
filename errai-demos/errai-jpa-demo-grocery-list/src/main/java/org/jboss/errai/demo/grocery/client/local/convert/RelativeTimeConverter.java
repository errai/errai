package org.jboss.errai.demo.grocery.client.local.convert;

import java.util.Date;

import org.jboss.errai.databinding.client.api.Converter;

public class RelativeTimeConverter implements Converter<Date, String> {

  @Override
  public Date toModelValue(String widgetValue) {
    throw new UnsupportedOperationException("This converter only converts Model -> Widget");
  }

  @Override
  public String toWidgetValue(Date modelValue) {
    return toRelativeTime(modelValue.getTime());
  }

  private static native String toRelativeTime(double millisSinceEpoch) /*-{
    return $wnd.toRelativeTime(new Date(millisSinceEpoch));
  }-*/;

}
