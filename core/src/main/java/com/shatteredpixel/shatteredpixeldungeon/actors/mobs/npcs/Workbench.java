package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.WorkbenchSprite;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndWorkbench;
import com.watabou.noosa.Game;
import com.watabou.utils.Callback;

public class Workbench extends NPC {

	{
		spriteClass = WorkbenchSprite.class;
		properties.add( Property.IMMOVABLE );
		properties.add( Property.INORGANIC );
	}

	@Override
	public String name() {
		return "Верстак";
	}

	@Override
	public String description() {
		return "Крепкий стол, исписанный рунами. На нём можно собрать свиток из обрывков.";
	}

	@Override
	public boolean interact( Char c ) {
		sprite.turnTo( pos, c.pos );
		if (c == Dungeon.hero) {
			Game.runOnRenderThread( new Callback() {
				@Override
				public void call() {
					GameScene.show( new WndWorkbench() );
				}
			} );
		}
		return true;
	}

	@Override
	public void damage( int dmg, Object src ) {
	}

	@Override
	public boolean add( Buff buff ) {
		return false;
	}

	@Override
	public boolean reset() {
		return true;
	}
}
