package org.jboss.errai.otec.client.mutation;

import org.jboss.errai.otec.client.State;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public interface Mutation<T extends State, D> {
  public MutationType getType();
  public int getPosition();
  public D getData();
  public int length();
  public void apply(T state);
  public Mutation<T, D> newBasedOn(int index);
  public Mutation<T, D> newBasedOn(int index, int truncate);
  public Mutation<T, D> combineWith(Mutation<T, D> combine);

}
