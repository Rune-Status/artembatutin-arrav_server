package net.edge.content.commands.impl;

import net.edge.content.commands.Command;
import net.edge.content.commands.CommandSignature;
import net.edge.world.World;
import net.edge.world.node.entity.player.Player;
import net.edge.world.node.entity.player.assets.Rights;

@CommandSignature(alias = {"vote"}, rights = {Rights.DEVELOPER, Rights.ADMINISTRATOR}, syntax = "Use this command as ::vote player amount")
public final class GiveVoteCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		Player vote = World.get().getPlayer(cmd[1].replaceAll("_", " ")).orElse(null);
		if(vote == null)
			return;
		int amount = Integer.parseInt(cmd[2]);
		vote.setVote(vote.getVote() + amount);
		player.message("You gave " + amount + " slayer points to " + vote.getFormatUsername());
		vote.message("You received " + amount + " slayer points from " + player.getFormatUsername());
	}
	
}