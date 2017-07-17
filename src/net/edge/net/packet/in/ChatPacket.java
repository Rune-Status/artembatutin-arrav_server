package net.edge.net.packet.in;

import net.edge.net.PunishmentHandler;
import net.edge.net.codec.IncomingMsg;
import net.edge.net.codec.ByteTransform;
import net.edge.net.packet.IncomingPacket;
import net.edge.world.node.actor.player.Player;
import net.edge.world.node.actor.player.assets.activity.ActivityManager.ActivityType;
import net.edge.world.node.actor.update.UpdateFlag;

/**
 * The message sent from the client when the player speaks.
 * @author lare96 <http://github.com/lare96>
 */
public final class ChatPacket implements IncomingPacket {
	
	@Override
	public void handle(Player player, int opcode, int size, IncomingMsg payload) {
		if(player.getActivityManager().contains(ActivityType.CHAT_MESSAGE))
			return;
		
		if(player.isMuted() || PunishmentHandler.isIPMuted(player.getSession().getHost())) {
			player.message("You are currently muted.");
			return;
		}
		
		int effects = payload.get(false, ByteTransform.S);
		int color = payload.get(false, ByteTransform.S);
		int chatLength = (size - 2);
		byte[] text = payload.getBytesReverse(chatLength, ByteTransform.A);
		if(effects < 0 || color < 0 || chatLength < 0)
			return;
		player.setChatEffects(effects);
		player.setChatColor(color);
		player.setChatText(text);
		player.getFlags().flag(UpdateFlag.CHAT);
		player.getActivityManager().execute(ActivityType.CHAT_MESSAGE);
	}
}
