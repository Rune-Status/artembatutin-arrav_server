package net.edge.content.commands.impl;

import net.edge.Application;
import net.edge.content.commands.Command;
import net.edge.content.commands.CommandSignature;
import net.edge.net.packet.out.SendLogout;
import net.edge.net.packet.out.SendUpdateTimer;
import net.edge.task.Task;
import net.edge.world.World;
import net.edge.world.entity.actor.player.Player;
import net.edge.world.entity.actor.player.PlayerSerialization;
import net.edge.world.entity.actor.player.assets.Rights;

import java.util.Iterator;

@CommandSignature(alias = {"update"}, rights = {Rights.ADMINISTRATOR}, syntax = "Updates the server, ::update seconds")
public final class UpdateCommand implements Command {
	
	/**
	 * The updating count in progress flag.
	 */
	public static int inProgess = 0;
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		inProgess = 1;
		int timer = Integer.parseInt(cmd[1]);
		Player other;
		Iterator<Player> it = World.get().getPlayers().iterator();
		while((other = it.next()) != null) {
			other.out(new SendUpdateTimer(timer * 50 / 30));
		}
		Application.UPDATING = timer;
		World.get().getTask().submit(new Task(1, true) {
			@Override
			protected void execute() {
				Application.UPDATING -= 0.6;
				System.out.println("Update count: " + Application.UPDATING);
				if(Application.UPDATING <= 0) {
					inProgess = 2;
					System.out.println("Setting player into updating mode.");
					System.out.println("Logging players out... - Players online: " + World.get().getPlayers().size());
					for(Player p : World.get().getPlayers()) {
						if(p == null)
							continue;
						new PlayerSerialization(p).serialize();
						p.out(new SendLogout());
					}
					System.out.println("Waiting for shutdown.");
					World.get().getTask().submit(new Task(10, false) {
						@Override
						protected void execute() {
							System.out.println("Terminating server instance - Players online: " + World.get().getPlayers().size());
							World.get().shutdown();
							System.exit(0);
							this.cancel();
						}
					});
				}
			}
		});
	}
	
}
