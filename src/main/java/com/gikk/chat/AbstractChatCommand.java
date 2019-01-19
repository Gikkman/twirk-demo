package com.gikk.chat;

import com.gikk.ChatService;
import com.gikk.chat.conditions.ICondition;
import com.gikk.twirk.types.emote.Emote;
import com.gikk.twirk.types.users.TwitchUser;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Gikkman
 */
public abstract class AbstractChatCommand {
    //***********************************************************************************************
    //											VARIABLES
    //***********************************************************************************************
    protected final ChatService chatService;
    private final List<ICondition> conditions = new ArrayList<>();
    
    protected AbstractChatCommand(ChatService chatService) {
        this.chatService = chatService;
    }

    //***********************************************************************************************
    //											PUBLIC
    //***********************************************************************************************
    /**
     * This method is the commands execution.
     *
     */
    public final void act(String command, TwitchUser sender, String content, List<Emote> emotes) {
        // Check if this request passes the required conditions
        // If it doesn't, send appropriate reply (if any) and return
        for (ICondition condition : conditions) {
            if (condition.check(command, sender) == false) {
                condition.getResponse(command, sender).ifPresent(chatService::broadcast);
                return;
            }
        }

        // Now execute the actual command
        boolean success = performCommand(command, sender, content, emotes);

        if (success) {
            // If command executed successfully, apply the conditions
            for (ICondition condition : conditions) {
                condition.apply(command, sender);
            }
        }
    }

    //***********************************************************************************************
    //											PROTECTED
    //***********************************************************************************************
    /**
     * Adds a condition for executing this chat command
     *
     * @param condition
     */
    protected final void addCondition(ICondition condition) {
        conditions.add(condition);
    }

    /**
     * Removes a previously added condition for executing this chat command
     *
     * @param condition
     */
    protected final void removeCondition(ICondition condition) {
        conditions.remove(condition);
    }

    //***********************************************************************************************
    //											ABSTRACT
    //***********************************************************************************************
    /**
     * This method must return the words this command should react to. Be aware
     * that the pattern recognizer is case-sensitive.
     * <p>
     * Several commands can react to the same pattern. If the command wants to
     * react to any incoming message, add the wildcard pattern "*"
     *
     * @return A string, comprising of all words this command reacts to
     */
    public abstract Set<String> getCommandWords();

    /**
     * This method is the commands execution. This will be called whenever a
     * chat line is seen that matches the commandPattern.<p>
     *
     * The return value is used to indicate whether to apply the conditions the
     * command might hold. For example: if the command has a currency cost, the
     * currency will only be deducted if this method returns true.
     *
     * @param command The pattern that caused us to fire this command
     * @param sender The IrcUser who issued the command
     * @param content The rest of the message
     * @return {@code true} if the command performed its duty, {@code false} if
     * not
     */
    public abstract boolean performCommand(String command, TwitchUser sender, String content, List<Emote> emotes);
}
