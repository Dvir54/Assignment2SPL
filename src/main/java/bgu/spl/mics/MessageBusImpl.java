package bgu.spl.mics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {

	private final ConcurrentHashMap<MicroService, BlockingQueue<Message>> serviceMessageMap;
	private final ConcurrentHashMap<Event<?>, Future<?>> eventFutureMap;
	private final ConcurrentHashMap<Class<? extends Event<?>>, BlockingQueue<MicroService>> eventServiceMap;
	private final ConcurrentHashMap<Class<? extends Broadcast>, BlockingQueue<MicroService>> broadcastServiceMap;


	private MessageBusImpl() {
		serviceMessageMap = new ConcurrentHashMap<>();
		eventFutureMap = new ConcurrentHashMap<>();
		eventServiceMap = new ConcurrentHashMap<>();
		broadcastServiceMap = new ConcurrentHashMap<>();
	}

	private static class MessageBusHolder {
		private static final MessageBusImpl INSTANCE = new MessageBusImpl();
	}

	public static MessageBusImpl getInstance() {
		return MessageBusHolder.INSTANCE;
	}
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {

		synchronized (type) {
			eventServiceMap.computeIfAbsent(type, k -> new LinkedBlockingQueue<>()).add(m);
		}

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized (type) {
			broadcastServiceMap.computeIfAbsent(type, k -> new LinkedBlockingQueue<>()).add(m);
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future = (Future<T>) eventFutureMap.get(e);
		if (future != null) {
			future.resolve(result);
			eventFutureMap.remove(e);
		}

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		BlockingQueue<MicroService> list = broadcastServiceMap.get(b.getClass());

		if (list != null && !list.isEmpty()) {
			for (MicroService m : list) {
				if (m != null) {
					BlockingQueue<Message> queue = serviceMessageMap.get(m);
					if (queue != null) {
						synchronized (queue) {
							queue.add(b);
						}
					}

				}
			}
		}

	}
	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		MicroService m;
		BlockingQueue<MicroService> queue = eventServiceMap.get(e.getClass());

		if (queue == null || queue.isEmpty()) {
			return null;
		}
		synchronized (queue) {
			m = queue.poll();
			queue.add(m);
		}

		BlockingQueue<Message> queue2 = serviceMessageMap.get(m);

		if (queue2 != null) {
			synchronized (queue2) {
				queue2.add(e);
			}
		}

		Future<T> future = new Future<>();
		eventFutureMap.put(e, future);
		return future;

	}

	@Override
	public void register(MicroService m) {
		synchronized (m) {
			serviceMessageMap.putIfAbsent(m, new LinkedBlockingQueue<>());
		}
	}

	@Override
	public void unregister(MicroService m) {
		synchronized (m) {
			serviceMessageMap.remove(m);
		}
		for (Class<? extends Event<?>> type : eventServiceMap.keySet()) {
			synchronized (type) {
				eventServiceMap.get(type).remove(m);
			}
		}

		for (Class<? extends Broadcast> type2 : broadcastServiceMap.keySet()) {
			synchronized (type2) {
				broadcastServiceMap.get(type2).remove(m);
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		BlockingQueue<Message> queue = serviceMessageMap.get(m);
		try {
			return queue.take();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw e;
		}
	}

}
