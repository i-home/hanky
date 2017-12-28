package hanky.text.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.text.Format;
import java.text.SimpleDateFormat;

import org.junit.jupiter.api.Test;

import hanky.text.ThreadSafeProxyFactory.Final;

class FormatterMethodsInterceptorTest {


	@SuppressWarnings("unused")
	private class TestSetters {
		private int value;

		public TestSetters(int value) {
			super();
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

	}

	@Test
	void testIsFormatMethod() {
		for (Method method : Format.class.getDeclaredMethods()) {
			if (method.getName().startsWith("format") || method.getName().startsWith("parse")) {
				assertEquals(true, new FormatterMethodsInterceptor<SimpleDateFormat>(new SimpleDateFormat(), false)
						.isFormatMethod(method));
			}
		}
	}

	@Test
	void testIsLockMethod() {
		Method method = Final.class.getDeclaredMethods()[0];
		assertEquals(true,
				new FormatterMethodsInterceptor<SimpleDateFormat>(new SimpleDateFormat(), false).isLockMethod(method));
	}

	@Test
	void testIsSetter() {
		Method method = TestSetters.class.getDeclaredMethods()[1];
		assertEquals("setValue", method.getName());
		assertEquals(true,
				new FormatterMethodsInterceptor<SimpleDateFormat>(new SimpleDateFormat(), false).isSetter(method));
	}
}
