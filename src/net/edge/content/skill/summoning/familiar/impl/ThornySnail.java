package net.edge.content.skill.summoning.familiar.impl;

import net.edge.util.rand.RandomUtils;
import net.edge.content.dialogue.impl.NpcDialogue;
import net.edge.content.skill.summoning.Summoning;
import net.edge.content.skill.summoning.familiar.Familiar;
import net.edge.content.skill.summoning.familiar.FamiliarAbility;
import net.edge.content.skill.summoning.familiar.ability.BeastOfBurden;
import net.edge.content.skill.summoning.familiar.passive.PassiveAbility;
import net.edge.content.skill.summoning.SummoningData;
import net.edge.world.node.actor.npc.Npc;
import net.edge.world.node.actor.player.Player;

import java.util.Optional;

/**
 * Represents the Thorny snail familiar.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public final class ThornySnail extends Familiar {
	
	/**
	 * Constructs a new {@link ThornySnail}.
	 */
	public ThornySnail() {
		super(SummoningData.THORNY_SNAIL);
	}
	
	private final BeastOfBurden bob = new BeastOfBurden(3);
	
	@Override
	public FamiliarAbility getAbilityType() {
		return bob;
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
	public void interact(Player player, Npc npc, int id) {
		if(id == 1) {
			player.getDialogueBuilder().append(RandomUtils.random(RANDOM_DIALOGUE));
		} else if(id == 2) {
			Summoning.openBeastOfBurden(player, npc);
		}
	}
	
	private final NpcDialogue[] RANDOM_DIALOGUE = new NpcDialogue[]{new NpcDialogue(getId(), "All this running around the place is fun!"), new NpcDialogue(getId(), "Okay, I have to ask, what are those things", "you people totter on about?"),};
}
