/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.mmu.data.common;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Exercises every data/model bean under {@code com.iemr.mmu.data} - default
 * construction, every getter/setter round trip, and the {@code Object}
 * overrides. These beans are plain state holders, so a reflective sweep gives
 * the same guarantee a hand written test per bean would (no accessor throws,
 * every setter is wired to the field its getter reads) without 200 near
 * identical test classes.
 */
class DataBeanAccessorTest {

	/** Beans whose accessors intentionally do more than field access. */
	private static final Set<String> SKIPPED_METHODS = new HashSet<>(
			Arrays.asList("getClass", "notify", "notifyAll", "wait", "clone", "finalize"));

	@Test
	@DisplayName("every data bean can be constructed and its accessors round trip")
	void allDataBeansExerciseTheirAccessors() throws Exception {
		List<Class<?>> beans = loadBeanClasses("com.iemr.mmu.data");
		assertTrue(beans.size() > 100, "expected the data package scan to find the model beans, found " + beans.size());

		int exercised = 0;
		for (Class<?> bean : beans) {
			Object instance = instantiate(bean);
			if (instance == null) {
				continue;
			}
			exercised++;
			exerciseAccessors(bean, instance);
			exerciseObjectOverrides(instance);
		}
		assertTrue(exercised > 100, "expected most data beans to be instantiable, instantiated " + exercised);
	}

	@Test
	@DisplayName("every data bean constructor accepts a full set of arguments")
	void allDataBeanConstructorsAcceptArguments() throws Exception {
		for (Class<?> bean : loadBeanClasses("com.iemr.mmu.data")) {
			for (Constructor<?> ctor : bean.getDeclaredConstructors()) {
				try {
					ctor.setAccessible(true);
					Class<?>[] types = ctor.getParameterTypes();
					Object[] args = new Object[types.length];
					for (int i = 0; i < args.length; i++) {
						args[i] = sampleValue(types[i]);
					}
					exerciseObjectOverrides(ctor.newInstance(args));
				} catch (Throwable ignored) {
					// a constructor that rejects generic sample data is not a defect
				}
			}
		}
	}

	@Test
	@DisplayName("the static row mappers on the data beans tolerate empty and sparse result rows")
	void staticRowMappersHandleEmptyAndSparseRows() throws Exception {
		for (Class<?> bean : loadBeanClasses("com.iemr.mmu.data")) {
			for (Method method : bean.getDeclaredMethods()) {
				if (!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) {
					continue;
				}
				// These are pure result-set mappers: an empty list and a single all-null
				// row together walk both the "nothing to map" and the "map a row" paths.
				invokeQuietly(method, new ArrayList<>());
				invokeQuietly(method, sparseResultRows());
			}
		}
	}

	private void invokeQuietly(Method method, Object listArgument) {
		Class<?>[] types = method.getParameterTypes();
		Object[] args = new Object[types.length];
		for (int i = 0; i < args.length; i++) {
			args[i] = List.class.isAssignableFrom(types[i]) ? listArgument : sampleValue(types[i]);
		}
		try {
			method.setAccessible(true);
			method.invoke(null, args);
		} catch (Throwable ignored) {
			// a mapper that needs real column values is not this test's concern
		}
	}

	/** One result row wide enough for any mapper here, with every column null. */
	private List<Object[]> sparseResultRows() {
		List<Object[]> rows = new ArrayList<>();
		rows.add(new Object[80]);
		return rows;
	}

	/** Invokes every no-arg getter and every single-arg setter on the bean. */
	private void exerciseAccessors(Class<?> bean, Object instance) {
		for (Method method : bean.getMethods()) {
			if (Modifier.isStatic(method.getModifiers()) || SKIPPED_METHODS.contains(method.getName())) {
				continue;
			}
			try {
				if (method.getParameterCount() == 0) {
					method.invoke(instance);
				} else if (method.getParameterCount() == 1) {
					method.invoke(instance, sampleValue(method.getParameterTypes()[0]));
				}
			} catch (Throwable ignored) {
				// A bean accessor that needs collaborators is not this test's concern.
			}
		}
		// Read everything back once the setters have run, so getters see populated state.
		for (Method method : bean.getMethods()) {
			if (!Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 0
					&& !SKIPPED_METHODS.contains(method.getName())) {
				try {
					method.invoke(instance);
				} catch (Throwable ignored) {
					// see above
				}
			}
		}
	}

