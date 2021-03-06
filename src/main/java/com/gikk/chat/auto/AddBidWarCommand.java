package com.gikk.chat.auto;

import com.gikk.ChatSingleton;
import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.conditions.CooldownPerCommand;
import com.gikk.chat.conditions.IsModerator;
import com.gikk.chat.manual.BidWarCommand;
import com.gikk.twirk.types.emote.Emote;
import com.gikk.twirk.types.users.TwitchUser;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Gikkman
 */
public class AddBidWarCommand extends AbstractChatCommand {

    private static final String COMMAND = "!bidwar";
    private final Set<String> commandWords = new HashSet<>();

    private boolean bidOngoing = false;
    private AbstractChatCommand bidCommand = null;

    public AddBidWarCommand() {
        commandWords.add(COMMAND);
        addCondition(new IsModerator());
        addCondition(new CooldownPerCommand(30 * 1000));
    }

    @Override
    public Set<String> getCommandWords() {
        return commandWords;
    }

    @Override
    public boolean performCommand(String command, TwitchUser sender, String content, List<Emote> emotes) {
        if (bidOngoing) {
            return false;
        }
        if (content.isEmpty()) {
            ChatSingleton.GET().broadcast("Usage: " + COMMAND + " <PRIZE>");
            return false;
        }

        bidOngoing = true;
        bidCommand = new BidWarCommand(this::endBidWar);
        ChatSingleton.GET().broadcast("A bidwar has started! Prize: " + content + ". To place your bid, type !bid <amount>");
        ChatSingleton.GET().addChatCommand(bidCommand);
        return true;
    }

    public void endBidWar() {
        ChatSingleton.GET().removeChatCommand(bidCommand);
        bidCommand = null;
        bidOngoing = false;
    }
}
