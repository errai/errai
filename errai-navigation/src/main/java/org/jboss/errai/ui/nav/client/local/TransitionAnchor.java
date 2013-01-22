package org.jboss.errai.ui.nav.client.local;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.ui.nav.client.local.spi.PageNode;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

/**
 * Represents an anchor widget that, when clicked, will navigate the user
 * to another page in the application's flow.
 * <p>
 * Instances of this class are normally obtained via dependency injection.
 * <p>
 * Instances of this class are immutable.
 *
 * @param <P> The type of the target page ("to page")
 * @author eric.wittmann@redhat.com
 */
public final class TransitionAnchor<P extends Widget> extends Anchor implements ClickHandler {

  private final Navigation navigation;
  private final Class<P> toPageWidgetType;

  /**
   * Creates a new TransitionAnchor with the given attributes.
   *
   * @param navigation
   *          The navigation system this page transition participates in.
   * @param toPage
   *          The page type this transition goes to. Not null.
   * @throws NullPointerException
   *           if any of the arguments are null.
   */
  TransitionAnchor(Navigation navigation, final Class<P> toPage) {
    this.navigation = Assert.notNull(navigation);
    this.toPageWidgetType = Assert.notNull(toPage);
    addClickHandler(this);
    addAttachHandler(new Handler() {
      @Override
      public void onAttachOrDetach(AttachEvent event) {
        if (event.isAttached())
          initHref(toPage);
      }
    });
  }

  /**
   * Initialize the anchor's href attribute.
   *
   * @param toPage
   *          The page type this transition goes to. Not null.
   */
  private void initHref(Class<P> toPage) {
    PageNode<P> toPageInstance = navigation.getNavGraph().getPage(toPage);
    HistoryToken token = HistoryToken.of(toPageInstance.name(), ImmutableMultimap.<String,String>of());
    String href = "#" + token.toString();
    setHref(href);
  }

  /**
   * The page this transition goes to.
   */
  public Class<P> toPageType() {
    return toPageWidgetType;
  }

  /**
   * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
   */
  @Override
  public void onClick(ClickEvent event) {
    navigation.goTo(toPageWidgetType, ImmutableMultimap.<String,String>of());
    event.stopPropagation();
    event.preventDefault();
  }

  /**
   * Programmatically click on the anchor.
   */
  public void click() {
    navigation.goTo(toPageWidgetType, ImmutableMultimap.<String,String>of());
  }

  /**
   * Programmatically click on the anchor (with some parameters).
   * @param state
   */
  public void click(Multimap<String,String> state) {
    navigation.goTo(toPageWidgetType, state);
  }

}
