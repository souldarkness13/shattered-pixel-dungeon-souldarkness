package com.shatteredpixel.shatteredpixeldungeon.levels.rooms.special;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Workbench;
import com.shatteredpixel.shatteredpixeldungeon.items.ScrollScrap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.watabou.utils.Random;

public class WorkbenchRoom extends SpecialRoom {

	@Override
	public int minWidth() {
		return 5;
	}

	@Override
	public int minHeight() {
		return 5;
	}

	@Override
	public void paint( Level level ) {
		Painter.fill( level, this, Terrain.WALL );
		Painter.fill( level, this, 1, Terrain.EMPTY_SP );

		entrance().set( Door.Type.REGULAR );

		Workbench bench = new Workbench();
		bench.pos = level.pointToCell( center() );
		level.mobs.add( bench );

		int n = Random.IntRange( 2, 4 );
		for (int i = 0; i < n; i++) {
			int pos;
			do {
				pos = level.pointToCell( random( 1 ) );
			} while (level.map[pos] != Terrain.EMPTY_SP
					|| level.heaps.get( pos ) != null
					|| pos == bench.pos);
			level.drop( new ScrollScrap().quantity( Random.IntRange( 1, 3 ) ), pos );
		}
	}
}
