package com.gikk;

import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        SchedulerSingleton.INTERNAL.INIT();
        ChatSingleton.INTERNAL.INIT();

        System.out.println("Welcome to this Bot example. In this example you will be able \n"
                + "to send and receive messages from a Twitch chat channel. You will \n"
                + "make all input directly here in the command prompt. \n"
                + "I hope you set the credentials in the SystemConfig.java file ^.^\n\n"
                + "Type .quite to end the example. \n\n");

        //As long as we don't type .quit into the command prompt, send everything we type as a message to twitch
        try (Scanner scanner = new Scanner(System.in)) {
            String line;
            while (!(line = scanner.nextLine()).matches(".quit")) {
                ChatSingleton.GET().broadcast(line);
            }
        }

        ChatSingleton.INTERNAL.QUIT();
        SchedulerSingleton.INTERNAL.QUIT();
    }
}
