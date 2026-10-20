package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class ExecutionerMark extends Trinket {

	{
		image = ItemSpriteSheet.EXEC_MARK;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String name() {
		return "Знак палача";
	}

	@Override
	public String desc() {
		return "Медальон палача. Если у раненого врага остаётся совсем мало здоровья, удар добивает его сразу. На боссов не действует.";
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return (isIdentified() ? "Сейчас: " : "На уровне +0: ") + "добивает врага, если после удара у него останется меньше " + threshold(lvl) + "% здоровья (кроме боссов)."
				+ "\n\nПо уровням +0 / +1 / +2 / +3:\nПорог: 8 / 10 / 12 / 15% здоровья врага";
	}

	private static final int[] THRESHOLD_VALUES = {8, 10, 12, 15};

	public static int threshold( int level ){
		if (level < 0) return 0;
		return THRESHOLD_VALUES[Math.min(level, THRESHOLD_VALUES.length - 1)];
	}

	public static int threshold(){
		return threshold( trinketLevel(ExecutionerMark.class) );
	}

}
