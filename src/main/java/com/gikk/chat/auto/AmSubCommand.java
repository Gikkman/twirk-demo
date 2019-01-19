package com.gikk.chat.auto;

import com.gikk.ChatService;
import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.conditions.CooldownPerUser;
import com.gikk.twirk.types.emote.Emote;
import com.gikk.twirk.types.users.TwitchUser;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AmSubCommand extends AbstractChatCommand {

    private static final String COMMAND = "!amsub";
    private final Set<String> commandWords = new HashSet<>();

    public AmSubCommand(ChatService chatService) {
        super(chatService);
        addCondition(new CooldownPerUser(30 * 60 * 1000)); // 30 minutes
        commandWords.add(COMMAND);
    }

    @Override
    public Set<String> getCommandWords() {
        return commandWords;
    }

    @Override
    public boolean performCommand(String command, TwitchUser sender, String content, List<Emote> emotes) {
        if (sender.isSub() || sender.isTurbo()) {
            chatService.broadcast("Yeah! You are a sub! Best viewer there, " + sender.getDisplayName());
        } else {
            chatService.broadcast("Na, just a normie there, " + sender.getDisplayName());
        }
        return true;
    }
}
