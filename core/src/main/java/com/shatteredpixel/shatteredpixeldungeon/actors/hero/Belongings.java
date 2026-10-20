/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Iterator;

public class Belongings implements Iterable<Item> {

	private Hero owner;

	public static class Backpack extends Bag {
		{
			image = ItemSpriteSheet.BACKPACK;
		}
		public int capacity(){
			int cap = super.capacity();
			for (Item item : items){
				if (item instanceof Bag){
					cap++;
				}
			}
			if (Dungeon.hero != null && Dungeon.hero.belongings.secondWep != null){
				//secondary weapons still occupy an inv. slot
				cap--;
			}
			return cap;
		}
	}

	public Backpack backpack;
	
	public Belongings( Hero owner ) {
		this.owner = owner;
		
		backpack = new Backpack();
		backpack.owner = owner;
	}

	public KindOfWeapon weapon = null;
	public Armor armor = null;
	public Artifact artifact = null;
	public KindofMisc misc = null;
	public Ring ring = null;

	//the hero can wear 4 rings: 'ring' is slot 0, the rest are the extra slots below
	public static final int RING_SLOTS = 4;
	public Ring ring2 = null;
	public Ring ring3 = null;
	public Ring ring4 = null;

	//used when thrown weapons temporary become the current weapon
	public KindOfWeapon thrownWeapon = null;

	//used to ensure that the duelist always uses the weapon she's using the ability of
	public KindOfWeapon abilityWeapon = null;

	//used by the champion subclass
	public KindOfWeapon secondWep = null;

	//*** these accessor methods are so that worn items can be affected by various effects/debuffs
	// we still want to access the raw equipped items in cases where effects should be ignored though,
	// such as when equipping something, showing an interface, or dealing with items from a dead hero

	//normally the primary equipped weapon, but can also be a thrown weapon or an ability's weapon
	public KindOfWeapon attackingWeapon(){
		if (thrownWeapon != null) return thrownWeapon;
		if (abilityWeapon != null) return abilityWeapon;
		return weapon();
	}

	//we cache whether belongings are lost to avoid lots of calls to hero.buff(LostInventory.class)
	private boolean lostInvent;
	public void lostInventory( boolean val ){
		lostInvent = val;
	}

	public boolean lostInventory(){
		return lostInvent;
	}

	public KindOfWeapon weapon(){
		if (!lostInventory() || (weapon != null && weapon.keptThroughLostInventory())){
			return weapon;
		} else {
			return null;
		}
	}

	public Armor armor(){
		if (!lostInventory() || (armor != null && armor.keptThroughLostInventory())){
			return armor;
		} else {
			return null;
		}
	}

	public Artifact artifact(){
		if (!lostInventory() || (artifact != null && artifact.keptThroughLostInventory())){
			return artifact;
		} else {
			return null;
		}
	}

	public KindofMisc misc(){
		if (!lostInventory() || (misc != null && misc.keptThroughLostInventory())){
			return misc;
		} else {
			return null;
		}
	}

	public Ring ring(){
		if (!lostInventory() || (ring != null && ring.keptThroughLostInventory())){
			return ring;
		} else {
			return null;
		}
	}

	//raw access to ring slot i (0 to RING_SLOTS-1), ignores lost inventory
	public Ring ringRaw( int i ){
		switch (i){
			case 0:  return ring;
			case 1:  return ring2;
			case 2:  return ring3;
			case 3:  return ring4;
			default: return null;
		}
	}

	public void setRingRaw( int i, Ring r ){
		switch (i){
			case 0:  ring  = r; break;
			case 1:  ring2 = r; break;
			case 2:  ring3 = r; break;
			case 3:  ring4 = r; break;
		}
	}

	//ring slot i, respects the lost inventory debuff. ring(0) is the same as ring()
	public Ring ring( int i ){
		Ring r = ringRaw(i);
		if (!lostInventory() || (r != null && r.keptThroughLostInventory())){
			return r;
		} else {
			return null;
		}
	}

	//index of the first empty ring slot, or -1 if all of them are taken
	public int freeRingSlot(){
		for (int i = 0; i < RING_SLOTS; i++){
			if (ringRaw(i) == null) return i;
		}
		return -1;
	}

