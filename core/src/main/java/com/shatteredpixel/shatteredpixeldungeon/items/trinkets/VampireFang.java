package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class VampireFang extends Trinket {

	{
		image = ItemSpriteSheet.VAMPIRE_FANG;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String name() {
		return "Клык вампира";
	}

	@Override
	public String desc() {
		return "Клык, который до сих пор тёплый на ощупь. Убитый враг отдаёт герою часть жизни.";
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return (isIdentified() ? "Сейчас: " : "На уровне +0: ") + "убийство лечит на " + heal(lvl) + "% максимального здоровья."
				+ "\n\nПо уровням +0 / +1 / +2 / +3:\nЛечение: 4 / 6 / 8 / 10% макс. HP";
	}

	private static final int[] HEAL_VALUES = {4, 6, 8, 10};

	public static int heal( int level ){
		if (level < 0) return 0;
		return HEAL_VALUES[Math.min(level, HEAL_VALUES.length - 1)];
	}

	public static int heal(){
		return heal( trinketLevel(VampireFang.class) );
	}

}
