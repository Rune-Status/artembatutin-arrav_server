package net.edge.content.newcombat.strategy.player.ranged.impl;

import net.edge.content.newcombat.CombatProjectileDefinition;
import net.edge.content.newcombat.attack.AttackStance;
import net.edge.content.newcombat.strategy.player.ranged.BowWeapon;
import net.edge.content.newcombat.weapon.RangedWeaponDefinition;

public class ShortbowWeapon extends BowWeapon {

    public ShortbowWeapon(RangedWeaponDefinition definition, CombatProjectileDefinition projectileDefinition) {
        super(definition, projectileDefinition);
    }

    @Override
    public int getAttackDelay(AttackStance stance) {
        return stance == AttackStance.RAPID ? 3 : 4;
    }

    @Override
    public int getAttackDistance(AttackStance stance) {
        return stance == AttackStance.LONGRANGE ? 9 : 7;
    }

}
