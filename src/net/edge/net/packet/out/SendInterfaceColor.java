package net.edge.net.packet.out;

import net.edge.net.codec.ByteOrder;
import net.edge.net.codec.ByteTransform;
import net.edge.net.codec.GameBuffer;
import net.edge.net.packet.OutgoingPacket;
import net.edge.world.node.entity.player.Player;

public final class SendInterfaceColor implements OutgoingPacket {
	
	private final int id, color;
	
	public SendInterfaceColor(int id, int color) {
		this.id = id;
		this.color = color;
	}
	
	@Override
	public void write(Player player) {
		GameBuffer msg = player.getSession().getStream();
		msg.message(122);
		msg.putShort(id, ByteTransform.A, ByteOrder.LITTLE);
		msg.putShort(color, ByteTransform.A, ByteOrder.LITTLE);
	}
}