package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class WindBoots extends Trinket {

	{
		image = ItemSpriteSheet.WIND_BOOTS;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String name() {
		return "Сапоги ветра";
	}

	@Override
	public String desc() {
		return "Лёгкие сапоги с крошечными крылышками. Герой двигается быстрее.";
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return (isIdentified() ? "Сейчас: " : "На уровне +0: ") + "скорость передвижения +" + bonus(lvl) + "%."
				+ "\n\nПо уровням +0 / +1 / +2 / +3:\nСкорость: +10 / +15 / +20 / +25%";
	}

	private static final int[] BONUS_VALUES = {10, 15, 20, 25};

	public static int bonus( int level ){
		if (level < 0) return 0;
		return BONUS_VALUES[Math.min(level, BONUS_VALUES.length - 1)];
	}

	public static int bonus(){
		return bonus( trinketLevel(WindBoots.class) );
	}

	public static float speedMultiplier(){
		return 1f + bonus() / 100f;
	}

}
