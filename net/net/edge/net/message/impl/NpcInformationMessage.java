package net.edge.net.message.impl;

import net.edge.net.codec.ByteMessage;
import net.edge.net.message.InputMessageListener;
import net.edge.util.rand.Chance;
import net.edge.world.node.entity.npc.NpcDefinition;
import net.edge.world.node.entity.npc.drop.NpcDrop;
import net.edge.world.node.entity.npc.drop.NpcDropManager;
import net.edge.world.node.entity.npc.drop.NpcDropTable;
import net.edge.world.node.entity.player.Player;
import net.edge.world.node.entity.player.assets.Rights;
import net.edge.world.node.item.ItemDefinition;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The message sent from the client which depends on the Npc Information panel integration.
 * @author Artem Batutin <artembatutin@gmail.com>
 */
public final class NpcInformationMessage implements InputMessageListener {
	
	@Override
	public void handleMessage(Player player, int opcode, int size, ByteMessage payload) {
		if(opcode == 19) {
			Chance chance = Chance.values()[payload.get()];
			int item = payload.getShort();
			int min = payload.getShort();
			int max = payload.getShort();
			if(player.getRights() == Rights.DEVELOPER) {
				NpcDropTable table = NpcDropManager.getTables().get(player.getAttr().get("npcInformation").getInt());
				if(table == null) {
					player.message("No table found.");
					return;
				}
				if(min == 99) {
					int index = 0;
					for(NpcDrop d : table.getDrops()) {
						String itemName = ItemDefinition.get(item).getName().toLowerCase().replaceAll(" ", "_");
						if(d != null) {
							String name = ItemDefinition.get(d.getId()).getName().toLowerCase().replaceAll(" ", "_");
							if(itemName.equals(name)) {
								table.getDrops().remove(index);
								table.sort();
								player.message("Removed: " + d.toString());
								return;
							}
						}
						index++;
					}
					player.message("Couldn't remove any drop.");
					return;
				}
				NpcDrop drop = new NpcDrop(item, min, max, chance);
				table.getDrops().add(drop);
				table.sort();
				player.message("Added " + drop.toString());
			} else {
				try {
					BufferedWriter out = new BufferedWriter(new FileWriter("./data/drops.txt", true));
					out.write(player.getAttr().get("npcInformation").getInt() + " { \"id\": " + item + "\"minimum\": " + min + "\"maximum\": " + max + "\"chance\": \"" + chance.name() + "\"}");
					out.newLine();
					out.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			int id = payload.getShort();
			if(id < 0 || id > NpcDefinition.DEFINITIONS.length) {
				player.message("No information found.");
				return;
			}
			player.getAttr().get("npcInformation").set(id);
			NpcDropTable drop = NpcDropManager.getTables().get(id);
			if(drop == null) {
				player.message("This monster doesn't have any drop table.");
				return;
			}
			player.getMessages().sendNpcInformation(id, drop);
			player.getMessages().sendInterface(-11);
		}
	}
}