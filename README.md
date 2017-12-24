# Hanky (handkerchief)

Hanky is an utility library for legacy projects which have variety range historical problems.

### Motivation

At my work we have a big legacy java project, and we can not control its whole code base. It has a bunch of problems with design, performance, and multithreading.

The one of them is using java.text.*Format classes in multithreaded environment. There is a global formatters factory in the main library, which creates many preinstantiated formatters and promote them to any involved classes. I believe the main idea was as fast as possible give predefined formatter to any interested procedures, not to crash execution logic, but we have what we have. Some classes promote given formatter to engaged classes and put them in different threads, and do it in many different places in many libraries, and code of some of them we can not control.
![figure 1](http://www.plantuml.com/plantuml/proxy?v=1&src=https://raw.githubusercontent.com/i-home/hanky/master/Throuble.puml)


### Idea

Because all client code of our factory get predefined formatters and using them only for parse/format operations by design. All we need is make execution of these operations threadsafe. In this case we can do the next: during our factory initialization process put in it predefined formatter wrapped by proxy class which overrides parse and format operations and redirect them to thredlocal clone of original formatter. In most cases that's enough.

### Usage
```java
import static hanky.text.ThreadSafeProxyFactory.safe;
...
SimpleDateFormat safeFormatter = safe(new SimpleDateFormat(DD_MM_YYYY));
...
Thread thread = new Thread(new MyRunnable(safeFormatter) {
	@Override
	public void run() {
		// Safe code
		String formatted = safeFormatter.format(...);
	}
});
...

```

### Restrictions

We can not prevent client code from changing valuable properties of our formatters in different ways. In this case all threads will work correctly with latest version formatter, it may lead to unexpected formatting results. But original design has the same problem.

If you really need to prevent formatter properties from unexpected changes your can use "safe" factory with two parameters to produce threadsafe formatter which does not allow to change his properties after locking:

```java
import static hanky.text.ThreadSafeProxyFactory.safe;
...
SimpleDateFormat safeFormatter = safe(new SimpleDateFormat(DD_MM_YYYY), true);
// or
SimpleDateFormat safeFormatter = safe(new SimpleDateFormat(DD_MM_YYYY), false);
// and when all settings is done
if (safeFormatter instanceof ThreadSafeProxyFactory.Final) ((ThreadSafeProxyFactory.Final) safeFormatter).lockSettings();
...
Thread thread = new Thread(new MyRunnable(safeFormatter) {
	@Override
	public void run() {
		// Safe code
		String formatted = safeFormatter.format(...);
	}
});
...

```



