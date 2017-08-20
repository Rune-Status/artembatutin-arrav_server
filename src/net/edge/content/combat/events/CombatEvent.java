package net.edge.content.combat.events;

import net.edge.content.combat.hit.CombatHit;
import net.edge.world.entity.actor.Actor;

public class CombatEvent {
    private final int delay;
    private int ticks;

    private Actor defender;
    private CombatHit hit;
    private EventInterface execute;

    public CombatEvent(Actor defender, int delay, CombatHit hit, EventInterface execute) {
        this.delay = delay;
        this.defender = defender;
        this.hit = hit;
        this.execute = execute;
    }

    public CombatEvent(Actor defender, int delay, EventInterface execute) {
        this.delay = delay;
        this.defender = defender;
        this.execute = execute;
    }

    public Actor getDefender() {
        return defender;
    }

    boolean canExecute() {
        return ++ticks >= delay;
    }

    public void execute() {
        execute.execute(defender, hit);
    }

}
