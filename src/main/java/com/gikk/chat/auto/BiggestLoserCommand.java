package com.gikk.chat.auto;

import com.gikk.ChatService;
import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.conditions.CooldownPerCommand;
import com.gikk.twirk.types.emote.Emote;
import com.gikk.twirk.types.users.TwitchUser;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Gikkman
 */
public class BiggestLoserCommand extends AbstractChatCommand {

    private final Set<String> COMMAND = new HashSet<>();

    public BiggestLoserCommand(ChatService chatService) {
        super(chatService);
        COMMAND.add("!biggestloser");
        addCondition(new CooldownPerCommand(60 * 1000));
    }

    @Override
    public Set<String> getCommandWords() {
        return COMMAND;
    }

    @Override
    public boolean performCommand(String command, TwitchUser sender, String content, List<Emote> emotes) {
        String out = "You, " + sender.getDisplayName() + ", is the biggest loser. Sorry Kappa";
        chatService.broadcast(out);
        return true;
    }
}