	public KindOfWeapon secondWep(){
		if (!lostInventory() || (secondWep != null && secondWep.keptThroughLostInventory())){
			return secondWep;
		} else {
			return null;
		}
	}

	// ***
	
	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String ARTIFACT   = "artifact";
	private static final String MISC       = "misc";
	private static final String RING       = "ring";
	private static final String RING2      = "ring2";
	private static final String RING3      = "ring3";
	private static final String RING4      = "ring4";

	private static final String SECOND_WEP = "second_wep";

	public void storeInBundle( Bundle bundle ) {
		
		backpack.storeInBundle( bundle );
		
		bundle.put( WEAPON, weapon );
		bundle.put( ARMOR, armor );
		bundle.put( ARTIFACT, artifact );
		bundle.put( MISC, misc );
		bundle.put( RING, ring );
		bundle.put( RING2, ring2 );
		bundle.put( RING3, ring3 );
		bundle.put( RING4, ring4 );
		bundle.put( SECOND_WEP, secondWep );
	}

	public static boolean bundleRestoring = false;
	
	public void restoreFromBundle( Bundle bundle ) {
		bundleRestoring = true;
		backpack.clear();
		backpack.restoreFromBundle( bundle );
		
		weapon = (KindOfWeapon) bundle.get(WEAPON);
		if (weapon() != null)       weapon().activate(owner);
		
		armor = (Armor)bundle.get( ARMOR );
		if (armor() != null)        armor().activate( owner );

		artifact = (Artifact) bundle.get(ARTIFACT);
		if (artifact() != null)     artifact().activate(owner);

		misc = (KindofMisc) bundle.get(MISC);
		if (misc() != null)         misc().activate( owner );

		ring = (Ring) bundle.get(RING);
		if (ring() != null)         ring().activate( owner );

		ring2 = (Ring) bundle.get(RING2);
		if (ring(1) != null)        ring(1).activate( owner );

		ring3 = (Ring) bundle.get(RING3);
		if (ring(2) != null)        ring(2).activate( owner );

		ring4 = (Ring) bundle.get(RING4);
		if (ring(3) != null)        ring(3).activate( owner );

		secondWep = (KindOfWeapon) bundle.get(SECOND_WEP);
		if (secondWep() != null)    secondWep().activate(owner);

		bundleRestoring = false;
	}

	public void clear(){
		backpack.clear();
		weapon = secondWep = null;
		armor = null;
		artifact = null;
		misc = null;
		ring = null;
		ring2 = ring3 = ring4 = null;
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		if (bundle.contains( ARMOR )){
			Armor armor = ((Armor)bundle.get( ARMOR ));
			if (armor instanceof ClassArmor){
				info.armorTier = 6;
			} else {
				info.armorTier = armor.tier;
			}
		} else {
			info.armorTier = 0;
		}
	}

	//ignores lost inventory debuff
	public ArrayList<Bag> getBags(){
		ArrayList<Bag> result = new ArrayList<>();

		result.add(backpack);

		for (Item i : this){
			if (i instanceof Bag){
				result.add((Bag)i);
			}
		}

		return result;
	}
	
