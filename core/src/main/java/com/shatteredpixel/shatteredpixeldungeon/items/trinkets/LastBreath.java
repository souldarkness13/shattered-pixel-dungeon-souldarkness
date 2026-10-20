package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class LastBreath extends Trinket {

	{
		image = ItemSpriteSheet.LAST_BREATH;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String name() {
		return "Последний вздох";
	}

	@Override
	public String desc() {
		return "Перо, которое никогда не падает на землю. Когда здоровье героя на исходе, оно один раз за этаж окутывает его щитом.";
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return (isIdentified() ? "Сейчас: " : "На уровне +0: ") + "раз за этаж, когда здоровье ниже 25%, даёт щит на " + shield(lvl) + "."
				+ "\n\nПо уровням +0 / +1 / +2 / +3:\nЩит: 8 / 12 / 16 / 20";
	}

	private static final int[] SHIELD_VALUES = {8, 12, 16, 20};

	public static int shield( int level ){
		if (level < 0) return 0;
		return SHIELD_VALUES[Math.min(level, SHIELD_VALUES.length - 1)];
	}

	public static int shield(){
		return shield( trinketLevel(LastBreath.class) );
	}

	//помнит, на каком этаже щит уже был использован
	public static class Used extends Buff {

		{
			type = buffType.NEUTRAL;
			revivePersists = true;
		}

		public int depth = -1;

		@Override
		public int icon() {
			return BuffIndicator.NONE;
		}

		private static final String DEPTH = "depth";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( DEPTH, depth );
		}

		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			depth = bundle.getInt( DEPTH );
		}
	}

	public static void onDamaged( Hero hero ){
		int shield = shield();
		if (shield <= 0 || !hero.isAlive() || hero.HP * 4 >= hero.HT) return;

		Used used = hero.buff(Used.class);
		if (used != null && used.depth == Dungeon.depth) return;
		if (used == null) used = Buff.affect(hero, Used.class);
		used.depth = Dungeon.depth;

		Buff.affect(hero, Barrier.class).setShield(shield);
		if (hero.sprite != null) {
			hero.sprite.showStatus(CharSprite.POSITIVE, "Последний вздох");
		}
		GLog.p("Перо окутывает вас защитным щитом.");
	}

}
