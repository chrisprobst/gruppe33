package com.indyforge.twod.engine.graphics;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.lang.reflect.InvocationTargetException;

import com.indyforge.twod.engine.graphics.rendering.scenegraph.SceneProcessor;
import com.indyforge.twod.engine.graphics.rendering.scenegraph.math.MathExt;
import com.indyforge.twod.engine.graphics.sprite.Sprite;
import com.indyforge.twod.engine.resources.assets.AssetManager;

/**
 * Utility class to bundle some default graphics routines like creating frames.
 * 
 * @author Christopher Probst
 * @author Matthias Hesse
 */
public final class GraphicsRoutines {

	/**
	 * Optimizes this image to be compatible with the screen.
	 * 
	 * @param image
	 *            The image you want to optimize.
	 * @return a optimized copy of the given image.
	 */
	public static BufferedImage optimizeImage(BufferedImage image) {

		if (image == null) {
			throw new NullPointerException("image");
		}

		// Create a new optimized image
		BufferedImage optimizedImage = GraphicsRoutines.createImage(
				image.getWidth(), image.getHeight(), image.getTransparency());

		// Get graphics context
		Graphics2D g2d = optimizedImage.createGraphics();

		try {
			// Draw the loaded image into the optimized image
			g2d.drawImage(image, 0, 0, null);
		} finally {
			g2d.dispose();
		}

		return optimizedImage;
	}

	/**
	 * Clear the given image with the given background color.
	 * 
	 * @param image
	 *            The image you want to clear.
	 * @param background
	 *            The background color. If null the image is not touched.
	 * @return the image.
	 */
	public static BufferedImage clear(BufferedImage image, Color background) {
		if (image == null) {
			throw new NullPointerException("image");
		}

		// Clear background if not null
		if (background != null) {
			Graphics2D g2d = image.createGraphics();
			try {
				g2d.setBackground(background);
				g2d.clearRect(0, 0, image.getWidth(), image.getHeight());
			} finally {
				g2d.dispose();
			}
		}
		return image;
	}

	/**
	 * Creates a compatible image.
	 * 
	 * @param width
	 *            The width of the new image.
	 * @param height
	 *            The height of the new image.
	 * @return the new compatible image.
	 */
	public static BufferedImage createImage(int width, int height) {
		return getGC().createCompatibleImage(width, height);
	}

	/**
	 * Creates a compatible image.
	 * 
	 * @param width
	 *            The width of the new image.
	 * @param height
	 *            The height of the new image.
	 * @param transparency
	 *            The transparency of the new image.
	 * @return the new compatible image.
	 */
	public static BufferedImage createImage(int width, int height,
			int transparency) {
		return getGC().createCompatibleImage(width, height, transparency);
	}

	/**
	 * Creates a compatible image.
	 * 
	 * @param imageDesc
	 *            The image description.
	 * @return the new compatible image.
	 */
	public static BufferedImage createImage(ImageDesc imageDesc) {
		if (imageDesc == null) {
			throw new NullPointerException("imageDesc");
		}

		return getGC().createCompatibleImage(imageDesc.width(),
				imageDesc.height(), imageDesc.transparency());
	}

	/**
	 * Creates a compatible volatile image.
	 * 
	 * @param width
	 *            The width of the new volatile image.
	 * @param height
	 *            The height of the new volatile image.
	 * @return the new compatible volatile image.
	 */
	public static VolatileImage createVolatileImage(int width, int height) {
		return getGC().createCompatibleVolatileImage(width, height);
	}

	/**
	 * Creates a compatible volatile image.
	 * 
	 * @param width
	 *            The width of the new volatile image.
	 * @param height
	 *            The height of the new volatile image.
	 * @param transparency
	 *            The transparency of the new volatile image.
	 * @return the new compatible volatile image.
	 */
	public static VolatileImage createVolatileImage(int width, int height,
			int transparency) {
		return getGC().createCompatibleVolatileImage(width, height,
				transparency);
	}

