package com.indyforge.twod.engine.graphics.rendering.scenegraph.gui;

import java.awt.event.KeyEvent;
import java.util.Iterator;

import com.indyforge.twod.engine.graphics.rendering.scenegraph.Entity;
import com.indyforge.twod.engine.graphics.rendering.scenegraph.EntityFilter;
import com.indyforge.twod.engine.graphics.rendering.scenegraph.GraphicsEntity;
import com.indyforge.twod.engine.graphics.rendering.scenegraph.Scene;
import com.indyforge.twod.engine.util.FilteredIterator;
import com.indyforge.twod.engine.util.Task;

/**
 * Represents a gui entity. Gui entities can be "selected" by using the defaults
 * keys.
 * 
 * @author Christopher Probst
 * 
 */
public class GuiEntity extends GuiListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 * The selected and selectable flag.
	 */
	private boolean selected = false, selectable = true;

	/*
	 * The container of a gui entity.
	 */
	private final GraphicsEntity container = new GraphicsEntity();

	@Override
	protected void onSelected(GuiEntity guiEntity) {
		super.onSelected(guiEntity);
		if (guiEntity == this) {
			selected = true;
		}
	}

	@Override
	protected void onDeselected(GuiEntity guiEntity) {
		super.onDeselected(guiEntity);
		if (guiEntity == this) {
			selected = false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.indyforge.twod.engine.graphics.rendering.scenegraph.Entity#onUpdate
	 * (float)
	 */
	@Override
	protected void onUpdate(float tpf) {
		super.onUpdate(tpf);

		// Find scene
		Scene scene = findScene();

		// Scene available and selected ?
		if (scene != null && selected) {

			// Read input values
			boolean up = scene.isSinglePressed(KeyEvent.VK_UP), down = scene
					.isSinglePressed(KeyEvent.VK_DOWN);

			if (up != down) {

				// The move dir
				int dir = up ? -1 : 1;

				// Iterate...
				for (int next = cacheIndex() + dir;; next += dir) {

					// Check!
					if (next < 0) {
						next = parent().children().size() - 1;
					} else if (next >= parent().children().size()) {
						next = 0;
					}

					// Lookup child
					final Entity nextChild = parent().childAt(next);

					// If gui entity and selectable... select it !
					if (nextChild instanceof GuiEntity
							&& ((GuiEntity) nextChild).isSelectable()) {

						// Queue the new task
						parent().taskQueue()
								.tasks()
								.offer(new ChangeSelectionTask(this,
										(GuiEntity) nextChild));
						break;
					}
				}
			}
		}
	}

	private static final class ChangeSelectionTask implements Task {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/*
		 * The gui entities.
		 */
		private final GuiEntity deselect, select;

		public ChangeSelectionTask(GuiEntity deselect, GuiEntity select) {
			if (deselect == null) {
				throw new NullPointerException("deselect");
			} else if (select == null) {
				throw new NullPointerException("select");
			}
			this.deselect = deselect;
			this.select = select;
		}

		@Override
		public void run() {
			deselect.deselect();
			select.select();
		}
	}

	public GuiEntity() {
		attach(container);
	}

	public GuiEntity thisVisible(boolean visible) {
		Iterator<Entity> ptr = new FilteredIterator<Entity>(new EntityFilter() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean accept(Entity element) {
				return element != container;
			}
		}, container.childIterator(true, true));
		while (ptr.hasNext()) {
			Entity e = ptr.next();
			if (e instanceof GraphicsEntity) {
				((GraphicsEntity) e).visible(visible);
			}
		}
		return this;
	}

	public GuiEntity containerVisible(boolean visible) {
		Iterator<Entity> ptr = container.childIterator(true, true);
		while (ptr.hasNext()) {
			Entity e = ptr.next();
			if (e instanceof GraphicsEntity) {
				((GraphicsEntity) e).visible(visible);
			}
		}
		return this;
	}

	/**
	 * @return the container.
	 */
	public GraphicsEntity container() {
		return container;
	}

	/**
	 * Selects this entity.
	 * 
	 * @return this for chaining.
	 */
	public GuiEntity select() {
		fireEvent(GuiEvent.Selected);
		return this;
	}

	/**
	 * Deselects this entity.
	 * 
	 * @return this for chaining.
	 */
	public GuiEntity deselect() {
		fireEvent(GuiEvent.Deselected);
		return this;
	}

	/**
	 * @return the selectable-flag.
	 */
	public boolean isSelectable() {
		return selectable;
	}

	/**
	 * Sets the selectable-flag.
	 * 
	 * @param selectable
	 *            If true this gui entity can be selected.
	 * @return this for chaining.
	 */
	public GuiEntity selectable(boolean selectable) {
		this.selectable = selectable;
		return this;
	}

	/**
	 * @return true if this gui entity is selected, otherwise false.
	 */
	public boolean isSelected() {
		return selected;
	}
}
