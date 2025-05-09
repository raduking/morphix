/*
 * Copyright 2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.morphix.reflection;

import java.lang.reflect.Member;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

/**
 * Constant pool wrapper class.
 *
 * @param <T> type of the class
 *
 * @author Radu Sebastian LAZIN
 */
public class ConstantPool<T> implements Iterable<Member> {

	/**
	 * Target class for this constant pool.
	 */
	private final Class<T> targetClass;

	/**
	 * Java constant pool.
	 */
	private final Object constantPoolObject;

	/**
	 * Constant Pool accessor.
	 */
	private final ConstantPoolAccessor constantPoolAccessor;

	/**
	 * Default constant pool accessor instance function.
	 */
	private static Supplier<ConstantPoolAccessor> instanceFunction = ConstantPoolAccessor::getInstance;

	/**
	 * Constructor.
	 *
	 * @param targetClass class for which to get the constant pool
	 */
	public ConstantPool(final Class<T> targetClass) {
		this(targetClass, instanceFunction);
	}

	/**
	 * Constructor.
	 *
	 * @param targetClass class for which to get the constant pool
	 * @param constantPoolAccessorInstanceFn constant pool accessor instance function
	 */
	protected ConstantPool(final Class<T> targetClass, final Supplier<ConstantPoolAccessor> constantPoolAccessorInstanceFn) {
		this.targetClass = targetClass;
		this.constantPoolAccessor = constantPoolAccessorInstanceFn.get();
		this.constantPoolObject = this.constantPoolAccessor.getConstantPool(targetClass);
	}

	/**
	 * Returns the size of the constant pool.
	 *
	 * @return the size of the constant pool
	 */
	public int getSize() {
		return constantPoolAccessor.getSize(constantPoolObject);
	}

	/**
	 * Returns the constant member at the given index from the constant pool.
	 *
	 * @param index index of the constant needed
	 * @return the constant member
	 */
	public Member getMemberAt(final int index) {
		return constantPoolAccessor.getMethodAt(constantPoolObject, index);
	}

	/**
	 * Returns an iterator on this constant pool.
	 */
	@Override
	public Iterator<Member> iterator() {
		return new ConstantPoolIterator<>(this);
	}

	/**
	 * Returns the target class.
	 *
	 * @return the target class
	 */
	public Class<T> getTargetClass() {
		return targetClass;
	}

	/**
	 * Sets the insatnce function.
	 *
	 * @param instanceFunction constant pool accessor instance function
	 */
	protected static void setInstanceFunction(final Supplier<ConstantPoolAccessor> instanceFunction) {
		ConstantPool.instanceFunction = instanceFunction;
	}

	/**
	 * Constant Pool iterator.
	 *
	 * @param <T> type of the class for which this constant pool refers to
	 *
	 * @author Radu Sebastian LAZIN
	 */
	static class ConstantPoolIterator<T> implements Iterator<Member> {

		private final ConstantPool<T> constantPool;
		private final int size;
		private int index;

		public ConstantPoolIterator(final ConstantPool<T> constantPool) {
			this.constantPool = constantPool;
			this.size = constantPool.getSize();
			this.index = this.size - 1;
		}

		@Override
		public boolean hasNext() {
			return 0 <= index && index < size;
		}

		@Override
		public Member next() {
			Member result = constantPool.getMemberAt(index);
			if (nextIndex() < -1) {
				throw new NoSuchElementException();
			}
			return result;
		}

		int nextIndex() {
			return --index;
		}
	}
}
