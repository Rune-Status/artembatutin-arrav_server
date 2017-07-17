package net.edge.net.packet.in;

import net.edge.net.codec.IncomingMsg;
import net.edge.net.packet.IncomingPacket;
import net.edge.world.node.actor.player.Player;
import net.edge.world.node.actor.player.assets.activity.ActivityManager;

/**
 * The message sent from the client when a player adds, removes, or sends someone
 * a message.
 * @author lare96 <http://github.com/lare96>
 */
public final class SocialPacket implements IncomingPacket {
	
	@Override
	public void handle(Player player, int opcode, int size, IncomingMsg payload) {
		if(player.getActivityManager().contains(ActivityManager.ActivityType.PRIVATE_MESSAGE))
			return;
		
		switch(opcode) {
			case 188:
				addFriend(player, payload);
				break;
			case 215:
				removeFriend(player, payload);
				break;
			case 133:
				addIgnore(player, payload);
				break;
			case 74:
				removeIgnore(player, payload);
				break;
			case 126:
				sendMessage(player, size, payload);
				break;
		}
		player.getActivityManager().execute(ActivityManager.ActivityType.PRIVATE_MESSAGE);
	}
	
	/**
	 * Handles the adding of a new friend.
	 * @param player  the player to handle this for.
	 * @param payload the payloadfer used for reading sent data.
	 */
	private void addFriend(Player player, IncomingMsg payload) {
		long name = payload.getLong();
		if(name < 0)
			return;
		player.getPrivateMessage().addFriend(name);
	}
	
	/**
	 * Handles the removing of an existing friend.
	 * @param player  the player to handle this for.
	 * @param payload the payloadfer used for reading sent data.
	 */
	private void removeFriend(Player player, IncomingMsg payload) {
		long name = payload.getLong();
		if(name < 0)
			return;
		player.getPrivateMessage().removeFriend(name);
	}
	
	/**
	 * Handles the adding of a new ignore.
	 * @param player  the player to handle this for.
	 * @param payload the payloadfer used for reading sent data.
	 */
	private void addIgnore(Player player, IncomingMsg payload) {
		long name = payload.getLong();
		if(name < 0)
			return;
		player.getPrivateMessage().addIgnore(name);
	}
	
	/**
	 * Handles the removing of an existing ignore.
	 * @param player  the player to handle this for.
	 * @param payload the payloadfer used for reading sent data.
	 */
	private void removeIgnore(Player player, IncomingMsg payload) {
		long name = payload.getLong();
		if(name < 0)
			return;
		player.getPrivateMessage().removeIgnore(name);
	}
	
	/**
	 * Handles the sending of a private message.
	 * @param player  the player to handle this for.
	 * @param payload the payloadfer used for reading sent data.
	 */
	private void sendMessage(Player player, int size, IncomingMsg payload) {
		long to = payload.getLong();
		int newSize = size - 8;
		byte[] message = payload.getBytes(newSize);
		if(to < 0 || newSize < 0 || message == null)
			return;
		if(!player.getFriends().contains(to)) {
			player.message("You cannot send a message to a " + "player not on your friends list!");
			return;
		}
		player.getPrivateMessage().sendPrivateMessage(to, message, newSize);
	}
}