	/**
	 * @return the active graphics configuration.
	 */
	public static GraphicsConfiguration getGC() {
		if (AssetManager.isHeadless()) {
			throw new HeadlessException();
		}
		return GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDefaultConfiguration();
	}

	/**
	 * Reads a sprite sub image.
	 * 
	 * @param image
	 *            The image which contains the sub images.
	 * @param rasterX
	 *            The columns of the image.
	 * @param rasterY
	 *            The rows of the image.
	 * @param x
	 *            The column of the sub image.
	 * @param y
	 *            The row of the sub image.
	 * @return the sub image.
	 * @see Sprite
	 */
	public static BufferedImage getSpriteSubImage(BufferedImage image,
			int rasterX, int rasterY, int x, int y) {

		if (image == null) {
			throw new NullPointerException("image");
		} else if (rasterX <= 0) {
			throw new IllegalArgumentException("rasterX must be > 0");
		} else if (rasterY <= 0) {
			throw new IllegalArgumentException("rasterY must be > 0");
		}

		// Clamp the coords
		x = MathExt.clamp(x, 0, rasterX - 1);
		y = MathExt.clamp(y, 0, rasterY - 1);

		// Calculating the size of one sub image
		int sizeXPlate = image.getWidth(null) / rasterX;
		int sizeYPlate = image.getHeight(null) / rasterY;

		// Return the optimized copy of the sub-image
		return optimizeImage(image.getSubimage(x * sizeXPlate, y * sizeYPlate,
				sizeXPlate, sizeYPlate));
	}

	/**
	 * Creates a new visible frame which contains the processor.
	 * 
	 * @param processor
	 *            The processor of the frame.
	 * @param title
	 *            The title of the frame.
	 * @param width
	 *            The width of the frame.
	 * @param height
	 *            The height of the frame.
	 * @return a new visible frame.
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public static Frame createFrame(SceneProcessor processor, String title,
			int width, int height) throws InterruptedException,
			InvocationTargetException {
		return createFrame(processor, title, width, height, true);
	}

	/**
	 * Creates a new frame which contains the processor.
	 * 
	 * @param processor
	 *            The processor of the frame.
	 * @param title
	 *            The title of the frame.
	 * @param width
	 *            The width of the frame.
	 * @param height
	 *            The height of the frame.
	 * @param visible
	 *            The visible flag of the frame.
	 * @return a new frame.
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public static Frame createFrame(final SceneProcessor processor,
			final String title, final int width, final int height,
			final boolean visible) throws InterruptedException,
			InvocationTargetException {

		if (processor == null) {
			throw new NullPointerException("processor");
		}

		/*
		 * Create a new frame for viewing. Should be ok that it is not invoked
		 * on EDT since this is a new variable and the EDT does not know it yet
		 * so there cannot be any cached data yet.
		 */
		final Frame frame = new Frame(title);

		/*
		 * Invoke the setup on the EDT.
		 */
		EventQueue.invokeAndWait(new Runnable() {

			@Override
			public void run() {
				// Set border layout
				frame.setLayout(new BorderLayout());

				// Disable the repaint events
				frame.setIgnoreRepaint(true);

				// Add to frame
				frame.add(processor.canvas(), BorderLayout.CENTER);

				// Set size
				frame.setSize(width, height);

				// Shutdown hook
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent e) {
						// Shutdown request
						processor.shutdownRequest(true);
					}
				});

				// Make visible (Does not need the EDT)
				frame.setVisible(visible);
			}
		});

		return frame;
	}

	public static Applet setupApplet(Applet applet, SceneProcessor processor) {
		// Set border layout
		applet.setLayout(new BorderLayout());

		// Disable the repaint events
		applet.setIgnoreRepaint(true);

		// Add to applet
		applet.add(processor.canvas(), BorderLayout.CENTER);

		return applet;
	}

	// Should not be instantiated...
	private GraphicsRoutines() {
	}
}
