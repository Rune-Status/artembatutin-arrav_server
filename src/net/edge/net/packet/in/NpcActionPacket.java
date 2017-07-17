package net.edge.net.packet.in;

import net.edge.Server;
import net.edge.content.combat.magic.CombatSpells;
import net.edge.content.minigame.MinigameHandler;
import net.edge.content.pets.Pet;
import net.edge.content.skill.slayer.Slayer;
import net.edge.content.skill.summoning.Summoning;
import net.edge.event.EventContainer;
import net.edge.event.impl.NpcEvent;
import net.edge.locale.Boundary;
import net.edge.locale.Position;
import net.edge.locale.loc.Location;
import net.edge.net.codec.IncomingMsg;
import net.edge.net.codec.ByteOrder;
import net.edge.net.codec.ByteTransform;
import net.edge.net.packet.IncomingPacket;
import net.edge.world.World;
import net.edge.world.node.actor.mob.Mob;
import net.edge.world.node.actor.mob.MobDefinition;
import net.edge.world.node.actor.player.Player;
import net.edge.world.node.actor.player.assets.Rights;
import net.edge.world.node.actor.player.assets.activity.ActivityManager;

import java.util.Optional;

/**
 * The message sent from the client when a player attacks or clicks on an NPC.
 * @author Artem Batutin <artembatutin@gmail.com
 */
public final class NpcActionPacket implements IncomingPacket {
	
	/*
	 * All of the npc events.
	 */
	public static final EventContainer<NpcEvent> FIRST = new EventContainer<>();
	public static final EventContainer<NpcEvent> SECOND = new EventContainer<>();
	public static final EventContainer<NpcEvent> THIRD = new EventContainer<>();
	public static final EventContainer<NpcEvent> FOURTH = new EventContainer<>();
	
	@Override
	public void handle(Player player, int opcode, int size, IncomingMsg payload) {
		if(player.getActivityManager().contains(ActivityManager.ActivityType.NPC_ACTION))
			return;
		switch(opcode) {
			case 72:
				attackOther(player, payload);
				break;
			case 131:
				attackMagic(player, payload);
				break;
			case 155:
				firstClick(player, payload);
				break;
			case 17:
				secondClick(player, payload);
				break;
			case 21:
				thirdClick(player, payload);
				break;
			case 18:
				fourthClick(player, payload);
				break;
		}
		player.getActivityManager().execute(ActivityManager.ActivityType.NPC_ACTION);
	}
	
	/**
	 * Handles the melee and ranged attacks on an NPC.
	 * @param player  the player this will be handled for.
	 * @param payload the payload that will read the sent data.
	 */
	private void attackOther(Player player, IncomingMsg payload) {
		int index = payload.getShort(false, ByteTransform.A);
		Mob mob = World.get().getNpcs().get(index - 1);
		if(mob == null || !checkAttack(player, mob))
			return;
		player.getTolerance().reset();
		player.getCombatBuilder().attack(mob);
	}
	
	/**
	 * Handles the magic attacks on an NPC.
	 * @param player  the player this will be handled for.
	 * @param payload the payload that will read the sent data.
	 */
	private void attackMagic(Player player, IncomingMsg payload) {
		int index = payload.getShort(true, ByteTransform.A, ByteOrder.LITTLE);
		int spellId = payload.getShort(true, ByteTransform.A);
		Mob mob = World.get().getNpcs().get(index - 1);
		Optional<CombatSpells> spell = CombatSpells.getSpell(spellId);
		if(mob == null || !spell.isPresent() || !checkAttack(player, mob))
			return;
		player.setCastSpell(spell.get().getSpell());
		player.getTolerance().reset();
		player.getCombatBuilder().attack(mob);
	}
	
	/**
	 * Handles the first click NPC slot.
	 * @param player  the player this will be handled for.
	 * @param payload the payload that will read the sent data.
	 */
	private void firstClick(Player player, IncomingMsg payload) {
		int index = payload.getShort(true, ByteOrder.LITTLE);
		Mob mob = World.get().getNpcs().get(index - 1);
		if(mob == null)
			return;
		Position position = mob.getPosition().copy();
		if(mob.getId() == 4650) {
			player.getMovementQueue().smartWalk(new Position(3081, 3508));
		}
		player.getMovementListener().append(() -> {
			if(new Boundary(position, mob.size()).within(player.getPosition(), player.size(), mob.getId() == 4650 ? 3 : 1)) {
				player.facePosition(mob.getPosition());
				mob.facePosition(player.getPosition());
				if(!MinigameHandler.execute(player, m -> m.onFirstClickNpc(player, mob))) {
					return;
				}
				if(Summoning.interact(player, mob, 1)) {
					return;
				}
				if(Pet.pickup(player, mob)) {
					return;
				}
				NpcEvent e = FIRST.get(mob.getId());
				if(e != null) {
					e.click(player, mob, 1);
				}
			}
		});
		if(player.getRights().greater(Rights.ADMINISTRATOR) && Server.DEBUG)
			player.message("[NPC1]:" + mob.toString());
	}
	
