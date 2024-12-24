package bgu.spl.mics;

import java.util.ArrayList;
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
	private static  MessageBusImpl instance;

	private MessageBusImpl() {
		serviceMessageMap = new ConcurrentHashMap<>();
		eventFutureMap = new ConcurrentHashMap<>();
		eventServiceMap = new ConcurrentHashMap<>();
		broadcastServiceMap = new ConcurrentHashMap<>();
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		try {
			eventServiceMap.get(type).put(m);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		broadcastServiceMap.get(type).add(m);

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
		if (list != null) {
			for (MicroService m : list) {
				try {
					serviceMessageMap.get(m).put(b);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}

			}
		}

	}
	
	@Override
	public synchronized <T> Future<T> sendEvent(Event<T> e) {
		BlockingQueue<MicroService> queue = eventServiceMap.get(e.getClass());
		if (queue == null || queue.isEmpty()) {
			return null;
		}

		MicroService m = queue.poll();
		queue.add(m);

		try {
			serviceMessageMap.get(m).put(e);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

		Future<T> future = new Future<>();
		eventFutureMap.put(e, future);
		return future;

	}

	@Override
	public void register(MicroService m) {
		serviceMessageMap.putIfAbsent(m, new LinkedBlockingQueue<>());

	}

	@Override
	public synchronized void unregister(MicroService m) {
		serviceMessageMap.remove(m);
		eventServiceMap.values().forEach(queue -> queue.remove(m));
		broadcastServiceMap.values().forEach(list -> list.remove(m));
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		BlockingQueue<Message> queue = serviceMessageMap.get(m);

		try {
			return queue.take();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
	}

	public static MessageBusImpl getInstance() {
		if (instance == null) {
			instance = new MessageBusImpl();
		}
		return instance;
	}

	

}
