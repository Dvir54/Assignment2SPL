package bgu.spl.mics;

import java.util.ArrayList;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {

	private final ConcurrentHashMap<MicroService, BlockingDeque<Message>> serviceMessageMap;
	private final ConcurrentHashMap<Class<? extends Event<?>>, Future<?>> eventFutureMap;
	private final ConcurrentHashMap<Class<? extends Event<?>>, BlockingDeque<MicroService>> eventServiceMap;
	private final ConcurrentHashMap<Class<? extends Broadcast>, ArrayList<MicroService>> broadcastServiceMap;
	private static MessageBusImpl instance;

	private MessageBusImpl() {
		serviceMessageMap = new ConcurrentHashMap<>();
		eventFutureMap = new ConcurrentHashMap<>();
		eventServiceMap = new ConcurrentHashMap<>();
		broadcastServiceMap = new ConcurrentHashMap<>();
	}

	@Override
	public synchronized <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		eventServiceMap.get(type).add(m);
	}

	@Override
	public  synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		broadcastServiceMap.get(type).add(m);

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub

	}

	@Override
	public synchronized void sendBroadcast(Broadcast b) {
		ArrayList<MicroService> list = broadcastServiceMap.get(b.getClass());
		if (list != null) {
			//לברר מדוע יש צורך
			for (MicroService m : list) {
				try {
					serviceMessageMap.get(m).put(b); // Add the broadcast to each subscriber's queue
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}

			}
		}

	}

	
	@Override
	public synchronized <T> Future<T> sendEvent(Event<T> e) {
		BlockingDeque<MicroService> queue = eventServiceMap.get(e.getClass());
		if (queue.isEmpty()) {
			return null;
		}

		MicroService m = queue.poll();
		queue.add(m);

		//לברר מדוע יש צורך
		try {
			serviceMessageMap.get(m).put(e); // Add the event to the MicroService's queue
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

		Future<T> future = new Future<>();
		eventFutureMap.put((Class<? extends Event<?>>) e.getClass(), future);
		return future;

	}

	@Override
	public synchronized void register(MicroService m) {
		serviceMessageMap.putIfAbsent(m, new LinkedBlockingDeque<>());

	}

	@Override
	public synchronized void unregister(MicroService m) {
		serviceMessageMap.remove(m);
		eventServiceMap.values().forEach(queue -> queue.remove(m));
		broadcastServiceMap.values().forEach(list -> list.remove(m));
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		return serviceMessageMap.get(m).take();
	}

	public static synchronized MessageBusImpl getInstance() {
		if (instance == null) {
			instance = new MessageBusImpl();
		}
		return instance;
	}

	

}
