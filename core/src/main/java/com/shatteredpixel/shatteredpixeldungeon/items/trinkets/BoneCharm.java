package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class BoneCharm extends Trinket {

	{
		image = ItemSpriteSheet.BONE_CHARM;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String name() {
		return "Костяной оберег";
	}

	@Override
	public String desc() {
		return "Оберег из кости неведомого зверя. Каждый удар по герою слабее, но урон не опускается ниже 1.";
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return (isIdentified() ? "Сейчас: " : "На уровне +0: ") + "каждый удар по вам слабее на " + reduction(lvl) + "."
				+ "\n\nПо уровням +0 / +1 / +2 / +3:\nСнижение урона: 1 / 1 / 2 / 2";
	}

	private static final int[] REDUCTION_VALUES = {1, 1, 2, 2};

	public static int reduction( int level ){
		if (level < 0) return 0;
		return REDUCTION_VALUES[Math.min(level, REDUCTION_VALUES.length - 1)];
	}

	public static int reduction(){
		return reduction( trinketLevel(BoneCharm.class) );
	}

}
