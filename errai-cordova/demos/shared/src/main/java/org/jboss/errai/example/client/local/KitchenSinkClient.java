package org.jboss.errai.example.client.local;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.*;
import com.googlecode.gwtphonegap.client.PhoneGap;
import com.googlecode.gwtphonegap.client.camera.PictureCallback;
import com.googlecode.gwtphonegap.client.camera.PictureOptions;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.example.client.shared.Member;
import org.jboss.errai.example.client.shared.MemberService;
import org.jboss.errai.ioc.client.api.Caller;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

/**
 * This is a GWT composite that gets added to the page by {@link KitchenSinkApp}
 * at page load time. The layout of this composite widget is declared in the
 * companion file KitchenSinkClient.ui.xml, which you will find in the same
 * source directory.
 * <p/>
 * Note on software architecture: the embedded event handlers in this class
 * communicate directly with the server. Although this makes a small project
 * such as this one easier to understand at a glance, it is not a great approach
 * for long-term success. If you are planning to extend this example into a
 * large application, read up on the <a
 * href="http://code.google.com/webtoolkit/doc/latest/DevGuideMvpActivitiesAndPlaces.html">MVP pattern</a>, which is
 * recommended for larger apps.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Templated("#main")
public class KitchenSinkClient extends Composite {
  private PhoneGap phoneGap;

  @Inject
  @DataField
  Label generalErrorLabel;

  @Inject
  private Caller<MemberService> memberService;

  @Inject
  @DataField
  Button registerButton;

  @Inject
  @DataField
  Label registerConfirmMessage;

  @Inject
  @DataField
  TextBox nameBox;

  @Inject
  @DataField
  Label nameValidationErr;

  @Inject
  @DataField
  TextBox emailBox;

  @Inject
  @DataField
  Label emailValidationErr;

  @Inject
  @DataField
  TextBox phoneBox;

  @Inject
  @DataField
  Label phoneValidationErr;

  @Inject
  @DataField
  Label tableEmptyMessage;

  @Inject
  @DataField
  MemberTable membersTable;

  @Inject
  @DataField
  Button takePicture;

  @Inject
  @DataField
  Image image;

  public KitchenSinkClient() {
    phoneGap = GWT.create(PhoneGap.class);
    phoneGap.initializePhoneGap();
  }

  @PostConstruct
  public final void init() {
    setGeneralErrorMessage("");
    setTableStatusMessage("Fetching member list...");
    registerButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        onRegisterButtonClick();
      }
    });

    takePicture.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        onTakePictureClick();
      }
    });
  }


  /**
   * Validates the new member data and sends it to the server if validation
   * passes. Displays validation messages if validation fails.
   */
  void onRegisterButtonClick() {
    Member newMember = new Member();
    newMember.setName(nameBox.getText());
    newMember.setEmail(emailBox.getText());
    newMember.setPhoneNumber(phoneBox.getText());
    newMember.setPicture(image.getUrl());

    nameValidationErr.setText("");
    emailValidationErr.setText("");
    phoneValidationErr.setText("");
    generalErrorLabel.setText("");

    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Set<ConstraintViolation<Member>> violations = validator.validate(newMember);

    for (ConstraintViolation<Member> cv : violations) {
      String prop = cv.getPropertyPath().toString();
      if (prop.equals("name")) {
        nameValidationErr.setText(cv.getMessage());
      } else if (prop.equals("email")) {
        emailValidationErr.setText(cv.getMessage());
      } else if (prop.equals("phoneNumber")) {
        phoneValidationErr.setText(cv.getMessage());
      } else {
        registerConfirmMessage.setText(cv.getMessage());
      }
    }

    if (!violations.isEmpty()) return;

    memberService.call(
        new RemoteCallback<Void>() {
          @Override
          public void callback(Void response) {
            registerConfirmMessage.setText("Registration Complete!");
            registerConfirmMessage.setStyleName("successMessage");

            // the server will also broadcast a @New Member CDI event, which causes the table to update
            // so we don't have to do that here.
          }
        },
        new ErrorCallback() {
          @Override
          public boolean error(Message message, Throwable throwable) {
            setGeneralErrorMessage("Member registration failed: " + throwable.getMessage());
            return false;
          }
        }
    ).register(newMember);
  }

  void onTakePictureClick() {
    PictureOptions options = new PictureOptions(25);
    options.setDestinationType(PictureOptions.DESTINATION_TYPE_DATA_URL);
    options.setSourceType(PictureOptions.PICTURE_SOURCE_TYPE_CAMERA);

    phoneGap.getCamera().getPicture(options, new PictureCallback() {

      @Override
      public void onSuccess(String data) {
        image.setUrl(UriUtils.fromSafeConstant("data:image/jpeg;base64," + data));
      }

      @Override
      public void onFailure(String error) {
        setGeneralErrorMessage("Could not take member picture: " + error);
      }
    });
  }

  /**
   * Adds the given member into the local Registered Members CellTable. Does not
   * communicate with the server.
   *
   * @param m The member to add to the CellTable being displayed in the web
   *          page.
   */
  public void addDisplayedMember(Member m) {
    membersTable.add(m);
    setTableStatusMessage("");
  }

  /**
   * Replaces the displayed list of members with the given list of members.
   *
   * @param members The list of members to display on the web page. Not null.
   */
  public void setDisplayedMembers(List<Member> members) {
    membersTable.set(members);
    if (members.isEmpty()) {
      setTableStatusMessage("No members registered yet.");
    } else {
      setTableStatusMessage("");
    }
  }

  /**
   * Sets the general error message that appears near the top of the page.
   */
  public void setGeneralErrorMessage(String string) {
    generalErrorLabel.setText(string);
  }

  /**
   * Sets the message that appears underneath the Registered Members table.
   */
  public void setTableStatusMessage(String message) {
    tableEmptyMessage.setText(message);
  }
}
