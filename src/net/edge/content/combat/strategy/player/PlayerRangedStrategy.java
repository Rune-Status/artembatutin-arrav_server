package net.edge.content.combat.strategy.player;

import net.edge.content.combat.CombatEffect;
import net.edge.content.combat.CombatProjectileDefinition;
import net.edge.content.combat.CombatType;
import net.edge.content.combat.content.RangedAmmunition;
import net.edge.content.combat.effect.CombatPoisonEffect;
import net.edge.content.combat.hit.CombatHit;
import net.edge.content.combat.hit.Hit;
import net.edge.content.combat.strategy.basic.RangedStrategy;
import net.edge.content.combat.attack.FightType;
import net.edge.content.combat.weapon.RangedWeaponDefinition;
import net.edge.content.skill.Skills;
import net.edge.net.packet.out.SendMessage;
import net.edge.world.Animation;
import net.edge.world.entity.actor.Actor;
import net.edge.world.entity.actor.player.Player;
import net.edge.world.entity.item.GroundItem;
import net.edge.world.entity.item.Item;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PlayerRangedStrategy extends RangedStrategy<Player> {
    private final RangedWeaponDefinition rangedDefinition;
    private CombatProjectileDefinition projectileDefinition;
    private Item ammunition;

    public PlayerRangedStrategy(RangedWeaponDefinition definition) {
        this.rangedDefinition = definition;
    }

    @Override
    public boolean canAttack(Player attacker, Actor defender) {
        if (attacker.getSkillLevel(Skills.RANGED) < rangedDefinition.getLevel()) {
            attacker.out(new SendMessage("You need a Ranged level of " + rangedDefinition.getLevel() + " to use this weapon."));
            attacker.getNewCombat().reset();
            return false;
        }

        Item ammo = attacker.getEquipment().get(rangedDefinition.getSlot());

        if (ammo != null) {
            if (rangedDefinition.getAllowed() == null) {
                attacker.getNewCombat().reset();
                return false;
            }

            for (RangedAmmunition data : rangedDefinition.getAllowed()) {
                for (int id : data.ammo) {
                    if (id == ammo.getId()) {
                        ammunition = ammo;
                        projectileDefinition = CombatProjectileDefinition.getDefinition(ammo.getName());
                        return true;
                    }
                }
            }

            attacker.out(new SendMessage(getInvalidAmmunitionMessage(rangedDefinition.getType())));
        } else attacker.out(new SendMessage(getNoAmmunitionMessage(rangedDefinition.getType())));
        attacker.getNewCombat().reset();
        return false;
    }

    @Override
    public void attack(Player attacker, Actor defender, Hit hit, Hit[] hits) {
        attacker.animation(getAttackAnimation(attacker, defender));
        projectileDefinition.getStart().ifPresent(attacker::graphic);
        projectileDefinition.sendProjectile(attacker, defender, false);
        addCombatExperience(attacker, hit);
    }

    @Override
    public void hit(Player attacker, Actor defender, Hit hit, Hit[] hits) {
        Predicate<CombatEffect> filter = effect -> effect.canEffect(attacker, defender, hit);
        Consumer<CombatEffect> execute = effect -> effect.execute(attacker, defender, hit);
        projectileDefinition.getEffect().filter(Objects::nonNull).filter(filter).ifPresent(execute);
        removeAmmunition(attacker, defender, rangedDefinition.getType());

        if (hit.getDamage() > 0) {
            defender.poison(CombatPoisonEffect.getPoisonType(attacker.getEquipment().get(rangedDefinition.getSlot())).orElse(null));
        }
    }

    @Override
    public void block(Actor attacker, Player defender, Hit hit, Hit[] hits) {
        defender.animation(getBlockAnimation(defender, attacker));
    }

    @Override
    public Animation getAttackAnimation(Player attacker, Actor defender) {
        if (attacker.getWeaponAnimation() != null && attacker.getWeaponAnimation().getAttacking()[0] != 422) {
            return new Animation(attacker.getWeaponAnimation().getAttacking()[attacker.getFightType().getStyle().ordinal()], Animation.AnimationPriority.HIGH);
        }
        return new Animation(attacker.getFightType().getAnimation(), Animation.AnimationPriority.HIGH);
    }

    @Override
    public Animation getBlockAnimation(Player attacker, Actor defender) {
        int animation = 404;
        if (attacker.getShieldAnimation() != null) {
            animation = attacker.getShieldAnimation().getBlock();
        } else if (attacker.getWeaponAnimation() != null) {
            animation = attacker.getWeaponAnimation().getBlocking();
        }
        return new Animation(animation, Animation.AnimationPriority.LOW);
    }

    @Override
    public int getAttackDelay(FightType fightType) {
        return 4;
    }

    @Override
    public int getAttackDistance(FightType fightType) {
        return 8;
    }

    @Override
    public CombatHit[] getHits(Player attacker, Actor defender) {
        return new CombatHit[] { nextRangedHit(attacker, defender, projectileDefinition.getHitDelay(attacker, defender, false), projectileDefinition.getHitsplatDelay()) };
    }

    @Override
    public CombatType getCombatType() {
        return CombatType.RANGED;
    }

    private void removeAmmunition(Player attacker, Actor defender, RangedWeaponDefinition.AttackType type) {
        if (attacker.getEquipment().remove(new Item(ammunition.getId())) > 0) {
            new GroundItem(new Item(ammunition.getId()), defender.getPosition(), attacker).register();

            if (!attacker.getEquipment().contains(ammunition.getId())) {
                attacker.out(new SendMessage(getLastFiredMessage(type)));
            }
        }
    }

    private static String getInvalidAmmunitionMessage(RangedWeaponDefinition.AttackType type) {
        if (type == RangedWeaponDefinition.AttackType.SHOT) {
            return "You can't use these arrows with this bow.";
        }
        if (type == RangedWeaponDefinition.AttackType.THROWN) {
            return "That's weird, you broke my fucking code dick";
        }
        return null;
    }

    private static String getNoAmmunitionMessage(RangedWeaponDefinition.AttackType type) {
        if (type == RangedWeaponDefinition.AttackType.SHOT) {
            return "You need some arrows to use this bow!";
        }
        if (type == RangedWeaponDefinition.AttackType.THROWN) {
            return "That's weird, you broke my fucking code dick";
        }
        return null;
    }

    private static String getLastFiredMessage(RangedWeaponDefinition.AttackType type) {
        if (type == RangedWeaponDefinition.AttackType.SHOT) {
            return "You shot your last arrow!";
        }
        if (type == RangedWeaponDefinition.AttackType.THROWN) {
            return "You threw your last shot!";
        }
        return null;
    }

}
