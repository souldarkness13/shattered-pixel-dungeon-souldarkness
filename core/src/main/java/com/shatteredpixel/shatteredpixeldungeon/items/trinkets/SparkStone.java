package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class SparkStone extends Trinket {

	{
		image = ItemSpriteSheet.SPARK_STONE;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String name() {
		return "Искровой камень";
	}

	@Override
	public String desc() {
		return "Тёмный камень, внутри которого бегают искры. Удар героя может выбить искру, и она ранит врагов рядом с целью.";
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return (isIdentified() ? "Сейчас: " : "На уровне +0: ") + "шанс искры " + chance(lvl) + "%, соседние враги получают " + splash(lvl) + "% урона."
				+ "\n\nПо уровням +0 / +1 / +2 / +3:\nШанс: 10 / 15 / 20 / 25%\nУрон по соседям: 40 / 50 / 60 / 70%";
	}

	private static final int[] CHANCE_VALUES = {10, 15, 20, 25};

	public static int chance( int level ){
		if (level < 0) return 0;
		return CHANCE_VALUES[Math.min(level, CHANCE_VALUES.length - 1)];
	}

	public static int chance(){
		return chance( trinketLevel(SparkStone.class) );
	}

	private static final int[] SPLASH_VALUES = {40, 50, 60, 70};

	public static int splash( int level ){
		if (level < 0) return 0;
		return SPLASH_VALUES[Math.min(level, SPLASH_VALUES.length - 1)];
	}

	public static int splash(){
		return splash( trinketLevel(SparkStone.class) );
	}

}
