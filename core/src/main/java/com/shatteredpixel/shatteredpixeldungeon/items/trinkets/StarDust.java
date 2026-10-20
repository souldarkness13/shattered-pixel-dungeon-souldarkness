package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class StarDust extends Trinket {

	{
		image = ItemSpriteSheet.STAR_DUST;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String name() {
		return "Звёздная пыль";
	}

	@Override
	public String desc() {
		return "Звёздная пыль в крошечной склянке. При повышении уровня герой восстанавливает часть здоровья.";
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return (isIdentified() ? "Сейчас: " : "На уровне +0: ") + "новый уровень лечит на " + heal(lvl) + "% максимального здоровья."
				+ "\n\nПо уровням +0 / +1 / +2 / +3:\nЛечение: 10 / 15 / 20 / 25% макс. HP";
	}

	private static final int[] HEAL_VALUES = {10, 15, 20, 25};

	public static int heal( int level ){
		if (level < 0) return 0;
		return HEAL_VALUES[Math.min(level, HEAL_VALUES.length - 1)];
	}

	public static int heal(){
		return heal( trinketLevel(StarDust.class) );
	}

}
