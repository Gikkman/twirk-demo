package com.gikk.chat.conditions;

import com.gikk.twirk.types.users.TwitchUser;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Adds a cooldown to a command. The command will not be issuable more than once
 * every [COOLDOWN] milliseconds.
 *
 * @author Gikkman
 */
public class CooldownPerCommand extends AbstractFreeForMod {

    private final long cooldownMillis;

    private final Map<String, Long> lastUsages = new HashMap<>();

    public CooldownPerCommand(long cooldownMillis) {
        this.cooldownMillis = cooldownMillis;
    }

    @Override
    public boolean iCheck(String command, TwitchUser user) {
        long lastUsage = lastUsages.getOrDefault(command, 0L);
        return System.currentTimeMillis() - lastUsage > cooldownMillis;
    }

    @Override
    public Optional<String> getResponse(String command, TwitchUser user) {
        return Optional.empty();
    }

    @Override
    public void iApply(String command, TwitchUser user) {
        lastUsages.put(command, System.currentTimeMillis());
    }
}
