package net.edge.event.but;

import net.edge.event.EventInitializer;
import net.edge.event.impl.ButtonEvent;
import net.edge.game.GameConstants;
import net.edge.net.PunishmentHandler;
import net.edge.world.node.actor.player.Player;
import net.edge.world.node.item.Item;

public class IronManSelection extends EventInitializer {
	
	@Override
	public void init() {
		ButtonEvent e = new ButtonEvent() {
			@Override
			public boolean click(Player player, int button) {
				boolean iron = button == 200;
				if(iron)
					player.setIron(1, true);
				else
					player.setIron(0, true);
				player.getActivityManager().enable();
				player.sendDefaultSidebars();
				player.closeWidget();
				player.getInventory().clear(false);
				if(iron) {
					player.getInventory().fillItems(GameConstants.IRON_STARTER);
				} else if(!PunishmentHandler.recievedStarter(player.getSession().getHost())) {
					player.getInventory().fillItems(GameConstants.REGULAR_STARTER);
					PunishmentHandler.addStarter(player.getSession().getHost());
				} else {
					player.getInventory().add(new Item(995, 500000));
					player.message("You already received your regular starter package before.");
				}
				player.getInventory().updateBulk();
				player.getAttr().get("introduction_stage").set(3);
				player.getActivityManager().enable();
				return true;
			}
		};
		e.register(200);
		e.register(201);

	}
	
}
