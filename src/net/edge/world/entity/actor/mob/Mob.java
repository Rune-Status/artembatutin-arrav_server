package net.edge.world.entity.actor.mob;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.edge.content.combat.CombatUtil;
import net.edge.content.combat.CombatType;
import net.edge.content.combat.effect.CombatPoisonEffect;
import net.edge.content.combat.magic.CombatWeaken;
import net.edge.content.combat.strategy.CombatStrategy;
import net.edge.world.locale.Position;
import net.edge.world.World;
import net.edge.world.entity.EntityType;
import net.edge.world.entity.actor.Actor;
import net.edge.world.Hit;
import net.edge.world.PoisonType;
import net.edge.world.entity.actor.mob.impl.DefaultMob;
import net.edge.world.entity.actor.mob.impl.KalphiteQueen;
import net.edge.world.entity.actor.mob.impl.corp.CorporealBeast;
import net.edge.world.entity.actor.mob.impl.glacor.Glacor;
import net.edge.world.entity.actor.mob.impl.gwd.CommanderZilyana;
import net.edge.world.entity.actor.mob.impl.gwd.GeneralGraardor;
import net.edge.world.entity.actor.mob.impl.gwd.KreeArra;
import net.edge.world.entity.actor.mob.strategy.impl.TormentedDemonCombatStrategy;
import net.edge.world.entity.actor.mob.strategy.impl.WildyWyrmCombatStrategy;
import net.edge.world.entity.actor.player.Player;
import net.edge.world.entity.actor.update.UpdateFlag;
import net.edge.world.locale.loc.Location;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;

/**
 * The character implementation that represents a node that is operated by the server.
 * This type of node functions solely through the server executing functions.
 * @author Artem batutin <artembatutin@gmail.com>
 * @author lare96 <http://github.com/lare96>
 */
public abstract class Mob extends Actor {
	
	/**
	 * A mapping which contains all the custom npcs by their id.
	 */
	public static final Int2ObjectArrayMap<Function<Position, Mob>> CUSTOM_MOBS = new Int2ObjectArrayMap<>(
			ImmutableMap.<Integer, Function<Position, Mob>>builder()
					.put(6247, s -> new CommanderZilyana())
					.put(6260, s -> new GeneralGraardor())
					.put(6222, s -> new KreeArra())
					.put(8133, s -> new CorporealBeast())
					.put(1158, KalphiteQueen::new)
					.put(14301, Glacor::new).build());

	/**
	 * Gets a certain npc by the specified {@code id} and supplies it's position.
	 * @param id  the id to get the npc by.
	 * @param pos the position to supply this npc to.
	 * @return the npc.
	 */
	public static Mob getNpc(int id, Position pos) {
		if(id >= 8349 && id <= 8351) {
			Mob mob = new DefaultMob(id, pos);
			return mob.setStrategy(Optional.of(new TormentedDemonCombatStrategy(mob)));
		}
		if(id == 3334) {
			Mob mob = new DefaultMob(id, pos);
			return mob.setStrategy(Optional.of(new WildyWyrmCombatStrategy(mob)));
		}
		return CUSTOM_MOBS.containsKey(id) ? CUSTOM_MOBS.get(id).apply(pos).create() : new DefaultMob(id, pos);
	}
	
	/**
	 * The identification for this NPC.
	 */
	private final int id;
	
	/**
	 * The maximum health of this NPC.
	 */
	private final int maxHealth;
	
	/**
	 * The original position that this NPC was created on.
	 */
	private final Position originalPosition;
	
	/**
	 * The movement coordinator for this NPC.
	 */
	private final MobMovementCoordinator movementCoordinator = new MobMovementCoordinator(this);
	
	/**
	 * The current health of this NPC.
	 */
	private int currentHealth;
	
	/**
	 * Determines if this NPC was originally random walking.
	 */
	private boolean originalRandomWalk;
	
