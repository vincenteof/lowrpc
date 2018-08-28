## LowRpc

LowRpc is a very toy-like implementation of a RPC framework which is built on Netty, and it mainly serves as my personal practice for network and concurrent programming. 

### Usage

For rpc server, use `@LowRpcService` to annotated the service you want to expose. 
Then use `SimpleRpcServerBuilder` to create server instance.
```java
@LowRpcService(name = "testService")
public class TestService {
    private AtomicInteger counter = new AtomicInteger(0);

    public String testPureWithoutParams() {
        return "Hello";
    }

    public Integer testStateWithoutParams() {
        return counter.getAndIncrement();
    }

    public Boolean testStateWithParams(Integer num) {
        return num == counter.get();
    }

    public String testAsync() { return "Async Ok"; }

    public String testCallback() { return "Callback Ok"; }
}

public class RpcServerMain {
    public static void main(String[] args) throws Exception {
        String beanPackName = "example.server.service";
        ServiceRegistry registry = ConsulServiceRegistry.getInstance();

        RpcServer server = SimpleRpcServer.builder(beanPackName, registry)
            .port(8322)
            .build();

        server.start();
    }
}
```

For rpc client, use `@LowRpcClient` and `RpcClientProxyFactory` to create a proxy object of the interface to send request.
```java
@LowRpcClient(serviceName = "testService")
public interface TestServiceClient {
    String testPureWithoutParams();

    Integer testStateWithoutParams();

    Boolean testStateWithParams(Integer num);

    LowFuture<String> testAsync();

    @LazyCreate
    LowFuture<String> testCallback();
}


public class RpcClientMain {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        TestServiceClient client = RpcClientProxyFactory.createProxy(
            TestServiceClient.class,
            ConsulServiceDiscovery.getInstance()
        );
        
        // sync call
        System.out.println(client.testPureWithoutParams());
        System.out.println(client.testStateWithoutParams());
        System.out.println(client.testStateWithParams(2));
        
        // async call
        LowFuture<String> future = client.testAsync();
        System.out.println(future.get());
        LowFuture<String> lazyFuture = client.testCallback();
        lazyFuture.withCallback(ret -> System.out.println("The result is: " + ret));
        lazyFuture.startCompute();
        
        // release resource
        NettyChannelManager.getInstance().shutdown();
    }
}
```



### Something left for improvement

- [x] Support for service registry and service discovery.
- [x] Support for both async and sync calls.
- [x] Handle services which have other dependencies. 
- [ ] More sophisticated error handling.
- [ ] Improve the performance.
- [ ] Add test cases.
- [ ] Make it more easy to use.

### Progress

##### 2018/8/1
Support for Consul service registry and discovery has been added. 

##### 2018/8/3
A simple dependency injection implementation has been added.

##### 2018/8/28
Both async call and sync call has been supported.





