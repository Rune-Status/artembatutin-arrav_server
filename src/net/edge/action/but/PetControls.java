package net.edge.action.but;

import net.edge.content.pets.Pet;
import net.edge.action.ActionInitializer;
import net.edge.action.impl.ButtonAction;
import net.edge.world.entity.region.TraversalMap;
import net.edge.world.locale.Position;
import net.edge.world.World;
import net.edge.world.entity.actor.player.Player;

import java.util.Optional;

public class PetControls extends ActionInitializer {
	
	@Override
	public void init() {
		ButtonAction e = new ButtonAction() {
			@Override
			public boolean click(Player player, int button) {
				Optional<Pet> pet = player.getPetManager().getPet();
				if(pet.isPresent()) {
					Pet p = pet.get();
					Position position = TraversalMap.getRandomNearby(player.getPosition(), player.getPosition(), p.size());
					if(position != null) {
						p.move(position);
					} else {
						p.move(player.getPosition());
					}
					p.forceChat(p.getProgress().getData().getType().getShout());
				}
				return true;
			}
		};
		e.register(74078);
		
		e = new ButtonAction() {
			@Override
			public boolean click(Player player, int button) {
				Pet.onLogout(player);
				player.message("Your pet is now gone");
				return true;
			}
		};
		e.register(74081);

	}
	
}