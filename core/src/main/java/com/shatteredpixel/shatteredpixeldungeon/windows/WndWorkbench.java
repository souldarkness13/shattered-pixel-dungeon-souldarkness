package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.ScrollScrap;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class WndWorkbench extends WndOptions {

	//scraps needed for one scroll
	private static final int COST = 4;

	public WndWorkbench() {
		super( new ItemSprite( new ScrollScrap() ),
				"Верстак",
				"Из обрывков можно собрать случайный свиток.\n\nУ вас обрывков: " + count() + "\nНужно: " + COST,
				"Собрать свиток" );
	}

	private static int count() {
		ScrollScrap s = Dungeon.hero.belongings.getItem( ScrollScrap.class );
		return s == null ? 0 : s.quantity();
	}

	@Override
	protected void onSelect( int index ) {
		if (index != 0) return;

		ScrollScrap scraps = Dungeon.hero.belongings.getItem( ScrollScrap.class );
		if (scraps == null || scraps.quantity() < COST) {
			GLog.w( "Не хватает обрывков!" );
			return;
		}

		for (int i = 0; i < COST; i++) {
			scraps.detach( Dungeon.hero.belongings.backpack );
		}
		Item scroll = Generator.random( Generator.Category.SCROLL );

		Sample.INSTANCE.play( Assets.Sounds.READ );
		if (scroll.doPickUp( Dungeon.hero )) {
			GLog.i( "Вы собрали: %s", scroll.name() );
		} else {
			Dungeon.level.drop( scroll, Dungeon.hero.pos ).sprite.drop();
		}
		Dungeon.hero.spend( 1f );
		Dungeon.hero.busy();
	}
}
