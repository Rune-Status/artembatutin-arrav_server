package net.edge.content.combat.magic.lunars.impl.spells;

import net.edge.World;
import net.edge.content.combat.Combat;
import net.edge.content.combat.magic.lunars.impl.LunarButtonSpell;
import net.edge.world.node.entity.EntityNode;
import net.edge.world.Animation;
import net.edge.world.Graphic;
import net.edge.world.node.entity.player.Player;
import net.edge.world.node.item.Item;

import java.util.List;
import java.util.Optional;

/**
 * Holds functionality for the cure group spell.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public final class CureGroup extends LunarButtonSpell {
	
	/**
	 * Constructs a new {@link CureGroup}.
	 */
	public CureGroup() {
		super(117170);
	}
	
	List<Player> local_players;
	
	@Override
	public void effect(Player caster, EntityNode victim) {
		for(Player target : local_players) {
			target.graphic(new Graphic(744, 90));
			target.getPoisonDamage().set(0);
			target.getMessages().sendConfig(174, 0);
			target.message("Your poison has been cured by " + caster.getFormatUsername());
		}
	}
	
	@Override
	public boolean prerequisites(Player caster, EntityNode victim) {
		local_players = Combat.charactersWithinDistance(caster, World.getLocalPlayers(caster), 1);
		
		if(local_players.isEmpty()) {
			if(caster.isPlayer()) {
				caster.toPlayer().message("There are no players within your radius to cast this spell for.");
			}
			return false;
		}
		for(Player target : local_players) {
			if(!target.isPoisoned() || !target.getAttr().get("accept_aid").getBoolean()) {
				continue;
			}
			if(target.isPoisoned()) {
				return true;
			}
		}
		Player player = (Player) caster;
		player.message("There are no players within your radius which are poisoned.");
		return false;
	}
	
	@Override
	public String name() {
		return "Cure Group";
	}
	
	@Override
	public Optional<Animation> startAnimation() {
		return Optional.of(new Animation(4409));
	}
	
	@Override
	public int levelRequired() {
		return 74;
	}
	
	@Override
	public double baseExperience() {
		return 74;
	}
	
	@Override
	public Optional<Item[]> itemsRequired(Player player) {
		return Optional.of(new Item[]{new Item(9075, 2), new Item(564, 2)});
	}
	
}