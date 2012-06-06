package com.indyforge.twod.engine.graphics.rendering.gui;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.indyforge.twod.engine.graphics.rendering.scenegraph.Entity;
import com.indyforge.twod.engine.graphics.rendering.scenegraph.RenderedImage;
import com.indyforge.twod.engine.graphics.rendering.scenegraph.Scene;
import com.indyforge.twod.engine.graphics.rendering.scenegraph.animation.RenderedAnimation;
import com.indyforge.twod.engine.graphics.rendering.scenegraph.math.Vector2f;
import com.indyforge.twod.engine.graphics.sprite.Animation;
import com.indyforge.twod.engine.graphics.sprite.Sprite;
import com.indyforge.twod.engine.resources.assets.Asset;
import com.indyforge.twod.engine.resources.assets.AssetManager;

public class GuiCreator {

	private Gui aktiveGui;

	private List<Gui> guiList;

	public GuiCreator() {

		// A List of all needed Guis
		List<Gui> guiList = new ArrayList<Gui>();
	}

	public static Scene createGuiScene() throws Exception {

		final Scene scene = new Scene(new AssetManager(new File(
				"scenes/default.zip")), 1024, 1024);

		scene.scale().set(scene.sizeAsVector());

		Asset<BufferedImage> back = scene.assetManager().loadImage(
				"assets/images/gui/main.jpg", true);

		Asset<BufferedImage> cursor = scene.assetManager().loadImage(
				"assets/images/bomb.png", true);

		// Hier laden wir das boom sprite... 5 *5 bilder
		final Sprite boom = new Sprite(scene.assetManager().loadImage(
				"assets/images/animated/boom.png", true), 5, 5);

		// Create new explosion entity
		final Animation animation = boom
				.newAnimationFromRange("explosion", 33, 0, 0, 25).loop(false)
				.paused(false);

		final RenderedImage backRI = new RenderedImage(back).centered(true);
		final RenderedImage bombRI = new RenderedImage(cursor).centered(true);

		backRI.position().set(0.5f, 0.5f);

		bombRI.position().set(157 / 1024f, 415 / 1024f);
		bombRI.scale().set(0.08f, 0.08f);

		final Vector2f[] fields = new Vector2f[] {
				new Vector2f(157 / 1024f, 415 / 1024f),
				new Vector2f(548 / 1024f, 495 / 1024f),
				new Vector2f(457 / 1024f, 759 / 1024f),
				new Vector2f(772 / 1024f, 973 / 1024f) };

		final Gui test = new Gui(backRI, bombRI ,fields);
		
		
		

		Entity border = new Entity() {

			boolean pressedUP = false, pressedDOWN = false,
					pressedENTER = false;

			@Override
			protected void onUpdate(float tpf) {
				super.onUpdate(tpf);

				if (!pressedUP && scene.isPressed(KeyEvent.VK_UP)) {

					test.updatePos(false);
					bombRI.position().set(test.getMenuVector());

					pressedUP = true;

				}
				if (pressedUP && !scene.isPressed(KeyEvent.VK_UP)) {
					pressedUP = false;
				}

				if (!pressedDOWN && scene.isPressed(KeyEvent.VK_DOWN)) {

					test.updatePos(true);
					bombRI.position().set(test.getMenuVector());

					pressedDOWN = true;

				}
				if (pressedDOWN && !scene.isPressed(KeyEvent.VK_DOWN)) {
					pressedDOWN = false;
				}

				if (!pressedENTER && scene.isPressed(KeyEvent.VK_ENTER)) {
					// Create an entity using the animation
					RenderedAnimation renderedAnimation = new RenderedAnimation() {
						protected void onAnimationFinished(
								RenderedAnimation renderedAnimation,
								Animation animation) {

							// WICHTIG ... Wenn die bombe tot is... (Das letzte
							// Frame erreicht wurde... detach!)

							renderedAnimation.detach();
						}
					};
					renderedAnimation.index(10); // Damit es auf jeden fall über
													// dem gui
													// is---
					renderedAnimation.animationBundle().add(animation).reset();
					renderedAnimation.animationName("explosion");

					renderedAnimation.position().set(test.getMenuVector());
					scene.attach(renderedAnimation);
					pressedENTER = true;
				}

				if (pressedENTER && !scene.isPressed(KeyEvent.VK_ENTER)) {
					pressedENTER = false;
				}

			}
		};

		scene.attach(border);
		scene.attach(backRI);
		scene.attach(bombRI);

		return scene;

	}

	public void showGuiExplosion() {

	}

}
