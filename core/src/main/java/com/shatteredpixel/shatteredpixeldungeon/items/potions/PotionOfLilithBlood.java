package com.shatteredpixel.shatteredpixeldungeon.items.potions;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vampirism;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class PotionOfLilithBlood extends Potion {

	private static final int SPRITE = ItemSpriteSheet.POTION_LILITH;

	{
		image = SPRITE;
	}

	//Потион из рандомного пула цветов не берёт, цвет у этого зелья всегда один
	@Override
	public void reset() {
		super.reset();
		image = SPRITE;
	}

	//Зелье всегда опознано
	@Override
	public boolean isKnown() {
		return true;
	}

	//Бросать нельзя, только пить
	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.remove(AC_THROW);
		return actions;
	}

	@Override
	public String name() {
		return "Кровь Лилит";
	}

	@Override
	public String desc() {
		return "Густая, тёплая и слишком живая для того, что должно быть мёртвым. "
				+ "Каждый ваш удар будет лечить на несколько HP. "
				+ "Первое выпитое зелье даёт +1 вампиризма, второе +2, третье +3, четвёртое +4. "
				+ "Всего " + Vampirism.MAX + " HP за удар. Эффект постоянный.";
	}

	@Override
	public void apply(Hero hero) {
		identify();
		Vampirism v = Buff.affect(hero, Vampirism.class);
		if (v.canIncrease()) {
			int gain = v.nextGain();
			v.increase();
			hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 8);
			GLog.p("Вампиризм +" + gain + ". Теперь каждый удар лечит на " + v.level() + " HP.");
		} else {
			GLog.w("Вампиризм уже на максимуме. Кровь Лилит пропала зря.");
		}
	}

	@Override
	public int value() {
		return 40 * quantity;
	}
}
