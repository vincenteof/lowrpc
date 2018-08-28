package rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.protocol.RpcRequest;
import rpc.protocol.RpcResponse;

import java.util.concurrent.*;

/**
 * class $classname
 *
 * Future implementation for rpc async result.
 *
 * @author Yingpeng.Chen
 * @date 2018/8/21, 11:51
 */
public class LowFuture<V> implements Future<V> {
    private static Logger LOG = LoggerFactory.getLogger(LowFuture.class);
    private static ExecutorService es = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private Future<V> current;
    private RpcRequest request;
    private LowCallback<V> cb;

    private LowFuture() {}

    public static <T> LowFuture<T> create(RpcRequest request) {
        LowFuture<T> future = new LowFuture<>();
        future.request = request;
        future.current = es.submit(future.new LowComputation());
        return future;
    }

//    public static <T> LowFuture<T> createWithCallback(RpcRequest request, LowCallback<T> cb) {
//        LowFuture<T> future = new LowFuture<>();
//        future.request = request;
//        future.cb = cb;
//        future.current = es.submit(future.new LowComputation());
//        return future;
//    }

    public static <T> LowFuture<T> lazyCreate(RpcRequest request) {
        LowFuture<T> future = new LowFuture<>();
        future.request = request;
        return future;
    }

    public void withCallback(LowCallback<V> cb) {
        this.cb = cb;
    }

    public boolean startCompute() {
        if (current == null) {
            current = es.submit(new LowComputation());
            return true;
        }
        return false;
    }

    private void assertStarted() {
        if (current == null) {
            throw new IllegalStateException("This `Future` has not started");
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        assertStarted();
        return current.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        assertStarted();
        return current.isCancelled();
    }

    @Override
    public boolean isDone() {
        assertStarted();
        return current.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        assertStarted();
        return current.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        assertStarted();
        return current.get(timeout, unit);
    }

    private class LowComputation implements Callable<V> {
        @Override
        public V call() throws Exception {
            RpcResultCollector collector = RpcResultCollector.getInstance();
            RpcResponse response;

            while ((response = collector.getIfPresent(request.getRequestId())) == null) {
                Thread.sleep(100);
                LOG.info("Wait for response for 100ms");
            }

            @SuppressWarnings("unchecked")
            V result = (V) response.getValue();
            if (cb != null) {
                cb.callback(result);
            }

            return result;
        }
    }
}
