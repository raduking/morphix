package org.morphix;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.morphix.Conversion.convertFrom;
import static org.morphix.Conversion.convertFromIterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.morphix.annotation.Expandable;
import org.morphix.reflection.InstanceCreator;

/**
 * Tests conversions from {@link Iterable} to {@link Iterable}.
 *
 * @author Radu Sebastian LAZIN
 */
class ConverterIterableToIterableTest {

	private static final Long TEST_LONG = 17L;
	private static final String TEST_STRING = "17";

	private static final Integer TEST_INTEGER_1 = 7;
	private static final Integer TEST_INTEGER_2 = 13;

	private static final String TEST_STRING_1 = "7";
	private static final String TEST_STRING_2 = "13";

	public static class A {
		int x;
	}

	public static class B {
		String x;

		@Override
		public boolean equals(final Object obj) {
			// basic equals implementation
			if (null == obj)
				return false;
			return Objects.equals(x, ((B) obj).x);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}

	public static class Source {
		Long id;
		List<A> bees;
	}

	public static class Destination {
		String id;
		List<B> bees;

		public List<B> getBees() {
			return bees;
		}
	}

	@Test
	void shouldAutoConvertLists() {
		Source src = new Source();
		src.id = TEST_LONG;

		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		src.bees = List.of(a1, a2);

		Destination dst = convertFrom(src, Destination::new);

		assertThat(dst.id, equalTo(TEST_STRING));

		B b1 = new B();
		b1.x = TEST_STRING_1;
		B b2 = new B();
		b2.x = TEST_STRING_2;
		List<B> expectedBees = List.of(b1, b2);

		assertThat(dst.bees, equalTo(expectedBees));
	}

	@Test
	void shouldIgnoreNullsInSource() {
		Source src = new Source();

		Destination dst = convertFrom(src, Destination::new);
		assertThat(dst.bees, equalTo(null));
	}

	public static class C {
		int y;
	}

	public static class D {
		String y;

		@SuppressWarnings("unused")
		private D(final String x) {
			// empty
		}
	}

	public static class SourceFail {
		List<C> dees;
	}

	public static class DestinationFail {
		List<D> dees;

		public List<D> getDees() {
			return dees;
		}
	}

	@Test
	void shouldFailIfConstructorForListElementsIsUnavailable() {
		assumeFalse(InstanceCreator.getInstance().isUsable());

		SourceFail src = new SourceFail();

		C c1 = new C();
		c1.y = TEST_INTEGER_1;
		C c2 = new C();
		c2.y = TEST_INTEGER_2;
		src.dees = List.of(c1, c2);

		assertThrows(ConverterException.class, () -> convertFrom(src, DestinationFail::new));
	}

	@Test
	void shouldConvertEvenIfConstructorForListElementsIsUnavailableWhenInstanceCreatorIsUsable() {
		assumeTrue(InstanceCreator.getInstance().isUsable());

		SourceFail src = new SourceFail();

		C c1 = new C();
		c1.y = TEST_INTEGER_1;
		C c2 = new C();
		c2.y = TEST_INTEGER_2;
		src.dees = List.of(c1, c2);

		DestinationFail dst = convertFrom(src, DestinationFail::new);

		assertThat(dst.dees, hasSize(src.dees.size()));
	}

	public static class DestinationNoGetter {
		List<D> dees;
	}

	@Test
	void shouldNotConvertIterableIfDestinationDoesNotHaveGetterMethodOnList() {
		SourceFail src = new SourceFail();

		C c1 = new C();
		c1.y = TEST_INTEGER_1;
		C c2 = new C();
		c2.y = TEST_INTEGER_2;
		src.dees = List.of(c1, c2);

		DestinationNoGetter dst = convertFrom(src, DestinationNoGetter::new);

		assertThat(dst.dees, equalTo(null));
	}

	public static class SourceWithIterable {
		Iterable<Long> iterable;

		public Iterable<Long> getIterable() {
			return iterable;
		}
	}

	public static class DestinationWithIterable {
		Iterable<Long> iterable;

		public Iterable<Long> getIterable() {
			return iterable;
		}
	}

