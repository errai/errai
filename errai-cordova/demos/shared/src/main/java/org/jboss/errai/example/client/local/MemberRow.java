package org.jboss.errai.example.client.local;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.example.client.shared.Member;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * Represents a row in the member table
 */
@Dependent
@Templated("Table.html#row")
public class MemberRow extends Composite {
  @Bound
  @DataField
  private DivElement name = DOM.createElement("div").cast();

  @Bound
  @DataField
  private DivElement email = DOM.createElement("div").cast();

  @Bound
  @DataField
  private DivElement phoneNumber = DOM.createElement("div").cast();

  @Inject
  @DataField
  private Anchor restUrl;

  @Inject
  @Bound
  @DataField
  private ValueImage picture;

  @Inject
  @AutoBound
  DataBinder<Member> binder;

  public void set(Member member) {
    binder.setModel(member, InitialState.FROM_MODEL);

    String url = "rest/members/" + member.getId();
    restUrl.setHref(url);
    restUrl.setText(url);
  }
}
