package net.edge.net;

import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import net.edge.GameConstants;
import net.edge.net.codec.game.GamePacketType;
import net.edge.net.codec.game.GameState;
import net.edge.net.codec.crypto.IsaacRandom;
import net.edge.net.codec.login.LoginRequest;
import net.edge.net.codec.login.LoginResponse;
import net.edge.net.codec.login.LoginState;
import net.edge.net.codec.login.LoginCode;
import net.edge.net.host.HostListType;
import net.edge.net.host.HostManager;
import net.edge.net.packet.OutgoingPacket;
import net.edge.util.TextUtils;
import net.edge.world.World;
import net.edge.world.entity.EntityState;
import net.edge.world.entity.actor.player.Player;
import net.edge.world.entity.actor.player.PlayerCredentials;
import net.edge.world.entity.actor.player.PlayerSerialization;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkState;

/**
 * Player's session which handles I/O operations.
 * @author Artem Batutin <artembatutin@gmail.com>
 */
public class Session {
	
	/**
	 * The asynchronous logger.
	 */
	private static final Logger LOGGER = Logger.getLogger(Session.class.getName());
	
	/**
	 * Game stream capacity.
	 */
	private static int BUFFER_SIZE = 5000;
	
	/**
	 * The cap limit of outgoing packets per session.
	 */
	public static int UPDATE_LIMIT = 200;
	
	/** The {@link Channel} to send and receive messages through. */
	private final Channel channel;
	/** The ip address that the connection was received from. */
	private final String hostAddress;
	/** The mac address the connection was received from. */
	private String macAddress;
	
	/** Condition if session is handling game process. */
	private boolean isGame;
	/** Condition if this session is active. */
	private boolean active = true;
	
	/** The player associated with this session. */
	private Player player;
	
	/* Incoming and outgoing packet queues. */
	/** The queue of {@link ByteBuf}s. */
	private Queue<ByteBuf> incoming;
	/** The queue of {@link OutgoingPacket}s. */
	private Queue<OutgoingPacket> outgoing;
	
	/* ISAAC cypher. */
	/** The message encryptor. */
	private IsaacRandom encryptor;
	/** The message decryptor. */
	private IsaacRandom decryptor;
	
	
	/* Login request decoding. */
	/** A cryptographically secure random number generator.*/
	private static final Random RANDOM = new SecureRandom();
	/** The current state of decoding the protocol. */
	private LoginState loginState = LoginState.HANDSHAKE;
	/** The size of the last portion of the protocol. */
	private int rsaBlockSize;
	
	/* Game packet decoding. */
	/** The state of the message currently being decoded. */
	private GameState gameState = GameState.OPCODE;
	/** The opcode of the message currently being decoded. */
	private int opcode = -1;
	/** The size of the message currently being decoded. */
	private int size = -1;
	/** The type of the message currently being decoded. */
	private GamePacketType type = GamePacketType.RAW;
	/** The main game stream buffer to encode all outgoing packets. */
	private ByteBuf stream = Unpooled.buffer(BUFFER_SIZE);
	/**
	 * Creates a new {@link Session}.
	 * @param channel The {@link Channel} to send and receive messages through.
	 */
	Session(Channel channel) {
		this.channel = channel;
		this.hostAddress = channel == null ? "" : ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
	}
	
	/**
	 * Handles an incoming message from the channel.
	 * @param msg message incoming.
	 * @throws Exception exception
	 */
	void handleMessage(ChannelHandlerContext ctx, Object msg) throws Exception {
		if(isGame) {
			game((ByteBuf) msg);
		} else {
			login(ctx, (ByteBuf) msg);
		}
	}
	
	/**
	 * Disposes of this {@code Session} by closing the {@link Channel}.
	 */
	void terminate() {
		if(player != null) {
			if(isGame && player.getState() != EntityState.AWAITING_REMOVAL && player.getState() != EntityState.INACTIVE) {
				World.get().queueLogout(player);
			} else if(player.getState() == EntityState.ACTIVE) {
				World.get().queueLogout(player);
			}
		}
	}
	
