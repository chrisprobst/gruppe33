/*
 * Copyright (C) 2011 Christopher Probst
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of the 'FoxNet RMI' nor the names of its 
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.foxnet.rmi.registry;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.foxnet.rmi.Remote;
import com.foxnet.rmi.binding.DynamicBinding;
import com.foxnet.rmi.binding.RemoteObject;

/**
 * @author Christopher Probst
 */
public final class DynamicRegistry extends Registry<DynamicBinding> {

	// Used to store the ids
	private final Map<Integer, DynamicBinding> ids = new HashMap<Integer, DynamicBinding>();

	// Used to store the dynamic objects
	private final Map<Remote, DynamicBinding> objects = new IdentityHashMap<Remote, DynamicBinding>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.foxnet.rmi.binding.registry.AbstractRegistry#getIndexMap()
	 */
	@Override
	protected Map<Integer, DynamicBinding> getIndexMap() {
		return ids;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.foxnet.rmi.registry.Registry#unbindAll()
	 */
	@Override
	public synchronized void unbindAll() {
		// Unbound all
		for (Remote target : objects.keySet()) {
			notifyTargetUnboundFrom(target);
		}

		// Clear the remaining maps
		objects.clear();
		ids.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.foxnet.rmi.registry.Registry#unbind(int)
	 */
	@Override
	public synchronized DynamicBinding unbind(int id) {
		// Remove from id map
		DynamicBinding db = ids.remove(id);

		if (db != null) {
			// Remove from object map
			objects.remove(db.getTarget());

			// Notify
			notifyTargetUnboundFrom(db.getTarget());
		}
		return db;
	}

	public synchronized DynamicBinding unbind(Object target) {
		// Remove from object map
		DynamicBinding db = objects.remove(target);

		if (db != null) {
			// Remove from id map
			ids.remove(db.getId());

			// Notify
			notifyTargetUnboundFrom(db.getTarget());
		}
		return db;
	}

	public synchronized DynamicBinding get(Remote target) {
		return objects.get(target);
	}

	public Object replaceRemoteObject(Object target) {
		if (!(target instanceof Remote)) {
			return target;
		}

		// Try to find binding
		DynamicBinding db = bindIfAbsent((Remote) target);

		// Get remote binding
		return new RemoteObject(db.getId(), db.getInterfaces());
	}

	public synchronized Object[] replaceRemoteObjects(Object[] arguments) {
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				// Get the reference
				arguments[i] = replaceRemoteObject(arguments[i]);
			}
		}

		return arguments;
	}

	public synchronized DynamicBinding bindIfAbsent(Remote target) {
		if (target == null) {
			throw new NullPointerException("target");
		}

		// Get old binding...
		DynamicBinding binding = objects.get(target);

		// Is there an old binding ?
		if (binding != null) {
			return binding;
		}

		// Create new temp object binding
		binding = new DynamicBinding(findNextId(), (Remote) target);

		// Put into maps
		objects.put(target, binding);
		ids.put(binding.getId(), binding);

		// Notify the target
		notifyTargetBoundTo(target);

		return binding;
	}
}