package org.jboss.errai.jpa.test.not.on.gwt.path;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * This entity is not on the GWT source path, and should not appear in Errai's Entity Manager.
 * <p>
 * Part of the regression test for ERRAI-675.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
@Entity
public class NonClientEntity {

    @Id private long id;

}