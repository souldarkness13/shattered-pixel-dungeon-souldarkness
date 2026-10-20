package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class WoundedFury extends Trinket {

	{
		image = ItemSpriteSheet.WOUNDED_FURY;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String name() {
		return "Ярость раненого";
	}

	@Override
	public String desc() {
		return "Сердце, которое бьётся тем яростнее, чем ближе конец. Раненый герой бьёт сильнее.";
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return (isIdentified() ? "Сейчас: " : "На уровне +0: ") + "при здоровье ниже 50% урон +" + bonus(lvl) + "%."
				+ "\n\nПо уровням +0 / +1 / +2 / +3:\nБонус урона: +10 / +15 / +20 / +25%";
	}

	private static final int[] BONUS_VALUES = {10, 15, 20, 25};

	public static int bonus( int level ){
		if (level < 0) return 0;
		return BONUS_VALUES[Math.min(level, BONUS_VALUES.length - 1)];
	}

	public static int bonus(){
		return bonus( trinketLevel(WoundedFury.class) );
	}

}
