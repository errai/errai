package org.jboss.errai.cdi.stereotypes.client;

import javax.enterprise.context.Dependent;
import java.io.Serializable;

@AnimalStereotype
@Dependent
public class ShortHairedDog implements Animal, Serializable {

    /**
	 *
	 */
    private static final long serialVersionUID = 1859229950272574260L;

}