package com.gikk;

import com.gikk.chat.ChatCommandFactoryService;
import com.gikk.chat.auto.AddBidWarCommand;
import com.gikk.chat.auto.AmSubCommand;
import com.gikk.chat.auto.BiggestLoserCommand;
import com.gikk.chat.auto.CommandsCommand;
import com.gikk.chat.auto.DiceGameCommand;
import com.gikk.chat.auto.PraiseBroadcasterCommand;
import com.gikk.chat.auto.QuoteCommand;
import java.util.Scanner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        System.out.println("Welcome to this Bot example. In this example you will be able \n"
                + "to send and receive messages from a Twitch chat channel. You will \n"
                + "make all input directly here in the command prompt. \n"
                + "I hope you set the credentials in the SystemConfig.java file ^.^\n\n"
                + "Type .quite to end the example. \n\n");
        
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ChatService chatService = ctx.getBean(ChatService.class);
        ChatCommandFactoryService chatCommandFactoryService = ctx.getBean(ChatCommandFactoryService.class);
        chatService.addChatCommand(chatCommandFactoryService.create(AddBidWarCommand.class));
        chatService.addChatCommand(chatCommandFactoryService.create(AmSubCommand.class));
        chatService.addChatCommand(chatCommandFactoryService.create(BiggestLoserCommand.class));
        chatService.addChatCommand(chatCommandFactoryService.create(CommandsCommand.class));
        chatService.addChatCommand(chatCommandFactoryService.create(DiceGameCommand.class));
        chatService.addChatCommand(chatCommandFactoryService.create(PraiseBroadcasterCommand.class));
        chatService.addChatCommand(chatCommandFactoryService.create(QuoteCommand.class));
        
        //As long as we don't type .quit into the command prompt, send everything we type as a message to twitch
        try (Scanner scanner = new Scanner(System.in)) {
            String line;
            while (!(line = scanner.nextLine()).matches(".quit")) {
                chatService.broadcast(line);
            }
        }
        ctx.close();
    }
}
