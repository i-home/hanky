package hanky.text;

import static hanky.text.ThreadSafeProxyFactory.safe;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import net.jodah.concurrentunit.ConcurrentTestCase;

class ThreadSafeProxyFactoryTest extends ConcurrentTestCase {

	private static final int NUM_OF_THREADS = 10;
	private static final String DD_MM_YYYY = "dd.MM.yyyy";

	@Test
	@DisplayName("Test weak SimpleDateFormat")
	void testWeakSimpleDateFormat() throws TimeoutException {
		testWeak(new SimpleDateFormat(DD_MM_YYYY), new Supplier<Date>() {
			@Override
			public Date get() {
				return new Date();
			}
		});
	}

	@Test
	@DisplayName("Test safe SimpleDateFormat")
	void testSafeSimpleDateFormat() throws TimeoutException, InterruptedException, ExecutionException {
		testSafe(new SimpleDateFormat(DD_MM_YYYY), new Supplier<Date>() {
			@Override
			public Date get() {
				return new Date();
			}
		});
	}

	@Test
	@DisplayName("Test safeUnLocked SimpleDateFormat")
	void testSafeUnLockedSimpleDateFormat() throws TimeoutException, InterruptedException, ExecutionException {
		testSafeUnLocked(new SimpleDateFormat(DD_MM_YYYY), new Supplier<Date>() {
			@Override
			public Date get() {
				return new Date();
			}
		}, new Consumer<SimpleDateFormat>() {
			@Override
			public void accept(SimpleDateFormat format) {
				format.setNumberFormat(new DecimalFormat("#,##0.00"));
			}
		});
	}

	@Test
	@DisplayName("Test safeLocked SimpleDateFormat")
	void testSafeLockedSimpleDateFormat() throws TimeoutException, InterruptedException, ExecutionException {
		testSafeLocked(new SimpleDateFormat(DD_MM_YYYY), new Supplier<Date>() {
			@Override
			public Date get() {
				return new Date();
			}
		}, new Consumer<SimpleDateFormat>() {
			@Override
			public void accept(SimpleDateFormat format) {
				format.setNumberFormat(new DecimalFormat("#,##0.00"));
			}
		});
	}

	@Test
	@DisplayName("Test safeLocked by method SimpleDateFormat")
	void testSafeLockedByMethodSimpleDateFormat() throws TimeoutException, InterruptedException, ExecutionException {
		testSafeLockedByMethod(new SimpleDateFormat(DD_MM_YYYY), new Supplier<Date>() {
			@Override
			public Date get() {
				return new Date();
			}
		}, new Consumer<SimpleDateFormat>() {
			@Override
			public void accept(SimpleDateFormat format) {
				format.setNumberFormat(new DecimalFormat("#,##0.00"));
			}
		});
	}

	@Test
	@DisplayName("Test weak DecimalFormat")
	void testWeakDecimalFormat() throws TimeoutException {
		testWeak(new DecimalFormat("#,##0.00"), new Supplier<Double>() {
			@Override
			public Double get() {
				return 1234.56;
			}
		});
	}

	@Test
	@DisplayName("Test safe DecimalFormat")
	void testSafeDecimalFormat() throws TimeoutException, InterruptedException, ExecutionException {
		testSafe(new DecimalFormat("#,##0.00"), new Supplier<Double>() {
			@Override
			public Double get() {
				return 1234.56;
			}
		});
	}

	@Test
	@DisplayName("Test safeUnLocked DecimalFormat")
	void testSafeUnLockedDecimalFormat() throws TimeoutException, InterruptedException, ExecutionException {
		testSafeUnLocked(new DecimalFormat("#,##0.00"), new Supplier<Double>() {
			@Override
			public Double get() {
				return 1234.56;
			}
		}, new Consumer<DecimalFormat>() {
			@Override
			public void accept(DecimalFormat format) {
				format.setGroupingSize(2);
			}
		});
	}

	@Test
	@DisplayName("Test safeLocked DecimalFormat")
	void testSafeLockedDecimalFormat() throws TimeoutException, InterruptedException, ExecutionException {
		testSafeLocked(new DecimalFormat("#,##0.00"), new Supplier<Double>() {
			@Override
			public Double get() {
				return 1234.56;
			}
		}, new Consumer<DecimalFormat>() {
			@Override
			public void accept(DecimalFormat format) {
				format.setGroupingSize(2);
			}
		});
	}

	@Test
	@DisplayName("Test safeLocked by method DecimalFormat")
	void testSafeLockedByMethodDecimalFormat() throws TimeoutException, InterruptedException, ExecutionException {
		testSafeLockedByMethod(new DecimalFormat("#,##0.00"), new Supplier<Double>() {
			@Override
			public Double get() {
				return 1234.56;
			}
		}, new Consumer<DecimalFormat>() {
			@Override
			public void accept(DecimalFormat format) {
				format.setGroupingSize(2);
			}
		});
	}

	<T extends Format, D extends Object> void testWeak(final T weak, final Supplier<D> factory)
			throws TimeoutException {
		assertThrows(Exception.class, new Executable() {

			@Override
			public void execute() throws Throwable {
				executeTest(factory, null, weak);
			}
		});
	}

	<T extends Format, D extends Object> void testSafe(final T weak, final Supplier<D> factory)
			throws TimeoutException, InterruptedException, ExecutionException {
		final T wrapped = safe(weak);
		executeTest(factory, null, wrapped);
	}

	<T extends Format, D extends Object> void testSafeUnLocked(final T weak, final Supplier<D> factory,
			final Consumer<T> modifier) throws TimeoutException, InterruptedException, ExecutionException {
		assertThrows(Exception.class, new Executable() {

			@Override
			public void execute() throws Throwable {
				final T wrapped = safe(weak);
				executeTest(factory, modifier, wrapped);
			}
		});
	}

	<T extends Format, D extends Object> void testSafeLocked(final T weak, final Supplier<D> factory,
			final Consumer<T> modifier) throws TimeoutException, InterruptedException, ExecutionException {
		final T wrapped = safe(weak, true);
		executeTest(factory, modifier, wrapped);
	}

	<T extends Format, D extends Object> void testSafeLockedByMethod(final T weak, final Supplier<D> factory,
			final Consumer<T> modifier) throws TimeoutException, InterruptedException, ExecutionException {
		final T wrapped = safe(weak, false);
		if (wrapped instanceof ThreadSafeProxyFactory.Final)
			((ThreadSafeProxyFactory.Final) wrapped).lockSettings();
		executeTest(factory, modifier, wrapped);
	}

	protected <T extends Format, D> void executeTest(final Supplier<D> producer, final Consumer<T> modifier,
			final T formatter) throws InterruptedException, ExecutionException {
		ExecutorService pool = Executors.newFixedThreadPool(NUM_OF_THREADS);
		List<Future<?>> q = new ArrayList<Future<?>>();
		for (int i = 0; i < NUM_OF_THREADS * 1000; i++) {
			q.add(pool.submit(new Runnable() {
				@Override
				public void run() {
					String first = formatter.format(producer.get());
					if (modifier != null)
						modifier.accept(formatter);
					try {
						String second = formatter.format(formatter.parseObject(formatter.format(producer.get())));
						if (!first.equals(second))
							threadFail(MessageFormat.format("Got two different formatted values {0} <=> {1}", first,
									second));
					} catch (ParseException e) {
						threadFail(e);
					}
				}
			}));
		}
		for (Future<?> f : q)
			f.get();
	}
}
