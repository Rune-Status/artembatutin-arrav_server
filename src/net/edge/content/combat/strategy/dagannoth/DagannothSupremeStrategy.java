package net.edge.content.combat.strategy.dagannoth;

import net.edge.content.combat.CombatHit;
import net.edge.task.Task;
import net.edge.content.combat.CombatType;
import net.edge.content.combat.strategy.Strategy;
import net.edge.world.World;
import net.edge.world.entity.EntityState;
import net.edge.world.entity.actor.Actor;
import net.edge.world.Animation;
import net.edge.world.Projectile;

import java.util.Arrays;
import java.util.Objects;

public final class DagannothSupremeStrategy implements Strategy {

	@Override
	public boolean canOutgoingAttack(Actor actor, Actor victim) {
		return actor.isMob() && victim.isPlayer();
	}
	
	@Override
	public void incomingAttack(Actor actor, Actor attacker, CombatHit data) {
		if(data.getType().equals(CombatType.RANGED) || data.getType().equals(CombatType.MAGIC)) {
			attacker.toPlayer().message("Your attacks are completely blocked...");
			Arrays.stream(data.getHits()).filter(Objects::nonNull).forEach(h -> h.setAccurate(false));
			return;
		}
	}

	@Override
	public CombatHit outgoingAttack(Actor actor, Actor victim) {
		actor.animation(new Animation(actor.toMob().getDefinition().getAttackAnimation()));
		World.get().submit(new Task(1, false) {
			@Override
			protected void execute() {
				this.cancel();
				if(actor.getState() != EntityState.ACTIVE || victim.getState() != EntityState.ACTIVE || actor.isDead() || victim.isDead())
					return;
				new Projectile(actor, victim, 1937, 44, 4, 60, 43, 0).sendProjectile();
			}
		});
		return new CombatHit(actor, victim, 1, CombatType.RANGED, true, 2);
	}

	@Override
	public int attackDelay(Actor actor) {
		return actor.getAttackDelay();
	}

	@Override
	public int attackDistance(Actor actor) {
		return 5;
	}

	@Override
	public int[] getMobs() {
		return new int[]{2881};
	}

}