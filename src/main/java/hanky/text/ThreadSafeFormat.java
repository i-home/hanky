package hanky.text;

import java.lang.reflect.Method;
import java.text.Format;
import java.util.concurrent.atomic.AtomicLong;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class ThreadSafeFormat {

	private static final String PARSE_METHOD_PREFIX = "parse";
	private static final String FORMAT_METHOD_PREFIX = "format";
	private static final String SETTER_PREFFIX = "set";

	@SuppressWarnings("unchecked")
	public static final <T extends Format> T wrap(final T weakFormatter) {
		Enhancer factory = new Enhancer();
		factory.setSuperclass(weakFormatter.getClass());
		factory.setCallback(new MethodInterceptor() {
			private final ThreadLocal<T> insatnce = new ThreadLocal<T>();
			private final ThreadLocal<Long> actual = new ThreadLocal<Long>();
			private final AtomicLong version = new AtomicLong(1);

			@Override
			public Object intercept(Object arg0, Method method, Object[] args, MethodProxy mps) throws Throwable {
				String name = method.getName();
				if (name.startsWith(SETTER_PREFFIX)) {
					version.incrementAndGet();
				} else if (name.startsWith(FORMAT_METHOD_PREFIX) || name.startsWith(PARSE_METHOD_PREFIX)) {
					Long ver = actual.get();
					if (ver != null && ver.longValue() == version.get()) {
						if (insatnce.get() == null) {
							T clone = (T) weakFormatter.clone();
							insatnce.set(clone);
						}
					} else {
						actual.set(version.get());
						T clone = (T) weakFormatter.clone();
						insatnce.set(clone);
					}
					return method.invoke(insatnce.get(), args);
				}
				return method.invoke(weakFormatter, args);
			}
		});

		return (T) factory.create();
	}
}
