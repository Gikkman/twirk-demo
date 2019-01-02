package com.gikk.chat.auto;

import com.gikk.ChatSingleton;
import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.conditions.CooldownPerCommand;
import com.gikk.twirk.types.TagMap;
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

    public BiggestLoserCommand() {
        COMMAND.add("!biggestloser");
        addCondition(new CooldownPerCommand(60 * 1000));
    }

    @Override
    public Set<String> getCommandWords() {
        return COMMAND;
    }

    @Override
    public boolean performCommand(String command, TwitchUser sender, String content, List<Emote> emotes, TagMap tagMap) {
        String out = "You, " + sender.getDisplayName() + ", is the biggest loser. Sorry Kappa";
        ChatSingleton.GET().broadcast(out);
        return true;
    }
}
