package net.edge.world.entity.actor.combat.strategy.player.special.impl;

import net.edge.content.skill.Skills;
import net.edge.world.Animation;
import net.edge.world.Graphic;
import net.edge.world.entity.actor.Actor;
import net.edge.world.entity.actor.combat.attack.CombatModifier;
import net.edge.world.entity.actor.combat.attack.FightType;
import net.edge.world.entity.actor.combat.hit.Hit;
import net.edge.world.entity.actor.combat.strategy.player.PlayerMeleeStrategy;
import net.edge.world.entity.actor.combat.weapon.WeaponInterface;
import net.edge.world.entity.actor.player.Player;

import java.util.Optional;

/**
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 * @since 2-9-2017.
 */
public class StatiusWarhammer extends PlayerMeleeStrategy {
	private static final Animation ANIMATION = new Animation(10505, Animation.AnimationPriority.HIGH);
	private static final Graphic GRAPHIC = new Graphic(1840);
	private static final CombatModifier MODIFIER = new CombatModifier().attack(0.23).damage(0.25);

	@Override
	public void start(Player attacker, Actor defender, Hit[] hits) {
		super.start(attacker, defender, hits);
		attacker.graphic(GRAPHIC);
	}

	@Override
	public void attack(Player player, Actor target, Hit hit) {
		if(target.isPlayer() && hit.isAccurate()) {
			Player victim = target.toPlayer();
			int currentDef = victim.getSkills()[Skills.DEFENCE].getRealLevel();
			int defDecrease = (int) (currentDef * 0.11);
			if((currentDef - defDecrease) <= 0 || currentDef <= 0)
				return;
			victim.getSkills()[Skills.DEFENCE].decreaseLevelReal(defDecrease);
			victim.message("Your opponent has reduced your Defence level.");
			player.message("Your hammer forces some of your opponent's defences to break.");
		}
	}
	@Override
	public void finishOutgoing(Player attacker, Actor defender) {
		WeaponInterface.setStrategy(attacker);
	}

	@Override
	public int getAttackDelay(Player attacker, Actor defender, FightType fightType) {
		return 4;
	}
	
	@Override
	public Animation getAttackAnimation(Player attacker, Actor defender) {
		return ANIMATION;
	}

	@Override
	public Optional<CombatModifier> getModifier(Player attacker) {
		return Optional.of(MODIFIER);
	}

}
