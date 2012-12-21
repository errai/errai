package org.jboss.errai.example.client.local;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.example.client.shared.Member;
import org.jboss.errai.example.client.shared.MemberService;
import org.jboss.errai.example.client.shared.New;
import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.api.Caller;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.cordova.Container;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.List;

/**
 * Entry point for the Errai Kitchen Sink application. The {@code @EntryPoint}
 * annotation indicates to the Errai framework that this class should be
 * instantiated inside the web browser when the web page is first loaded.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
@Templated("#template")
public class KitchenSinkApp extends Composite {

  /**
   * This is the client-side proxy to the Errai service implemented by
   * MemberServiceImpl. The proxy is generated at build time, and injected into
   * this field when the page loads. You can create additional Errai services by
   * following this same pattern; just be sure that the client-side class you
   * inject the Caller into is an injectable class (client-side injectable
   * classes are annotated with {@code @EntryPoint}, {@code @ApplicationScoped},
   * or {@code @Singleton}).
   */
  @Inject
  private Caller<MemberService> memberService;

  @Inject
  @DataField("kitchensink")
  private KitchenSinkClient kitchenSinkUi;

  /**
   * Builds the UI and populates the member list by making an RPC call to the server.
   * <p>
   * Note that because this method performs an RPC call to the server, it is annotated
   * with AfterInitialization rather than PostConstruct: the contract of PostConstruct
   * only guarantees that all of <em>this</em> bean's dependencies have been injected,
   * but it does not guarantee that the entire runtime environment has completed its
   * bootstrapping routine. Methods annotated with the Errai-specific AfterInitialization
   * are only called once everything is up and running, including the communication
   * channel to the server.
   */
  @AfterInitialization
  public void createUI() {
    RootPanel.get("rootPanel").add(this);
    fetchMemberList();
  }

  /**
   * Responds to the CDI event that's fired every time a new member is added to
   * the database.
   *
   * @param newMember The member that was just added to the database.
   */
  public void onMemberAdded(@Observes @New Member newMember) {
    kitchenSinkUi.addDisplayedMember(newMember);
  }

  /**
   * Fetches the member list from the server, adding each member to the table in the UI.
   */
  private void fetchMemberList() {

    // note that GWT.log messages only show up in development mode. They have no effect in production mode.
    GWT.log("Requesting member list...");

    memberService.call(new RemoteCallback<List<Member>>() {
      @Override
      public void callback(List<Member> response) {
        GWT.log("Got member list. Size: " + response.size());
        kitchenSinkUi.setDisplayedMembers(response);
      }
    },
    new ErrorCallback() {
      @Override
      public boolean error(Message message, Throwable throwable) {
        throwable.printStackTrace();
        kitchenSinkUi.setGeneralErrorMessage("Failed to retrieve list of members: " + throwable.getMessage());
        return false;
      }
    }).retrieveAllMembersOrderedByName();
  }

}
