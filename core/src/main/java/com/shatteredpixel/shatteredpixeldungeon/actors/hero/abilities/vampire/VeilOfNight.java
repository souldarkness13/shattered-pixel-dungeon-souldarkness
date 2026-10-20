package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.vampire;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.audio.Sample;

//The hero wraps herself in darkness: invisible, shielded, and with stronger vampirism
//(Blood Moon talent) while the veil lasts.
public class VeilOfNight extends ArmorAbility {

	{
		baseChargeUse = 35f;
	}

	@Override
	protected void activate( ClassArmor armor, Hero hero, Integer target ) {

		armor.charge -= chargeUse( hero );
		armor.updateQuickslot();

		//Long Night: +2 turns per point
		float duration = 8f + 2f * hero.pointsInTalent( Talent.LONG_NIGHT );

		Buff.affect( hero, Invisibility.class, duration );
		Buff.affect( hero, VeilBuff.class, duration );

		//Shadow Mantle: shield is 30% / 35% / 40% / 45% / 50% of max health
		float shieldPct = 0.30f + 0.05f * hero.pointsInTalent( Talent.SHADOW_MANTLE );
		int shield = Math.min( hero.HT, hero.shielding() + Math.round( hero.HT * shieldPct ) );
		Buff.affect( hero, Barrier.class ).setShield( shield );

		Sample.INSTANCE.play( Assets.Sounds.MELD );
		hero.spend( Actor.TICK );
		hero.busy();
		hero.sprite.operate( hero.pos );
	}

	//while this buff is active, the Blood Moon talent boosts vampirism (see Hero.attackProc)
	public static class VeilBuff extends FlavourBuff {}

	@Override
	public int icon() {
		return HeroIcon.VEIL_OF_NIGHT; //placeholder icon
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{ Talent.LONG_NIGHT, Talent.SHADOW_MANTLE, Talent.BLOOD_MOON, Talent.HEROIC_ENERGY };
	}
}
