package com.gikk.chat.conditions;

import java.util.Optional;

import com.gikk.twirk.enums.USER_TYPE;
import com.gikk.twirk.types.users.TwitchUser;

/**Adds a requirement that the user is at least a moderator
 *
 * @author Gikkman
 */
public class IsModerator implements ICondition
{

	@Override
	public boolean check(String command, TwitchUser user)
	{
		return user.getUserType().value >= USER_TYPE.MOD.value;
	}

	@Override
	public Optional<String> getResponse(String command, TwitchUser user)
	{
		return Optional.empty();
	}

	@Override
	public void apply(String command, TwitchUser user)
	{
	}
}
