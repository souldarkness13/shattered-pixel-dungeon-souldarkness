package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.watabou.noosa.TextureFilm;

public class WorkbenchSprite extends MobSprite {

	public WorkbenchSprite() {
		super();

		texture( "sprites/workbench.png" );
		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		idle = new Animation( 1, true );
		idle.frames( frames, 0 );

		run = idle.clone();
		attack = idle.clone();
		die = idle.clone();

		play( idle );
	}
}
