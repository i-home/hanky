package hanky.text;

import java.text.Format;

import hanky.text.interceptor.FormatterMethodsInterceptor;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * @author i-home aka Silver mr.kmets@gmail.com
 *
 */
public class ThreadSafeProxyFactory {

	/**
	 * threadsafe formatter implements this interface when it can be locked from
	 * changes
	 */
	public static interface Final {
		/**
		 * lock threadsafe formatter implementation from changes
		 */
		void lockSettings();
	}
	
	/**
	 * @param <T>
	 *            formatter implementation type
	 * @param weak:
	 *            java.text.Format subclass instance
	 * @return threadsafe version of input formatter
	 */
	@SuppressWarnings("unchecked")
	public static final <T extends Format> T safe(final T weak) {
		Enhancer factory = new Enhancer();
		factory.setSuperclass(weak.getClass());
		factory.setCallback(getCallback(weak, false));
		return (T) factory.create();
	}

	/**
	 * @param <T>
	 *            formatter implementation type
	 * @param weak:
	 *            java.text.Format subclass instance
	 * @param locked:
	 *            if true lock properties of constructed proxy from changes
	 *            immediate
	 * @return threadsafe version of input formatter with Final interface
	 *         implementation
	 */
	@SuppressWarnings("unchecked")
	public static final <T extends Format> T safe(final T weak, final boolean locked) {
		Enhancer factory = new Enhancer();
		factory.setSuperclass(weak.getClass());
		factory.setCallback(getCallback(weak, locked));
		factory.setInterfaces(new Class[] { Final.class });
		return (T) factory.create();
	}

	protected static <T extends Format> MethodInterceptor getCallback(final T weak, final boolean locked) {
		return new FormatterMethodsInterceptor<T>(weak, locked);
	}
}
