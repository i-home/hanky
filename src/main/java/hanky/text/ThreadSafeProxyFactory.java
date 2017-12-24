package hanky.text;

import java.lang.reflect.Method;
import java.text.Format;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @author i-home aka Silver mr.kmets@gmail.com
 *
 */
public class ThreadSafeProxyFactory {

	/**
	 * threadsafe formatter implements this interface
	 * when it can be locked from changes
	 */
	public interface Final {
		/**
		 * lock threadsafe formatter implementation from changes
		 */
		void lockSettings();
	}

	private static final String LOCK_SETTINGS_METHOD_NAME = "lockSettings";
	private static final String PARSE_METHOD_PREFIX = "parse";
	private static final String FORMAT_METHOD_PREFIX = "format";
	private static final String SETTER_PREFFIX = "set";
	private static final long INITIAL_VERSION = 1l;
	private static final long LOCKED = -1l;

	/**
	 * @param <T> formatter implementation type
	 * @param weak: java.text.Format subclass instance
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
	 * @param <T> formatter implementation type
	 * @param weak: java.text.Format subclass instance
	 * @param locked: if true lock properties of constructed proxy from changes immediate
	 * @return threadsafe version of input formatter with Final interface implementation
	 */
	@SuppressWarnings("unchecked")
	public static final <T extends Format> T safe(final T weak, final boolean locked) {
		Enhancer factory = new Enhancer();
		factory.setSuperclass(weak.getClass());
		factory.setCallback(getCallback(weak, locked));
		factory.setInterfaces(new Class[] { Final.class });
		return (T) factory.create();
	}

	@SuppressWarnings("unchecked")
	private static <T extends Format> MethodInterceptor getCallback(final T weak, final boolean locked) {
		return new MethodInterceptor() {
			private final ThreadLocal<T> insatnce = new ThreadLocal<T>();
			private final ThreadLocal<Long> actual = new ThreadLocal<Long>();
			private final AtomicLong version = new AtomicLong(locked ? LOCKED : INITIAL_VERSION);

			@Override
			public Object intercept(Object arg0, Method method, Object[] args, MethodProxy mps) throws Throwable {
				String name = method.getName();
				if (name.equals(LOCK_SETTINGS_METHOD_NAME)) {
					version.set(LOCKED);
					return null;
				} else if (name.startsWith(SETTER_PREFFIX)) {
					if (version.get() != LOCKED) {
						if (version.incrementAndGet() < LOCKED)
							version.set(INITIAL_VERSION);
						return method.invoke(weak, args);
					} else {
						return null;
					}
				} else if (name.startsWith(FORMAT_METHOD_PREFIX) || name.startsWith(PARSE_METHOD_PREFIX)) {
					Long ver = actual.get();
					if (ver != null && ver.longValue() == version.get()) {
						if (insatnce.get() == null) {
							T clone = (T) weak.clone();
							insatnce.set(clone);
						}
					} else {
						actual.set(version.get());
						T clone = (T) weak.clone();
						insatnce.set(clone);
					}
					return method.invoke(insatnce.get(), args);
				}
				return method.invoke(weak, args);
			}
		};
	}
}
