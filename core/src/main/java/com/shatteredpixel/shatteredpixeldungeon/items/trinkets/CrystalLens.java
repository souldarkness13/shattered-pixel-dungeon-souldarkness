package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class CrystalLens extends Trinket {

	{
		image = ItemSpriteSheet.CRYSTAL_LENS;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String name() {
		return "Хрустальная линза";
	}

	@Override
	public String desc() {
		return "Линза из чистого кристалла. Позволяет извлекать больше опыта из каждой победы.";
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return (isIdentified() ? "Сейчас: " : "На уровне +0: ") + "опыт за убийства +" + bonus(lvl) + "%."
				+ "\n\nПо уровням +0 / +1 / +2 / +3:\nОпыт: +10 / +20 / +30 / +40%";
	}

	private static final int[] BONUS_VALUES = {10, 20, 30, 40};

	public static int bonus( int level ){
		if (level < 0) return 0;
		return BONUS_VALUES[Math.min(level, BONUS_VALUES.length - 1)];
	}

	public static int bonus(){
		return bonus( trinketLevel(CrystalLens.class) );
	}

}