	/**
	 * Enqueues the given {@link OutgoingPacket} for transport.
	 */
	public void enqueue(OutgoingPacket pkt) {
		outgoing.offer(pkt);
	}
	
	
	/**
	 * Handling an incoming packet/message.
	 * @param packet incoming message.
	 */
	public void handle(ByteBuf packet) {
		try {
			NetworkConstants.MESSAGES[packet.getOpcode()].handle(player, packet.getOpcode(), packet.capacity(), packet);
		} finally {
			packet.release();
		}
	}
	
	/**
	 * Polling all incoming packets.
	 */
	public void pollIncomingPackets() {
		if(!incoming.isEmpty()) {
			int count = 0;
			while(!incoming.isEmpty() && count < 20) {
				ByteBuf msg = incoming.poll();
				handle(msg);
				count++;
			}
		}
	}
	
	/**
	 * Polling all outgoing packets.
	 */
	public void pollOutgoingPackets() {
		if(!outgoing.isEmpty()) {
			int count = 0;
			while(!outgoing.isEmpty() && count < UPDATE_LIMIT) {
				OutgoingPacket pkt = outgoing.poll();
				if(channel.isWritable()) {
					write(pkt);
				} else {
					outgoing.offer(pkt);
				}
				count++;
			}
		}
	}
	
	/**
	 * Writes the given {@link OutgoingPacket} to the stream.
	 */
	public void write(OutgoingPacket packet) {
		//if(channel.isActive() && channel.isRegistered()) {
			packet.write(player, stream);
		//}
	}
	
	/**
	 * Flushes all pending {@link ByteBuf}s within the channel's queue. Repeated calls to this method are relatively
	 * expensive, which is why messages should be queued up with {@code queue(MessageWriter)} and flushed once at the end of
	 * the cycle.
	 */
	public void flushQueue() {
		if(!active)
			return;
		Channel channel = getChannel();
		if(channel.isActive()) {
			channel.eventLoop().execute(() -> {
				channel.writeAndFlush(stream.retain(), channel.voidPromise());
				stream.clear();
			});
		}
	}
	
	private void game(ByteBuf in) {
		try {
			switch(gameState) {
				case OPCODE:
					opcode(in);
					break;
				case SIZE:
					size(in);
					break;
				case PAYLOAD:
					payload(in);
					break;
			}
		} finally {
			in.readableBytes();
		}
	}
	
	/**
	 * Decodes the opcode of the {@link ByteBuf}.
	 * @param in The data being decoded.
	 */
	private void opcode(ByteBuf in) {
		if(in.isReadable()) {
			opcode = in.readUnsignedByte();
			opcode = (opcode - decryptor.nextInt()) & 0xFF;
			size = NetworkConstants.MESSAGE_SIZES[opcode];
			
			if(size == -1) {
				type = GamePacketType.VARIABLE_BYTE;
			} else if(size == -2) {
				type = GamePacketType.VARIABLE_SHORT;
			} else {
				type = GamePacketType.FIXED;
			}
			
			if(size == 0) {
				queueMessage(Unpooled.EMPTY_BUFFER);
				opcode(in);
				return;
			}
			
			gameState = size == -1 || size == -2 ? GameState.SIZE : GameState.PAYLOAD;
			if(gameState == GameState.SIZE) {
				size(in);
			} else {
				payload(in);
			}
		}
	}
	
	/**
	 * Decodes the size of the {@link ByteBuf}.
	 * @param in The data being decoded.
	 */
	private void size(ByteBuf in) {
		int bytes = size == -1 ? Byte.BYTES : Short.BYTES;
		if(in.isReadable(bytes)) {
			size = 0;
			for(int i = 0; i < bytes; i++) {
				size |= in.readUnsignedByte() << 8 * (bytes - 1 - i);
			}
			gameState = GameState.PAYLOAD;
			payload(in);
		}
	}
	
	/**
	 * Decodes the payload of the {@link ByteBuf}.
	 * @param in The data being decoded.
	 */
	private void payload(ByteBuf in) {
		if(in.isReadable(size)) {
			ByteBuf newBuffer = in.readBytes(size);
			queueMessage(newBuffer);
			opcode(in);
		}
	}
	
