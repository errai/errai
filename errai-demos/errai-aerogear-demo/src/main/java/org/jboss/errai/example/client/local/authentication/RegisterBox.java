package org.jboss.errai.example.client.local.authentication;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import org.jboss.errai.aerogear.api.pipeline.auth.User;
import org.jboss.errai.ui.shared.api.annotations.*;
import org.jboss.errai.ui.shared.api.annotations.Model;

import javax.inject.Inject;
import java.io.IOException;

@Templated("#register-box")
public class RegisterBox extends AuthenticationDialogBox {

  @Inject
  @Model
  User user;

  @Inject
  @DataField
  private Label message;

  @Inject
  @Bound
  @DataField
  private TextBox firstName;

  @Inject
  @Bound
  @DataField
  private TextBox lastName;

  @Inject
  @Bound
  @DataField
  private TextBox email;

  @Inject
  @Bound
  @DataField
  private TextBox username;

  @Inject
  @Bound
  @DataField
  private TextBox password;

  @Inject
  @DataField
  private Anchor register;

  @Inject
  @DataField
  private Anchor cancel;

  @DataField
  private ValueListBox<String> role = new ValueListBox<String>(new Renderer<String>() {
    @Override
    public String render(String object) {
      return object;
    }

    @Override
    public void render(String object, Appendable appendable) throws IOException {
      appendable.append(object);
    }
  });


  public RegisterBox() {
    role.setAcceptableValues(Lists.newArrayList("admin", "simple"));
    role.setValue("simple");
  }

  @EventHandler("cancel")
  private void onCancelClicked(ClickEvent event) {
    hide();
  }

  @EventHandler("register")
  private void onRegisterClicked(ClickEvent event) {
    final User user = this.user;
    user.setRole(role.getValue());
    authenticator.enroll(user, new AsyncCallback<String>() {
      @Override
      public void onSuccess(String result) {
        hide();
        refresh();
      }

      @Override
      public void onFailure(Throwable caught) {
        message.setText("Registration failed: " + caught);
        hide();
      }
    });
  }
}
