/*
 * Shattered Pixel Dungeon - shopkeeper quests (mod)
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ShopQuest {

	private static final int SLAYER   = 1; // убить N врагов
	private static final int GOLD     = 2; // собрать N золота
	private static final int WEAPON   = 3; // принести оружие тира >= N
	private static final int DELIVERY = 4; // доставить посылку торговцу на следующем этаже-магазине
	private static final int BOSS     = 5; // убить чемпиона-мини-босса на этом этаже

	private static final int[] SHOP_DEPTHS = {6, 11, 16, 20};

	private static int type;    // 0 = активного квеста нет
	private static int depth;   // этаж, где квест выдан
	private static int target;  // N / тир / этаж получателя
	private static int start;   // значение статистики в момент принятия
	private static int bossId;  // id моба-цели

	//награда: два кольца на выбор, как у Wandmaker. Генерируются при принятии квеста.
	private static Ring ring1;
	private static Ring ring2;

	//этажи, где квест уже выполнен
	private static ArrayList<Integer> done = new ArrayList<>();

	// ---------- состояние / сохранение ----------

	private static void clear() {
		type = 0; depth = 0; target = 0; start = 0; bossId = 0;
		ring1 = null; ring2 = null;
	}

	public static void reset() {
		clear();
		done.clear();
	}

	private static final String NODE   = "shopquest";
	private static final String TYPE   = "type";
	private static final String DEPTH  = "depth";
	private static final String TARGET = "target";
	private static final String START  = "start";
	private static final String BOSSID = "bossid";
	private static final String RING1  = "ring1";
	private static final String RING2  = "ring2";
	private static final String DONE   = "done";

	public static void storeInBundle( Bundle bundle ) {
		Bundle node = new Bundle();
		node.put( TYPE, type );
		node.put( DEPTH, depth );
		node.put( TARGET, target );
		node.put( START, start );
		node.put( BOSSID, bossId );
		if (ring1 != null) node.put( RING1, ring1 );
		if (ring2 != null) node.put( RING2, ring2 );
		int[] arr = new int[done.size()];
		for (int i = 0; i < arr.length; i++) arr[i] = done.get( i );
		node.put( DONE, arr );
		bundle.put( NODE, node );
	}

	public static void restoreFromBundle( Bundle bundle ) {
		reset();
		Bundle node = bundle.getBundle( NODE );
		if (node.isNull()) return;
		type   = node.getInt( TYPE );
		depth  = node.getInt( DEPTH );
		target = node.getInt( TARGET );
		start  = node.getInt( START );
		bossId = node.getInt( BOSSID );
		if (node.contains( RING1 )) ring1 = (Ring) node.get( RING1 );
		if (node.contains( RING2 )) ring2 = (Ring) node.get( RING2 );
		int[] arr = node.getIntArray( DONE );
		if (arr != null) for (int d : arr) done.add( d );
	}

	// ---------- вспомогательное ----------

	private static int nextShop( int d ) {
		for (int s : SHOP_DEPTHS) if (s > d) return s;
		return 0;
	}

	//тип квеста детерминирован сидом забега и этажом
	private static int rollType() {
		int[] pool = nextShop( Dungeon.depth ) > 0
				? new int[]{ SLAYER, GOLD, WEAPON, DELIVERY, BOSS }
				: new int[]{ SLAYER, GOLD, WEAPON, BOSS };
		return pool[ new java.util.Random( Dungeon.seed + Dungeon.depth * 31L ).nextInt( pool.length ) ];
	}

	private static int targetFor( int t ) {
		switch (t) {
			case SLAYER:   return 10 + Dungeon.depth;
			case GOLD:     return Dungeon.depth * 80;
			case WEAPON:   return Math.min( 5, 2 + Dungeon.depth / 5 );
			case DELIVERY: return nextShop( Dungeon.depth );
			default:       return 0;
		}
	}

	private static String typeName( int t ) {
		switch (t) {
			case SLAYER:   return "slayer";
			case GOLD:     return "gold";
			case WEAPON:   return "weapon";
			case DELIVERY: return "delivery";
			default:       return "boss";
		}
	}

	private static MeleeWeapon findWeapon() {
		for (Item i : Dungeon.hero.belongings.backpack.items) {
			if (i instanceof MeleeWeapon
					&& !i.isEquipped( Dungeon.hero )
					&& !i.unique
					&& i.level() <= 0
					&& ((MeleeWeapon) i).tier >= target) {
				return (MeleeWeapon) i;
			}
		}
		return null;
	}

	private static boolean bossDead() {
		Actor a = Actor.findById( bossId );
		return !(a instanceof Mob) || !((Mob) a).isAlive();
	}

	private static int progress() {
		switch (type) {
			case SLAYER: return Statistics.enemiesSlain - start;
			case GOLD:   return Statistics.goldCollected - start;
			default:     return 0;
		}
	}

	private static boolean isComplete() {
		switch (type) {
			case WEAPON:   return findWeapon() != null;
			case DELIVERY: return Dungeon.depth == target;
			case BOSS:     return bossDead();
			default:       return progress() >= target;
		}
	}

	//квест устарел, если герой ушёл дальше места, где его можно сдать
	private static void expireIfNeeded() {
		if (type == 0) return;
		boolean expired = (type == DELIVERY) ? Dungeon.depth > target : Dungeon.depth != depth;
		if (expired) clear();
	}

	// ---------- награда ----------

	private static Ring newRing( int bonus ) {
		Ring r = (Ring) Generator.random( Generator.Category.RING );
		r.cursed = false;
		r.level( 0 );
		r.upgrade( bonus );
		r.identify();
		return r;
	}

	private static void rollRings() {
		//+1 на 6 и 11 этажах, +2 на 16 и 20; за мини-босса ещё +1
		int bonus = (Dungeon.depth >= 16 ? 2 : 1) + (type == BOSS ? 1 : 0);
		ring1 = newRing( bonus );
		ring2 = newRing( bonus );
		int tries = 0;
		while (ring2.getClass() == ring1.getClass() && tries++ < 50) {
			Generator.undoDrop( ring2 );
			ring2 = newRing( bonus );
		}
	}

	private static void claim( Ring ring ) {
		if (type == WEAPON) {
			MeleeWeapon w = findWeapon();
			if (w != null) w.detach( Dungeon.hero.belongings.backpack );
		}
		if (!ring.doPickUp( Dungeon.hero )) {
			Dungeon.level.drop( ring, Dungeon.hero.pos );
		}
		done.add( depth );
		String name = ring.name();
		clear();
		GLog.p( Messages.get( Shopkeeper.class, "quest_reward", name ) );
	}

	private static void showRewardChoice( final Shopkeeper keeper, String title ) {
		if (ring1 == null || ring2 == null) rollRings(); //страховка для старых сохранений
		final Ring[] rings = { ring1, ring2 };
		GameScene.show( new WndOptions( keeper.sprite(), title,
				Messages.get( Shopkeeper.class, "quest_choose" ),
				rings[0].name(), rings[1].name() ) {
			@Override
			protected void onSelect( int index ) {
				claim( rings[index] );
			}

			@Override
			protected boolean hasIcon( int index ) {
				return true;
			}

			@Override
			protected Image getIcon( int index ) {
				return new ItemSprite( rings[index] );
			}
		} );
	}

	// ---------- принятие квеста ----------

	private static boolean spawnBoss() {
		for (int tries = 0; tries < 20; tries++) {
			Mob m = Dungeon.level.createMob();
			if (m == null) continue;
			m.state = m.WANDERING;
			m.pos = Dungeon.level.randomRespawnCell( m );
			if (m.pos == -1 || Dungeon.level.distance( Dungeon.hero.pos, m.pos ) < 8) continue;

			m.HT = m.HP = Math.round( m.HT * 1.5f );
			switch (Random.Int( 5 )) {
				case 0: default: Buff.affect( m, ChampionEnemy.Giant.class );      break;
				case 1:          Buff.affect( m, ChampionEnemy.Blazing.class );    break;
				case 2:          Buff.affect( m, ChampionEnemy.Projecting.class ); break;
				case 3:          Buff.affect( m, ChampionEnemy.AntiMagic.class );  break;
				case 4:          Buff.affect( m, ChampionEnemy.Growing.class );    break;
			}
			GameScene.add( m );
			bossId = m.id();
			return true;
		}
		return false;
	}

	private static void accept() {
		int t = rollType();
		depth = Dungeon.depth;
		bossId = 0;
		start = 0;

		//если не нашлось места для босса, квест превращается в охоту на врагов
		if (t == BOSS && !spawnBoss()) t = SLAYER;

		type = t;
		target = targetFor( t );
		if (t == SLAYER) start = Statistics.enemiesSlain;
		if (t == GOLD)   start = Statistics.goldCollected;

		rollRings();
		GLog.i( Messages.get( Shopkeeper.class, "quest_accepted_" + typeName( t ), target ) );
	}

	// ---------- диалог ----------

	/** Вызывать из Shopkeeper.interact на потоке рендера. */
	public static void talk( final Shopkeeper keeper ) {
		final String title = Messages.titleCase( keeper.name() );

		expireIfNeeded();

		if (type == 0) {
			if (done.contains( Dungeon.depth )) {
				GameScene.show( new WndTitledMessage( keeper.sprite(), title,
						Messages.get( Shopkeeper.class, "quest_done" ) ) );
				return;
			}

			//предложение квеста
			int t = rollType();
			String msg = Messages.get( Shopkeeper.class, "quest_offer_" + typeName( t ), targetFor( t ) );
			GameScene.show( new WndOptions( keeper.sprite(), title, msg,
					Messages.get( Shopkeeper.class, "quest_accept" ),
					Messages.get( Shopkeeper.class, "quest_later" ) ) {
				@Override
				protected void onSelect( int index ) {
					if (index == 0) accept();
				}
			} );
			return;
		}

		if (isComplete()) {
			GameScene.show( new WndOptions( keeper.sprite(), title,
					Messages.get( Shopkeeper.class, "quest_complete_" + typeName( type ) ),
					Messages.get( Shopkeeper.class, "quest_turnin" ),
					Messages.get( Shopkeeper.class, "quest_later" ) ) {
				@Override
				protected void onSelect( int index ) {
					if (index == 0) showRewardChoice( keeper, title );
				}
			} );
		} else {
			String msg = Messages.get( Shopkeeper.class, "quest_progress_" + typeName( type ),
					Math.min( progress(), target ), target );
			GameScene.show( new WndTitledMessage( keeper.sprite(), title, msg ) );
		}
	}
}
