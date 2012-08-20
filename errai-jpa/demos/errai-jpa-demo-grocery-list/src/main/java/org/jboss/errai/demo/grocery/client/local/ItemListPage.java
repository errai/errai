package org.jboss.errai.demo.grocery.client.local;

import javax.enterprise.context.Dependent;

import org.jboss.errai.demo.grocery.client.local.nav.CompositePage;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Dependent
@Templated("#main")
public class ItemListPage extends CompositePage {

}
