# Hanky (handkerchief)

Hanky is an utility library for legacy projects which have variety range historical problems.

### Motivation

At my work we have a big legacy java project, and we can not control its whole code base. It has a bunch of problems with design, performance, and multithreading.

The one of them is using java.text.*Format classes in multithreaded environment. There is a global formatters factory in the main library, which creates many preinstantiated formatters and promote them to any involved classes. I believe the main idea was as fast as possible give predefined formatter to any interested procedures, not to crash execution logic, but we have what we have. Some classes promote given formatter to engaged classes and put them in different threads, and do it in many different places in many libraries, and code of some of them we can not control.

### Idea

Because all client code of our factory get predefined formatters and using them only for parse/format operations by design. All we need is make execution of these operations threadsafe. In this case we can do the next: during our factory initialization process put in it predefined formatter wrapped by proxy class which overrides parseand format operations and redirect them to thredlocal clone of original formatter. In most cases that's enough.

### Restrictions

We can not prevent client code from changing valuable properties of our formatters in different ways. In this case all threads will work correctly with latest version formatter, it may lead to unexpected formatting results. But original design has the same problem.