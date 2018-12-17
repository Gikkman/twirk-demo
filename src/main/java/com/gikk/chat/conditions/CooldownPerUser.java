package com.gikk.chat.conditions;

import com.gikk.twirk.types.users.TwitchUser;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adds a per-user cooldown to a command. This will limit each user from using
 * the command more than once every [COOLDOWN] milliseconds
 *
 * @author Gikkman
 */
public class CooldownPerUser extends AbstractFreeForMod {

    private final long cooldownMillis;
    private final Map<Long, Long> lastUsages = new ConcurrentHashMap<>();

    public CooldownPerUser(long cooldownMillis) {
        this.cooldownMillis = cooldownMillis;
    }

    @Override
    public boolean iCheck(String command, TwitchUser user) {
        Long lastUsage = lastUsages.getOrDefault(user.getUserID(), 0L);
        return System.currentTimeMillis() - lastUsage > cooldownMillis;
    }

    @Override
    public Optional<String> getResponse(String command, TwitchUser user) {
        Long lastUsage = lastUsages.get(user.getUserID());
        long millisPassed = System.currentTimeMillis() - lastUsage;
        long millisLeft = cooldownMillis - millisPassed;
        return Optional.of("@" + user.getDisplayName() + ": " + command + " is on "
                + "cooldown for an additional " + millisLeft / 1000L
                + " seconds for you");
    }

    @Override
    public void iApply(String command, TwitchUser user) {
        lastUsages.put(user.getUserID(), System.currentTimeMillis());
    }
}
