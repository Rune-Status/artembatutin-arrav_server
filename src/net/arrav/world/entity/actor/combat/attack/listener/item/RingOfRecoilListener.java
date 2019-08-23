package net.arrav.world.entity.actor.combat.attack.listener.item;

import net.arrav.net.packet.out.SendMessage;
import net.arrav.world.entity.actor.Actor;
import net.arrav.world.entity.actor.combat.CombatType;
import net.arrav.world.entity.actor.combat.attack.listener.ItemCombatListenerSignature;
import net.arrav.world.entity.actor.combat.attack.listener.SimplifiedListener;
import net.arrav.world.entity.actor.combat.hit.Hit;
import net.arrav.world.entity.actor.combat.hit.Hitsplat;
import net.arrav.world.entity.actor.player.Player;
import net.arrav.world.entity.item.container.impl.Equipment;

/**
 * @author Michael | Chex
 */
@ItemCombatListenerSignature(items = {2550})
public class RingOfRecoilListener extends SimplifiedListener<Player> {
	
	@Override
	public void block(Actor attacker, Player defender, Hit hit, CombatType combatType) {
		if(hit.getDamage() < 10) {
			return;
		}
		
		int recoil = hit.getDamage() / 10;
		int charges = defender.ringOfRecoil;
		charges -= recoil;
		
		if(charges <= 0) {
			defender.out(new SendMessage("Your ring of recoil has shattered!"));
			defender.getEquipment().unequip(Equipment.RING_SLOT, null, true, -1);
			defender.getCombat().removeListener(this);
			// if charge is negative, recoil was too high for it's charge
			// so we add the -charges to get the amount of recoil left
			recoil += charges;
			charges = 400;
		}
		
		defender.ringOfRecoil = charges;
		attacker.damage(new Hit(recoil, Hitsplat.NORMAL_LOCAL));
		attacker.getCombat().getDamageCache().add(defender, new Hit(recoil));
	}
	
}
