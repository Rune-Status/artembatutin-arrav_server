package net.edge.net.codec.game;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.edge.net.codec.IsaacCipher;
import net.edge.net.codec.MessageType;
import net.edge.net.packet.Packet;

/**
 * A {@link MessageToByteEncoder} implementation that encodes all {@link Packet}s into {@link ByteBuf}s.
 * @author lare96 <http://github.org/lare96>
 */
public final class GameMessageEncoder extends MessageToByteEncoder<Packet> {
	
	/**
	 * The encryptor for this message.
	 */
	private final IsaacCipher encryptor;
	
	/**
	 * Creates a new {@link GameMessageEncoder}.
	 * @param encryptor The encryptor for this encoder.
	 */
	public GameMessageEncoder(IsaacCipher encryptor) {
		this.encryptor = encryptor;
	}
	
	@Override
	public void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
		out.writeByte(msg.getOpcode() + encryptor.nextInt());
		if(msg.getType() == MessageType.VARIABLE) {
			out.writeByte(msg.getSize());
		} else if(msg.getType() == MessageType.VARIABLE_SHORT) {
			out.writeShort(msg.getSize());
		}
		out.writeBytes(msg.getPayload().getBuffer());
		msg.getPayload().release();
	}
}