package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

//Панель-подсказка для алхимического котла: "3 одинаковых семени = зелье".
//Зелья, которые ты ещё не опознал, показываются как "?".
public class PotionHintPanel extends Component {

	private static final int CELL_W = 48;
	private static final int CELL_H = 18;
	private static final int GAP = 2;

	private NinePatch bg;
	private Button blocker; //не пропускает нажатия к кнопкам под панелью
	private RenderedTextBlock title;
	private RenderedTextBlock note;

	private ArrayList<Class<? extends Plant.Seed>> seeds;
	private ArrayList<ItemSprite> seedSprites;
	private ArrayList<RenderedTextBlock> mults;
	private ArrayList<RenderedTextBlock> equals;
	private ArrayList<ItemSprite> potionSprites;
	private ArrayList<RenderedTextBlock> unknownMarks;

	private int cols = 0;

	//поля не инициализируем в объявлении: createChildren() вызывается из конструктора Component
	@Override
	protected void createChildren() {
		super.createChildren();

		bg = Chrome.get(Chrome.Type.TOAST);
		add(bg);

		blocker = new Button(){
			@Override
			protected void onClick() {
				//ничего, только перехватываем нажатие
			}
		};
		add(blocker);

		title = PixelScene.renderTextBlock(7);
		title.hardlight(Window.TITLE_COLOR);
		add(title);

		note = PixelScene.renderTextBlock(5);
		note.hardlight(0xBBBBBB);
		add(note);

		seeds = new ArrayList<>(Potion.SeedToPotion.types.keySet());
		//порядок у HashMap случайный, поэтому сортируем, чтобы список не прыгал
		Collections.sort(seeds, new Comparator<Class<? extends Plant.Seed>>() {
			@Override
			public int compare(Class<? extends Plant.Seed> a, Class<? extends Plant.Seed> b) {
				return a.getName().compareTo(b.getName());
			}
		});

		seedSprites = new ArrayList<>();
		mults = new ArrayList<>();
		equals = new ArrayList<>();
		potionSprites = new ArrayList<>();
		unknownMarks = new ArrayList<>();

		for (Class<? extends Plant.Seed> seedClass : seeds){
			ItemSprite seed = new ItemSprite(Reflection.newInstance(seedClass));
			add(seed);
			seedSprites.add(seed);

			RenderedTextBlock mult = PixelScene.renderTextBlock("x3", 6);
			add(mult);
			mults.add(mult);

			RenderedTextBlock eq = PixelScene.renderTextBlock("=", 6);
			add(eq);
			equals.add(eq);

			ItemSprite potion = new ItemSprite();
			potion.visible = false;
			add(potion);
			potionSprites.add(potion);

			RenderedTextBlock mark = PixelScene.renderTextBlock("?", 9);
			mark.hardlight(0xAAAAAA);
			add(mark);
			unknownMarks.add(mark);
		}
	}

	//availH: доступная высота экрана. На низких экранах панель делается шире и ниже (3 колонки)
	public void place( float px, float py, int availH ) {
		cols = availH >= 200 ? 2 : 3;
		int rows = (seeds.size() + cols - 1) / cols;
		int innerW = cols * CELL_W + (cols - 1) * GAP;

		title.maxWidth(innerW);
		title.text("3 семени = зелье");

		note.maxWidth(innerW);
		note.text("Только 3 одинаковых семени. Разные семена дают случайный результат.");

		float w = innerW + bg.marginHor();
		float h = bg.marginVer() + title.height() + 3 + rows * CELL_H + 3 + note.height();

		setRect(px, py, w, h);
		refresh();
	}

	private static boolean isKnown( Class<? extends Potion> potionClass ){
		try {
			return Potion.getKnown().contains(potionClass);
		} catch (Exception e){
			return false;
		}
	}

	//обновляет, какие зелья уже опознаны
	public void refresh() {
		if (seeds == null) return;

		for (int i = 0; i < seeds.size(); i++){
			Class<? extends Potion> potionClass = Potion.SeedToPotion.types.get(seeds.get(i));
			boolean known = potionClass != null && isKnown(potionClass);
			if (known){
				potionSprites.get(i).view(Reflection.newInstance(potionClass));
			}
			potionSprites.get(i).visible = known;
			unknownMarks.get(i).visible = !known;
		}

		if (cols != 0) layout();
	}

	@Override
	protected void layout() {
		super.layout();

		if (cols == 0) return;

		bg.x = x;
		bg.y = y;
		bg.size(width, height);
		blocker.setRect(x, y, width, height);

		float left = x + bg.marginLeft();
		float top = y + bg.marginTop();
		float innerW = cols * CELL_W + (cols - 1) * GAP;

		title.setPos(left + (innerW - title.width()) / 2f, top);
		top += title.height() + 3;

		int rows = (seeds.size() + cols - 1) / cols;

		for (int i = 0; i < seeds.size(); i++){
			int col = i % cols;
			int row = i / cols;
			float cx = left + col * (CELL_W + GAP);
			float cy = top + row * CELL_H;

			ItemSprite seed = seedSprites.get(i);
			seed.x = cx + (16 - seed.width()) / 2f;
			seed.y = cy + (CELL_H - seed.height()) / 2f;
			PixelScene.align(seed);

			RenderedTextBlock mult = mults.get(i);
			mult.setPos(cx + 16, cy + (CELL_H - mult.height()) / 2f);
			PixelScene.align(mult);

			RenderedTextBlock eq = equals.get(i);
			eq.setPos(cx + 26, cy + (CELL_H - eq.height()) / 2f);
			PixelScene.align(eq);

			float potionLeft = cx + 33;

			ItemSprite potion = potionSprites.get(i);
			potion.x = potionLeft + (14 - potion.width()) / 2f;
			potion.y = cy + (CELL_H - potion.height()) / 2f;
			PixelScene.align(potion);

			RenderedTextBlock mark = unknownMarks.get(i);
			mark.setPos(potionLeft + (14 - mark.width()) / 2f, cy + (CELL_H - mark.height()) / 2f);
			PixelScene.align(mark);
		}

		note.setPos(left, top + rows * CELL_H + 3);
	}
}
