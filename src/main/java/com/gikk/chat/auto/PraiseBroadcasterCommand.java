package com.gikk.chat.auto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gikk.Chat;
import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.conditions.IsOwner;
import com.gikk.twirk.types.emote.Emote;
import com.gikk.twirk.types.users.TwitchUser;

public class PraiseBroadcasterCommand extends AbstractChatCommand
{
	private static final String COMMAND = "!owner";
	private final Set<String> commandWords = new HashSet<>();

	public PraiseBroadcasterCommand()
	{
		addCondition(new IsOwner());
		commandWords.add(COMMAND);
	}

	@Override
	public Set<String> getCommandWords()
	{
		return commandWords;
	}

	@Override
	public boolean performCommand(String command, TwitchUser sender, String content, List<Emote> emotes)
	{
		Chat.GET().broadcast("Praise be " + sender.getDisplayName());
		return true;
	}
}
