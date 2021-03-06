package com.indyforge.twod.engine.graphics.rendering.scenegraph.util.iteration;

import java.util.Iterator;

import com.indyforge.twod.engine.graphics.rendering.scenegraph.Entity;
import com.indyforge.twod.engine.util.iteration.ArrayIterator;
import com.indyforge.twod.engine.util.iteration.CompositeIterator;


/**
 * This is a child iterator implementation.
 * 
 * @author Christopher Probst
 */
public final class ChildIterator extends AbstractEntityIterator {

	// The peer iterator
	private final Iterator<? extends Entity> peerIterator;

	/**
	 * Creates a new child iterator using the given root. The root will be part
	 * of the iteration, too.
	 * 
	 * @param root
	 *            The root entity.
	 */
	public ChildIterator(Entity root) {
		this(root, true);
	}

	/**
	 * Creates a new child iterator using the given root.
	 * 
	 * @param root
	 *            The root entity.
	 * @param includeRoot
	 *            The include-root flag.
	 */
	@SuppressWarnings("unchecked")
	public ChildIterator(Entity root, boolean includeRoot) {
		if (root == null) {
			throw new NullPointerException("root");
		}

		// Save peer iterator
		peerIterator = includeRoot ? new CompositeIterator<Entity>(
				new ArrayIterator<Entity>(root), root.children().iterator())
				: root.children().iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return peerIterator.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Entity next() {
		// Get the ptr and return
		return removePtr = peerIterator.next();
	}
}