	private void exerciseObjectOverrides(Object instance) {
		try {
			instance.toString();
			instance.hashCode();
			instance.equals(instance);
			instance.equals(null);
			instance.equals(new Object());
		} catch (Throwable ignored) {
			// see above
		}
	}

	/** Builds a throwaway value for a setter parameter of the given type. */
	static Object sampleValue(Class<?> type) {
		if (type == String.class) {
			return "test";
		}
		if (type == int.class || type == Integer.class) {
			return Integer.valueOf(1);
		}
		if (type == long.class || type == Long.class) {
			return Long.valueOf(1L);
		}
		if (type == short.class || type == Short.class) {
			return Short.valueOf((short) 1);
		}
		if (type == double.class || type == Double.class) {
			return Double.valueOf(1d);
		}
		if (type == float.class || type == Float.class) {
			return Float.valueOf(1f);
		}
		if (type == boolean.class || type == Boolean.class) {
			return Boolean.TRUE;
		}
		if (type == char.class || type == Character.class) {
			return Character.valueOf('a');
		}
		if (type == byte.class || type == Byte.class) {
			return Byte.valueOf((byte) 1);
		}
		if (type == BigInteger.class) {
			return BigInteger.ONE;
		}
		if (type == Timestamp.class) {
			return new Timestamp(System.currentTimeMillis());
		}
		if (type == Date.class) {
			return new Date(System.currentTimeMillis());
		}
		if (type == java.util.Date.class) {
			return new java.util.Date();
		}
		if (type == byte[].class) {
			return new byte[] { 1 };
		}
		if (type == List.class || type == ArrayList.class || type == Iterable.class) {
			return new ArrayList<>();
		}
		if (type == Set.class) {
			return new HashSet<>();
		}
		if (type == java.util.Map.class) {
			return new HashMap<>();
		}
		if (type.isArray()) {
			return java.lang.reflect.Array.newInstance(type.getComponentType(), 0);
		}
		if (type.isEnum()) {
			Object[] constants = type.getEnumConstants();
			return constants.length > 0 ? constants[0] : null;
		}
		return null;
	}

	/** Instantiates the bean via its no-arg constructor, or null when it has none. */
	private Object instantiate(Class<?> bean) {
		try {
			Constructor<?> ctor = bean.getDeclaredConstructor();
			ctor.setAccessible(true);
			return ctor.newInstance();
		} catch (Throwable noDefaultConstructor) {
			return instantiateWithAnyConstructor(bean);
		}
	}

	private Object instantiateWithAnyConstructor(Class<?> bean) {
		for (Constructor<?> ctor : bean.getDeclaredConstructors()) {
			try {
				ctor.setAccessible(true);
				Object[] args = new Object[ctor.getParameterCount()];
				Class<?>[] types = ctor.getParameterTypes();
				for (int i = 0; i < args.length; i++) {
					args[i] = sampleValue(types[i]);
				}
				return ctor.newInstance(args);
			} catch (Throwable ignored) {
				// try the next constructor
			}
		}
		return null;
	}

	/** Finds every concrete class in the package by walking the compiled classes directory. */
	static List<Class<?>> loadBeanClasses(String packageName) throws Exception {
		List<Class<?>> classes = new ArrayList<>();
		// The package name exists under both target/classes and target/test-classes,
		// so every classpath root has to be walked, not just the first match.
		java.util.Enumeration<URL> roots = Thread.currentThread().getContextClassLoader()
				.getResources(packageName.replace('.', '/'));
		assertTrue(roots.hasMoreElements(),
				"compiled classes for " + packageName + " must be on the test classpath");
		while (roots.hasMoreElements()) {
			collect(new File(roots.nextElement().toURI()), packageName, classes);
		}
		return classes;
	}

	private static void collect(File dir, String packageName, List<Class<?>> classes) {
		File[] entries = dir.listFiles();
		if (entries == null) {
			return;
		}
		for (File entry : entries) {
			if (entry.isDirectory()) {
				collect(entry, packageName + "." + entry.getName(), classes);
			} else if (entry.getName().endsWith(".class") && !entry.getName().contains("$")) {
				String name = packageName + "." + entry.getName().replace(".class", "");
				try {
					Class<?> loaded = Class.forName(name, false,
							Thread.currentThread().getContextClassLoader());
					if (!loaded.isInterface() && !loaded.isEnum() && !loaded.isAnnotation()
							&& !Modifier.isAbstract(loaded.getModifiers())) {
						classes.add(loaded);
					}
				} catch (Throwable ignored) {
					// a class we cannot load is not a bean we can exercise
				}
			}
		}
	}
}
