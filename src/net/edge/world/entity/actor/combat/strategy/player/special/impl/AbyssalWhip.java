package net.edge.world.entity.actor.combat.strategy.player.special.impl;

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
public final class AbyssalWhip extends PlayerMeleeStrategy {
    
    private static final Graphic GRAPHIC = new Graphic(2108, 100);
    private static final Animation ANIMATION = new Animation(11971, Animation.AnimationPriority.HIGH);
    private static final CombatModifier MODIFIER = new CombatModifier().attack(1.00).damage(0.375);

    @Override
    public void start(Player attacker, Actor defender, Hit[] hits) {
        super.start(attacker, defender, hits);
        defender.graphic(GRAPHIC);
    }

    @Override
    public void attack(Player player, Actor target, Hit hit) {
        super.attack(player, target, hit);

        if(target.isPlayer()) {
            //Gathering the oponent's running energy.
            final Player victimPlayer = target.toPlayer();
            double energy = 0;
            if(victimPlayer.getRunEnergy() != 0)
                energy = victimPlayer.getRunEnergy() / 4;

            //Decreasing oponent's energy and increasing the attacker's energy.
            victimPlayer.setRunEnergy(victimPlayer.getRunEnergy() - energy);
            player.setRunEnergy(player.getRunEnergy() + energy);
        }
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
