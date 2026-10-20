package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class MirrorShard extends Trinket {

	{
		image = ItemSpriteSheet.MIRROR_SHARD;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String name() {
		return "Осколок зеркала";
	}

	@Override
	public String desc() {
		return "Осколок волшебного зеркала. Когда враг бьёт героя в ближнем бою, часть удара возвращается к нему.";
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return (isIdentified() ? "Сейчас: " : "На уровне +0: ") + "шанс " + chance(lvl) + "% вернуть врагу половину урона от его удара в ближнем бою."
				+ "\n\nПо уровням +0 / +1 / +2 / +3:\nШанс: 20 / 25 / 30 / 35%";
	}

	private static final int[] CHANCE_VALUES = {20, 25, 30, 35};

	public static int chance( int level ){
		if (level < 0) return 0;
		return CHANCE_VALUES[Math.min(level, CHANCE_VALUES.length - 1)];
	}

	public static int chance(){
		return chance( trinketLevel(MirrorShard.class) );
	}

}
