package org.jboss.errai.example.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.*;
import net.auroris.ColorPicker.client.Color;
import net.auroris.ColorPicker.client.ColorPicker;
import org.jboss.errai.aerogear.api.pipeline.Pipe;
import org.jboss.errai.example.client.local.pipe.ProjectPipe;
import org.jboss.errai.example.shared.Project;
import org.jboss.errai.ui.shared.api.annotations.*;

import javax.inject.Inject;

import static org.jboss.errai.example.client.local.Animator.hide;

/**
 * @author edewit@redhat.com
 */
@Templated("App.html#project-form")
public class ProjectForm extends Composite {

  @Inject
  @Model
  private Project project;

  @Inject
  @ProjectPipe
  private Pipe<Project> projectPipe;

  @Inject
  @Bound
  @DataField("project-title")
  private TextBox title;

  @Inject
  @DataField
  private Anchor colorPickerAnchor;

  private ColorPickerDialog colorPicker = new ColorPickerDialog();

  @Inject
  @DataField
  private Anchor submit;

  @Inject
  @DataField
  private Anchor cancel;
  private String style;

  @EventHandler("colorPickerAnchor")
  public void onPickerClicked(ClickEvent event) {
    colorPicker.show();
    colorPicker.setPopupPosition(event.getClientX() + 10, event.getClientY());
  }

  @EventHandler("submit")
  public void onSubmitClicked(ClickEvent event) {
    final com.google.gwt.dom.client.Element div = getContainer(event);
    project.setStyle(style);
    projectPipe.save(project, new DefaultCallback<Project>() {
      @Override
      public void onSuccess(final Project newProject) {
        hide(div, new DefaultCallback<Void>() {

          @Override
          public void onSuccess(Void result) {
            //taskAddedEventSource.fire(new TaskAddedEvent(newTask));
          }
        });
      }
    });
  }

  @EventHandler("cancel")
  public void onCancelClicked(ClickEvent event) {
    hide(getContainer(event));
  }

  private com.google.gwt.dom.client.Element getContainer(ClickEvent event) {
    return event.getRelativeElement().getParentElement().getParentElement();
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
          Element element = ProjectForm.this.colorPickerAnchor.getElement();
          element.getStyle().setBackgroundColor(picker.getHexColor());
          Color color = new Color();
          try {
            color.setHex(picker.getHexColor());
          } catch (Exception e) {
            throw new RuntimeException("could not parse colors", e);
          }
          style = "project-" + color.getRed() + "-" + color.getGreen() + "-" + color.getBlue();
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
}
