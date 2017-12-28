package hanky.text.interceptor;

import java.lang.reflect.Method;
import java.text.Format;
import java.util.concurrent.atomic.AtomicLong;

import hanky.text.ThreadSafeProxyFactory.Final;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @author i-home aka Silver mr.kmets@gmail.com
 * 
 * format() and parse() methods interceptor
 * 
 * @param <T> concrete Format type
 */
@SuppressWarnings("unchecked")
public class FormatterMethodsInterceptor<T extends Format> implements MethodInterceptor {

	private static final String PARSE_METHOD_PREFIX = "parse";
	private static final String FORMAT_METHOD_PREFIX = "format";
	private static final String SETTER_PREFFIX = "set";
	private static final long INITIAL_VERSION = 1l;
	private static final long LOCKED = -1l;

	private final T weak;
	private final ThreadLocal<T> insatnce = new ThreadLocal<T>();
	private final ThreadLocal<Long> actual = new ThreadLocal<Long>();
	private final AtomicLong version;

	/**
	 * @param weak original formatter
	 * @param locked if true immediate block formatter setters
	 */
	public FormatterMethodsInterceptor(T weak, boolean locked) {
		this.weak = weak;
		version = new AtomicLong(locked ? LOCKED : INITIAL_VERSION);
	}

	/* (non-Javadoc)
	 * @see net.sf.cglib.proxy.MethodInterceptor#intercept(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], net.sf.cglib.proxy.MethodProxy)
	 */
	@Override
	public Object intercept(Object arg0, Method method, Object[] args, MethodProxy mps) throws Throwable {
		if (isLockMethod(method)) {
			lock();
			return null;
		} else if (isSetter(method)) {
			if (isLocked()) {
				return null;
			} else {
				incrementVersion();
			}
		} else if (isFormatMethod(method)) {
			if (isActualVersion()) {
				if (insatnce.get() == null) {
					insatnce.set((T) weak.clone());
				}
			} else {
				actual.set(version.get());
				insatnce.set((T) weak.clone());
			}
			return method.invoke(insatnce.get(), args);
		}
		return method.invoke(weak, args);
	}

	protected boolean isActualVersion() {
		return Long.valueOf(version.get()).equals(actual.get());
	}

	protected void incrementVersion() {
		if (version.incrementAndGet() < LOCKED)
			version.set(INITIAL_VERSION);
	}

	protected boolean isLocked() {
		return version.get() == LOCKED;
	}

	protected void lock() {
		version.set(LOCKED);
	}

	protected boolean isSetter(Method method) {
		return method.getName().startsWith(SETTER_PREFFIX);
	}

	protected boolean isFormatMethod(Method method) {
		String name = method.getName();
		return name.startsWith(FORMAT_METHOD_PREFIX) || name.startsWith(PARSE_METHOD_PREFIX);
	}

	protected boolean isLockMethod(Method method) {
		boolean result = false;
		for (Method m : Final.class.getDeclaredMethods())
			result |= m.equals(method);
		return result;
	}
}