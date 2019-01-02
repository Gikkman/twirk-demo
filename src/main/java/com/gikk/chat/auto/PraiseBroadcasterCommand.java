package com.gikk.chat.auto;

import com.gikk.ChatSingleton;
import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.conditions.IsOwner;
import com.gikk.twirk.types.TagMap;
import com.gikk.twirk.types.emote.Emote;
import com.gikk.twirk.types.users.TwitchUser;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PraiseBroadcasterCommand extends AbstractChatCommand {

    private static final String COMMAND = "!owner";
    private final Set<String> commandWords = new HashSet<>();

    public PraiseBroadcasterCommand() {
        addCondition(new IsOwner());
        commandWords.add(COMMAND);
    }

    @Override
    public Set<String> getCommandWords() {
        return commandWords;
    }

    @Override
    public boolean performCommand(String command, TwitchUser sender, String content, List<Emote> emotes, TagMap tagMap) {
        ChatSingleton.GET().broadcast("Praise be " + sender.getDisplayName());
        return true;
    }
}
