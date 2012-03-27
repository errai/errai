package org.jboss.errai.codegen.tests.model;

import java.util.List;

/**
 * @author Mike Brock
 */
public class SnapshotInterfaceWithCollectionsImpl implements SnapshotInterfaceWithCollections {
  public List<Person> persons;

  public SnapshotInterfaceWithCollectionsImpl(List<Person> persons) {
    this.persons = persons;
  }

  @Override
  public List<Person> getPersons() {
    return persons;
  }
}
