package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.vampire;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

//Burst of blood magic: damages and makes bleed every visible enemy nearby,
//and heals the hero for half of the damage dealt.
public class BloodNova extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {

		armor.charge -= chargeUse( hero );
		armor.updateQuickslot();

		hero.busy();

		//Crimson Reach: radius 3 / 4 / 5 / 6 instead of 2
		int radius = 2 + hero.pointsInTalent( Talent.CRIMSON_REACH );
		int scaling = hero.STR() - 10;
		//Lingering Wounds: stronger bleeding
		float bleed = 3f + 2f * hero.pointsInTalent( Talent.LINGERING_WOUNDS );

		int totalDamage = 0;
		int kills = 0;

		for (Mob mob : Dungeon.level.mobs.toArray( new Mob[0] )) {
			if (mob.alignment != Char.Alignment.ENEMY) continue;
			if (!Dungeon.level.heroFOV[mob.pos]) continue;
			if (Dungeon.level.distance( hero.pos, mob.pos ) > radius) continue;

			int damage = Hero.heroDamageIntRange( 4 + scaling, 9 + 2 * scaling );
			damage = Math.max( 0, damage - mob.drRoll() );
			totalDamage += damage;

			mob.damage( damage, hero );

			if (mob.isAlive()) {
				Buff.affect( mob, Bleeding.class ).set( bleed );
			} else {
				kills++;
			}
		}

		//heal for half of the damage dealt
		int heal = Math.round( totalDamage * 0.5f );
		if (heal > 0 && hero.HP < hero.HT) {
			hero.HP = Math.min( hero.HT, hero.HP + heal );
			hero.sprite.emitter().burst( Speck.factory( Speck.HEALING ), 3 );
		}

		//Crimson Shell: each enemy killed by the nova grants shield
		int shellPts = hero.pointsInTalent( Talent.CRIMSON_SHELL );
		if (kills > 0 && shellPts > 0) {
			int shield = Math.min( hero.HT, hero.shielding() + kills * 2 * shellPts );
			Buff.affect( hero, Barrier.class ).setShield( shield );
		}

		WandOfBlastWave.BlastWave.blast( hero.pos );
		Sample.INSTANCE.play( Assets.Sounds.BLAST, 1f, 0.6f );
		PixelScene.shake( 2, 0.5f );

		Invisibility.dispel();
		hero.spendAndNext( Actor.TICK );
	}

	@Override
	public int icon() {
		return HeroIcon.BLOOD_NOVA; //placeholder icon
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.CRIMSON_REACH, Talent.CRIMSON_SHELL, Talent.LINGERING_WOUNDS, Talent.HEROIC_ENERGY };
	}
}
