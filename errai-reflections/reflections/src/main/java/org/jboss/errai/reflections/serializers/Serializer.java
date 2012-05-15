package org.jboss.errai.reflections.serializers;

import org.jboss.errai.reflections.Reflections;

import java.io.File;
import java.io.InputStream;

/** Serilizer of a {@link org.jboss.errai.reflections.Reflections} instance */
public interface Serializer {
    /** reads the input stream into a new Reflections instance, populating it's store */
    Reflections read(InputStream inputStream);

    /** saves a Reflections instance into the given filename */
    File save(Reflections reflections, String filename);

    /** returns a string serialization of the given Reflections instance */
    String toString(Reflections reflections);
}
