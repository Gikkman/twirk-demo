package com.gikk;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.gikk.chat.AbstractChatCommand;
import com.gikk.chat.listener.TimedMessages;
import com.gikk.twirk.Twirk;
import com.gikk.twirk.TwirkBuilder;
import com.gikk.twirk.events.TwirkListener;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.TwitchUser;
import com.gikk.util.ClassFinder;
import com.gikk.util.Log;

public class Chat
{
	private final Set<AbstractChatCommand> chatCommands = ConcurrentHashMap.newKeySet();
	private Twirk twirk;

	private Chat()
	{
		try
		{
			twirk = createTwirkInstance();
		}
		catch (IOException ex)
		{
			Log.error("Chat setup exception", ex);
			System.exit(-1);
		}

		// Discover all default commands, instantiate them, and add them to the
		// collection of Chat Commands
		List<Class<?>> cmds = ClassFinder.find("com.gikk.chat.auto");
		for (Class<?> c : cmds)
		{
			try
			{
				AbstractChatCommand abs = (AbstractChatCommand) c.newInstance();
				if (!addChatCommand(abs))
				{
					Log.error("Could not add automatic chat command",
						abs.getCommandWords());
				}
			}
			catch (Exception ex)
			{
				Log.error("Could not instantiate AbstractChatCommand",
					c,
					ex.getMessage());
			}
		}
	}

	/***************************************************************************
	 *                          CHAT INTERACTION
	 **************************************************************************/

	/**Fetches the Chat singleton object
	 * @return the Chat
	 */
	public static Chat GET()
	{
		return INTERNAL.INSTANCE;
	}

	/**Broadcasts a message in the connected chat channel
	 *
	 * @param message the message
	 */
	public void broadcast(String message)
	{
		if (message == null || message.isEmpty())
		{
			return;
		}

		twirk.channelMessage(message);
	}

	/**Broadcasts a message in the connected chat channel, with a
	 * {@code @sender: } prefix.
	 *
	 * @param atSender the sender
	 * @param message the message
	 */
	public void broadcast(TwitchUser atSender, String message)
	{
		if (message == null || message.isEmpty())
		{
			return;
		}
		twirk.channelMessage("@" + atSender.getDisplayName() + ": " + message);
	}

	/***************************************************************************
	 *                          ADD / REMOVE
	 **************************************************************************/

	/**Adds a chat command to the chat
	 *
	 * @param command the command
	 * @return {@true} if the command was added
	 */
	public synchronized boolean addChatCommand(AbstractChatCommand command)
	{
		return chatCommands.add(command);
	}

	/**Removes a chat command to the chat
	 *
	 * @param command the command
	 * @return {@true} if the command was removed
	 */
	public synchronized boolean removeChatCommand(AbstractChatCommand command)
	{
		return chatCommands.remove(command);
	}

	public synchronized String getKeywords()
	{
		return chatCommands.stream()
			.map(AbstractChatCommand::getCommandWords)
			.flatMap(Collection::stream)
			.collect(Collectors.joining(" "));
	}

	/***************************************************************************
	 *                          PRIVATE
	 **************************************************************************/
	private void doReconnect(int attempt)
	{
		try
		{
			Log.info("Reconnect attemtp " + attempt);
			boolean connected = false;
			try
			{
				connected = twirk.connect();
			}
			catch (IOException | InterruptedException ex)
			{
				Log.info("Reconnect exception", ex);
			}

			if (connected)
			{
				return;
			}

			// Attempt to reconnect 10 times. If all those failed, attempt to dispose of this resource and create
			// a new one
			if (attempt < 10)
			{
				doReconnect(attempt + 1);
			}
			else
			{

				doRebuild(1);
			}
		}
		catch (Exception e)
		{
			Log.error("Unknown reconnect error", attempt, e);
			twirk.close();
		}
	}

	private void doRebuild(int attempt)
	{
		try
		{
			Log.info("Rebuild attemtp " + attempt);
			boolean connected = false;
			try
			{
				Twirk newTwirk = createTwirkInstance();
				Twirk oldTwirk = twirk;

				twirk = newTwirk;
				connected = twirk.connect();
				oldTwirk.close();
			}
			catch (IOException | InterruptedException ex)
			{
				Log.info("Rebuild exception", ex);
			}

			if (connected)
			{
				return;
			}

			// Give it 5 rebuild attempts, and after that we give up
			if (attempt < 5)
			{
				doRebuild(attempt + 1);
			}
			else
			{
				Log.error("All rebuild attempts failed. Twitch unreachable. Shutting down Twirk.");
				twirk.close();
			}
		}
		catch (Exception e)
		{
			Log.error("Unknown reconnect error", attempt, e);
			twirk.close();
		}
	}

	private Twirk createTwirkInstance() throws IOException
	{
		// Add a hashtag to the channel, if the writer had forgotten it
		String channel = SystemConfig.BOT_CHANNEL.startsWith("#") ? SystemConfig.BOT_CHANNEL : "#" + SystemConfig.BOT_CHANNEL;

		// Connect to IRC chat
		Twirk t = new TwirkBuilder(SystemConfig.BOT_CHANNEL, SystemConfig.BOT_USER, SystemConfig.BOT_PASSWORD)
			.setVerboseMode(true)
			.build();

		t.addIrcListener(new InternalListerenr());
		t.addIrcListener(new TimedMessages());

		return t;
	}

	/***************************************************************************
	 *                          SINGLETON INTENRALS
	 **************************************************************************/

	static class INTERNAL
	{
		private static final Chat INSTANCE = new Chat();

		static void INIT()
		{
			try
			{
				INSTANCE.twirk.connect();
			}
			catch (IOException ex)
			{
				Log.error("IOException connection to Twitch chat", ex.getMessage());
			}
			catch (InterruptedException ex)
			{
				Log.error("Iterrupted while connecting to Twitch chat", ex.getMessage());
			}
		}

		static void QUIT()
		{
			INSTANCE.twirk.close();
		}
	}

	/***************************************************************************
	 *                          INTENRAL CLASSES
	 **************************************************************************/
	private class InternalListerenr implements TwirkListener
	{
		@Override
		public void onDisconnect()
		{
			doReconnect(1);
		}

		@Override
		public void onPrivMsg(TwitchUser twitchUser, TwitchMessage twitchMessage)
		{
			String message = twitchMessage.getContent().trim();
			List<String> split = Arrays.stream(message.split("\\s", 2))
				.collect(Collectors.toList());

			String command = split.size() > 0 ? split.get(0).trim() : "";
			String content = split.size() > 1 ? split.get(1).trim() : "";

			if (command.isEmpty())
			{
				return;
			}

			// Call all commands that listens to this keyword
			for (AbstractChatCommand cmd : chatCommands)
			{
				if (cmd.getCommandWords().contains(command))
				{
					cmd.act(command, twitchUser, content, twitchMessage.getEmotes());
				}
			}
		}
	}
}
