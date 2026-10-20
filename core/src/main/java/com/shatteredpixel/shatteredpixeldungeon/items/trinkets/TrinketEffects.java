package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

//Все эффекты новых аксессуаров, которые вмешиваются в бой и опыт героя.
//Сюда обращается Hero.java в нескольких местах.
public class TrinketEffects {

	private TrinketEffects(){}

	private static void status( Char ch, int color, String text ){
		if (ch != null && ch.sprite != null){
			ch.sprite.showStatus(color, text);
		}
	}

	private static void healHero( Hero hero, int amount ){
		int heal = Math.min(amount, hero.HT - hero.HP);
		if (heal <= 0) return;
		hero.HP += heal;
		if (hero.sprite != null){
			hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
			hero.sprite.showStatus(CharSprite.POSITIVE, Integer.toString(heal));
		}
	}

	//вызывается в конце Hero.attackProc, damage уже итоговый
	public static int onHeroAttack( Hero hero, Char enemy, int damage ){
		if (enemy == null || enemy == hero || damage <= 0 || enemy.alignment != Char.Alignment.ENEMY){
			return damage;
		}

		//--- модификаторы урона
		float mult = 1f;

		int fury = WoundedFury.bonus();
		if (fury > 0 && hero.HP * 2 < hero.HT){
			mult += fury / 100f;
		}

		int every = WaspSting.every();
		if (every > 0 && WaspSting.registerHit(every)){
			mult += WaspSting.bonus() / 100f;
			status(enemy, CharSprite.WARNING, "Жало!");
		}

		if (mult != 1f){
			damage = Math.round(damage * mult);
		}

		//--- знак палача: добивает раненых врагов (кроме боссов)
		int threshold = ExecutionerMark.threshold();
		if (threshold > 0
				&& !Char.hasProp(enemy, Char.Property.BOSS)
				&& !Char.hasProp(enemy, Char.Property.MINIBOSS)){
			int total = enemy.HP + enemy.shielding();
			if (damage < total && (total - damage) <= enemy.HT * threshold / 100f){
				damage = total;
				status(enemy, CharSprite.NEGATIVE, "Казнь!");
			}
		}

		boolean kills = damage >= enemy.HP + enemy.shielding();

		//--- эффекты при ударе
		int poison = VenomThorn.chance();
		if (poison > 0 && !kills && Random.Int(100) < poison){
			Buff.affect(enemy, Poison.class).set(VenomThorn.turns());
		}

		int spark = SparkStone.chance();
		if (spark > 0 && Random.Int(100) < spark){
			int splash = Math.max(1, Math.round(damage * SparkStone.splash() / 100f));
			for (int i : PathFinder.NEIGHBOURS8){
				int cell = enemy.pos + i;
				if (cell < 0 || cell >= Dungeon.level.length()) continue;
				Char ch = Actor.findChar(cell);
				if (ch != null && ch != hero && ch != enemy
						&& ch.alignment == Char.Alignment.ENEMY && ch.isAlive()){
					ch.damage(splash, hero);
					if (ch.sprite != null) ch.sprite.flash();
				}
			}
		}

		int chalice = LilithChalice.heal();
		if (chalice > 0){
			healHero(hero, chalice);
		}

		int fang = VampireFang.heal();
		if (fang > 0 && kills){
			healHero(hero, Math.max(1, Math.round(hero.HT * fang / 100f)));
		}

		return damage;
	}

	//вызывается в Hero.defenseProc перед возвратом
	public static int onHeroDefend( Hero hero, Char attacker, int damage ){
		if (damage <= 0 || attacker == null) return damage;

		int bone = BoneCharm.reduction();
		if (bone > 0){
			damage = Math.max(1, damage - bone);
		}

		int mirror = MirrorShard.chance();
		if (mirror > 0 && attacker != hero && attacker.isAlive()
				&& attacker.alignment == Char.Alignment.ENEMY
				&& Dungeon.level.adjacent(hero.pos, attacker.pos)
				&& Random.Int(100) < mirror){
			int reflected = Math.max(1, Math.round(damage * 0.5f));
			attacker.damage(reflected, hero);
			if (attacker.sprite != null) attacker.sprite.flash();
		}

		return damage;
	}

	//вызывается в Hero.damage после того, как герой реально получил урон
	public static void onHeroDamaged( Hero hero ){
		LastBreath.onDamaged(hero);
	}

	//вызывается в начале Hero.earnExp
	public static int modifyExp( int exp, Class source ){
		if (source == AscensionChallenge.class || source == PotionOfExperience.class) return exp;
		int bonus = CrystalLens.bonus();
		if (bonus <= 0 || exp <= 0) return exp;
		float value = exp * (1f + bonus / 100f);
		int result = (int)value;
		if (Random.Float() < value - result) result++;
		return result;
	}

	//вызывается в Hero.earnExp при получении уровня
	public static void onLevelUp( Hero hero ){
		int dust = StarDust.heal();
		if (dust > 0){
			healHero(hero, Math.max(1, Math.round(hero.HT * dust / 100f)));
		}
	}

	//вызывается в Hero.speed
	public static float speedMultiplier(){
		return WindBoots.speedMultiplier();
	}
}
