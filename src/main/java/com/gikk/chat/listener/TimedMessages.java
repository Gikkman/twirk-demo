package com.gikk.chat.listener;

import com.gikk.ChatService;
import com.gikk.SchedulerService;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gikkman
 */
@Service
public class TimedMessages implements TwirkListener {
    // Make sure you got at least two messages here, or this will break

    private final static String[] MESSAGES = {
        "This is another timed message",
        "This is a timed message"
    };

    private final ChatService chatService;
    private final Random rng = new Random();
    private final Set<Long> seen = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private int chatCount = 0;

    private long lastMessage = 0;
    private int previousIndex = -1;

    @Autowired
    public TimedMessages(ChatService chatService, SchedulerService schedulerService) {
        this.chatService = chatService;
        
        // First message after 30 seconds, then once per 5 minutes
        schedulerService.repeatedTask(30 * 1000, 5 * 60 * 1000, this::tryToSend);
    }
    
    @PostConstruct
    private void postConstruct() {
        chatService.addIrcListener(this);
    }

    @Override
    public void onPrivMsg(TwitchUser sender, TwitchMessage message) {
        seen.add(sender.getUserID());
        chatCount++;
    }

    private void tryToSend() {
        long millisSinceLastMessage = System.currentTimeMillis() - lastMessage;
        if ((seen.size() >= 1 || chatCount >= 5) && millisSinceLastMessage > 30 * 60 * 1000) {
            lastMessage = System.currentTimeMillis();

            int messageIndex = getRandomIndex();
            String message = MESSAGES[messageIndex];

            chatService.broadcast(message);
            previousIndex = messageIndex;

            seen.clear();
            chatCount = 0;
        }
    }

    private int getRandomIndex() {
        int randomNumber = -1;
        do {
            randomNumber = rng.nextInt(MESSAGES.length);
        } while (randomNumber == previousIndex);
        return randomNumber;
    }
}