	/**
	 * Determines if this NPC respawns.
	 */
	private boolean respawn;
	
	/**
	 * The spell that this NPC is weakened by.
	 */
	private CombatWeaken weakenedBy;
	
	/**
	 * The player slot this npc was spawned for.
	 */
	private int owner = -1;
	
	/**
	 * The transformation identifier.
	 */
	private OptionalInt transform = OptionalInt.empty();
	
	/**
	 * The flag determining if this {@link Mob} is active in his region.
	 */
	private boolean active = false;
	
	/**
	 * The flag determining if the {@link Mob} is a smart npc.
	 */
	private boolean smart;
	
	/**
	 * The special amount of this npc, between 0 and 100. 101 sets it off.
	 */
	private OptionalInt special = OptionalInt.empty();
	
	/**
	 * The cached field to determine this npcs dynamic combat strategy.
	 * <p></p>
	 * if this field is set, it'll use this strategy instead and each npc
	 * will have it's own strategy instead of sharing it on a global state.
	 */
	private Optional<CombatStrategy> strategy = Optional.empty();
	
	/**
	 * Creates a new {@link Mob}.
	 * @param id       the identification for this NPC.
	 * @param position the position of this character in the world.
	 */
	public Mob(int id, Position position) {
		super(position, EntityType.NPC);
		this.id = id;
		this.originalPosition = position.copy();
		this.maxHealth = getDefinition().getHitpoints();
		this.currentHealth = maxHealth;
		this.owner = -1;
		getMovementCoordinator().setRadius(3);
	}
	
	/**
	 * Creates the particular {@link Mob instance}.
	 * @return new {@link Mob} instance.
	 */
	public abstract Mob create();
	
	@Override
	public void register() {
		
	}
	
	@Override
	public void dispose() {
		setVisible(false);
		setPosition(new Position(1, 1));
		World.get().getTask().cancel(this);
		getRegion().removeChar(this);
	}
	
	@Override
	public void move(Position destination) {
		getMovementQueue().reset();
		super.setPosition(destination.copy());
		setUpdates(true, true);
		setTeleportStage(-1);
	}
	
	@Override
	public void preUpdate() {
		if(active()) {
			update();
			getMovementQueue().sequence();
		}
	}
	
	@Override
	public void update() {
		//No sequencing.
	}
	
	@Override
	public void appendDeath() {
		setDead(true);
		World.get().submit(new MobDeath(this));
	}
	
	@Override
	public Hit decrementHealth(Hit hit) {
		if(hit.getDamage() > currentHealth) {
			hit.setDamage(currentHealth);
			if(hit.getType() == Hit.HitType.CRITICAL) {
				hit.setType(Hit.HitType.NORMAL);
			}
		}
		if (!getAttr().get("invincible").getBoolean()) {
			currentHealth -= hit.getDamage();
		}
		if(currentHealth <= 0) {
			setCurrentHealth(0);
		}
		return hit;
	}
	
	@Override
	public Set<Player> getLocalPlayers() {
		return getRegion().getPlayers();
	}
	
	@Override
	public Set<Mob> getLocalMobs() {
		return getRegion().getMobs();
	}
	
	@Override
	public int getAttackSpeed() {
		return this.getDefinition().getAttackSpeed();
	}
	
	@Override
	public int getCurrentHealth() {
		return currentHealth;
	}
	
	@Override
	public CombatStrategy determineStrategy() {
		return strategy.orElse(CombatUtil.determineStrategy(id));
	}
	
	@Override
	public int getBaseAttack(CombatType type) {
		int value;
		if(type == CombatType.MAGIC)
			value = getDefinition().getMagicLevel();
		else if(type == CombatType.RANGED)
			value = getDefinition().getRangedLevel();
		else
			value = getDefinition().getAttackLevel();
		if(weakenedBy == CombatWeaken.ATTACK_LOW || weakenedBy == CombatWeaken.ATTACK_HIGH)
			value -= (int) ((weakenedBy.getRate()) * (value));
		return value;
	}
	
