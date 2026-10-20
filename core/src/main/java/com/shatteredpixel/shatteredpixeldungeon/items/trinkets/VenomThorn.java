package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class VenomThorn extends Trinket {

	{
		image = ItemSpriteSheet.VENOM_THORN;
	}

	@Override
	protected int upgradeEnergyCost() {
		//6 -> 6(12) -> 8(20) -> 10(30)
		return 6+2*level();
	}

	@Override
	public String name() {
		return "Ядовитый шип";
	}

	@Override
	public String desc() {
		return "Острый шип неизвестного растения, пропитанный ядом. Удары героя могут отравить врага.";
	}

	@Override
	public String statsDesc() {
		int lvl = isIdentified() ? buffedLvl() : 0;
		return (isIdentified() ? "Сейчас: " : "На уровне +0: ") + "шанс отравить врага при ударе " + chance(lvl) + "%, сила яда " + turns(lvl) + "."
				+ "\n\nПо уровням +0 / +1 / +2 / +3:\nШанс: 15 / 20 / 25 / 30%\nСила яда: 4 / 6 / 8 / 10";
	}

	private static final int[] CHANCE_VALUES = {15, 20, 25, 30};

	public static int chance( int level ){
		if (level < 0) return 0;
		return CHANCE_VALUES[Math.min(level, CHANCE_VALUES.length - 1)];
	}

	public static int chance(){
		return chance( trinketLevel(VenomThorn.class) );
	}

	private static final int[] TURNS_VALUES = {4, 6, 8, 10};

	public static int turns( int level ){
		if (level < 0) return 0;
		return TURNS_VALUES[Math.min(level, TURNS_VALUES.length - 1)];
	}

	public static int turns(){
		return turns( trinketLevel(VenomThorn.class) );
	}

}
