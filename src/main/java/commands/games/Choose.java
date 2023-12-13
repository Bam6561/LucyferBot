package commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

/**
 * Choose is a command invocation that chooses randomly between any number of options.
 *
 * @author Danny Nguyen
 * @version 1.7.2
 * @since 1.0
 */
public class Choose extends Command {
  private String optionsString;

  public Choose() {
    this.name = "choose";
    this.aliases = new String[]{"choose", "pick"};
    this.arguments = "[1, ++]Options";
    this.help = "Chooses randomly between any number of options.";
  }

  /**
   * Checks if user provided any parameters to read a choose command request.
   * <p>
   * Users can provide any number of options separated by a comma.
   * </p>
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    if (numberOfParameters != 0) {
      readChooseRequest(ce, parameters);
    } else {
      ce.getChannel().sendMessage("Options need to be separated by a comma.").queue();
    }
  }

  /**
   * Checks if the choose command request was formatted correctly before
   * sending an embed containing a random choice from user provided options.
   *
   * @param ce         object containing information about the command event
   * @param parameters user provided parameters
   */
  private void readChooseRequest(CommandEvent ce, String[] parameters) {
    String[] options = readOptions(parameters);
    boolean noEmptyOptionProvided = !checkIfEmptyOptionProvided(options);
    if (noEmptyOptionProvided) {
      processChooseRequest(ce, options);
    } else {
      ce.getChannel().sendMessage("None of the options provided can be empty.").queue();
    }
  }

  /**
   * Splits user provided parameters into an array of options with the comma character as a delimiter.
   *
   * @param parameters user provided parameters
   * @return array of options
   */
  private String[] readOptions(String[] parameters) {
    StringBuilder options = new StringBuilder();
    for (int i = 1; i < parameters.length; i++) {
      options.append(parameters[i]).append(" ");
    }
    setOptionsString(options.toString()); // Store user input options
    return optionsString.split(","); // Split options provided
  }

  /**
   * Checks for empty options.
   *
   * @param options array of options provided by the user
   * @return whether there exists an empty option in the array
   */
  private boolean checkIfEmptyOptionProvided(String[] options) {
    for (String option : options) { // Find the first blank option (if any)
      if (option.equals(" ")) return true;
    }
    return false;
  }

  /**
   * Randomly choose an option from user provided options.
   *
   * @param ce object containing information about the command event
   */
  private void processChooseRequest(CommandEvent ce, String[] options) {
    Random random = new Random();
    int randomOption = random.nextInt(options.length);

    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Choice");
    display.setDescription("Based on the options you provided... \n\n" + getOptionsString()
        + "\n\n**I have chosen:** \n||" + options[randomOption] + "||");
    Settings.sendEmbed(ce, display);
  }

  private String getOptionsString() {
    return this.optionsString;
  }

  private void setOptionsString(String optionsString) {
    this.optionsString = optionsString;
  }
}
