package commands.audio.managers;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.Random;

public class AudioScheduler extends AudioEventAdapter {
  private final AudioPlayer audioPlayer;
  private ArrayList<AudioTrack> queueList;
  private ArrayList<String> requesterList;
  private Boolean loop = false;

  public AudioScheduler(AudioPlayer audioPlayer) {
    this.audioPlayer = audioPlayer;
    this.queueList = new ArrayList<AudioTrack>();
    this.requesterList = new ArrayList<String>();
  }

  public void queue(AudioTrack audioTrack) {
    if (!this.audioPlayer.startTrack(audioTrack, true)) {
      queueList.add(audioTrack);
    }
  }

  public void nextTrack() {
    if (!queueList.isEmpty()) {
      this.audioPlayer.startTrack(this.queueList.get(0), false);
      queueList.remove(0);
      requesterList.remove(0);
    } else {
      audioPlayer.stopTrack();
      queueList.clear();
      requesterList.clear();
    }
  }

  @Override
  public void onTrackStart(AudioPlayer audioPlayer, AudioTrack audioTrack) {
  }

  @Override
  public void onTrackEnd(AudioPlayer audioPlayer, AudioTrack audioTrack, AudioTrackEndReason endReason) {
    if (endReason.mayStartNext) {
      if (this.loop) {
        this.audioPlayer.startTrack(audioTrack.makeClone(), false);
        return;
      }
      nextTrack();
    }
  }

  public void addToRequesterList(String requester) { // Track requester array
    requesterList.add(requester);
  }

  public void clearQueue(CommandEvent ce) {
    this.queueList.clear();
    this.requesterList.clear();
    StringBuilder queueClearConfirmation = new StringBuilder();
    queueClearConfirmation.append("**Queue Clear:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(queueClearConfirmation).queue();
  }

  public void setLoopState(CommandEvent ce) {
    StringBuilder loopConfirmation = new StringBuilder();
    if (this.loop) {
      this.loop = false;
      loopConfirmation.append("**LOOP:** `OFF` [").append(ce.getAuthor().getAsTag()).append("]");
      ce.getChannel().sendMessage(loopConfirmation).queue();
    } else {
      this.loop = true;
      loopConfirmation.append("**LOOP:** `ON` [").append(ce.getAuthor().getAsTag()).append("]");
      ce.getChannel().sendMessage(loopConfirmation).queue();
    }
  }

  public void getQueue(CommandEvent ce, int queuePage) { // Track queue
    if (!this.queueList.isEmpty()) { // No tracks
      int totalQueuePages = this.queueList.size() / 10; // Full pages
      if ((this.queueList.size() % 10) > 0) { // Partially filled pages
        totalQueuePages += 1;
      }
      if (queuePage >= totalQueuePages) { // Page number cannot exceed total pages
        queuePage = totalQueuePages - 1;
      }
      if (queuePage < 0) { // Page number cannot be below 0
        queuePage = 0;
      }
      int queuePageDisplay = queuePage * 10; // Which queue page to start
      if (queuePageDisplay == this.queueList.size()) { // Don't display 0 ending first
        queuePageDisplay -= 10;
      }
      int lastQueueEntry = Math.min((queuePageDisplay + 10), this.queueList.size()); // Last queue entry to display
      // Display last entries
      // Display only 10 entries at a time
      StringBuilder queueString = new StringBuilder();
      for (int i = queuePageDisplay; i < lastQueueEntry; i++) { // Queue Entries
        queueString.append("**[").append(i + 1).append("]** `").append(queueList.get(i).getInfo().title)
            .append("` ").append(requesterList.get(i)).append("\n");
      }
      AudioTrack audioTrack = audioPlayer.getPlayingTrack();
      EmbedBuilder display = new EmbedBuilder();
      display.setTitle("__**Queue**__");
      StringBuilder description = new StringBuilder();
      description.append("**Now Playing:** `").append(audioPlayer.getPlayingTrack().getInfo().title).
          append("` ").append(audioTrack.getPosition()).append("/").append(audioTrack.getDuration()).
          append("\nPage `").append(queuePage + 1).append("` / `").append(totalQueuePages).append("`");
      display.setDescription(description);
      display.addField("**Tracks:**", String.valueOf(queueString), false);
      Settings.sendEmbed(ce, display);
    } else {
      ce.getChannel().sendMessage("Queue is empty.").queue();
    }
  }

  public void removeQueueEntry(CommandEvent ce, int entryNumber) {
    try {
      entryNumber = entryNumber - 1;
      StringBuilder removeQueueEntryConfirmation = new StringBuilder();
      removeQueueEntryConfirmation.append("**Removed:** **[").append(entryNumber + 1).append("]** `")
          .append(this.queueList.get(entryNumber).getInfo().title).append("`")
          .append(this.requesterList.get(entryNumber))
          .append(" *[").append(ce.getAuthor().getAsTag()).append("]*");
      this.queueList.remove(entryNumber);
      this.requesterList.remove(entryNumber);
      ce.getChannel().sendMessage(removeQueueEntryConfirmation).queue();
    } catch (NullPointerException error) {
      ce.getChannel().sendMessage("Queue entry number does not exist.").queue();
    }
  }

  public void shuffleQueue() {
    Random rand = new Random();
    for (int i = 0; i < queueList.size(); i++) {
      int indexSwitch = rand.nextInt(queueList.size());
      AudioTrack audioTrackTemp = queueList.get(i);
      String stringTemp = requesterList.get(i);
      queueList.set(i, queueList.get(indexSwitch));
      queueList.set(indexSwitch, audioTrackTemp);
      requesterList.set(i, requesterList.get(indexSwitch));
      requesterList.set(indexSwitch, stringTemp);
    }
  }

  public void skipTrack() {
    nextTrack();
  }
}
