package org.jboss.errai.ui.shared.chain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author edewit@redhat.com
 */
public class Chain implements Command {

  private List<Command> commands = new ArrayList<Command>();

  public void addCommand(Command command) {
    this.commands.add(command);
  }

  @Override
  public void execute(Context context) {
    for (Command command : commands) {
      command.execute(context);
    }
  }

  @Override
  public Context createInitialContext() {
    Context context = new Context();
    for (Command command : commands) {
      context.putAll(command.createInitialContext());
    }
    return context;
  }

  public List<Command> getCommands() {
    return new ArrayList<Command>(commands);
  }
}
