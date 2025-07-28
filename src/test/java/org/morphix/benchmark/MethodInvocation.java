package org.morphix.benchmark;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.morphix.invoke.HandleMethods;
import org.morphix.reflection.Methods;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class MethodInvocation {

	static class A {

		public Integer sum(final Integer x, final int y) {
			return x + y;
		}

	}

	private A a;

	@Setup
	public void setup() {
		a = new A();
	}

    @Benchmark
    public Integer testMethodsInvoke() {
    	Method method = Methods.getSafeDeclaredMethodInHierarchy("sum", A.class, Integer.class, int.class);

    	return Methods.IgnoreAccess.invoke(method, a, 10, 20);
    }

    @Benchmark
    public Integer testHandleMethodsInvoke() {
    	MethodHandle method = HandleMethods.getMethod("sum", A.class, Integer.class, Integer.class, int.class);

    	return HandleMethods.invoke(method, a, 10, 10);
    }
}
