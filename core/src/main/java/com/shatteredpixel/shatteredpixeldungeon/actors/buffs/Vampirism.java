package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

//Постоянный бафф от зелья "Кровь Лилит".
//N-е зелье даёт +N вампиризма: 1, 2, 3, 4 (всего 4 зелья, итого максимум 10).
//Каждая единица вампиризма = +1 HP за удар героя.
public class Vampirism extends Buff {

	public static final int MAX_DOSES = 4;
	public static final int MAX = 10; // 1+2+3+4

	private int doses = 0;
	private int level = 0;

	{
		type = buffType.POSITIVE;
		revivePersists = true;
	}

	public int level() {
		return level;
	}

	public int doses() {
		return doses;
	}

	public boolean canIncrease() {
		return doses < MAX_DOSES;
	}

	//сколько вампиризма даст следующее зелье
	public int nextGain() {
		return doses + 1;
	}

	public void increase() {
		if (doses < MAX_DOSES) {
			doses++;
			level += doses;
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.NONE;
	}

	@Override
	public String toString() {
		return "Вампиризм";
	}

	@Override
	public String desc() {
		return "Каждый ваш удар лечит на " + level + " HP. Выпито зелий: " + doses + " из " + MAX_DOSES + " (максимум " + MAX + " HP за удар).";
	}

	private static final String LEVEL = "level";
	private static final String DOSES = "doses";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LEVEL, level);
		bundle.put(DOSES, doses);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		level = bundle.getInt(LEVEL);
		doses = bundle.getInt(DOSES);
	}
}