	@Test
	void shouldIgnoreDirectAssignmentOnIterableToIterable() {
		SourceWithIterable src = new SourceWithIterable();
		src.iterable = List.of(1L, 2L);

		DestinationWithIterable dst = convertFrom(src, DestinationWithIterable::new);

		assertThat(dst.iterable.getClass(), equalTo(dst.iterable.getClass()));
	}

	public static class DestinationWithArrayList {
		ArrayList<Long> iterable;

		public ArrayList<Long> getIterable() {
			return iterable;
		}
	}

	@Test
	void shouldNotIgnoreListSubclassesInDestination() {
		SourceWithIterable src = new SourceWithIterable();
		src.iterable = List.of(1L, 2L);

		DestinationWithArrayList dst = convertFrom(src, DestinationWithArrayList::new);

		assertThat(dst.iterable, notNullValue());
	}

	public static class E {
		int z;
	}

	public static abstract class F {
		String z;
	}

	public static class SourceWithList {
		List<E> fees;
	}

	public static class DestinationWithList {
		List<F> fees;

		public List<F> getFees() {
			return fees;
		}
	}

	@Test
	void shouldFailIfConstructorForListElementsFails() {
		SourceWithList src = new SourceWithList();

		E e1 = new E();
		e1.z = TEST_INTEGER_1;
		E e2 = new E();
		e2.z = TEST_INTEGER_2;
		src.fees = List.of(e1, e2);

		assertThrows(ConverterException.class, () -> convertFrom(src, DestinationWithList::new));
	}

	public static class DestinationWithGenerics<T> {
		List<T> iterable;

		public List<T> getIterable() {
			return iterable;
		}
	}

	@Test
	void shouldFailOnGenericTypedLists() {
		SourceWithIterable src = new SourceWithIterable();
		src.iterable = List.of(1L, 2L);

		assertThrows(ConverterException.class, () -> convertFrom(src, DestinationWithGenerics::new));
	}

	public static class DestinationWithoutGenerics {
		@SuppressWarnings("rawtypes")
		List iterable;

		@SuppressWarnings("rawtypes")
		public List getIterable() {
			return iterable;
		}
	}

	@Test
	void shouldFailOnRawTypedListsInDestination() {
		SourceWithIterable src = new SourceWithIterable();
		src.iterable = List.of(1L, 2L);

		DestinationWithoutGenerics dst = convertFrom(src, DestinationWithoutGenerics::new);

		assertThat(dst.iterable, nullValue());
	}

	public static class SrcBase {
		List<String> integers;

		void setIntegers(final List<String> integers) {
			this.integers = integers;
		}

		List<String> getIntegers() {
			return integers;
		}
	}

	public static class SrcDerived extends SrcBase {
		// empty
	}

	public static class DstDerived {
		List<Integer> integers;

		List<Integer> getIntegers() {
			return integers;
		}

		void setIntegers(final List<Integer> integers) {
			this.integers = integers;
		}
	}

	@Test
	void shouldCallGetterOnIterablesInBaseClass() {
		SrcDerived src = new SrcDerived();
		src.integers = List.of(TEST_STRING_1, TEST_STRING_2);

		DstDerived dst = convertFrom(src, DstDerived::new);

		assertThat(dst.integers, equalTo(List.of(TEST_INTEGER_1, TEST_INTEGER_2)));
	}

	public static class SrcWithExpandable {
		@Expandable
		List<String> strings;

		@Expandable
		List<Integer> integers;

		List<Long> longs;

		List<String> getStrings() {
			return strings;
		}

		List<Integer> getIntegers() {
			return integers;
		}

		List<Long> getLongs() {
			return longs;
		}
	}

	public static class DstWithExpandable {
		List<Integer> strings;

		List<String> integers;

		List<String> longs;

		List<Integer> getStrings() {
			return strings;
		}

		List<String> getIntegers() {
			return integers;
		}

		List<String> getLongs() {
			return longs;
		}
	}

