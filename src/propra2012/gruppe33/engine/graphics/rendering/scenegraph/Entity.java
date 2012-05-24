package propra2012.gruppe33.engine.graphics.rendering.scenegraph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import propra2012.gruppe33.engine.graphics.rendering.scenegraph.util.ChildIterator;
import propra2012.gruppe33.engine.graphics.rendering.scenegraph.util.ParentIterator;
import propra2012.gruppe33.engine.graphics.rendering.scenegraph.util.RecursiveChildIterator;
import propra2012.gruppe33.engine.graphics.rendering.scenegraph.util.RootFilter;
import propra2012.gruppe33.engine.graphics.rendering.scenegraph.util.SiblingIterator;
import propra2012.gruppe33.engine.util.CompositeIterator;
import propra2012.gruppe33.engine.util.FilteredIterator;
import propra2012.gruppe33.engine.util.IterationRoutines;

/**
 * 
 * An entity is basically a node of tree. Each entity (node) can have multiple
 * children which are sorted by their indeces. This ensures modifiable event
 * ordering. The entity-tree-concept is the key feature for processing
 * hierarchically ordered data.
 * 
 * @author Christopher Probst
 * @see EntityFilter
 */
public class Entity implements Comparable<Entity>, Iterable<Entity>,
		Serializable {

	/**
	 * 
	 * The default entity events.
	 * 
	 * @author Christopher Probst
	 * 
	 */
	public enum EntityEvent {
		Attached, Detached, Update
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Fires the event for the given entity.
	 * 
	 * @param event
	 *            The event.
	 * @param params
	 *            The parameters.
	 */
	public static void fireEvent(Entity entity, Object event, Object... params) {
		if (entity == null) {
			throw new NullPointerException("entity");
		}
		// Just call the method
		entity.onEvent(event, params);
	}

	/**
	 * Fires the given event for all entities.
	 * 
	 * @param entityIterator
	 *            The entity iterator.
	 * @param event
	 *            The event.
	 * @param params
	 *            The parameters.
	 */
	public static void fireEvent(Iterator<? extends Entity> entityIterator,
			Object event, Object... params) {
		if (entityIterator == null) {
			throw new NullPointerException("entityIterator");
		}

		// Post event to all entities
		while (entityIterator.hasNext()) {
			fireEvent(entityIterator.next(), event, params);
		}
	}

	/*
	 * The name of this entity.
	 */
	private String name = super.toString();

	/*
	 * The index of this entity.
	 */
	private int index = 0;

	/*
	 * The parent entity which ones this entity.
	 */
	private Entity parent = null;

	/*
	 * The map which contains all children. The children are stored in sets
	 * which are mapped to their indeces.
	 */
	private final NavigableMap<Integer, Set<Entity>> children = new TreeMap<Integer, Set<Entity>>();

	/*
	 * Used to create event iterators. The default impl. iterates over all
	 * children and sub-children.
	 */
	private Iterable<? extends Entity> iterableEventEntities = new Iterable<Entity>() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<Entity> iterator() {
			return childIterator(true, true);
		}
	};

	/*
	 * The tags of this entity.
	 */
	private final Set<String> tags = new HashSet<String>();

	/*
	 * Used to cache children iterations.
	 */
	private List<Entity> cachedChildren = null;

	/*
	 * The attach/detach event creation flags.
	 */
	private boolean attachEvents = true, detachEvents = true;

	/**
	 * Removes the given set if it is empty.
	 * 
	 * @param order
	 *            The order of the given set.
	 * @param entities
	 *            The entity set.
	 */
	private void removeSetIfEmpty(int order, Set<Entity> entities) {
		if (entities != null && entities.isEmpty()) {

			// Try to remove the set from the map
			Set<Entity> removedEntities = children.remove(order);

			// If it is not the same set readd it (should never happen...)
			if (removedEntities != entities) {

				// Put again into map
				children.put(order, removedEntities);

				// Throw an exception
				throw new IllegalArgumentException("You tried to remove "
						+ "a set which does not represent the set which "
						+ "is mapped to the given order");
			}
		}
	}

	/**
	 * Returns the lazy entity set of the given order. If there is no set yet a
	 * new one will be created.
	 * 
	 * @param order
	 *            The order.
	 * @return a valid set.
	 */
	private Set<Entity> lazyEntities(int order) {
		// Try to get the entities
		Set<Entity> entities = children.get(order);

		// Lazy creation
		if (entities == null) {

			/*
			 * Always use linked-hash-set for better iteration performance and
			 * correct ordering.
			 */
			children.put(order, entities = new LinkedHashSet<Entity>());
		}

		return entities;
	}

	/**
	 * Clears the cached children.
	 */
	private void clearCachedChildren() {
		cachedChildren = null;
	}

	/**
	 * OVERRIDE FOR CUSTOM BEHAVIOUR.
	 * 
	 * This method is called when an event is fired.
	 * 
	 * @param event
	 *            The event.
	 * @param params
	 *            The parameters.
	 */
	protected void onEvent(Object event, Object... params) {
		/*
		 * Process the default events.
		 */
		if (event instanceof EntityEvent) {
			switch ((EntityEvent) event) {
			case Attached:

				// Convert
				Entity parent = (Entity) params[0],
				child = (Entity) params[1];

				// Invoke default event
				onAttached(parent, child);

				// Evaluate the other events...
				if (this == parent) {
					onChildAttached(child);
				} else if (this == child) {
					onAttached();
				} else if (parent == child) {
					onParentAttached();
				}
				break;
			case Detached:
				// Convert
				parent = (Entity) params[0];
				child = (Entity) params[1];

				// Invoke default event
				onDetached(parent, child);

				// Evaluate the other events...
				if (this == parent) {
					onChildDetached(child);
				} else if (this == child) {
					onDetached();
				} else if (parent == child) {
					onParentDetached();
				}
				break;

			case Update:
				onUpdate((Float) params[0]);
			}
		}
	}

	/**
	 * OVERRIDE FOR CUSTOM BEHAVIOUR.
	 * 
	 * This method is called when an entity was attached to an entity.
	 * 
	 * @param parent
	 *            The parent which attached the given child.
	 * @param child
	 *            The child which has been attached.
	 */
	protected void onAttached(Entity parent, Entity child) {

	}

	/**
	 * OVERRIDE FOR CUSTOM BEHAVIOUR.
	 * 
	 * This method is called when this entity was attached to an entity.
	 */
	protected void onAttached() {
	}

	/**
	 * OVERRIDE FOR CUSTOM BEHAVIOUR.
	 * 
	 * This method is called when a child is attached to this entity.
	 * 
	 * @param child
	 *            The child which has been attached.
	 */
	protected void onChildAttached(Entity child) {

	}

	/**
	 * OVERRIDE FOR CUSTOM BEHAVIOUR.
	 * 
	 * This method is called when the parent of this entity was attached to an
	 * entity.
	 */
	protected void onParentAttached() {
	}

	/**
	 * OVERRIDE FOR CUSTOM BEHAVIOUR.
	 * 
	 * This method is called when an entity was detached from an entity.
	 * 
	 * @param parent
	 *            The parent which detached the given child.
	 * @param child
	 *            The child which has been detached.
	 */
	protected void onDetached(Entity parent, Entity child) {
	}

	/**
	 * OVERRIDE FOR CUSTOM BEHAVIOUR.
	 * 
	 * This method is called when this entity was detached from an entity.
	 */
	protected void onDetached() {
	}

	/**
	 * OVERRIDE FOR CUSTOM BEHAVIOUR.
	 * 
	 * This method is called when a child is detached from this entity.
	 * 
	 * @param child
	 *            The child which has been detached.
	 */
	protected void onChildDetached(Entity child) {

	}

	/**
	 * OVERRIDE FOR CUSTOM BEHAVIOUR.
	 * 
	 * This method is called when the parent of this entity was detached from an
	 * entity.
	 */
	protected void onParentDetached() {
	}

	/**
	 * OVERRIDE FOR CUSTOM UPDATE BEHAVIOUR:
	 * 
	 * This method gets called every frame to update the state of this entity.
	 * 
	 * @param tpf
	 *            The time per frame in seconds. Used to interpolate
	 *            time-sensitive data.
	 */
	protected void onUpdate(float tpf) {
	}

	/**
	 * @return the name.
	 */
	public String name() {
		return name;
	}

	/**
	 * Sets the name of this entity.
	 * 
	 * @param name
	 *            The new name.
	 * @return this for chaining.
	 */
	public Entity name(String name) {
		if (name == null) {
			throw new NullPointerException("name");
		}

		this.name = name;
		return this;
	}

	/**
	 * @return true if this entity has children, otherwise false.
	 */
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	/**
	 * @return a snapshot of all children sorted by their indeces as
	 *         unmodifiable list.
	 */
	public List<Entity> children() {

		// Do we already have created the cache?
		if (cachedChildren == null) {

			// Create a new list
			cachedChildren = new ArrayList<Entity>();

			// Copy all entities to the list
			for (Set<Entity> entities : children.values()) {
				for (Entity entity : entities) {
					cachedChildren.add(entity);
				}
			}

			// Make unmodifiable
			cachedChildren = Collections.unmodifiableList(cachedChildren);
		}

		return cachedChildren;
	}

	/**
	 * @return the parent or null.
	 */
	public Entity parent() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Entity> iterator() {
		return new ChildIterator(this, false);
	}

	/**
	 * {@link SiblingIterator#SiblingIterator(Entity)}
	 */
	public Iterator<Entity> siblingIterator() {
		return new SiblingIterator(this);
	}

	/**
	 * {@link SiblingIterator#SiblingIterator(Entity, boolean)}
	 */
	public Iterator<Entity> siblingIterator(boolean includeParent) {
		return new SiblingIterator(this, includeParent);
	}

	/**
	 * 
	 * {@link ChildIterator#ChildIterator(Entity)}
	 * <p>
	 * OR
	 * <p>
	 * {@link RecursiveChildIterator#RecursiveChildIterator(Entity)}
	 */
	public Iterator<Entity> childIterator(boolean recursive) {
		return recursive ? new RecursiveChildIterator(this)
				: new ChildIterator(this);
	}

	/**
	 * 
	 * {@link ChildIterator#ChildIterator(Entity, boolean)}
	 * <p>
	 * OR
	 * <p>
	 * {@link RecursiveChildIterator#RecursiveChildIterator(Entity, boolean)}
	 */
	public Iterator<Entity> childIterator(boolean includeThis, boolean recursive) {
		return recursive ? new RecursiveChildIterator(this, includeThis)
				: new ChildIterator(this, includeThis);
	}

	/**
	 * {@link ParentIterator#ParentIterator(Entity)}
	 */
	public Iterator<Entity> parentIterator() {
		return new ParentIterator(this);
	}

	/**
	 * {@link ParentIterator#ParentIterator(Entity, boolean)}
	 */
	public Iterator<Entity> parentIterator(boolean includeThis) {
		return new ParentIterator(this, includeThis);
	}

	/**
	 * @return true if this entity has no parent, otherwise false.
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * @return the index of this entity.
	 */
	public int index() {
		return index;
	}

	/**
	 * Sets the index of this entity. If this entity is already attached the
	 * parent will sort its children again.
	 * 
	 * @param index
	 *            The new index.
	 * @return the old index.
	 */
	public int index(int index) {
		// A new index ??
		if (index != this.index) {

			// Save the old index
			int oldIndex = this.index;

			// Is there already a parent ?
			if (parent != null) {

				// Lookup
				Set<Entity> entities = parent.children.get(oldIndex);

				// If the set exist
				if (entities != null) {
					// Remove this entity
					if (!entities.remove(this)) {
						throw new IllegalStateException("This entity could "
								+ "not be removed from the old parent set. "
								+ "Please check your code.");
					}

					// Clean up
					parent.removeSetIfEmpty(oldIndex, entities);
				} else {
					throw new IllegalStateException("The parent set of this "
							+ "entity is null. Please check your code.");
				}

				// Try to add
				if (!parent.lazyEntities(index).add(this)) {
					throw new IllegalStateException("This entity could not be "
							+ "added to the new parent set. "
							+ "Please check your code.");
				}
			}

			// Save
			this.index = index;

			// Return the old index
			return oldIndex;
		} else {
			return this.index;
		}
	}

	/**
	 * @return the root entity (an entity which has no parent) of this entity or
	 *         this entity if this entity has no parent (which means that this
	 *         entity is a root entity).
	 */
	public Entity root() {
		return IterationRoutines.next(new FilteredIterator<Entity>(
				RootFilter.INSTANCE, parentIterator(true)));
	}

	/**
	 * Sets the fires-attach-events flag.
	 * 
	 * @param attachEvents
	 *            True if this entity should fire attach events, otherwise
	 *            false.
	 * @return this for chaining.
	 */
	public Entity firesAttachEvents(boolean attachEvents) {
		this.attachEvents = attachEvents;
		return this;
	}

	/**
	 * Sets the fires-detach-events flag.
	 * 
	 * @param detachEvents
	 *            True if this entity should fire detach events, otherwise
	 *            false.
	 * @return this for chaining.
	 */
	public Entity firesDetachEvents(boolean detachEvents) {
		this.detachEvents = detachEvents;
		return this;
	}

	/**
	 * @return true if this entity is able to fire attach events, otherwise
	 *         false.
	 */
	public boolean firesAttachEvents() {
		return attachEvents;
	}

	/**
	 * @return true if this entity is able to fire detach events, otherwise
	 *         false.
	 */
	public boolean firesDetachEvents() {
		return detachEvents;
	}

	/**
	 * Attaches all children given by the iterable interface to this entity. If
	 * these entities have already parents they are detached automatically and
	 * will be attached afterwards.
	 * 
	 * @param children
	 *            The entities you want to attach as children.
	 * @return this for chaining.
	 */
	public Entity attach(Iterable<Entity> children) {
		if (children == null) {
			throw new NullPointerException("children");
		}
		attach(children.iterator());
		return this;
	}

	/**
	 * Attaches all children given by the iterator to this entity. If these
	 * entities have already parents they are detached automatically and will be
	 * attached afterwards.
	 * 
	 * @param children
	 *            The entities you want to attach as children.
	 * @return this for chaining.
	 */
	public Entity attach(Iterator<Entity> children) {
		if (children == null) {
			throw new NullPointerException("children");
		}
		// Iterate over the given children
		while (children.hasNext()) {

			// Attach the next child
			attach(children.next());
		}
		return this;
	}

	/**
	 * Attaches a child to this entity. If this entity has already a parent it
	 * is detached automatically and will be attached afterwards.
	 * 
	 * @param child
	 *            The entity you want to attach as child.
	 * @return this for chaining.
	 */
	@SuppressWarnings("unchecked")
	public Entity attach(Entity child) {
		if (child == null) {
			throw new NullPointerException("child");
		} else if (child == this) {
			throw new IllegalArgumentException("You cannot attach an entity "
					+ "to it self");
		} else if (child.parent != null) {
			// If this entity is already the parent...
			if (child.parent == this) {
				return this;
			} else if (!child.detach()) {
				throw new IllegalStateException("Unable to detach child");
			}
		}

		// Lookup entity set
		Set<Entity> entities = lazyEntities(child.index());

		if (entities.add(child)) {

			// Set parent
			child.parent = this;

			// Cache is invalid now
			clearCachedChildren();

			/*
			 * Is this entity configured to fire attach events ?
			 */
			if (attachEvents) {
				// Fire the attached event recursive
				fireEvent(
						// Use composite itr
						new CompositeIterator<Entity>(
						// Notify THIS tree
								iterableEventEntities.iterator(),
								// Notify the child tree
								child.iterableEventEntities.iterator()),
						EntityEvent.Attached, this, child);
			}
			return this;
		} else {
			throw new IllegalStateException("This entity could not add the "
					+ "given child to its set. Please check your code.");
		}
	}

	/**
	 * Detaches all children from this entity.
	 */
	public boolean detachAll() {
		return detach(iterator());
	}

	/**
	 * @return the tag-set of this entity.
	 */
	public Set<String> tags() {
		return tags;
	}

	/**
	 * Detaches all children given by the iterable interface from this entity.
	 * 
	 * @param children
	 *            The entities you want to detach from this entity.
	 * @return true if all entities were children of this entity and are
	 *         detached successfully, otherwise false.
	 */
	public boolean detach(Iterable<Entity> children) {
		if (children == null) {
			throw new NullPointerException("children");
		}
		return detach(children.iterator());
	}

	/**
	 * Detaches all children given by the iterator from this entity.
	 * 
	 * @param children
	 *            The entities you want to detach from this entity.
	 * @return true if all entities were children of this entity and are
	 *         detached successfully, otherwise false.
	 */
	public boolean detach(Iterator<Entity> children) {
		if (children == null) {
			throw new NullPointerException("children");
		}
		boolean success = true;

		// Iterate over the given children
		while (children.hasNext()) {

			// Detach the next child
			if (!detach(children.next())) {
				success = false;
			}
		}

		return success;
	}

	/**
	 * Detaches this entity from its parent.
	 * 
	 * @return true if there was a parent and this entity is detached
	 *         successfully, otherwise false.
	 */
	public boolean detach() {
		return parent != null ? parent.detach(this) : false;
	}

	/**
	 * Detaches a entity from this entity.
	 * 
	 * @param child
	 *            The entity you want to detach from this entity.
	 * @return true if the entity was a child of this entity and is detached
	 *         successfully, otherwise false.
	 */
	@SuppressWarnings("unchecked")
	public boolean detach(Entity child) {
		if (child == null) {
			throw new NullPointerException("child");
		} else if (child == this) {
			throw new IllegalArgumentException("You cannot detach an entity "
					+ "from it self");
		} else if (child.parent != this) {
			return false;
		}

		// Lookup
		Set<Entity> entities = children.get(child.index());

		// Check
		if (entities == null) {
			throw new IllegalStateException("The entity set of the given "
					+ "child order does not exist. Please check your code.");
		} else if (entities.remove(child)) {

			// Clean up
			removeSetIfEmpty(child.index(), entities);

			// Clear parent
			child.parent = null;

			// Cache is invalid now
			clearCachedChildren();

			/*
			 * Is this entity configured to fire detach events ?
			 */
			if (detachEvents) {
				// Fire the detached event recursive
				fireEvent(
						// Use composite itr
						new CompositeIterator<Entity>(
						// Notify THIS tree
								iterableEventEntities.iterator(),
								// Notify the child tree
								child.iterableEventEntities.iterator()),
						EntityEvent.Detached, this, child);
			}

			return true;
		} else {
			throw new IllegalStateException("This entity could not remove "
					+ "the given child from its set. Please check your code.");
		}
	}

	/**
	 * 
	 * @return the iterable event entities.
	 */
	public Iterable<? extends Entity> iterableEventEntities() {
		return iterableEventEntities;
	}

	/**
	 * Sets the iterable event entities.
	 * 
	 * @param iterableEventEntities
	 *            The new iterable event entities.
	 * @return this for chaining.
	 */
	public Entity iterableEventEntities(
			Iterable<? extends Entity> iterableEventEntities) {
		if (iterableEventEntities == null) {
			throw new NullPointerException("iterableEventEntities");
		}

		this.iterableEventEntities = iterableEventEntities;
		return this;
	}

	/**
	 * Updates this entity and all children.
	 * 
	 * @param tpf
	 *            The passed time to update time-sensitive data.
	 * @return this for chaining.
	 */
	public Entity update(float tpf) {
		return update(null, tpf);
	}

	/**
	 * Updates this entity and all children.
	 * 
	 * @param EntityFilter
	 *            The entity filter or null.
	 * @param tpf
	 *            The passed time to update time-sensitive data.
	 * @return this for chaining.
	 */
	public Entity update(EntityFilter entityFilter, float tpf) {

		// Fire event for all children
		fireEvent(new FilteredIterator<Entity>(entityFilter,
				iterableEventEntities.iterator()), EntityEvent.Update, tpf);
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Entity o) {
		if (index > o.index) {
			return 1;
		} else if (index == o.index) {
			return 0;
		} else {
			return -1;
		}
	}

	@Override
	public String toString() {
		return "[Name = " + name + ", Index = " + index + "]";
	}
}
