package org.jboss.errai.example.client.local;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.*;
import net.auroris.ColorPicker.client.Color;
import net.auroris.ColorPicker.client.ColorPicker;
import org.jboss.errai.example.client.local.util.ColorConverter;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
public abstract class ColorPickerForm extends Composite {

  @Inject
  @DataField
  @Bound(property = "style", converter = ColorConverter.class)
  Anchor colorPickerAnchor;

  ColorPickerDialog colorPicker = new ColorPickerDialog();

  @EventHandler("colorPickerAnchor")
  public void onPickerClicked(ClickEvent event) {
    colorPicker.show();
    colorPicker.setPopupPosition(event.getClientX() + 10, event.getClientY());
  }

  @PostConstruct
  private void init() {
    updateColorPickerAnchor();
  }

  private class ColorPickerDialog extends DialogBox {
    private ColorPicker picker;

    public ColorPickerDialog() {

      // Define the panels
      VerticalPanel panel = new VerticalPanel();
      FlowPanel flowPanel = new FlowPanel();
      picker = new ColorPicker();

      // Define the buttons
      Anchor ok = new Anchor("Ok");
      ok.setStyleName("btn");
      ok.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Element element = ColorPickerForm.this.colorPickerAnchor.getElement();
          element.getStyle().setBackgroundColor(picker.getHexColor());
          Color color = new Color();
          try {
            color.setHex(picker.getHexColor());
          } catch (Exception e) {
            throw new RuntimeException("could not parse colors", e);
          }
          updateModel(color);
          updateColorPickerAnchor();
          ColorPickerDialog.this.hide();
        }
      });

      Anchor cancel = new Anchor("Cancel");
      cancel.setStyleName("btn");
      cancel.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          ColorPickerDialog.this.hide();
        }
      });
      flowPanel.add(ok);
      flowPanel.add(cancel);

      panel.add(picker);
      panel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
      panel.add(flowPanel);

      setWidget(panel);
    }
  }

  protected void updateColorPickerAnchor() {
    Style style = colorPickerAnchor.getElement().getStyle();
    style.setBackgroundColor(colorPickerAnchor.getText());
    style.setTextIndent(-99999, Style.Unit.PX);
  }

  protected abstract void updateModel(Color color);
}
