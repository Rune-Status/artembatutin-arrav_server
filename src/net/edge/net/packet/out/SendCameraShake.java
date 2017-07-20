package net.edge.net.packet.out;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import net.edge.net.codec.GameBuffer;
import net.edge.net.packet.OutgoingPacket;
import net.edge.world.entity.actor.player.Player;

public final class SendCameraShake implements OutgoingPacket {
	
	private final int parameter, jitter, amplitude, frequency;
	
	public SendCameraShake(int parameter, int jitter, int amplitude, int frequency) {
		Preconditions.checkArgument(parameter <= 4);
		this.parameter = parameter;
		this.jitter = jitter;
		this.amplitude = amplitude;
		this.frequency = frequency;
	}
	
	@Override
	public ByteBuf write(Player player, GameBuffer msg) {
		msg.message(35);
		msg.put(parameter);
		msg.put(jitter);
		msg.put(amplitude);
		msg.put(frequency);
		return msg.getBuffer();
	}
}
