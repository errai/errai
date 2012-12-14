package org.jboss.errai.example.client.local;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.jboss.errai.example.client.shared.Member;
import org.jboss.errai.example.client.shared.MemberService;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.api.Caller;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * This is a GWT composite that gets added to the page by {@link KitchenSinkApp}
 * at page load time. The layout of this composite widget is declared in the
 * companion file KitchenSinkClient.ui.xml, which you will find in the same
 * source directory.
 * <p>
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
public class KitchenSinkClient extends Composite {

  private static final KitchenSinkTemplates TEMPLATES = GWT.create(KitchenSinkTemplates.class);

  private static KitchenSinkClientUiBinder uiBinder = GWT.create(KitchenSinkClientUiBinder.class);

  interface KitchenSinkClientUiBinder extends
      UiBinder<Widget, KitchenSinkClient> {
  }

  private final Caller<MemberService> memberService;

  /**
   * The list of members. Add to this list via the
   * {@link #addDisplayedMember(Member)} method to ensure the update is visible to
   * the user.
   */
  private final List<Member> members = new ArrayList<Member>();

  // The following fields are all injected during instance construction by GWT UiBinder

  @UiField Label generalErrorLabel;

  @UiField Button registerButton;
  @UiField Label registerConfirmMessage;

  @UiField TextBox nameBox;
  @UiField Label nameValidationErr;

  @UiField TextBox emailBox;
  @UiField Label emailValidationErr;

  @UiField TextBox phoneBox;
  @UiField Label phoneValidationErr;

  @UiField Label tableEmptyMessage;

  @UiField(provided=true) CellTable<Member> membersTable = new CellTable<Member>();

  public KitchenSinkClient(Caller<MemberService> memberService) {
    this.memberService = memberService;
    initWidget(uiBinder.createAndBindUi(this));

    // This sets up the structure of the Registered Members CellTable

    membersTable.addColumn(new Column<Member, String>(new TextCell()) {
      @Override
      public String getValue(Member m) {
        return m.getName();
      }
    }, "Name");

    membersTable.addColumn(new Column<Member, String>(new TextCell()) {
      @Override
      public String getValue(Member m) {
        return m.getEmail();
      }
    }, "Email");

    membersTable.addColumn(new Column<Member, String>(new TextCell()) {
      @Override
      public String getValue(Member m) {
        return m.getPhoneNumber();
      }
    }, "Phone Number");

    membersTable.addColumn(new Column<Member, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Member m) {
        String url = "rest/members/" + m.getId();
        return TEMPLATES.link(UriUtils.fromString(url), url);
      }
    }, "REST URL");
}

  /**
   * Validates the new member data and sends it to the server if validation
   * passes. Displays validation messages if validation fails.
   *
   * @param event The click event (ignored)
   */
  @UiHandler("registerButton")
  void onRegisterButtonClick(ClickEvent event) {
    Member newMember = new Member();
    newMember.setName(nameBox.getText());
    newMember.setEmail(emailBox.getText());
    newMember.setPhoneNumber(phoneBox.getText());

    nameValidationErr.setText("");
    emailValidationErr.setText("");
    phoneValidationErr.setText("");
    registerConfirmMessage.setText("");
    registerConfirmMessage.setStyleName("errorMessage");

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
              registerConfirmMessage.setText("Member registration failed: " + throwable.getMessage());
              return false;
            }
          }).register(newMember);
  }

  /**
   * Adds the given member into the local Registered Members CellTable. Does not
   * communicate with the server.
   *
   * @param m
   *          The member to add to the CellTable being displayed in the web
   *          page.
   */
  public void addDisplayedMember(Member m) {
    members.add(m);
    Collections.sort(members);
    membersTable.setRowData(members);
    setTableStatusMessage("");
  }

  /**
   * Replaces the displayed list of members with the given list of members.
   *
   * @param members The list of members to display on the web page. Not null.
   */
  public void setDisplayedMembers(List<Member> members) {
    this.members.clear();
    this.members.addAll(members);
    membersTable.setRowData(this.members);
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

  /**
   * This is GWT's facility for building HTML snippets that are not vulnerable
   * to XSS attacks.
   * <p>
   * See <a href=
   * "http://code.google.com/webtoolkit/doc/latest/DevGuideSecuritySafeHtml.html#Creating_SafeHtml_Values"
   * >the GWT user guide</a> for details.
   */
  public interface KitchenSinkTemplates extends SafeHtmlTemplates {
    @Template("<a target=\"_blank\" href=\"{0}\">{1}</a>")
    SafeHtml link(SafeUri url, String linkText);
  }

}
