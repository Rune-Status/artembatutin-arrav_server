package net.edge.content.commands.impl;

import net.edge.World;
import net.edge.content.commands.Command;
import net.edge.content.commands.CommandSignature;
import net.edge.content.skill.SkillData;
import net.edge.content.skill.Skills;
import net.edge.world.node.entity.player.Player;
import net.edge.world.node.entity.player.assets.Rights;

@CommandSignature(alias = {"setplevel"}, rights = {Rights.DEVELOPER}, syntax = "Use this command as ::setplevel playername skill level")
public final class SetPlayerlevelCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		Player p = World.getPlayer(cmd[1].replaceAll("_", " ")).orElse(null);
		if(p == null)
			return;
		String skill = cmd[2].toUpperCase();
		int level = Integer.parseInt(cmd[3]);
		
		int skillId = SkillData.valueOf(skill).getId();
		
		p.getSkills()[skillId].setRealLevel(level);
		Skills.refresh(p, skillId);
		player.message("You've successfully set " + skill + " to level " + level + ".");
		p.message(player.getFormatUsername() + " has set " + skill + " to level " + level + ".");
	}
	
}