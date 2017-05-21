package net.edge.world.content.skill.herblore;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.edge.task.Task;
import net.edge.world.content.skill.SkillData;
import net.edge.world.content.skill.action.impl.ProducingSkillAction;
import net.edge.world.Animation;
import net.edge.world.node.entity.player.Player;
import net.edge.world.node.item.Item;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Represents the procession for grinding items.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public final class Grinding extends ProducingSkillAction {
	
	/**
	 * The {@link GrindingData} holding all the data required for processing
	 * the creation of grindable items.
	 */
	private final GrindingData definition;
	
	/**
	 * Represents the identifier for the pestle and mortar.
	 */
	private static final Item PESTLE_MORTAR = new Item(233);
	
	/**
	 * Represents the animation for tar creation.
	 */
	private static final Animation ANIMATION = new Animation(364);
	
	/**
	 * Constructs a new {@link Grinding}.
	 */
	public Grinding(Player player, Item firstItem, Item secondItem) {
		super(player, Optional.of(player.getPosition()));
		
		Item item = firstItem.getId() == PESTLE_MORTAR.getId() ? secondItem : firstItem;
		this.definition = GrindingData.getDefinition(item.getId()).orElse(null);
	}
	
	/**
	 * Produces guam tars if the player has the requirements required.
	 * @param player     {@link #getPlayer()};
	 * @param firstItem  the first item that was used on the second item.
	 * @param secondItem the second item that was used on by the first item.
	 * @return <true> if the produce was successful, <false> otherwise.
	 */
	public static boolean produce(Player player, Item firstItem, Item secondItem) {
		Grinding grinding = new Grinding(player, firstItem, secondItem);
		
		if(grinding.definition == null) {
			return false;
		}
		
		if(firstItem.getId() == PESTLE_MORTAR.getId() && secondItem.getId() == grinding.definition.item.getId() || firstItem.getId() == grinding.definition.item.getId() && secondItem.getId() == PESTLE_MORTAR.getId()) {
			grinding.start();
			return true;
		}
		return false;
	}
	
	@Override
	public void onProduce(Task t, boolean success) {
		if(success) {
			getPlayer().animation(ANIMATION);
		}
	}
	
	@Override
	public Optional<Item[]> removeItem() {
		return Optional.of(new Item[]{definition.item});
	}
	
	@Override
	public Optional<Item[]> produceItem() {
		return Optional.of(new Item[]{definition.product});
	}
	
	@Override
	public int delay() {
		return 3;
	}
	
	@Override
	public boolean instant() {
		return true;
	}
	
	@Override
	public boolean init() {
		if(!checkGrinding()) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean canExecute() {
		if(!checkGrinding()) {
			return false;
		}
		return true;
	}
	
	@Override
	public double experience() {
		return definition.experience;
	}
	
	@Override
	public SkillData skill() {
		return SkillData.HERBLORE;
	}
	
	private boolean checkGrinding() {
		if(definition == null) {
			return false;
		}
		if(!getPlayer().getInventory().contains(PESTLE_MORTAR)) {
			getPlayer().message("You need a pestle and mortar to do this.");
			return false;
		}
		return true;
	}
	
	/**
	 * The data required for processing the creation of grindable items.
	 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
	 */
	private enum GrindingData {
		UNICORN_HORN(237, 235, 20.0),
		CHOCOLATE_BAR(1973, 1975, 50),
		NEST(5075, 6693, 65),
		KEBBIT_TEETH(10109, 10111, 100),
		BLUE_DRAGON_SCALE(243, 241, 175),
		DIAMOND_ROOT(14703, 14704, 225),
		DESERT_GOAT_HORN(9735, 9736, 320),
		RUNE_SHARDS(6466, 6467, 300),
		MUD_RUNE(4698, 9594, 350),
		ASHES(592, 8865, 380),
		SEAWEED(401, 6683, 400),
		EDIBLE_SEAWEED(403, 6683, 420),
		BAT_BONES(530, 2391, 445),
		CHARCOAL(973, 704, 460),
		RAW_COD(341, 7528, 490),
		KELP(7516, 7517, 520),
		CRAB_MEAT(7518, 7527, 540),
		ASTRAL_RUNE_SHARDS(11156, 11155, 600),
		SUQAH_TOOTH(9079, 9082, 650),
		DRIED_THISTLE(3263, 3264, 710),
		GARLIC(1550, 4698, 765),
		BLACK_MUSHROOM(4620, 4622, 840);
		
		/**
		 * Caches our enum values.
		 */
		private static final ImmutableSet<GrindingData> VALUES = Sets.immutableEnumSet(EnumSet.allOf(GrindingData.class));
		
		/**
		 * The identification for the producible item.
		 */
		private final Item item;
		
		/**
		 * The identification for the final product.
		 */
		private final Item product;
		
		/**
		 * The experience identification for the final product.
		 */
		private final double experience;
		
		/**
		 * Constructs a new {@link GrindingData} enumerator.
		 * @param item       {@link #item}.
		 * @param product    {@link #product}.
		 * @param experience {@link #experience}.
		 */
		private GrindingData(int item, int product, double experience) {
			this.item = new Item(item);
			this.product = new Item(product);
			this.experience = experience;
		}
		
		@Override
		public final String toString() {
			return name().toLowerCase().replaceAll("_", " ");
		}
		
		/**
		 * Gets the definition for this guam tar.
		 * @param identifier the identifier to check for.
		 * @return an optional holding the {@link GuamTar} value found,
		 * {@link Optional#empty} otherwise.
		 */
		public static Optional<GrindingData> getDefinition(int identifier) {
			return VALUES.stream().filter(def -> def.item.getId() == identifier).findAny();
		}
	}
}