	/**
	 * Prepares a {@link ByteBuf} to be queued upstream and handled on the main game thread.
	 * @param payload The payload of the {@code Packet}.
	 */
	private void queueMessage(ByteBuf payload) {
		checkState(opcode >= 0, "opcode < 0");
		checkState(size >= 0, "size < 0");
		checkState(type != GamePacketType.RAW, "type == GamePacketType.RAW");
		try {
			if(NetworkConstants.MESSAGES[opcode] == null) {
				LOGGER.info("Unhandled packet " + opcode + " - " + size);
				payload.release();
				return;
			}
			
			ByteBuf packet = payload.incoming(opcode, type);
			if(packet.getOpcode() != 0) {
				if(packet.getOpcode() == 41) {
					handle(packet);//item equipping
					return;
				}
				incoming.offer(packet);
			}
		} finally {
			opcode = -1;
			size = -1;
			gameState = GameState.OPCODE;
		}
	}
	
	/**
	 * Handles a {@link LoginRequest}.
	 * @param request The message containing the credentials.
	 * @throws Exception If any errors occur while handling credentials.
	 */
	private void handleRequest(final LoginRequest request) throws Exception {
		player = new Player(new PlayerCredentials(request.getUsername(), request.getPassword()));
		LoginCode response = LoginCode.NORMAL;
		macAddress = request.getMacAddress();
		
		// Validate the username and password, change login response if needed
		// for invalid credentials or the world being full.
		boolean invalidCredentials = !request.getUsername().matches("^[a-zA-Z0-9_ ]{1,12}$") || request.getPassword().isEmpty() || request.getPassword().length() > 20;
		response = invalidCredentials ? LoginCode.INVALID_CREDENTIALS : World.get().getPlayers().remaining() == 0 ? LoginCode.WORLD_FULL : response;
		
		// Validating login before deserialization.
		if(response == LoginCode.NORMAL) {
			player.credentials.setUsername(request.getUsername());
			player.credentials.password = request.getPassword();
		}
		
		// Validating player login possibility.
		if(response == LoginCode.NORMAL && World.get().getPlayer(request.getUsernameHash()).isPresent()) {
			response = LoginCode.ACCOUNT_ONLINE;
		}
		
		// Deserialization
		PlayerSerialization.SerializeResponse serial = null;
		if(response == LoginCode.NORMAL) {
			serial = new PlayerSerialization(player).loginCheck(request.getPassword());
			response = serial.getResponse();
		}
		
		ChannelFuture future = channel.writeAndFlush(new LoginResponse(response, player.getRights(), player.isIronMan()).toBuf());
		if(response != LoginCode.NORMAL) {
			future.addListener(ChannelFutureListener.CLOSE);
			return;
		}
		future.awaitUninterruptibly();
		afterRequest();
		final JsonObject reader = serial.getReader();
		new PlayerSerialization(player).deserialize(reader);
		World.get().queueLogin(player);
	}
	
	private void login(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		switch(loginState) {
			case HANDSHAKE:
				decodeHandshake(ctx, in);
				loginState = LoginState.LOGIN_BLOCK;
				break;
			case LOGIN_BLOCK:
				decodeLoginBlock(ctx, in);
		}
	}
	
	/**
	 * Decodes the handshake portion of the login protocol.
	 * @param ctx The channel handler context.
	 * @param in  The data that is being decoded.
	 * @throws Exception If any exceptions occur while decoding this portion of the protocol.
	 */
	private void decodeHandshake(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		if(in.readableBytes() >= 14) {
			int build = in.readUnsignedShort();
			if(build != GameConstants.CLIENT_BUILD) {
				write(ctx, LoginCode.WRONG_BUILD_NUMBER);
				return;
			}
			//mac address
			int macId = in.readInt();
			String mac = String.valueOf(macId);
			ctx.channel().attr(NetworkConstants.USR_MAC).set(mac);
			if(HostManager.contains(mac, HostListType.BANNED_MAC)) {
				write(ctx, LoginCode.ACCOUNT_DISABLED);
				return;
			}
			//username hash
			long usernameHash = in.readLong();
			ctx.channel().attr(NetworkConstants.USR_HASH).set(usernameHash);
			if(World.get().getPlayer(usernameHash).isPresent()) {
				write(ctx, LoginCode.ACCOUNT_ONLINE);
				return;
			}
			ByteBuf buf = ctx.alloc().buffer(17);
			buf.writeLong(0);
			buf.writeByte(0);
			buf.writeLong(RANDOM.nextLong());
			ctx.writeAndFlush(buf, ctx.voidPromise());
		}
	}
	
