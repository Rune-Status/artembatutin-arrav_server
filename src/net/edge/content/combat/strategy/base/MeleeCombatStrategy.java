package net.edge.content.combat.strategy.base;

import net.edge.content.combat.CombatHit;
import net.edge.content.combat.CombatType;
import net.edge.content.combat.strategy.CombatStrategy;
import net.edge.content.combat.weapon.WeaponInterface;
import net.edge.content.minigame.MinigameHandler;
import net.edge.world.node.entity.EntityNode;
import net.edge.world.Animation;
import net.edge.world.node.entity.npc.Npc;
import net.edge.world.node.entity.player.Player;

public final class MeleeCombatStrategy implements CombatStrategy {
	
	@Override
	public boolean canOutgoingAttack(EntityNode character, EntityNode victim) {
		if(character.isNpc()) {
			return true;
		}
		Player player = (Player) character;
		
		if(!MinigameHandler.execute(player, m -> m.canHit(player, victim, CombatType.MELEE))) {
			return false;
		}
		
		player.getCombatBuilder().setCombatType(CombatType.MELEE);
		return true;
	}
	
	@Override
	public CombatHit outgoingAttack(EntityNode character, EntityNode victim) {
		if(character.isNpc()) {
			Npc npc = (Npc) character;
			npc.animation(new Animation(npc.getDefinition().getAttackAnimation()));
		} else if(character.isPlayer()) {
			Player player = (Player) character;
			if(player.getWeaponAnimation() != null && !player.getFightType().isAnimationPrioritized()) {
				player.animation(new Animation(player.getWeaponAnimation().getAttacking()[player.getFightType().getStyle().ordinal()], Animation.AnimationPriority.HIGH));
			} else {
				player.animation(new Animation(player.getFightType().getAnimation(), Animation.AnimationPriority.HIGH));
			}
		}
		return new CombatHit(character, victim, 1, CombatType.MELEE, true);
	}
	
	@Override
	public int attackDelay(EntityNode character) {
		return character.getAttackSpeed();
	}
	
	@Override
	public int attackDistance(EntityNode character) {
		if(character.isNpc())
			return 1;
		if(character.toPlayer().getWeapon() == WeaponInterface.HALBERD)
			return 2;
		return 1;
	}
	
	@Override
	public int[] getNpcs() {
		return new int[]{6261};
	}
	
}
