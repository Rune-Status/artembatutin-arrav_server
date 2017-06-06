package net.edge.event.npc;

import net.edge.content.market.MarketCounter;
import net.edge.content.skill.slayer.Slayer;
import net.edge.event.EventInitializer;
import net.edge.event.impl.NpcEvent;
import net.edge.world.node.entity.npc.Npc;
import net.edge.world.node.entity.player.Player;

public class Shops extends EventInitializer {
	public enum Shop {
		GENERAL_KEEPER(551, 12, true, true),
		GENERAL_ASSISTANT(552, 12, true, true),
		FLETCHING_HUNTMAN(1281, 20, 2),
		HERBLORE_KAQEMEEX(455, 10, 2),
		SUMMONING_JATIX(587, 9, true, true),
		SUPPLY_SHOP_ANNA(6180, 8, 1),
		WOODCUTTING_AXES(4906, 21, 1),
		FISHING_SUPPLIES(4901, 18, 1),
		MINING_PICKAXES(3295, 11, 1),
		CRAFTING(805, 7, 1),
		COOKING_CHEF(847, 6, 1),
		ORNAMENTAL_ARMORY(4657, 23, true, true),
		THIEVING(2270, 26, 1),
		PET_SHOP(6892, 25, 2),
		NIGHTS_WATCH_CAPTAIN(3705, 24, 1),
		WILDERNESS_BANKER_CAPES(7605, 25, 1),
		HAZELMERE_BLOOD_MONEY(669, 0, 2),
		PARTY_PETE_EDGE_TOKENS(659, 1, 2),
		CULINAROMANCER_GLOVES(3400, 3, 2),
		HUNTER_SHOP(5113, 4, 1),
		SMITHING_THURGO(604, 19, 1),
		SLAYER_SHOP(8462, 2, 3),
		
		;
		
		private final int owner, shop;
		private final boolean first, second, third;
		
		Shop(int owner, int shop, int action) {
			this(owner, shop, action == 1, action == 2, action == 3);
		}
		
		Shop(int owner, int shop, boolean first, boolean second) {
			this(owner, shop, first, second, false);
		}
		
		Shop(int owner, int shop, boolean first, boolean second, boolean third) {
			this.owner = owner;
			this.shop = shop;
			this.first = first;
			this.second = second;
			this.third = third;
		}
	}
	
	@Override
	public void init() {
		for(Shop s : Shop.values()) {
			NpcEvent e = new NpcEvent() {
				@Override
				public boolean click(Player player, Npc npc, int click) {
					MarketCounter.getShops().get(s.shop).openShop(player);
					return true;
				}
			};
			if(s.first)
				e.registerFirst(s.owner);
			if(s.second)
				e.registerSecond(s.owner);
			if(s.third)
				e.registerThird(s.owner);
		}
	}
}
