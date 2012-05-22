package org.jboss.errai.ui.test.client.demo.pages;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ui.shared.api.annotations.Insert;
import org.jboss.errai.ui.shared.api.annotations.Page;
import org.jboss.errai.ui.test.client.demo.MyPageTemplate;
import org.jboss.errai.ui.test.client.demo.component.ContentFragment;
import org.jboss.errai.ui.test.client.demo.model.Profile;
import org.jboss.errai.ui.test.client.demo.service.ProfileService;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;

@Page("/profile/{profileId}")
public class ViewProfilePage extends MyPageTemplate {

  private String profileId;

  @Insert
  ContentFragment content;

  @Inject
  private ProfileService profileService;

  @PostConstruct
  public void init() {

    Profile p = profileService.loadProfile(profileId);
    content.setTitle(p.getName());
    
    content.getBigButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        Window.alert("You clicked me!");
      }
    });
  }
}
