package net.edge.net.packet.in;

import net.edge.content.minigame.MinigameHandler;
import net.edge.net.codec.IncomingMsg;
import net.edge.net.packet.IncomingPacket;
import net.edge.world.World;
import net.edge.world.entity.actor.player.Player;
import net.edge.world.entity.actor.player.assets.activity.ActivityManager;
import net.edge.world.entity.item.container.session.ExchangeSessionManager;

/**
 * The message sent from the client when a player clicks certain options on an
 * interface.
 * @author lare96 <http://github.com/lare96>
 */
public final class InterfaceClickPacket implements IncomingPacket {
	
	@Override
	public void handle(Player player, int opcode, int size, IncomingMsg payload) {
		if(player.getActivityManager().contains(ActivityManager.ActivityType.INTERFACE_CLICK))
			return;
		
		if(ExchangeSessionManager.get().reset(player)) {
			return;
		}
		
		MinigameHandler.executeVoid(player, t -> t.onInterfaceClick(player));
		player.closeWidget();
		player.getActivityManager().execute(ActivityManager.ActivityType.INTERFACE_CLICK);
	}
}
