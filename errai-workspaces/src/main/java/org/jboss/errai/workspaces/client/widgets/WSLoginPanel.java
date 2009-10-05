package org.jboss.errai.workspaces.client.widgets;

import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.*;
import org.jboss.errai.workspaces.client.Workspace;
import org.jboss.errai.workspaces.client.framework.WSComponent;
import org.jboss.errai.bus.client.security.AuthenticationHandler;
import org.jboss.errai.bus.client.security.Credential;
import org.jboss.errai.bus.client.security.impl.NameCredential;
import org.jboss.errai.bus.client.security.impl.PasswordCredential;

public class WSLoginPanel implements WSComponent {
    public Widget getWidget() {
        return new Composite() {
            {
                VerticalPanel mainPanel = new VerticalPanel();
                HorizontalPanel login = new HorizontalPanel();
                HorizontalPanel password = new HorizontalPanel();
                HorizontalPanel buttons = new HorizontalPanel();

                mainPanel.add(login);
                mainPanel.add(password);
                mainPanel.add(buttons);

                Label userNameLabel = new Label("User:");
                Label passwordLabel = new Label("Password:");

                final TextBox userNameInput = new TextBox();
                final TextBox passwordInput = new PasswordTextBox();

                login.add(userNameLabel);
                login.add(userNameInput);
                login.setWidth("100%");
                login.setCellHorizontalAlignment(userNameInput, HasAlignment.ALIGN_RIGHT);

                password.add(passwordLabel);
                password.add(passwordInput);
                password.setWidth("100%");
                password.setCellHorizontalAlignment(passwordInput, HasAlignment.ALIGN_RIGHT);

                final Button loginButton = new Button("Login");
                buttons.add(loginButton);
                buttons.setWidth("100%");
                buttons.setCellHorizontalAlignment(loginButton, HasAlignment.ALIGN_RIGHT);

                loginButton.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        Workspace.getSecurityService().doAuthentication(
                                new AuthenticationHandler() {
                                    public void doLogin(Credential[] credentials) {
                                        for (Credential c : credentials) {
                                            if (c instanceof NameCredential) {
                                                ((NameCredential) c).setName(userNameInput.getText());
                                            }
                                            else if (c instanceof PasswordCredential) {
                                                ((PasswordCredential) c).setPassword(passwordInput.getText());
                                            }
                                        }
                                    }
                                });
                    }
                });

                KeyDownHandler formSubmit = new KeyDownHandler() {
                    public void onKeyDown(KeyDownEvent event) {
                        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                            loginButton.click();
                        }
                    }
                };

                userNameInput.addKeyDownHandler(formSubmit);
                passwordInput.addKeyDownHandler(formSubmit);

                initWidget(mainPanel);
            }
        };
    }

}
