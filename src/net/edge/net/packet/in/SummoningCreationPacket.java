package net.edge.net.packet.in;

import io.netty.buffer.ByteBuf;
import net.edge.content.skill.summoning.PouchCreation;
import net.edge.content.skill.summoning.SummoningData;
import net.edge.net.packet.IncomingPacket;
import net.edge.world.entity.actor.player.Player;

import static net.edge.content.skill.summoning.SummoningData.VALUES;

/**
 * This message sent from the client when the player clicks a summoning creation button.
 * @author Artem Batutin<artembatutin@gmail.com>
 */
public final class SummoningCreationPacket implements IncomingPacket {
	
	@Override
	public void handle(Player player, int opcode, int size, ByteBuf buf) {
		int click = buf.get();
		if(click < 0 || click >= VALUES.length)
			return;
		SummoningData data = VALUES[click];
		PouchCreation creation = new PouchCreation(player, data);
		creation.start();
	}
	
}
