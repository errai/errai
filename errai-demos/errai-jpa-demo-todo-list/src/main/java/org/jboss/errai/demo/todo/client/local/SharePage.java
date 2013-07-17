package org.jboss.errai.demo.todo.client.local;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
@Templated("#main")
@Page(path="share")
public class SharePage extends Composite {
  @Inject private @DataField Label overallErrorMessage;
  @Inject private @DataField TextBox email;
  @Inject private @DataField Button shareButton;

  @PostConstruct
  private void init() {
    overallErrorMessage.setVisible(false);
  }

  @EventHandler("shareButton")
  private void doShare(ClickEvent e) {

  }
}
