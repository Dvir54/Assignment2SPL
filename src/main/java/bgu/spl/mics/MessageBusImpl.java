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
		private static final MessageBusImpl instance = new MessageBusImpl();
	}

	public static MessageBusImpl getInstance() {
		return MessageBusHolder.instance;
	}
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		BlockingQueue queue = eventServiceMap.computeIfAbsent(type, k -> new LinkedBlockingQueue<>());
		queue.add(m);

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		BlockingQueue queue = broadcastServiceMap.computeIfAbsent(type, k -> new LinkedBlockingQueue<>());
		queue.add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future = (Future<T>) eventFutureMap.get(e);
		if (future != null) {
			future.resolve(result);
			//eventFutureMap.remove(e);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		if (broadcastServiceMap.containsKey(b.getClass())) {
			BlockingQueue<MicroService> list = broadcastServiceMap.get(b.getClass());
			if (list != null && !list.isEmpty()) {
				synchronized (list) {
					if (list != null && !list.isEmpty()) {
						for (MicroService m : list) {
							BlockingQueue<Message> queue = serviceMessageMap.get(m);
							if (queue != null) {
								synchronized (queue) {
									queue.add(b);
									queue.notifyAll();
								}
							}
						}

					}
				}

			}
		}
	}

	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		synchronized (e.getClass()) { //makes sure the roundrobin remains correct in case two events of the same type were sent
			if (eventServiceMap.containsKey(e.getClass())) {
				MicroService m = null;
				BlockingQueue<MicroService> queue = eventServiceMap.get(e.getClass());
				if (queue != null && !queue.isEmpty()) {
						m = queue.poll();
						queue.add(m);
					}

				if(m != null) {
					BlockingQueue<Message> queue2 = serviceMessageMap.get(m);
					if (queue2 != null) {
						synchronized (queue2) {
							if (queue2 != null) {
								queue2.add(e);
							}
						}
					}
				}
			}
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
	public void unregister(MicroService m) {
		synchronized (serviceMessageMap) {
			for(Message msg : serviceMessageMap.get(m)){
				if(msg instanceof Event) {
					complete((Event) msg, null);
				}
			}
			serviceMessageMap.remove(m);
		}
		synchronized (eventServiceMap) {
			for (Class<? extends Event<?>> type : eventServiceMap.keySet()) {
				eventServiceMap.get(type).remove(m);
			}
		}
		synchronized (broadcastServiceMap) {
			for (Class<? extends Broadcast> type2 : broadcastServiceMap.keySet()) {
				broadcastServiceMap.get(type2).remove(m);
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
			if(!serviceMessageMap.containsKey(m)) {
				throw new IllegalStateException("No such microservice");
			}
			return serviceMessageMap.get(m).take();
		}
}
