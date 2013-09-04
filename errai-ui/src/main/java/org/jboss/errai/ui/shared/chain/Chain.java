package org.jboss.errai.ui.shared.chain;

import org.w3c.dom.Element;

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
  public void execute(Element element) {
    for (Command command : commands) {
      command.execute(element);
    }
  }

  public List<Command> getCommands() {
    return new ArrayList<Command>(commands);
  }
}
