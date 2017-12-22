package hanky.text;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static hanky.text.ThreadSafeFormat.wrap;

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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.jodah.concurrentunit.ConcurrentTestCase;

class ThreadSafeFormatTest extends ConcurrentTestCase {

	private static final int NUM_OF_THREADS = 10;
	private static final String DD_MM_YYYY = "dd.MM.yyyy";

	@Test
	@DisplayName("Test weak formatter")
	void testWeak() throws TimeoutException {
		assertThrows(Exception.class, () -> {
			final SimpleDateFormat weak = new SimpleDateFormat(DD_MM_YYYY);
			ExecutorService pool = Executors.newFixedThreadPool(NUM_OF_THREADS);
			List<Future<?>> q = new ArrayList<>();
			for (int i = 0; i < NUM_OF_THREADS * 1000; i++) {
				q.add(pool.submit(new Runnable() {
					@Override
					public void run() {
						String first = weak.format(new Date());
						try {
							String second = weak.format(weak.parse(weak.format(new Date())));
							if (!first.equals(second))
								threadFail(MessageFormat.format("Got two different formatted dates {0} <=> {1}", first,
										second));
						} catch (ParseException | NumberFormatException e) {
							threadFail(e);
						}
					}
				}));
			}
			for (Future<?> f : q) f.get();
		});
	}

	@Test
	@DisplayName("Test wrapped formatter")
	void testWrap() throws TimeoutException, InterruptedException, ExecutionException {
		final SimpleDateFormat wrapped = wrap(new SimpleDateFormat(DD_MM_YYYY));
		ExecutorService pool = Executors.newFixedThreadPool(NUM_OF_THREADS);
		List<Future<?>> q = new ArrayList<>();
		for (int i = 0; i < NUM_OF_THREADS * 1000; i++) {
			q.add(pool.submit(new Runnable() {
				@Override
				public void run() {
					String first = wrapped.format(new Date());
					try {
						String second = wrapped.format(wrapped.parse(wrapped.format(new Date())));
						if (!first.equals(second))
							threadFail(MessageFormat.format("Got two different formatted dates {0} <=> {1}", first,
									second));
					} catch (ParseException e) {
						threadFail(e);
					}
				}
			}));
		}
		for (Future<?> f : q) f.get();
	}
}
