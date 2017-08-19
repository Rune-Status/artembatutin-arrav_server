package net.edge.content.commands.impl;

import net.edge.content.commands.Command;
import net.edge.content.commands.CommandSignature;
import net.edge.world.entity.actor.player.Player;
import net.edge.world.entity.actor.player.assets.Rights;

@CommandSignature(alias = {"movedown"}, rights = {Rights.ADMINISTRATOR, Rights.ADMINISTRATOR}, syntax = "Moves a plain down, ::moveup")
public final class MoveDownCommand implements Command {

	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		player.move(player.getPosition().move(0, 0, -1));
	}

}
