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

	/**
	 *
	 * PRE: type != null, m != null, eventServiceMap != null
	 * POST:
	 * A BlockingQueue corresponding to the type is created in eventServiceMap if it does not already exist.
	 * The provided MicroService (m) is added to the BlockingQueue associated with the given type.
	 * The eventServiceMap is updated to include the mapping of type to the queue containing the newly subscribed MicroService.
	 *
	 **/

	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		BlockingQueue queue = eventServiceMap.computeIfAbsent(type, k -> new LinkedBlockingQueue<>());
		queue.add(m);

	}

	/**
	 *
	 * PRE: type != null, m != null, broadcastServiceMap != null
	 * POST:
	 * A BlockingQueue corresponding to the type is created in broadcastServiceMap if it does not already exist.
	 * The provided MicroService (m) is added to the BlockingQueue associated with the given type.
	 * The broadcastServiceMap is updated to include the mapping of type to the queue containing the newly subscribed MicroService.
	 *
	 **/

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

	/**
	 *
	 * PRE: m != null, serviceMap != null
	 * POST:
	 * If the MicroService (m) is not already present in serviceMessageMap, a new LinkedBlockingQueue is created and associated with m.
	 * If the MicroService (m) is already present in serviceMessageMap, the map remains unchanged.
	 * The serviceMessageMap will include the provided MicroService (m) as a key, mapped to its own LinkedBlockingQueue.
	 *
	 **/

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

	public ConcurrentHashMap<MicroService, BlockingQueue<Message>> getServiceMessageMap() {
		return serviceMessageMap;
	}

	public ConcurrentHashMap<Class<? extends Event<?>>, BlockingQueue<MicroService>>  getEventServiceMap() {
		return eventServiceMap;
	}

	public ConcurrentHashMap<Class<? extends Broadcast>, BlockingQueue<MicroService>> getBroadcastServiceMap() {
		return broadcastServiceMap;
	}

}
