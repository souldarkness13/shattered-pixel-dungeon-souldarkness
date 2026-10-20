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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Random;


public abstract class KindofMisc extends EquipableItem {

	//Slots: 1 artifact slot, 1 extra artifact slot ('misc'), and 4 ring slots.
	//Rings only go into ring slots, the misc slot only holds a second artifact.

	@Override
	public boolean doEquip(final Hero hero) {

		final boolean isRing = this instanceof Ring;

		boolean equipFull;
		if (isRing) {
			equipFull = hero.belongings.freeRingSlot() == -1;
		} else {
			equipFull = hero.belongings.artifact != null && hero.belongings.misc != null;
		}

		if (equipFull) {

			//the player picks which equipped item gets replaced
			final KindofMisc[] options;
			if (isRing) {
				options = new KindofMisc[Belongings.RING_SLOTS];
				for (int i = 0; i < options.length; i++){
					options[i] = hero.belongings.ringRaw(i);
				}
			} else {
				options = new KindofMisc[]{ hero.belongings.artifact, hero.belongings.misc };
			}

			String[] names = new String[options.length];
			for (int i = 0; i < names.length; i++){
				names[i] = Messages.titleCase(options[i].title());
			}

			GameScene.show(
					new WndOptions(new ItemSprite(this),
							Messages.get(KindofMisc.class, "unequip_title"),
							Messages.get(KindofMisc.class, "unequip_message"),
							names) {

						@Override
						protected void onSelect(int index) {

							KindofMisc equipped = options[index];
							//we directly remove the item because we want to have inventory capacity
							// to unequip the equipped one, but don't want to trigger any other
							// item detaching logic
							int slot = Dungeon.quickslot.getSlot(KindofMisc.this);
							slotOfUnequipped = -1;
							Dungeon.hero.belongings.backpack.items.remove(KindofMisc.this);
							if (equipped.doUnequip(hero, true, false)) {
								Dungeon.hero.belongings.backpack.items.add(KindofMisc.this);
								doEquip(hero);
							} else {
								Dungeon.hero.belongings.backpack.items.add(KindofMisc.this);
							}
							if (slot != -1) {
								Dungeon.quickslot.setSlot(slot, KindofMisc.this);
							} else if (slotOfUnequipped != -1 && defaultAction() != null){
								Dungeon.quickslot.setSlot(slotOfUnequipped, KindofMisc.this);
							}
							updateQuickslot();
						}
					});

			return false;

		} else {

			// 15/25% chance
			if (hero.heroClass != HeroClass.CLERIC && hero.hasTalent(Talent.HOLY_INTUITION)
					&& cursed && !cursedKnown
					&& Random.Int(20) < 1 + 2*hero.pointsInTalent(Talent.HOLY_INTUITION)){
				cursedKnown = true;
				GLog.p(Messages.get(this, "curse_detected"));
				return false;
			}

			if (isRing){
				hero.belongings.setRingRaw( hero.belongings.freeRingSlot(), (Ring) this );
			} else {
				if (hero.belongings.artifact == null)   hero.belongings.artifact = (Artifact) this;
				else                                    hero.belongings.misc = (Artifact) this;
			}

			detach( hero.belongings.backpack );

			Talent.onItemEquipped(hero, this);
			activate( hero );

			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( Messages.get(this, "equip_cursed", this) );
			}

			hero.spendAndNext( timeToEquip(hero) );
			return true;

		}

	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)){

			if (hero.belongings.artifact == this) {
				hero.belongings.artifact = null;
			} else if (hero.belongings.misc == this) {
				hero.belongings.misc = null;
			} else {
				for (int i = 0; i < Belongings.RING_SLOTS; i++){
					if (hero.belongings.ringRaw(i) == this){
						hero.belongings.setRingRaw(i, null);
						break;
					}
				}
			}

			return true;

		} else {

			return false;

		}
	}

	@Override
	public boolean isEquipped( Hero hero ) {
		if (hero == null) return false;
		if (hero.belongings.artifact() == this || hero.belongings.misc() == this) return true;
		for (int i = 0; i < Belongings.RING_SLOTS; i++){
			if (hero.belongings.ring(i) == this) return true;
		}
		return false;
	}

}
