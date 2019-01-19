package com.gikk;

import com.gikk.chat.AbstractChatCommand;
import com.gikk.twirk.Twirk;
import com.gikk.twirk.TwirkBuilder;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;
import com.gikk.util.Log;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final Set<AbstractChatCommand> chatCommands = ConcurrentHashMap.newKeySet();
    private final SystemConfig systemConfig;
    private Twirk twirk;

    @Autowired
    private ChatService(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
        try {
            twirk = createTwirkInstance();
        } catch (IOException ex) {
            Log.error("Chat setup exception", ex);
            System.exit(-1);
        }
    }
    
    @PostConstruct
    private void postConstruct() {
        try {
            twirk.connect();
        } catch (IOException ex) {
            Logger.getLogger(ChatService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ChatService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @PreDestroy
    private void preDestroy() {
        twirk.close();
    }

    /* *************************************************************************
     * CHAT INTERACTION
     **************************************************************************/

    /**
     * Broadcasts a message in the connected chat channel
     *
     * @param message the message
     */
    public void broadcast(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }

        twirk.channelMessage(message);
    }

    /**
     * Broadcasts a message in the connected chat channel, with a
	 * {@code @sender: } prefix.
     *
     * @param atSender the sender
     * @param message the message
     */
    public void broadcast(TwitchUser atSender, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        twirk.channelMessage("@" + atSender.getDisplayName() + ": " + message);
    }

    /**
     * *************************************************************************
     * ADD / REMOVE
	 *************************************************************************
     */
    /**
     * Adds a chat command to the chat
     *
     * @param command the command
     * @return true if the command was added
     */
    public final synchronized boolean addChatCommand(AbstractChatCommand command) {
        return chatCommands.add(command);
    }
    
    public final synchronized  void addIrcListener(TwirkListener listener) {
        twirk.addIrcListener(listener);
    }

    /**
     * Removes a chat command to the chat
     *
     * @param command the command
     * @return true if the command was removed
     */
    public synchronized boolean removeChatCommand(AbstractChatCommand command) {
        return chatCommands.remove(command);
    }

    public synchronized String getKeywords() {
        return chatCommands.stream()
                .map(AbstractChatCommand::getCommandWords)
                .flatMap(Collection::stream)
                .collect(Collectors.joining(" "));
    }

    /**
     * *************************************************************************
     * PRIVATE
	 *************************************************************************
     */
    private void doReconnect(int attempt) {
        try {
            Log.info("Reconnect attemtp " + attempt);
            boolean connected = false;
            try {
                connected = twirk.connect();
            } catch (IOException | InterruptedException ex) {
                Log.info("Reconnect exception", ex);
            }

            if (connected) {
                return;
            }

            // Attempt to reconnect 10 times. If all those failed, attempt to dispose of this resource and create
            // a new one
            if (attempt < 10) {
                doReconnect(attempt + 1);
            } else {

                doRebuild(1);
            }
        } catch (Exception e) {
            Log.error("Unknown reconnect error", attempt, e);
            twirk.close();
        }
    }

    private void doRebuild(int attempt) {
        try {
            Log.info("Rebuild attemtp " + attempt);
            boolean connected = false;
            try {
                Twirk newTwirk = createTwirkInstance();
                Twirk oldTwirk = twirk;

                twirk = newTwirk;
                connected = twirk.connect();
                oldTwirk.close();
            } catch (IOException | InterruptedException ex) {
                Log.info("Rebuild exception", ex);
            }

            if (connected) {
                return;
            }

            // Give it 5 rebuild attempts, and after that we give up
            if (attempt < 5) {
                doRebuild(attempt + 1);
            } else {
                Log.error("All rebuild attempts failed. Twitch unreachable. Shutting down Twirk.");
                twirk.close();
            }
        } catch (Exception e) {
            Log.error("Unknown reconnect error", attempt, e);
            twirk.close();
        }
    }

    private Twirk createTwirkInstance() throws IOException {
        // Add a hashtag to the channel, if the writer had forgotten it
        String channel = systemConfig.getChannel();
        channel = channel.startsWith("#") ? channel : "#" + channel;

        // Connect to IRC chat
        Twirk t = new TwirkBuilder(channel, systemConfig.getUsername(), systemConfig.getPassword())
                .setVerboseMode(true)
                .build();

        t.addIrcListener(new InternalListerenr());

        return t;
    }

    /**
     * *************************************************************************
     * INTENRAL CLASSES
	 *************************************************************************
     */
    private class InternalListerenr implements TwirkListener {

        @Override
        public void onDisconnect() {
            doReconnect(1);
        }

        @Override
        public void onPrivMsg(TwitchUser twitchUser, TwitchMessage twitchMessage) {
            String message = twitchMessage.getContent().trim();
            List<String> split = Arrays.stream(message.split("\\s", 2))
                    .collect(Collectors.toList());

            String command = split.size() > 0 ? split.get(0).trim() : "";
            String content = split.size() > 1 ? split.get(1).trim() : "";

            if (command.isEmpty()) {
                return;
            }

            // Call all commands that listens to this keyword
            for (AbstractChatCommand cmd : chatCommands) {
                if (cmd.getCommandWords().contains(command)) {
                    cmd.act(command, twitchUser, content, twitchMessage.getEmotes());
                }
            }
        }
    }
}
