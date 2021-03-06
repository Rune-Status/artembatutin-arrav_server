package net.arrav.action.obj;

import net.arrav.action.ActionInitializer;
import net.arrav.action.impl.ObjectAction;
import net.arrav.world.Animation;
import net.arrav.world.entity.actor.move.ForcedMovement;
import net.arrav.world.entity.actor.move.ForcedMovementManager;
import net.arrav.world.entity.actor.player.Player;
import net.arrav.world.entity.object.GameObject;

public class EdgevilleDitch extends ActionInitializer {
	@Override
	public void init() {
		ObjectAction jump = new ObjectAction() {
			@Override
			public boolean click(Player player, GameObject object, int click) {
				ForcedMovement jump;
				int wild = 3;
				if(player.getPosition().getX() < 3000 && player.getPosition().getX() > 2994) {
					if(player.getPosition().getX() > 2997)
						wild = -3;
					jump = ForcedMovement.create(player, player.getPosition().move(wild, 0), new Animation(6132));
				} else {
					if(player.getPosition().getY() > 3520)
						wild = -3;
					jump = ForcedMovement.create(player, player.getPosition().move(0, wild), new Animation(6132));
				}
				jump.setSecondSpeed(50);
				ForcedMovementManager.submit(player, jump);
				return true;
			}
		};
		jump.registerFirst(23271);
		jump.registerFirst(43443);
	}
}
