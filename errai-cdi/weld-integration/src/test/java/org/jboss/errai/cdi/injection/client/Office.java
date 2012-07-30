package org.jboss.errai.cdi.injection.client;

import javax.enterprise.inject.Specializes;

@Specializes
public class Office extends Building {

    @Override
    protected String getClassName() {
        return Office.class.getName();
    }

}