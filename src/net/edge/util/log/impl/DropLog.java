package net.edge.util.log.impl;

import net.edge.util.TextUtils;
import net.edge.util.log.LogDetails;
import net.edge.world.node.actor.mob.MobDefinition;
import net.edge.world.node.actor.player.Player;
import net.edge.world.node.item.Item;

import java.util.Optional;

/**
 * The class which represents a drop log.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public final class DropLog extends LogDetails {
	
	/**
	 * The npc whom dropped this item for this drop log.
	 */
	private final MobDefinition npc;
	
	/**
	 * The item dropped for this drop log.
	 */
	private final Item item;
	
	/**
	 * Constructs a new {@link DropLog}.
	 * @param npc  {@link #npc}.
	 * @param item {@link #item}.
	 */
	public DropLog(Player player, MobDefinition npc, Item item) {
		super(player.getFormatUsername(), "Drop");
		this.npc = npc;
		this.item = item;
	}
	
	@Override
	public Optional<String> formatInformation() {
		StringBuilder builder = new StringBuilder();
		builder.append("Got " + item.getAmount() + "x " + TextUtils.appendIndefiniteArticle(item.getDefinition().getName()) + (item.getAmount() > 1 ? TextUtils.determinePluralCheck(item.getDefinition().getName()) : "") + (npc != null ? (" from " + npc.getName() + ".") : ""));
		return Optional.of(builder.toString());
	}
}
