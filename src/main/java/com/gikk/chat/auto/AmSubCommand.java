package com.gikk.chat.auto;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gikk.Chat;
import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.conditions.CooldownPerUser;
import com.gikk.twirk.types.emote.Emote;
import com.gikk.twirk.types.users.TwitchUser;

public class AmSubCommand extends AbstractChatCommand
{
	private static final String COMMAND = "!amsub";
	private final Set<String> commandWords = new HashSet<>();

	public AmSubCommand()
	{
		addCondition(new CooldownPerUser(30 * 60 * 1000)); // 30 minutes
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
		if (sender.isSub() || sender.isTurbo())
		{
			Chat.GET().broadcast("Yeah! You are a sub! Best viewer there, " + sender.getDisplayName());
		}
		else
		{
			Chat.GET().broadcast("Na, just a normie there, " + sender.getDisplayName());
		}
		return true;
	}
}
