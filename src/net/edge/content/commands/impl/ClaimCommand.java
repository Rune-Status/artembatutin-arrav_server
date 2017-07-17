package net.edge.content.commands.impl;

import net.edge.net.database.connection.use.Donating;
import net.edge.content.commands.Command;
import net.edge.content.commands.CommandSignature;
import net.edge.world.World;
import net.edge.world.node.actor.player.Player;
import net.edge.world.node.actor.player.assets.Rights;

@CommandSignature(alias = {"claim"}, rights = {Rights.ADMINISTRATOR, Rights.ADMINISTRATOR, Rights.SENIOR_MODERATOR, Rights.MODERATOR, Rights.GOLDEN_DONATOR, Rights.EXTREME_DONATOR, Rights.SUPER_DONATOR, Rights.DONATOR, Rights.IRON_MAN, Rights.DESIGNER, Rights.YOUTUBER, Rights.HELPER, Rights.PLAYER}, syntax = "Use this command as ::claim username")
public final class ClaimCommand implements Command {

	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		new Donating(player, World.getDonation());
	}

}
