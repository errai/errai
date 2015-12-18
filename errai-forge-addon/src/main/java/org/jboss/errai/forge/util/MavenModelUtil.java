package org.jboss.errai.forge.util;

import org.apache.maven.model.Profile;

import java.util.Collection;

public class MavenModelUtil {

  public static Profile getProfileById(final String id, final Collection<Profile> profiles) {
    if (profiles != null) {
      for (final Profile profile : profiles) {
        if (id.equals(profile.getId())) {
          return profile;
        }
      }
    }

    return null;
  }

}
