package com.gikk.chat.auto;

import com.gikk.ChatService;
import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.ChatCommandFactoryService;
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
    private final ChatCommandFactoryService chatCommandFactoryService;
    private final Set<String> commandWords = new HashSet<>();

    private boolean bidOngoing = false;
    private BidWarCommand bidCommand = null;

    public AddBidWarCommand(ChatService chatService, ChatCommandFactoryService chatCommandFactoryService) {
        super(chatService);
        this.chatCommandFactoryService = chatCommandFactoryService;
        
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
            chatService.broadcast("Usage: " + COMMAND + " <PRIZE>");
            return false;
        }
        bidOngoing = true;
        
        bidCommand = chatCommandFactoryService.create(BidWarCommand.class);
        bidCommand.setOnEnd(this::endBidWar);
        
        chatService.broadcast("A bidwar has started! Prize: " + content + ". To place your bid, type !bid <amount>");
        chatService.addChatCommand(bidCommand);
        
        return true;
    }

    public void endBidWar() {
        chatService.removeChatCommand(bidCommand);
        bidCommand = null;
        bidOngoing = false;
    }
}
