package net.edge.content.skill.summoning.familiar.impl;

import net.edge.util.rand.RandomUtils;
import net.edge.content.dialogue.impl.NpcDialogue;
import net.edge.content.skill.summoning.familiar.Familiar;
import net.edge.content.skill.summoning.familiar.FamiliarAbility;
import net.edge.content.skill.summoning.familiar.ability.LightEnhancer;
import net.edge.content.skill.summoning.familiar.passive.PassiveAbility;
import net.edge.content.skill.summoning.SummoningData;
import net.edge.world.node.actor.mob.Mob;
import net.edge.world.node.actor.player.Player;

import java.util.Optional;

/**
 * Represents the Vampyre bat familiar.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public final class VampyreBat extends Familiar {
	
	/**
	 * Constructs a new {@link VampyreBat}.
	 */
	public VampyreBat() {
		super(SummoningData.VAMPYRE_BAT);
	}
	
	private final LightEnhancer ability = new LightEnhancer();
	
	@Override
	public FamiliarAbility getAbilityType() {
		return ability;
	}
	
	@Override
	public Optional<PassiveAbility> getPassiveAbility() {
		return Optional.empty();
	}
	
	@Override
	public boolean isCombatic() {
		return true;
	}
	
	@Override
	public void interact(Player player, Mob mob, int id) {
		if(id == 1) {
			player.getDialogueBuilder().append(new NpcDialogue(getId(), RandomUtils.random(RANDOM_DIALOGUE)));
		}
	}
	
	private static final String[] RANDOM_DIALOGUE = new String[]{"You're vasting all that blood, can I have some?", "Ven are you going to feed me?",};
}
