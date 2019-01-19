package com.gikk.chat.auto;

import com.gikk.ChatService;
import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.conditions.CooldownPerCommand;
import com.gikk.chat.conditions.IsModerator;
import com.gikk.twirk.types.emote.Emote;
import com.gikk.twirk.types.users.TwitchUser;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandsCommand extends AbstractChatCommand {

    private static final String COMMAND = "!commands";

    private final Set<String> commandWords = new HashSet<>();

    public CommandsCommand(ChatService chatService) {
        super(chatService);
        addCondition(new CooldownPerCommand(5 * 1000)); // 5 seconds
        addCondition(new IsModerator());
        commandWords.add(COMMAND);
    }

    @Override
    public Set<String> getCommandWords() {
        return commandWords;
    }

    @Override
    public boolean performCommand(String command, TwitchUser sender, String content, List<Emote> emotes) {
        chatService.broadcast(chatService.getKeywords());
        return true;
    }
}
