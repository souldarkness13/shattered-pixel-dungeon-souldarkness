package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class LilithChalice extends Trinket {

	{
		image = ItemSpriteSheet.LILITH_CHALICE;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String name() {
		return "Чаша Лилит";
	}

	@Override
	public String desc() {
		return "Чаша, из которой пила сама Лилит. Каждый удар героя исцеляет его. Складывается с вампиризмом от зелья.";
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return (isIdentified() ? "Сейчас: " : "На уровне +0: ") + "каждый удар лечит на " + heal(lvl) + " HP."
				+ "\n\nПо уровням +0 / +1 / +2 / +3:\nЛечение за удар: 1 / 1 / 2 / 3 HP";
	}

	private static final int[] HEAL_VALUES = {1, 1, 2, 3};

	public static int heal( int level ){
		if (level < 0) return 0;
		return HEAL_VALUES[Math.min(level, HEAL_VALUES.length - 1)];
	}

	public static int heal(){
		return heal( trinketLevel(LilithChalice.class) );
	}

}
