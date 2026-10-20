package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class WaspSting extends Trinket {

	{
		image = ItemSpriteSheet.WASP_STING;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String name() {
		return "Осиное жало";
	}

	@Override
	public String desc() {
		return "Жало гигантской осы. Каждый несколько ударов подряд герой бьёт заметно сильнее.";
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return (isIdentified() ? "Сейчас: " : "На уровне +0: ") + "каждый " + every(lvl) + "-й удар наносит на " + bonus(lvl) + "% больше урона."
				+ "\n\nПо уровням +0 / +1 / +2 / +3:\nКаждый удар №: 5 / 4 / 4 / 3\nБонус урона: 50 / 60 / 70 / 80%";
	}

	private static final int[] EVERY_VALUES = {5, 4, 4, 3};

	public static int every( int level ){
		if (level < 0) return 0;
		return EVERY_VALUES[Math.min(level, EVERY_VALUES.length - 1)];
	}

	public static int every(){
		return every( trinketLevel(WaspSting.class) );
	}

	private static final int[] BONUS_VALUES = {50, 60, 70, 80};

	public static int bonus( int level ){
		if (level < 0) return 0;
		return BONUS_VALUES[Math.min(level, BONUS_VALUES.length - 1)];
	}

	public static int bonus(){
		return bonus( trinketLevel(WaspSting.class) );
	}

	private static int hitCounter = 0;

	//true, если этот удар получает бонус (счётчик не сохраняется между запусками игры)
	public static boolean registerHit( int every ){
		hitCounter++;
		if (hitCounter >= every){
			hitCounter = 0;
			return true;
		}
		return false;
	}

}
