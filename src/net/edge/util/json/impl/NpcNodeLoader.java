package net.edge.util.json.impl;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.edge.util.json.JsonLoader;
import net.edge.world.locale.Position;
import net.edge.world.Direction;
import net.edge.world.World;
import net.edge.world.entity.actor.mob.Mob;

import java.util.Objects;

/**
 * The {@link JsonLoader} implementation that loads all npc nodes.
 * @author lare96 <http://github.com/lare96>
 */
public final class NpcNodeLoader extends JsonLoader {
	
	/**
	 * Creates a new {@link NpcNodeLoader}.
	 */
	public NpcNodeLoader() {
		super("./data/json/npcs/npc_nodes.json");
	}
	
	@Override
	public void load(JsonObject reader, Gson builder) {
		int id = reader.get("id").getAsInt();
		Position position = Objects.requireNonNull(builder.fromJson(reader.get("position").getAsJsonObject(), Position.class));
		Direction dir = reader.get("face") != null ? builder.fromJson(reader.get("face"), Direction.class) : Direction.NONE;
		boolean coordinate = reader.get("random-walk").getAsBoolean();
		int radius = 0;
		if(reader.has("walk-radius"))
			radius = reader.get("walk-radius").getAsInt();
		Preconditions.checkState(!(coordinate && radius == 0));
		Preconditions.checkState(!(!coordinate && radius > 0));
		Preconditions.checkState(!(dir != Direction.NONE && radius > 0));
		Mob mob = Mob.getNpc(id, position);
		mob.setOriginalRandomWalk(coordinate);
		mob.getMovementCoordinator().setCoordinate(coordinate);
		mob.getMovementCoordinator().setRadius(radius);
		if(dir != Direction.NONE) {
			mob.getMovementCoordinator().setFacingDirection(dir);
		}
		mob.setRespawn(true);
		if(!World.get().getNpcs().add(mob))
			throw new IllegalStateException(mob.toString() + " could not be added to the world!");
	}

}
