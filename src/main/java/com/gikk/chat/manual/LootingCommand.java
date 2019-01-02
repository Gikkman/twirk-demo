package com.gikk.chat.manual;

import com.gikk.ChatSingleton;
import com.gikk.SchedulerSingleton;
import com.gikk.SystemConfig;
import com.gikk.chat.AbstractChatCommand;
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
public class LootingCommand extends AbstractChatCommand {

    private final static String COMMAND = "LootTheRoom";
    private final static int LOOT_TIME_MINUTES = 3;

    private final Set<String> commandWords = new HashSet<>();
    private final Set<TwitchUser> lootingUsers = new HashSet<>();

    private final String currency;
    private final double payout;

    private LootingCommand(boolean completed, long secondsPlayed) {
        commandWords.add(COMMAND);
        currency = SystemConfig.CURRENCY;
        payout = 30;
    }

    private static void enableLooting(boolean completed, long secondsPlayed) {
        LootingCommand cmd = new LootingCommand(completed, secondsPlayed);
        ChatSingleton.GET().addChatCommand(cmd);
        SchedulerSingleton.GET().executeTask(cmd::prompt);
    }

    @Override
    public Set<String> getCommandWords() {
        return commandWords;
    }

    @Override
    public boolean performCommand(String command, TwitchUser sender, String content, List<Emote> emotes, TagMap tagMap) {
        lootingUsers.add(sender);
        return true;
    }

    private void prompt() {
        long payoutFormated = (long) payout;
        ChatSingleton.GET().broadcast("A quest is completed!"
                + " Hurry, join on the looting! "
                + "Type " + COMMAND + " to loot your share of"
                + " " + payoutFormated + " " + currency);
        SchedulerSingleton.GET().delayedTask((LOOT_TIME_MINUTES - 1) * 60 * 1000, this::latePrompt);
        SchedulerSingleton.GET().delayedTask((LOOT_TIME_MINUTES) * 60 * 1000, this::payout);
    }

    private void payout() {
        ChatSingleton.GET().removeChatCommand(this);
        StringBuilder b = new StringBuilder();

        for (TwitchUser u : lootingUsers) {
            if (b.length() > 0) {
                b.append(", ");
            }
            b.append(u.getDisplayName());
        }
        long payoutFormated = (long) payout;
        ChatSingleton.GET().broadcast("Good looting everyone! Payed out "
                + payoutFormated + " " + currency + " to the"
                + " following viewers:");
        ChatSingleton.GET().broadcast(b.toString());

        // Do payout
    }

    private void latePrompt() {
        long payoutFormatted = (long) payout;
        ChatSingleton.GET().broadcast("Only one minute of looting left! Hurry, or you'll"
                + " miss the " + payoutFormatted + " " + currency
                + "! Type LootTheRoom to get your share!");
    }
}
