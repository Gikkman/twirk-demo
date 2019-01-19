package com.gikk.chat.auto;

import com.gikk.ChatService;
import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.conditions.IsOwner;
import com.gikk.twirk.types.emote.Emote;
import com.gikk.twirk.types.users.TwitchUser;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PraiseBroadcasterCommand extends AbstractChatCommand {

    private static final String COMMAND = "!owner";
    private final Set<String> commandWords = new HashSet<>();

    public PraiseBroadcasterCommand(ChatService chatService) {
        super(chatService);
        addCondition(new IsOwner());
        commandWords.add(COMMAND);
    }

    @Override
    public Set<String> getCommandWords() {
        return commandWords;
    }

    @Override
    public boolean performCommand(String command, TwitchUser sender, String content, List<Emote> emotes) {
        chatService.broadcast("Praise be " + sender.getDisplayName());
        return true;
    }
}
