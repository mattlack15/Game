package me.gravitinos.aigame.server.event;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;

public class EventSubscriptions {
    private static class MethodCaller {
        public BiConsumer<Object, Object> consumer;
        public Method m;

        public MethodCaller(Method m, BiConsumer<Object, Object> consumer) {
            this.consumer = consumer;
            this.m = m;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof MethodCaller && ((MethodCaller) obj).m.equals(this.m));
        }
    }

    private static Map<Class<?>, List<Class<?>>> eventMap = new HashMap<>();
    private static Map<Class<?>, List<WeakReference<Object>>> objectMap = new HashMap<>();
    private static Map<Class<?>, Map<Class<?>, List<MethodCaller>>> callerMap = new HashMap<>();

    public static void call(Object event) {
        List<Class<?>> subscribedClasses = eventMap.get(event.getClass());
        if(subscribedClasses == null)
            return;
        subscribedClasses.forEach((c) -> {
            List<MethodCaller> callers = callerMap.get(c).get(event.getClass());
            List<WeakReference<Object>> objects = objectMap.get(c);
            objects.removeIf(w -> {
                Object a = w.get();
                if(a != null) {
                    callers.forEach(methodCaller -> methodCaller.consumer.accept(a, event));
                    return false;
                }
                return true;
            });
        });
    }

    public static void subscribe(Object o, Class<?> c) {
        callerMap.putIfAbsent(c, new HashMap<>()); //Add to the caller map (object class -> event class -> callers)
        for (Method m : c.getDeclaredMethods()) {
            if (m.isAnnotationPresent(EventSubscription.class)) {
                if (m.getParameterTypes().length < 1)
                    continue;
                Class<?> eventType = m.getParameterTypes()[0];
                if (!Event.class.isAssignableFrom(eventType))
                    continue;

                callerMap.get(c).putIfAbsent(eventType, new ArrayList<>());
                List<MethodCaller> callerList = callerMap.get(c).get(eventType);
                MethodCaller caller = new MethodCaller(m, (a, b) -> {
                    try {
                        m.setAccessible(true);
                        m.invoke(a, b);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
                if (!callerList.contains(caller)) {
                    callerList.add(caller);
                }

                eventMap.putIfAbsent(eventType, new ArrayList<>());
                if (!eventMap.get(eventType).contains(c))
                    eventMap.get(eventType).add(c);
                objectMap.putIfAbsent(c, new ArrayList<>());
                List<WeakReference<Object>> objects = objectMap.get(c);
                boolean contains = false;
                List<WeakReference<Object>> remove = new ArrayList<>();
                for (WeakReference<Object> object : objects) {
                    if (object.get() == o) {
                        contains = true;
                        break;
                    } else if (object.get() == null) {
                        remove.add(object);
                    }
                }
                objects.removeAll(remove);
                if (!contains) {
                    objects.add(new WeakReference<>(o));
                }
            }
        }
    }

    public static void unSubscribe(Object o, Class<?> c) {
        objectMap.putIfAbsent(c, new ArrayList<>());
        objectMap.get(c).removeIf(r -> r.get() == o);
    }
}