	@Override
	public int getBaseDefence(CombatType type) {
		int value = getDefinition().getDefenceLevel();
		if(weakenedBy == CombatWeaken.DEFENCE_LOW || weakenedBy == CombatWeaken.DEFENCE_HIGH)
			value -= (int) ((weakenedBy.getRate()) * (value));
		return value;
	}
	
	@Override
	public void onSuccessfulHit(Actor victim, CombatType type) {
		if(getDefinition().isPoisonous())
			victim.poison(CombatPoisonEffect.getPoisonType(id).orElse(PoisonType.DEFAULT_NPC));
	}
	
	@Override
	public void healEntity(int damage) {
		if((currentHealth + damage) > maxHealth) {
			currentHealth = maxHealth;
			return;
		}
		currentHealth += damage;
	}
	
	@Override
	public boolean weaken(CombatWeaken effect) {
		if(weakenedBy == null) {
			weakenedBy = effect;
			return true;
		}
		return false;
	}
	
	@Override
	public boolean inMulti() {
		return Location.inMultiCombat(this);
	}
	
	@Override
	public boolean inWilderness() {
		return Location.inWilderness(this);
	}
	
	/**
	 * Activates the {@code TRANSFORM} update mask for this non-player
	 * character.
	 * @param id the new npc to transform this npc into.
	 */
	public void transform(int id) {
		transform = OptionalInt.of(id);
		getFlags().flag(UpdateFlag.TRANSFORM);
	}
	
	/**
	 * Removes the {@code TRANSFORM} update mask for this non-player character.
	 */
	public void untransform() {
		transform = OptionalInt.empty();
		getFlags().flag(UpdateFlag.TRANSFORM);
	}

	/**
	 * Gets the identification of this NPC.
	 * @return the identification.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Gets the {@link MobType} of this npc.
	 * @return type.
	 */
	public MobType getMobType() {
		return MobType.NONE;
	}
	
	/**
	 * Gets the maximum health of this NPC.
	 * @return the maximum health.
	 */
	public int getMaxHealth() {
		return maxHealth;
	}
	
	/**
	 * Sets the value for {@link Mob#currentHealth}.
	 * @param currentHealth the new value to set.
	 */
	public void setCurrentHealth(int currentHealth) {
		this.currentHealth = currentHealth;
		if(currentHealth == 0) {
			if(!isDead()) {
				appendDeath();
			}
		}
	}
	
	/**
	 * Gets the special amount for this NPC.
	 * @return the special amount.
	 */
	public OptionalInt getSpecial() {
		return special;
	}
	
	/**
	 * Sets the value for {@link Mob#special}.
	 * @param special the new value to set.
	 */
	public void setSpecial(int special) {
		this.special = OptionalInt.of(special);
	}
	
	/**
	 * Resets the {@link Mob#special}.
	 */
	public void resetSpecial() {
		this.special = OptionalInt.empty();
	}
	
	/**
	 * Gets the original position that this NPC was created on.
	 * @return the original position.
	 */
	public Position getOriginalPosition() {
		return originalPosition;
	}
	
	/**
	 * Gets the movement coordinator for this NPC.
	 * @return the movement coordinator.
	 */
	public MobMovementCoordinator getMovementCoordinator() {
		return movementCoordinator;
	}
	
	/**
	 * Determines if this NPC was originally random walking.
	 * @return {@code true} if this NPC was originally walking, {@code false}
	 * otherwise.
	 */
	public boolean isOriginalRandomWalk() {
		return originalRandomWalk;
	}
	
	/**
	 * Sets the value for {@link Mob#originalRandomWalk}.
	 * @param originalRandomWalk the new value to set.
	 */
	public void setOriginalRandomWalk(boolean originalRandomWalk) {
		this.originalRandomWalk = originalRandomWalk;
	}
	
