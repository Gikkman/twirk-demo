package com.gikk.chat.conditions;

import com.gikk.twirk.enums.USER_TYPE;
import com.gikk.twirk.types.users.TwitchUser;

/**Abstract class for conditions that does not apply to moderators
 *
 * @author Gikkman
 */
public abstract class AbstractFreeForMod implements ICondition
{
	private final boolean active;

	public AbstractFreeForMod()
	{
		this(true);
	}

	public AbstractFreeForMod(boolean active)
	{
		this.active = active;
	}

	@Override
	public final boolean check(String command, TwitchUser user)
	{
		if (active && user.getUserType().value >= USER_TYPE.MOD.value)
		{
			return true;
		}
		return iCheck(command, user);
	}

	@Override
	public final void apply(String command, TwitchUser user)
	{
		if (active && user.getUserType().value >= USER_TYPE.MOD.value)
		{
			return;
		}
		iApply(command, user);
	}

	public abstract boolean iCheck(String command, TwitchUser user);

	protected abstract void iApply(String command, TwitchUser user);
}
