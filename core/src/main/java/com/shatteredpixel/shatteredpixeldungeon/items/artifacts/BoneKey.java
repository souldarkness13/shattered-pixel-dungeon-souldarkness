package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Regeneration;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.Key;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfEnergy;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

//Skeleton-style key: opens any locked door or chest next to the hero, spends a charge,
//and recharges slowly over time like a wand.
public class BoneKey extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_BONEKEY; //placeholder icon

		levelCap = 10;
		chargeCap = 2;
		charge = 2;
		partialCharge = 0;

		defaultAction = AC_UNLOCK;

		unique = true;
		bones = false;
	}

	public static final String AC_UNLOCK = "UNLOCK";

	//turns needed to regain one charge (before ring of energy bonuses)
	private static final float TURNS_PER_CHARGE = 150f;

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero ) && !cursed && hero.buff(MagicImmune.class) == null) {
			actions.add(AC_UNLOCK);
		}
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute(hero, action);

		if (hero.buff(MagicImmune.class) != null) return;

		if (action.equals( AC_UNLOCK )) {
			if (!isEquipped(hero))  GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if (cursed)        GLog.i( Messages.get(this, "cursed") );
			else if (charge <= 0)   GLog.w( Messages.get(this, "no_charge") );
			else                    GameScene.selectCell( selector );
		}
	}

	private void spendCharge( Hero hero, int cell ) {
		charge--;
		updateQuickslot();
		hero.spend( Key.TIME_TO_UNLOCK );
		hero.busy();
		hero.sprite.operate( cell );
		Sample.INSTANCE.play( Assets.Sounds.UNLOCK );
		Talent.onArtifactUsed( hero );
	}

	private final CellSelector.Listener selector = new CellSelector.Listener() {

		@Override
		public void onSelect( Integer cell ) {
			if (cell == null) return;

			Hero hero = Dungeon.hero;

			if (!Dungeon.level.adjacent( hero.pos, cell )) {
				GLog.w( Messages.get(BoneKey.this, "not_adjacent") );
				return;
			}

			Heap heap = Dungeon.level.heaps.get( cell );
			int terrain = Dungeon.level.map[cell];

			if (heap != null
					&& (heap.type == Heap.Type.LOCKED_CHEST || heap.type == Heap.Type.CRYSTAL_CHEST)) {

				spendCharge( hero, cell );
				heap.open( hero );

			} else if (terrain == Terrain.LOCKED_DOOR) {

				spendCharge( hero, cell );
				Level.set( cell, Terrain.DOOR );
				GameScene.updateMap( cell );
				GameScene.updateKeyDisplay();

			} else if (terrain == Terrain.CRYSTAL_DOOR) {

				spendCharge( hero, cell );
				Level.set( cell, Terrain.EMPTY );
				GameScene.updateMap( cell );
				Sample.INSTANCE.play( Assets.Sounds.TELEPORT );
				CellEmitter.get( cell ).start( Speck.factory( Speck.DISCOVER ), 0.025f, 20 );
				GameScene.updateKeyDisplay();

			} else {
				GLog.w( Messages.get(BoneKey.this, "nothing") );
			}
		}

		@Override
		public String prompt() {
			return Messages.get(BoneKey.class, "prompt");
		}
	};

	//used by the Hungry Dark talent to refund charges instantly
	public void directCharge( int amount ) {
		charge = Math.min( charge + amount, chargeCap );
		updateQuickslot();
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		//the real cap depends on the vampire stage and is set by the recharge buff,
		//so use the highest possible cap here to avoid clipping saved charges
		chargeCap = 8;
		super.restoreFromBundle( bundle );
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new boneRecharge();
	}

	@Override
	public int value() {
		return 0;
	}

	public class boneRecharge extends ArtifactBuff {

		@Override
		public boolean act() {
			//evolution: stage 1 = 2 charges / 150 turns, stage 2 = 3 / 115, stage 3 = 4 / 80
			int stage = 1;
			if (target instanceof Hero) {
				stage = Math.max(1, ((Hero) target).vampireStage());
			}
			chargeCap = stage + 1;
			if (target instanceof Hero) {
				//Skeleton's Reserve talent: +1 max charge per point
				chargeCap += ((Hero) target).pointsInTalent(Talent.SKELETONS_RESERVE);
			}
			float baseTurns = TURNS_PER_CHARGE - 35f * (stage - 1);
			if (target instanceof Hero) {
				//Bone Collector talent: +25% / +50% recharge speed
				baseTurns /= 1f + 0.25f * ((Hero) target).pointsInTalent(Talent.BONE_COLLECTOR);
			}

			if (charge < chargeCap && !cursed && target.buff(MagicImmune.class) == null) {
				if (Regeneration.regenOn()) {
					float turnsToCharge = baseTurns / RingOfEnergy.artifactChargeMultiplier(target);
					partialCharge += 1f / turnsToCharge;
				}

				while (partialCharge >= 1) {
					charge++;
					partialCharge -= 1;
					if (charge >= chargeCap) {
						charge = chargeCap;
						partialCharge = 0;
					}
				}
			} else {
				partialCharge = 0;
			}

			updateQuickslot();

			spend( TICK );

			return true;
		}
	}
}
