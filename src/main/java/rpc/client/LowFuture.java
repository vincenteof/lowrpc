package rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.protocol.RpcRequest;
import rpc.protocol.RpcResponse;

import java.util.concurrent.*;

/**
 * class $classname
 *
 * @author Yingpeng.Chen
 * @date 2018/8/21, 11:51
 */
public class LowFuture<V> implements Future<V> {
    private static Logger LOG = LoggerFactory.getLogger(LowFuture.class);
    private static ExecutorService es = Executors.newCachedThreadPool();

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

    public static <T> LowFuture<T> createWithCallback(RpcRequest request, LowCallback<T> cb) {
        LowFuture<T> future = new LowFuture<>();
        future.request = request;
        future.cb = cb;
        future.current = es.submit(future.new LowComputation());;
        return future;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return current.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return current.isCancelled();
    }

    @Override
    public boolean isDone() {
        return current.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return current.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
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
            cb.callback(result);

            return result;
        }
    }
}
