package com.gikk.chat.conditions;

import java.util.Optional;

import com.gikk.twirk.types.users.TwitchUser;

/**Base interface for chat command conditions
 *
 * @author Gikkman
 */
public interface ICondition
{
	/**Checks whether this condition passes or not
	 *
	 * @param user The user we're checking the condition for
	 * @return {@code true} if this condition passes
	 */
	boolean check(String command, TwitchUser user);

	/**What response (if any) we should give the user, should the condition not
	 * pass. Not all conditions have a response.
	 *
	 * @param user The user we checked the condition for
	 * @return a explanation why the condition failed
	 */
	Optional<String> getResponse(String command, TwitchUser user);

	/**Updates this condition, for a particular user. This might be stuff such
	 * as updating a timer, removing currency, or the similar.
	 *
	 * @param user the user which issued the command
	 */
	void apply(String command, TwitchUser user);
}
