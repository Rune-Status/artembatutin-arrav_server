package net.edge.content.combat.effect;

import net.edge.world.node.entity.EntityNode;
import net.edge.world.node.entity.player.Player;
import net.edge.world.node.entity.update.UpdateFlag;

/**
 * The combat effect applied when a player needs to be skulled.
 * @author lare96 <http://github.com/lare96>
 */
public final class CombatSkullEffect extends CombatEffect {
	
	/**
	 * Creates a new {@link CombatSkullEffect}.
	 */
	public CombatSkullEffect() {
		super(50);
	}
	
	@Override
	public boolean apply(EntityNode c) {
		if(c.isPlayer()) {
			Player player = (Player) c;
			if(player.getSkullTimer().get() > 0) {
				return false;
			}
			player.getSkullTimer().set(3000);
			player.setSkullIcon(Player.WHITE_SKULL);
			player.getFlags().flag(UpdateFlag.APPEARANCE);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removeOn(EntityNode c) {
		if(c.isPlayer()) {
			Player player = (Player) c;
			if(player.getSkullTimer().get() <= 0) {
				player.setSkullIcon(-1);
				player.getFlags().flag(UpdateFlag.APPEARANCE);
				return true;
			}
			return false;
		}
		return true;
	}
	
	@Override
	public void process(EntityNode c) {
		if(c.isPlayer()) {
			Player player = (Player) c;
			player.getSkullTimer().decrementAndGet(50, 0);
		}
	}
	
	@Override
	public boolean onLogin(EntityNode c) {
		if(c.isPlayer()) {
			Player player = (Player) c;
			
			if(player.getSkullTimer().get() > 0) {
				player.setSkullIcon(Player.WHITE_SKULL);
				return true;
			}
			//			if(FightCavesHandler.isChampion(player))
			//				player.setSkullIcon(Player.RED_SKULL);
		}
		return false;
	}
}