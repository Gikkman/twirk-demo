/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gikk.chat;

import com.gikk.ChatService;
import com.gikk.SchedulerService;
import com.gikk.SystemConfig;
import com.gikk.chat.auto.AddBidWarCommand;
import com.gikk.chat.auto.AmSubCommand;
import com.gikk.chat.auto.BiggestLoserCommand;
import com.gikk.chat.auto.CommandsCommand;
import com.gikk.chat.auto.DiceGameCommand;
import com.gikk.chat.auto.PraiseBroadcasterCommand;
import com.gikk.chat.auto.QuoteCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gikkman
 */
@Service
public class ChatCommandFactoryService {
    private final ChatService chatService;
    private final SchedulerService schedulerService;
    private final SystemConfig systemConfig;
    
    @Autowired
    ChatCommandFactoryService(ChatService chatService, SchedulerService schedulerService, SystemConfig systemConfig) {
        this.chatService = chatService;
        this.schedulerService = schedulerService;
        this.systemConfig = systemConfig;
    }
    
    public <T extends AbstractChatCommand> T create(Class<T> type) {
        if(AddBidWarCommand.class.equals(type)) 
            return (T) new AddBidWarCommand(chatService, this);
        else if(AmSubCommand.class.equals(type))
            return (T) new AmSubCommand(chatService);
        else if(BiggestLoserCommand.class.equals(type))
            return (T) new BiggestLoserCommand(chatService);
        else if(CommandsCommand.class.equals(type))
            return (T) new CommandsCommand(chatService);
        else if(DiceGameCommand.class.equals(type))
            return (T) new DiceGameCommand(chatService, systemConfig);
        else if(PraiseBroadcasterCommand.class.equals(type))
            return (T) new PraiseBroadcasterCommand(chatService);
        else if(QuoteCommand.class.equals(type))
            return (T) new QuoteCommand(chatService, systemConfig);
        else
            throw new RuntimeException("No factory configured for class " + type);
    }
}
