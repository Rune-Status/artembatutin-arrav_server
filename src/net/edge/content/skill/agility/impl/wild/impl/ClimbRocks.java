package net.edge.content.skill.agility.impl.wild.impl;

import net.edge.task.LinkedTaskSequence;
import net.edge.content.skill.agility.obstacle.ObstacleActivity;
import net.edge.content.skill.agility.obstacle.ObstacleType;
import net.edge.locale.Position;
import net.edge.world.Animation;
import net.edge.world.Direction;
import net.edge.world.node.actor.player.Player;

import java.util.stream.IntStream;

public final class ClimbRocks extends ObstacleActivity {

	public ClimbRocks(Player player) {
		super(new Position(player.getPosition().getX(), 3937), new Position(player.getPosition().getX(), 3933), ObstacleType.ROCKS.getAnimation(), 52, 0);
	}

	@Override
	public boolean canExecute(Player player) {
		if(!IntStream.rangeClosed(2993, 2995).anyMatch(i -> player.getPosition().getX() == i)) {
			player.message("You can't cross this obstacle from here!");
			return false;
		}
		return true;
	}

	@Override
	public void onSubmit(Player player) {
		LinkedTaskSequence seq = new LinkedTaskSequence();

		seq.connect(1, () -> player.faceDirection(Direction.SOUTH));

		seq.connect(1, () -> player.animation(new Animation(3378)));

		seq.connect(6, () -> {
			player.animation(null);
			player.move(new Position(player.getPosition().getX(), 3933));
		});
		seq.start();
	}
}
