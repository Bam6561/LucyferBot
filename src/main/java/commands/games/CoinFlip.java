package commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

public class CoinFlip extends Command {
  public CoinFlip() {
    this.name = "flip";
    this.aliases = new String[]{"coinflip", "coin", "flip"};
    this.arguments = "[0]Once [1]NumberOfFlips";
    this.help = "Flips a coin.";
  }

  // Sends an embed containing results of x number of coin flips
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    switch (numberOfArguments) {
      case 0 -> oneCoinFlip(ce); // Flip a coin once
      case 1 -> multipleCoinFlips(ce, arguments); // Flip a coin multiple times
      default -> ce.getChannel().sendMessage("Invalid number of arguments.").queue(); // Invalid arguments
    }
  }

  // Flips a coin once
  private void oneCoinFlip(CommandEvent ce) {
    Random random = new Random();
    String flipResult = "";

    if (random.nextInt(2) == 0) {
      flipResult += "The coin landed on **Heads**.";
    } else {
      flipResult += "The coin landed on **Tails**.";
    }

    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Coin Flip__");
    display.setDescription(flipResult);

    Settings.sendEmbed(ce, display);
  }

  // Flips a coin multiple times
  private void multipleCoinFlips(CommandEvent ce, String[] arguments) {
    try { // Ensure requested number of flips is an integer
      int numberOfFlips = Integer.parseInt(arguments[1]);
      boolean validNumberOfFlips = (numberOfFlips >= 1) && (numberOfFlips <= 10);
      if (validNumberOfFlips) {
        multipleFlipResults(ce, numberOfFlips);
      } else {
        ce.getChannel().sendMessage("Specify an integer between (1-10) times to flip the coin.").queue();
      }
    } catch (NumberFormatException error) { // Non-integer input
      ce.getChannel().sendMessage("Specify an integer between (1-10) times to flip the coin.").queue();
    }
  }

  // Generates multiple coin flip results
  private void multipleFlipResults(CommandEvent ce, int numberOfFlips) {
    Random rand = new Random();
    StringBuilder flipResults = new StringBuilder();

    for (int i = 0; i < numberOfFlips; i++) { // Generate list of flip results
      if (rand.nextInt(2) == 0) {
        flipResults.append("\n").append(i + 1).append(": **(Heads)** ");
      } else {
        flipResults.append("\n").append(i + 1).append(": **(Tails)** ");
      }
    }

    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Coin Flips__");
    display.setDescription(flipResults.toString());

    Settings.sendEmbed(ce, display);
  }
}