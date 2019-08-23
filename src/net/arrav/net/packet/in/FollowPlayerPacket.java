package net.arrav.net.packet.in;

import net.arrav.net.codec.ByteOrder;
import net.arrav.net.codec.game.GamePacket;
import net.arrav.net.packet.IncomingPacket;
import net.arrav.world.World;
import net.arrav.world.entity.actor.player.Player;
import net.arrav.world.entity.actor.player.assets.activity.ActivityManager;

/**
 * The message sent from the client when a player tries to follow another player.
 * @author lare96 <http://github.com/lare96>
 */
public final class FollowPlayerPacket implements IncomingPacket {
	
	@Override
	public void handle(Player player, int opcode, int size, GamePacket buf) {
		if(player.getActivityManager().contains(ActivityManager.ActivityType.FOLLOW_PLAYER))
			return;
		int index = buf.getShort(false, ByteOrder.LITTLE);
		Player follow = World.get().getPlayers().get(index - 1);
		
		if(follow == null || !follow.getPosition().isViewableFrom(player.getPosition()) || follow.same(player))
			return;
		player.getMovementQueue().follow(follow);
		player.getActivityManager().execute(ActivityManager.ActivityType.FOLLOW_PLAYER);
	}
}
