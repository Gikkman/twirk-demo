package com.gikk.chat.auto;

import com.gikk.ChatService;
import com.gikk.SystemConfig;
import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.conditions.CooldownPerCommand;
import com.gikk.twirk.types.emote.Emote;
import com.gikk.twirk.types.users.TwitchUser;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Gikkman
 */
public class DiceGameCommand extends AbstractChatCommand {

    private final static String COMMAND = "!dice";
    private final static int COOLDOWN_MILLIS = 30 * 1000;  // 30 seconds
    private static final Map<Integer, Integer> PAYOUTS = new HashMap<>();

    static {
        // Roll 10 to get 3 times money, 11 to get 6 times, 12 to get 15 times
        PAYOUTS.put(10, 3);
        PAYOUTS.put(11, 6);
        PAYOUTS.put(12, 15);
    }

    private final String currency;
    private final Set<String> commandWords = new HashSet<>();

    public DiceGameCommand(ChatService chatService, SystemConfig systemConfig) {
        super(chatService);
        this.currency = systemConfig.getCurrency();
        
        commandWords.add(COMMAND);
        addCondition(new CooldownPerCommand(COOLDOWN_MILLIS));
    }

    @Override
    public Set<String> getCommandWords() {
        return commandWords;
    }

    @Override
    public boolean performCommand(String command, TwitchUser sender, String content, List<Emote> emotes) {
        try {
            Double wagerSum = Double.parseDouble(content);

            Random rng = new Random();
            int d1 = rng.nextInt(6) + 1; // RNG 1-6 (inclusive)
            int d2 = rng.nextInt(6) + 1;

            Integer winMultiplier = PAYOUTS.get(d1 + d2);
            chatService.broadcast(sender, "You won " + winMultiplier * wagerSum + " " + currency);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
