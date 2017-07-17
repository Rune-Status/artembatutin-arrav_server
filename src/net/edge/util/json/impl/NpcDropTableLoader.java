package net.edge.util.json.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.edge.util.json.JsonLoader;
import net.edge.world.node.actor.npc.drop.ItemCache;
import net.edge.world.node.actor.npc.drop.NpcDrop;
import net.edge.world.node.actor.npc.drop.NpcDropManager;
import net.edge.world.node.actor.npc.drop.NpcDropTable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The {@link JsonLoader} implementation that loads all npc drops.
 * @author lare96 <http://github.com/lare96>
 */
public final class NpcDropTableLoader extends JsonLoader {
	
	/**
	 * Creates a new {@link NpcDropTableLoader}.
	 */
	public NpcDropTableLoader() {
		super("./data/json/npcs/npc_drops.json");
	}
	
	/**
	 * A constant defined to write a new set of npc ids for the client.
	 */
	private final boolean OUTPUT = false;
	
	/**
	 * A set of written ids.
	 */
	private Set<Integer> written = new HashSet<>();
	
	/**
	 * The writer to write our ids.
	 */
	private PrintWriter writer;
	
	@Override
	public void load(JsonObject reader, Gson builder) {
		int[] array = builder.fromJson(reader.get("ids"), int[].class);
		NpcDrop[] unique = Objects.requireNonNull(builder.fromJson(reader.get("unique"), NpcDrop[].class));
		ItemCache[] common = Objects.requireNonNull(builder.fromJson(reader.get("common"), ItemCache[].class));
		if(Arrays.stream(common).anyMatch(Objects::isNull))
			throw new NullPointerException("Invalid common drop table [" + array[0] + "]," + " npc_drops.json");
		Arrays.stream(array).forEach(id -> NpcDropManager.TABLES.put(id, new NpcDropTable(unique, common)));
		
		for(int i = 0; i < array.length; i++) {
			if(i != 0)
				NpcDropManager.REDIRECTS.put(array[i], array[0]);
			NpcDropManager.getTables().put(array[i], new NpcDropTable(unique, common));
		}
		
		if(OUTPUT && writer != null) {
			for(int i : array) {
				if(!written.contains(i)) {
					writer.print(i + "-");
				}
			}
		}
		
	}
	
	@Override
	public void start() {
		try {
			File out = new File("./drops2.txt");
			writer = new PrintWriter(out);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void end() {
		if(writer != null) {
			writer.flush();
			writer.close();
		}
		written.clear();
	}
}