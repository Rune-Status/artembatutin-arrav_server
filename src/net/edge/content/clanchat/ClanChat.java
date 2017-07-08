package net.edge.content.clanchat;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.edge.world.World;
import net.edge.world.node.entity.player.Player;

import java.util.Comparator;
import java.util.Optional;

/**
 * Represents a single clan chat in the world.
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 * @author Artem Batutin <artembatutin@gmail.com>
 */
public final class ClanChat {
	
	/**
	 * The name of the owner of this clan.
	 */
	private final String owner;
	
	/**
	 * The clan chat members in this clan chat.
	 */
	private final ObjectArrayList<ClanMember> members = new ObjectArrayList<>(100);
	
	/**
	 * A collection of clan chat ranks.
	 */
	private final Object2ObjectArrayMap<String, ClanChatRank> ranks = new Object2ObjectArrayMap<>();
	
	/**
	 * A list of banned members from this clan chat.
	 */
	private final ObjectList<String> banned = new ObjectArrayList<>();
	
	/**
	 * A list of muted members from this clan chat.
	 */
	private final ObjectList<String> muted = new ObjectArrayList<>();
	
	/**
	 * The name of the clan chat.
	 */
	private String name;
	
	/**
	 * The settings of this clan chat.
	 */
	private ClanChatSettings settings = new ClanChatSettings();
	
	/**
	 * Constructs a new {@link ClanChat}.
	 */
	public ClanChat(String name, String owner) {
		this.name = name;
		this.owner = owner;
	}
	
	/**
	 * Adds a player to the member's list.
	 * @param player the player to add.
	 */
	public boolean add(Player player, ClanChatRank rank) {
		if(true)
			return false;
		ClanMember member = new ClanMember(player, this, rank);
		if(members.add(member)) {
			player.setClan(Optional.of(member));
			
			if(muted.contains(player.getCredentials().getUsername())) {
				member.setMute(true);
			}
			if(rank.getValue() >= getLowest().getValue()) {
				World.getClanManager().update(ClanChatUpdate.BAN_MODIFICATION, member);
			}
			if(rank != ClanChatRank.MEMBER) {
				if(!ranks.containsKey(player.getCredentials().getUsername())) {
					ranks.put(player.getCredentials().getUsername(), rank);
				}
			}
			World.getClanManager().update(ClanChatUpdate.JOINING, member);
			World.getClanManager().update(ClanChatUpdate.MEMBER_LIST_MODIFICATION, this);
			return true;
		}
		return false;
	}
	
	/**
	 * Removes a player to the member's list.
	 * @param player the player to add.
	 */
	public void remove(Player player, boolean logout) {
		if(!player.getClan().isPresent()) {
			return;
		}
		members.remove(player.getClan().get());
		
		if(!logout) {
			player.setClan(Optional.empty());
		}
		
		World.getClanManager().update(ClanChatUpdate.MEMBER_LIST_MODIFICATION, this);
		World.getClanManager().clearOnLogin(player);
	}
	
	/**
	 * Gets the rank if any were saved.
	 * @param username the player's username to check
	 * @return the saved {@link ClanChatRank}, if none {@link ClanChatRank#MEMBER}.
	 */
	public ClanChatRank getRank(String username) {
		if(ranks.containsKey(username)) {
			return ranks.get(username);
		}
		return ClanChatRank.MEMBER;
	}
	
	/**
	 * @return {@link #owner}.
	 */
	public String getOwner() {
		return owner;
	}
	
	/**
	 * @return {@link #name}.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return {@link #settings}.
	 */
	public ClanChatSettings getSettings() {
		return settings;
	}
	
	/**
	 * @return the members
	 */
	public ObjectList<ClanMember> getMembers() {
		return members;
	}
	
	/**
	 * @return the banned
	 */
	public ObjectList<String> getBanned() {
		return banned;
	}
	
	/**
	 * @return the banned
	 */
	public ObjectList<String> getMuted() {
		return muted;
	}
	
	/**
	 * @return the ranks
	 */
	public Object2ObjectArrayMap<String, ClanChatRank> getRanked() {
		return ranks;
	}
	
	/**
	 * Gets the lowest authority rank in the clan.
	 * @return the lowest authority.
	 */
	public ClanChatRank getLowest() {
		return settings.getBan().getValue() >= settings.getMute().getValue() ? settings.getMute() : settings.getBan();
	}
	
}
