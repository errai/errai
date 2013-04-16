package org.jboss.errai.otec.mutation;

import org.jboss.errai.otec.Position;
import org.jboss.errai.otec.State;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public interface Mutation<T extends State, P extends Position, D extends Data> {
  public MutationType getType();
  public P getPosition();
  public D getData();
  public void apply(T state);
}