	@SuppressWarnings("unchecked")
	public<T extends Item> T getItem( Class<T> itemClass ) {

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return (T) item;
				}
			}
		}
		
		return null;
	}

	public<T extends Item> ArrayList<T> getAllItems( Class<T> itemClass ) {
		ArrayList<T> result = new ArrayList<>();

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add((T) item);
				}
			}
		}

		return result;
	}
	
	public boolean contains( Item contains ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (contains == item) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Item getSimilar( Item similar ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (similar != item && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return item;
				}
			}
		}
		
		return null;
	}
	
	public ArrayList<Item> getAllSimilar( Item similar ){
		ArrayList<Item> result = new ArrayList<>();

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (item != similar && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add(item);
				}
			}
		}
		
		return result;
	}

	//triggers when a run ends, so ignores lost inventory effects
	public void identify() {
		for (Item item : this) {
			item.identify(false);
		}
	}
	
	public void observe() {
		if (weapon() != null) {
			if (ShardOfOblivion.passiveIDDisabled() && weapon() instanceof Weapon){
				((Weapon) weapon()).setIDReady();
			} else {
				weapon().identify();
				Badges.validateItemLevelAquired(weapon());
			}
		}
		if (secondWep() != null){
			if (ShardOfOblivion.passiveIDDisabled() && secondWep() instanceof Weapon){
				((Weapon) secondWep()).setIDReady();
			} else {
				secondWep().identify();
				Badges.validateItemLevelAquired(secondWep());
			}
		}
		if (armor() != null) {
			if (ShardOfOblivion.passiveIDDisabled()){
				armor().setIDReady();
			} else {
				armor().identify();
				Badges.validateItemLevelAquired(armor());
			}
		}
		if (artifact() != null) {
			//oblivion shard does not prevent artifact IDing
			artifact().identify();
			Badges.validateItemLevelAquired(artifact());
		}
		if (misc() != null) {
			if (ShardOfOblivion.passiveIDDisabled() && misc() instanceof Ring){
				((Ring) misc()).setIDReady();
			} else {
				misc().identify();
				Badges.validateItemLevelAquired(misc());
			}
		}
		if (ring() != null) {
			if (ShardOfOblivion.passiveIDDisabled()){
				ring().setIDReady();
			} else {
				ring().identify();
				Badges.validateItemLevelAquired(ring());
			}
		}
		for (int i = 1; i < RING_SLOTS; i++) {
			Ring extra = ring(i);
			if (extra != null) {
				if (ShardOfOblivion.passiveIDDisabled()){
					extra.setIDReady();
				} else {
					extra.identify();
					Badges.validateItemLevelAquired(extra);
				}
			}
		}
		if (ShardOfOblivion.passiveIDDisabled()){
			GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready_worn"));
		}
		for (Item item : backpack) {
			if (item instanceof EquipableItem || item instanceof Wand) {
				item.cursedKnown = true;
			}
		}
		Item.updateQuickslot();
	}
	
	public void uncurseEquipped() {
		ScrollOfRemoveCurse.uncurse( owner, armor(), weapon(), artifact(), misc(), ring(), ring(1), ring(2), ring(3), secondWep());
	}
	
	public Item randomUnequipped() {
		if (owner.buff(LostInventory.class) != null) return null;

		return Random.element( backpack.items );
	}
	
	public int charge( float charge ) {
		
		int count = 0;
		
		for (Wand.Charger charger : owner.buffs(Wand.Charger.class)){
			charger.gainCharge(charge);
			count++;
		}
		
		return count;
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}
	
	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		
		private Iterator<Item> backpackIterator = backpack.iterator();
		
		private Item[] equipped = {weapon, armor, artifact, misc, ring, secondWep, ring2, ring3, ring4};
		private int backpackIndex = equipped.length;

		//index in 'equipped' of the item returned by the last next() call, or -1 for backpack items
		private int lastEquipped = -1;
		
		@Override
		public boolean hasNext() {
			
			for (int i=index; i < backpackIndex; i++) {
				if (equipped[i] != null) {
					return true;
				}
			}
			
			return backpackIterator.hasNext();
		}

		@Override
		public Item next() {
			
			while (index < backpackIndex) {
				int i = index++;
				Item item = equipped[i];
				if (item != null) {
					lastEquipped = i;
					return item;
				}
			}

			lastEquipped = -1;
			return backpackIterator.next();
		}

		@Override
		public void remove() {
			switch (lastEquipped) {
			case 0:
				equipped[0] = weapon = null;
				break;
			case 1:
				equipped[1] = armor = null;
				break;
			case 2:
				equipped[2] = artifact = null;
				break;
			case 3:
				equipped[3] = misc = null;
				break;
			case 4:
				equipped[4] = ring = null;
				break;
			case 5:
				equipped[5] = secondWep = null;
				break;
			case 6:
				equipped[6] = ring2 = null;
				break;
			case 7:
				equipped[7] = ring3 = null;
				break;
			case 8:
				equipped[8] = ring4 = null;
				break;
			default:
				backpackIterator.remove();
			}
		}
	}
}
