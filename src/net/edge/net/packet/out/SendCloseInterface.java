package net.edge.net.packet.out;

import net.edge.net.codec.ByteOrder;
import net.edge.net.codec.GameBuffer;
import net.edge.net.packet.OutgoingPacket;
import net.edge.world.node.entity.player.Player;

public final class SendCloseInterface implements OutgoingPacket {
	@Override
	public void write(Player player) {
		GameBuffer msg = player.getSession().getStream();
		msg.message(219);
		player.getDialogueBuilder().interrupt();
	}
}