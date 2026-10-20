package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.vampire;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

//The hero turns into a swarm of bats and flies to a target spot (like Heroic Leap),
//damaging and draining every enemy next to the landing spot.
public class BatSwarm extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	@Override
	public String targetingPrompt() {
		return Messages.get( this, "prompt" );
	}

	@Override
	public float chargeUse( Hero hero ) {
		float chargeUse = super.chargeUse( hero );
		if (hero.buff( SwarmTracker.class ) != null) {
			//Relentless Flight: a second flight is 16% / 30% / 41% / 50% cheaper
			chargeUse *= Math.pow( 0.84, hero.pointsInTalent( Talent.RELENTLESS_FLIGHT ) );
		}
		return chargeUse;
	}

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {
		if (target == null) return;

		if (hero.rooted) {
			PixelScene.shake( 1, 1f );
			return;
		}

		Ballistica route = new Ballistica( hero.pos, target, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID );
		int cell = route.collisionPos;

		//can't occupy the same cell as another char, so move back one.
		int backTrace = route.dist - 1;
		while (Actor.findChar( cell ) != null && cell != hero.pos) {
			cell = route.path.get( backTrace );
			backTrace--;
		}

		armor.charge -= chargeUse( hero );
		armor.updateQuickslot();

		final int dest = cell;
		hero.busy();
		hero.sprite.jump( hero.pos, cell, new Callback() {
			@Override
			public void call() {
				hero.move( dest );
				Dungeon.level.occupyCell( hero );
				Dungeon.observe();
				GameScene.updateFog();

				int scaling = hero.STR() - 10;
				int leechPts = hero.pointsInTalent( Talent.LEECHING_SWARM );
				int screechPts = hero.pointsInTalent( Talent.SCREECHING_FLAP );

				for (int i : PathFinder.NEIGHBOURS8) {
					Char mob = Actor.findChar( hero.pos + i );
					if (mob == null || mob == hero || mob.alignment != Char.Alignment.ENEMY) continue;

					int damage = Hero.heroDamageIntRange( 3 + scaling, 8 + 2 * scaling );
					damage = Math.max( 0, damage - mob.drRoll() );
					mob.damage( damage, hero );

					//Leeching Swarm: heal for 30% / 60% / 90% / 120% of the damage dealt
					if (leechPts > 0 && damage > 0 && hero.HP < hero.HT) {
						int heal = Math.max( 1, Math.round( damage * 0.3f * leechPts ) );
						hero.HP = Math.min( hero.HT, hero.HP + heal );
						hero.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 1 );
					}

					//Screeching Flap: 20% / 40% / 60% / 80% chance to stun, otherwise cripple
					if (screechPts > 0 && mob.isAlive()) {
						if (Random.Int( 100 ) < 20 * screechPts) {
							Buff.affect( mob, Paralysis.class, 2f );
						} else {
							Buff.affect( mob, Cripple.class, 3f );
						}
					}
				}

				WandOfBlastWave.BlastWave.blast( dest );
				PixelScene.shake( 2, 0.5f );

				Invisibility.dispel();
				hero.spendAndNext( Actor.TICK );

				if (hero.buff( SwarmTracker.class ) != null) {
					hero.buff( SwarmTracker.class ).detach();
				} else {
					if (hero.hasTalent( Talent.RELENTLESS_FLIGHT )) {
						Buff.affect( hero, SwarmTracker.class, 3 );
					}
				}
			}
		} );
	}

	public static class SwarmTracker extends FlavourBuff {}

	@Override
	public int icon() {
		return HeroIcon.BAT_SWARM; //placeholder icon
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.LEECHING_SWARM, Talent.SCREECHING_FLAP, Talent.RELENTLESS_FLIGHT, Talent.HEROIC_ENERGY };
	}
}