	/**
	 * Decodes the portion of the login protocol to sucessfully login.
	 * @param ctx The channel handler context.
	 * @param in  The data that is being decoded.
	 * @throws Exception If any exceptions occur while decoding this portion of the protocol.
	 */
	private void decodeLoginBlock(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
		if(in.readableBytes() >= 1) {
			//RSA size
			rsaBlockSize = in.readUnsignedByte();
			if(rsaBlockSize == 0) {
				write(ctx, LoginCode.COULD_NOT_COMPLETE_LOGIN);
			}
		}
		if(in.readableBytes() >= rsaBlockSize) {
			int expectedSize = in.readUnsignedByte();
			if(expectedSize != rsaBlockSize - 1) {
				write(ctx, LoginCode.COULD_NOT_COMPLETE_LOGIN);
				return;
			}
			byte[] rsaBytes = new byte[rsaBlockSize - 1];
			in.readBytes(rsaBytes);
			byte[] rsaData = new BigInteger(rsaBytes).toByteArray();
			ByteBuf rsaBuffer = Unpooled.wrappedBuffer(rsaData);
			try {
				long clientHalf = rsaBuffer.readLong();
				long serverHalf = rsaBuffer.readLong();
				int[] isaacSeed = {(int) (clientHalf >> 32), (int) clientHalf, (int) (serverHalf >> 32), (int) serverHalf};
				decryptor = new IsaacRandom(isaacSeed);
				for(int i = 0; i < isaacSeed.length; i++) {
					isaacSeed[i] += 50;
				}
				encryptor = new IsaacRandom(isaacSeed);
				String password = rsaBuffer.getCString().toLowerCase();
				long usernameHash = ctx.channel().attr(NetworkConstants.USR_HASH).get();
				String username = TextUtils.hashToName(usernameHash).replaceAll("_", " ").toLowerCase().trim();
				String macAddress = ctx.channel().attr(NetworkConstants.USR_MAC).get();
				handleRequest(new LoginRequest(usernameHash, username, password, macAddress));
			} finally {
				if(rsaBuffer.isReadable()) {
					rsaBuffer.release();
				}
			}
		}
	}
	
	/**
	 * Writes a closed response to the login channel.
	 */
	public static void write(ChannelHandlerContext ctx, LoginCode response) {
		Channel channel = ctx.channel();
		LoginResponse message = new LoginResponse(response);
		ByteBuf initialMessage = ctx.alloc().buffer(8).writeLong(0); // Write initial message.
		channel.write(initialMessage, channel.voidPromise());
		channel.writeAndFlush(message.toBuf()).addListener(ChannelFutureListener.CLOSE); // Write response message.
	}
	
	/**
	 * Gets the host address from the channel.
	 * @param ctx channel.
	 * @return host address as string.
	 */
	static String address(ChannelHandlerContext ctx) {
		return ((InetSocketAddress )ctx.channel().remoteAddress()).getAddress().getHostAddress();
	}
	
	private void afterRequest() {
		player.setSession(this);
		outgoing = new ConcurrentLinkedQueue<>();
		incoming = new ConcurrentLinkedQueue<>();
		isGame = true;
		stream.outgoing(encryptor);
	}
	
	/**
	 * Getting a {@link ByteBufAllocator} to allocate buffers.
	 * @return allocator.
	 */
	public ByteBufAllocator alloc() {
		return getChannel().alloc();
	}
	
	/**
	 * Setting the connection condition.
	 * @param active flag.
	 */
	public void setActive(boolean active) {
		this.active = active;
	}
	
	/**
	 * Gets the mac address of this session.
	 * @return mac address.
	 */
	public String getMacAddress() {
		return macAddress;
	}
	
	/**
	 * Gets the host address of this session.
	 * @return host address.
	 */
	public String getHost() {
		return hostAddress;
	}
	
	/**
	 * Gets the channel of this session.
	 * @return channel.
	 */
	public Channel getChannel() {
		return channel;
	}
}