	/**
	 * Handles the second click NPC slot.
	 * @param player  the player this will be handled for.
	 * @param payload the payload that will read the sent data.
	 */
	private void secondClick(Player player, IncomingMsg payload) {
		int index = payload.getShort(false, ByteTransform.A, ByteOrder.LITTLE);
		Mob mob = World.get().getNpcs().get(index - 1);
		if(mob == null)
			return;
		Position position = mob.getPosition().copy();
		player.getMovementListener().append(() -> {
			if(new Boundary(position, mob.size()).within(player.getPosition(), player.size(), 1)) {
				player.facePosition(mob.getPosition());
				mob.facePosition(player.getPosition());
				if(!MinigameHandler.execute(player, m -> m.onSecondClickNpc(player, mob))) {
					return;
				}
				if(Summoning.interact(player, mob, 2)) {
					return;
				}
				NpcEvent e = SECOND.get(mob.getId());
				if(e != null) {
					e.click(player, mob, 2);
				}
			}
		});
		if(player.getRights().greater(Rights.ADMINISTRATOR) && Server.DEBUG)
			player.message("[NPC2]:" + mob.toString());
	}
	
	/**
	 * Handles the third click NPC slot.
	 * @param player  the player this will be handled for.
	 * @param payload the payload that will read the sent data.
	 */
	private void thirdClick(Player player, IncomingMsg payload) {
		int index = payload.getShort(true);
		Mob mob = World.get().getNpcs().get(index - 1);
		if(mob == null)
			return;
		Position position = mob.getPosition().copy();
		player.getMovementListener().append(() -> {
			if(new Boundary(position, mob.size()).within(player.getPosition(), player.size(), 1)) {
				player.facePosition(mob.getPosition());
				mob.facePosition(player.getPosition());
				if(Summoning.interact(player, mob, 3)) {
					return;
				}
				NpcEvent e = THIRD.get(mob.getId());
				if(e != null) {
					e.click(player, mob, 3);
				}
			}
		});
		if(player.getRights().greater(Rights.ADMINISTRATOR) && Server.DEBUG)
			player.message("[NPC3]:" + mob.toString());
	}
	
	/**
	 * Handles the fourth click NPC slot.
	 * @param player  the player this will be handled for.
	 * @param payload the payload that will read the sent data.
	 */
	private void fourthClick(Player player, IncomingMsg payload) {
		int index = payload.getShort(true, ByteOrder.LITTLE);
		Mob mob = World.get().getNpcs().get(index - 1);
		if(mob == null)
			return;
		final int id = mob.getId();
		Position position = mob.getPosition();
		player.getMovementListener().append(() -> {
			if(new Boundary(position, mob.size()).within(player.getPosition(), player.size(), 1)) {
				player.facePosition(mob.getPosition());
				mob.facePosition(player.getPosition());
				
				if(Summoning.interact(player, mob, 4)) {
					return;
				}
				NpcEvent e = FOURTH.get(mob.getId());
				if(e != null) {
					e.click(player, mob, 4);
				}
			}
		});
		if(player.getRights().greater(Rights.ADMINISTRATOR) && Server.DEBUG)
			player.message("[NPC4]:" + mob.toString());
	}
	
	/**
	 * Determines if {@code player} can make an attack on {@code mob}.
	 * @param player the player attempting to make an attack.
	 * @param mob    the mob being attacked.
	 * @return {@code true} if the player can make an attack, {@code false}
	 * otherwise.
	 */
	private boolean checkAttack(Player player, Mob mob) {
		if(!MobDefinition.DEFINITIONS[mob.getId()].isAttackable())
			return false;
		if(!Location.inMultiCombat(player) && player.getCombatBuilder().isBeingAttacked() && !mob.same(player.getCombatBuilder().getAggressor())) {
			player.message("You are already under attack!");
			return false;
		}
		if(!Slayer.canAttack(player, mob)) {
			return false;
		}
		return true;
	}
}