	@Test
	void shouldNotConvertExpandableFieldsIfTheyAreSpecified() {
		SrcWithExpandable src = new SrcWithExpandable();
		src.strings = new ArrayList<>();
		src.integers = new ArrayList<>();
		int size = 2;
		for (int i = 0; i < size; ++i) {
			src.strings.add(String.valueOf(i));
			src.integers.add(i);
		}

		List<String> expandedFields = List.of("strings");
		DstWithExpandable dst = convertFrom(src, DstWithExpandable::new, expandedFields);

		assertThat(dst.strings, hasSize(size));
		assertThat(dst.integers, hasSize(0));

		for (int i = 0; i < size; ++i) {
			assertThat(dst.strings.get(i), equalTo(i));
		}
	}

	@Test
	void shouldConvertExpandableFieldsIfExpandedFieldNamesIsNull() {
		SrcWithExpandable src = new SrcWithExpandable();
		src.strings = new ArrayList<>();
		src.integers = new ArrayList<>();
		int size = 2;
		for (int i = 0; i < size; ++i) {
			src.strings.add(String.valueOf(i));
			src.integers.add(i);
		}

		DstWithExpandable dst = convertFrom(src, DstWithExpandable::new, (List<String>) null);

		assertThat(dst.strings, hasSize(size));
		assertThat(dst.integers, hasSize(size));

		for (int i = 0; i < size; ++i) {
			assertThat(dst.integers.get(i), equalTo(String.valueOf(i)));
			assertThat(dst.strings.get(i), equalTo(i));
		}
	}

	@Test
	void shouldNotConvertIterableExpandableFields() {
		List<SrcWithExpandable> srcList = new ArrayList<>();
		int srcSize = 2;
		int size = 2;
		for (int j = 0; j < srcSize; ++j) {
			SrcWithExpandable src = new SrcWithExpandable();
			src.strings = new ArrayList<>();
			src.integers = new ArrayList<>();
			for (int i = 0; i < size; ++i) {
				src.strings.add(String.valueOf(i));
				src.integers.add(i);
			}
			srcList.add(src);
		}

		List<String> expandedFields = List.of("strings");
		List<DstWithExpandable> dstList = convertFromIterable(srcList, DstWithExpandable::new, expandedFields);

		assertThat(dstList, hasSize(srcSize));
		for (DstWithExpandable dst : dstList) {
			assertThat(dst.strings, hasSize(size));
			assertThat(dst.integers, hasSize(0));
		}
	}

	public static class Src {
		Long id;
		List<A> as;
	}

	public static class Dst {
		String id;
		List<A> as;

		public List<A> getAs() {
			return as;
		}
	}

	@Test
	void shouldInstantiateNewArrayListForLists() {
		Src src = new Src();
		src.id = TEST_LONG;

		A a1 = new A();
		a1.x = TEST_INTEGER_1;
		A a2 = new A();
		a2.x = TEST_INTEGER_2;
		src.as = List.of(a1, a2);

		Dst dst = convertFrom(src, Dst::new);

		assertThat(dst.as != src.as, equalTo(true));
	}

	public static enum EString {
		A,
		B,
		C
	}

	public static class SrcListString {
		List<String> as;
	}

	public static class DstListString {
		List<EString> as;

		public List<EString> getAs() {
			return as;
		}
	}

	@Test
	void shouldConvertListStringsToEnums() {
		SrcListString src = new SrcListString();

		src.as = List.of("A", "C");

		DstListString dst = convertFrom(src, DstListString::new);

		assertThat(dst.as, hasSize(src.as.size()));
		for (int i = 0; i < src.as.size(); ++i) {
			EString e = dst.as.get(i);
			assertThat(e, notNullValue());
			assertThat(e, equalTo(EString.valueOf(src.as.get(i))));
		}
	}

	public static class SrcWithCollection {
		Collection<Integer> coll;
	}

	public static class DstWithCollection {
		Collection<String> coll;

		public Collection<String> getColl() {
			return coll;
		}
	}

	@Test
	void shouldConvertCollections() {
		// any collection should be ArrayList compatible
		SrcWithCollection src = new SrcWithCollection();
		src.coll = new ArrayList<>();
		int size = 2;
		for (int i = 0; i < size; ++i) {
			src.coll.add(i);
		}

		DstWithCollection dst = convertFrom(src, DstWithCollection::new);

		Iterator<String> it = dst.coll.iterator();
		for (int i = 0; i < size; ++i) {
			String s = it.next();
			assertThat(s, equalTo(String.valueOf(i)));
		}
	}
}
