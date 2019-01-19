package com.gikk.chat.manual;

import com.gikk.ChatService;
import com.gikk.SchedulerService;
import com.gikk.SystemConfig;
import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.conditions.CooldownPerUser;
import com.gikk.twirk.types.emote.Emote;
import com.gikk.twirk.types.users.TwitchUser;
import com.gikk.util.Touple;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Gikkman
 */
public class BidWarCommand extends AbstractChatCommand {

    private final static String COMMAND = "!bid";
    private final Set<String> commandWords = new HashSet<>();
    private final SchedulerService schedulerService;
    private final String currency;

    private Runnable onEnd;

    // Start at -15 to give it 15 seconds before it
    // starts calling potential winners
    private final AtomicInteger seconds = new AtomicInteger(-15);
    private Future future = null;
    private Touple<TwitchUser, Long> highestBid = null;

    public BidWarCommand(ChatService chatService, SchedulerService schedulerService, SystemConfig systemConfig) {
        super(chatService);
        this.schedulerService = schedulerService;
        this.currency = systemConfig.getCurrency();
        commandWords.add(COMMAND);
        
        addCondition(new CooldownPerUser(500));
    }
    
    public void setOnEnd(Runnable onEnd) {
        this.onEnd = onEnd;
    }

    @Override
    public Set<String> getCommandWords() {
        return commandWords;
    }

    @Override
    public boolean performCommand(String command, TwitchUser sender, String content, List<Emote> emotes) {
        long bid = 0;
        try {
            String amount = content.split("\\s", 2)[0];
            Double dBid = Double.parseDouble(amount);   //Not safe if people write bogus
            bid = dBid.longValue();
        } catch (Exception ex) {
            chatService.broadcast(sender, "Could not parse a valid number from "
                    + "your bid attempt DansGame");
            return false;
        }
        long senderBalance = 100L; // If you store balances somewhere, get them here

        if (senderBalance < bid) {
            chatService.broadcast(sender, "You can't afford your bid of " + bid
                    + " with your current "
                    + senderBalance + " " + currency
                    + " DansGame");
            return true;
        }

        if (highestBid == null || bid > highestBid.right) {
            highestBid = new Touple<>(sender, bid);
            resetBidCallouts();
        }
        return true;
    }

    private void resetBidCallouts() {
        if (future == null) {
            future = schedulerService.repeatedTask(1000, 1000, this::tick);
        }
        // Since timer starts at -15, we let those first 15 seconds run no matter what
        if (seconds.get() > 0) {
            seconds.set(0);
        }
    }

    private void tick() {
        int currentSecond = seconds.getAndIncrement();

        // We cannot proceed without a bid.
        // If no bids came in the first 30 seconds, close the bid war
        if (highestBid == null) {
            if (currentSecond == 15) {
                future.cancel(false);
                chatService.broadcast("No bids were entered in 45 seconds. "
                        + "Shutting down bid war");
                onEnd.run();
            } else {
                return;
            }
        }

        // Here, we know we have a valid bid
        Long bid = highestBid.right;
        TwitchUser leader = highestBid.left;

        if (currentSecond % 5 == 0) {
            System.out.println("BidWar Timer: " + currentSecond);
        }

        if (currentSecond == 5) {
            chatService.broadcast(bid + " " + currency + " from "
                    + leader.getDisplayName() + "!"
                    + " Going once! PogChamp");
        } else if (currentSecond == 10) {
            chatService.broadcast("Gooooing twice! PogChamp PogChamp PogChamp");
        } else if (currentSecond == 15) {
            future.cancel(false);
            chatService.broadcast("SOLD!!! FeelsRareMan With a winning bid of "
                    + bid + " " + currency + ", "
                    + leader.getDisplayName() + " is our winner! FeelsRareMan");
            onEnd.run();

            // Remember to actually remove currency bidded here
        }
    }
}