	/**
	 * Gets the spell that this NPC is weakened by.
	 * @return the weakening spell.
	 */
	public CombatWeaken getWeakenedBy() {
		return weakenedBy;
	}
	
	/**
	 * Sets the value for {@link Mob#weakenedBy}.
	 * @param weakenedBy the new value to set.
	 */
	public void setWeakenedBy(CombatWeaken weakenedBy) {
		this.weakenedBy = weakenedBy;
	}
	
	/**
	 * Determines if this NPC respawns.
	 * @return {@code true} if this NPC respawns, {@code false} otherwise.
	 */
	public boolean isRespawn() {
		return respawn;
	}
	
	/**
	 * Sets the value for {@link Mob#respawn}.
	 * @param respawn the new value to set.
	 */
	public void setRespawn(boolean respawn) {
		this.respawn = respawn;
	}
	
	/**
	 * @return the player's slot this npc was spawned for.
	 */
	public int getOwner() {
		return owner;
	}
	
	/**
	 * The flag which identifies if this npc was spawned for the player by the username.
	 * @param spawnedFor the player to check for.
	 * @return <true> if the npc was spawned for the player, <false> otherwise.
	 */
	public boolean isOwner(Player spawnedFor) {
		return this.owner != -1 && this.owner == spawnedFor.getSlot();
	}
	
	/**
	 * Sets the player's slot this npc was spawned for.
	 * @param player the player we're spawning this npc for.
	 */
	public void setOwner(Player player) {
		this.owner = player.getSlot();
		player.getMobs().add(this);
	}
	
	@Override
	public boolean active() {
		return active;
	}
	
	/**
	 * Sets the new value for {@link Mob#active}.
	 * @param active the new value to set.
	 */
	public void setActive(boolean active) {
		this.active = active;
		if(!active && World.getNpcMovementTask().getMobs().contains(this)) {
			World.getNpcMovementTask().getMobs().remove(this);
		} else if(active && movementCoordinator.isCoordinate() && !World.getNpcMovementTask().getMobs().contains(this)) {
			World.getNpcMovementTask().getMobs().add(this);
		}
	}
	
	/**
	 * Determines if the npc is a smart npc.
	 * @return {@code true} if the npc is smart, {@code false} otherwise.
	 */
	public boolean isSmart() {
		return smart;
	}
	
	/**
	 * Sets the new value for {@link Mob#smart}.
	 * @param smart the new value to set.
	 */
	public void setSmart(boolean smart) {
		this.smart = smart;
	}
	
	/**
	 * Gets the definition for this NPC.
	 * @return the definition.
	 */
	public MobDefinition getDefinition() {
		return MobDefinition.DEFINITIONS[transform.orElse(id)];
	}
	
	/**
	 * Gets the transformation identifier.
	 * @return the transformation id.
	 */
	public OptionalInt getTransform() {
		return transform;
	}
	
	/**
	 * Gets the combat strategy.
	 * @return combat strategy.
	 */
	public Optional<CombatStrategy> getStrategy() {
		return strategy;
	}
	
	/**
	 * Sets a new value for {@link #strategy}.
	 * @param strategy the new value to set.
	 */
	public Mob setStrategy(Optional<CombatStrategy> strategy) {
		this.strategy = strategy;
		return this;
	}
	
	/**
	 * Determines if this npc is a familiar.
	 * @return <true> if the npc is a familiar, <false> otherwise.
	 */
	public boolean isFamiliar() {
		return false;
	}
	
	/**
	 * Determines if this npc is a pet.
	 * @return <true> if the npc is a pet, <false> otherwise.
	 */
	public boolean isPet() {
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Mob) {
			Mob other = (Mob) obj;
			return getId() == other.getId() && getSlot() == other.getSlot();
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getId(), getSlot());
	}
	
	@Override
	public String toString() {
		return "NPC[slot= " + getSlot() + ", name=" + getDefinition().getName() + "]";
	}
	
}