package com.gikk.chat.listener;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.gikk.Chat;
import com.gikk.SchedulerSingleton;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;

/**
 *
 * @author Gikkman
 */
public class TimedMessages implements TwirkListener
{
	// Make sure you got at least two messages here, or this will break
	private final static String[] MESSAGES = {
		"This is another timed message",
		"This is a timed message"
	};

	private final Random rng = new Random();
	private final Set<Long> seen = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private int chatCount = 0;

	private long lastMessage = System.currentTimeMillis();
	private int previousIndex = -1;

	public TimedMessages()
	{
		SchedulerSingleton.GET().repeatedTask(5 * 60 * 1000, 5 * 60 * 1000, this::tryToSend);
	}

	@Override
	public void onPrivMsg(TwitchUser sender, TwitchMessage message)
	{
		seen.add(sender.getUserID());
		chatCount++;
	}

	private void tryToSend()
	{
		long millisSinceLastMessage = System.currentTimeMillis() - lastMessage;
		if ((seen.size() > 5 || chatCount > 30) && millisSinceLastMessage > 30 * 60 * 1000)
		{
			lastMessage = System.currentTimeMillis();

			int messageIndex = getRandomIndex();
			String message = MESSAGES[messageIndex];

			Chat.GET().broadcast(message);
			previousIndex = messageIndex;

			seen.clear();
			chatCount = 0;
		}
	}

	private int getRandomIndex()
	{
		int randomNumber = -1;
		do
		{
			randomNumber = rng.nextInt(MESSAGES.length);
		}
		while (randomNumber == previousIndex);
		return randomNumber;
	}
}
