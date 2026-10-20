package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class ScrollScrap extends Item {

	{
		image = ItemSpriteSheet.SCROLL_SCRAP;
		stackable = true;
		defaultAction = null;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 5 * quantity;
	}
}
