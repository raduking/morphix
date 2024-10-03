package org.morphix.reflection.jvm;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;

import org.morphix.reflection.AccessSetter;
import org.morphix.reflection.Constructors;
import org.morphix.reflection.Fields;
import org.morphix.reflection.Reflection;

import sun.misc.Unsafe; // NOSONAR

/**
 * This class is used to make accessible some methods which normally are not accessible.
 * From Java 12, {@link AccessibleObject}.override was added to the reflection blacklist.
 * Access modification can still be circumvented by using the Unsafe technique to get the
 * implementation lookup from {@link MethodHandles}
 *
 * @param <T> accessible object type
 *
 * @author Radu Sebastian LAZIN
 */
public class MemberAccessorOracleJDK<T extends AccessibleObject & Member> implements AutoCloseable {

	protected static final String FIELD_NAME_THE_UNSAFE = "theUnsafe";
	protected static final String FIELD_NAME_IMPL_LOOKUP = "IMPL_LOOKUP";

	private static MethodHandle overrideSetter = null;

	static {
		initialize(FIELD_NAME_THE_UNSAFE);
	}

	private final AccessSetter<T> accessSetter;
	private final T member;

	protected static void initialize(final String unsafeFieldName) {
		try {
			Unsafe unsafe = Fields.getIgnoreAccess(null, Unsafe.class.getDeclaredField(unsafeFieldName));

			// Unsafe.staticFieldOffset and Unsafe.staticFieldBase are now deprecated
			/*
			Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
			long implLookupFieldOffset = unsafe.staticFieldOffset(implLookupField);
			Object lookupStaticFieldBase = unsafe.staticFieldBase(implLookupField);
			MethodHandles.Lookup implLookup = (MethodHandles.Lookup) unsafe.getObject(lookupStaticFieldBase, implLookupFieldOffset);
			*/

			Class<MethodHandles.Lookup> lookupClass = MethodHandles.Lookup.class;
			Constructor<MethodHandles.Lookup> lookupConstructor = Constructors.getDeclaredConstructor(lookupClass, Class.class);
			MethodHandles.Lookup lookup = Constructors.newInstanceIgnoreAccess(lookupConstructor, lookupClass);

			VarHandle varHandle = lookup.findStaticVarHandle(lookupClass, FIELD_NAME_IMPL_LOOKUP, lookupClass);
			long fieldOffset = Fields.getIgnoreAccess(varHandle, "fieldOffset");
			MethodHandles.Lookup implLookup = Reflection.cast(unsafe.getObject(lookupClass, fieldOffset));

			overrideSetter = implLookup.findSetter(AccessibleObject.class, "override", boolean.class);
		} catch (Throwable swallow) { // NOSONAR
			// swallow exception since it just won't initialize the overrideSetter if
			// the actual JDK/JRE doesn't support it this way
		}
	}

	public MemberAccessorOracleJDK(final T member) {
		this.member = requireNonNull(member, "member");
		accessSetter = AccessSetter.ofOverride(overrideSetter);
		accessSetter.setAccessible(member, true);
	}

	@Override
	public void close() {
		accessSetter.setAccessible(member, false);
	}

}
