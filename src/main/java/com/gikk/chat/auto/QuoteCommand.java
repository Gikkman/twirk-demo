package com.gikk.chat.auto;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.gikk.Chat;
import com.gikk.SystemConfig;
import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.conditions.CooldownPerUser;
import com.gikk.twirk.types.emote.Emote;
import com.gikk.twirk.types.users.TwitchUser;

/**
 *
 * @author Gikkman
 */
public class QuoteCommand extends AbstractChatCommand
{
	private final static String COMMAND_GET = "!quote";
	private final static String COMMAND_ADD = "!addquote";
	private final Map<Integer, String> quoteMap = new ConcurrentHashMap<>();

	private final Set<String> commandWords = new HashSet<>();
	private final String currency;

	public QuoteCommand()
	{
		commandWords.add(COMMAND_GET);
		commandWords.add(COMMAND_ADD);
		currency = SystemConfig.CURRENCY;

		addCondition(new CooldownPerUser(60 * 1000));
	}

	@Override
	public Set<String> getCommandWords()
	{
		return commandWords;
	}

	@Override
	public boolean performCommand(String command, TwitchUser sender, String content, List<Emote> emotes)
	{
		switch (command)
		{
			case COMMAND_GET:
				return commandGet(content);
			case COMMAND_ADD:
				return commandAdd(sender, content, emotes);
			default:
				return false;
		}
	}

	private boolean commandGet(String content)
	{
		try
		{
			Integer index = Integer.parseInt(content);
			if (quoteMap.containsKey(index))
			{
				Chat.GET().broadcast(quoteMap.get(index));
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			return false;
		}
	}

	private boolean commandAdd(TwitchUser sender, String content, List<Emote> emotes)
	{
		List<String> parts = Arrays.stream(content.split("\\s", 2))
			.collect(Collectors.toList());
		if (parts.size() < 2)
		{
			showAddUsage();
			return false;
		}

		/**A correct quote is formated:
		 * !quote <USER> "<QUOTE>"
		 *
		 * This block checks that we have a user, and that the quote is
		 * surrounded by "-signs
		 */
		String saidByString = parts.get(0).trim();
		String quote = parts.get(1).trim();
		if (saidByString.isEmpty() || quote.isEmpty())
		{
			showAddUsage();
			return false;
		}

		//If the quote starts or ends with an emote, we gotta add an extra space,
		//so that the emote is displayed despite the "s
		for (Emote e : emotes)
		{
			if (quote.startsWith(e.getPattern()))
			{
				quote = " " + quote;
			}
			if (quote.endsWith(e.getPattern()))
			{
				quote = quote + " ";
			}
		}

		if (!quote.startsWith("\""))
		{
			quote = "\"" + quote;
		}
		if (!quote.endsWith("\""))
		{
			quote += "\"";
		}

		int idx = quoteMap.size() + 1;
		quoteMap.put(idx, quote + " by " + saidByString);
		Chat.GET().broadcast(sender, "Quote added as #" + idx);
		return true;
	}

	private void showAddUsage()
	{
		Chat.GET().broadcast("Usage: " + COMMAND_ADD + "<USER> <QUOTE>");
	}
}
