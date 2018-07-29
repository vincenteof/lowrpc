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
}

public class RpcServerMain {
    public static void main(String[] args) throws Exception {
        RpcServer server = SimpleRpcServerBuilder.builder(ConsulServiceRegistry.getInstance())
            .port(8322)
            .beansPackName("example.server.service")
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
}


public class RpcClientMain {
    public static void main(String[] args) {
        TestServiceClient client = RpcClientProxyFactory.createProxy(
            TestServiceClient.class,
            ConsulServiceDiscovery.getInstance()
        );
        System.out.println(client.testPureWithoutParams());
        System.out.println(client.testStateWithoutParams());
        System.out.println(client.testStateWithParams(2));
    }
}
```



### Something left for improvement

- [x] Support for service registry and service discovery.
- [ ] Support for both async and sync calls.
- [ ] Handle services which have other dependencies. 
- [ ] More sophisticated error handling.
- [ ] Improve the performance.
- [ ] Add test cases.
- [ ] Make it more easy to use.

### Progress

1. Support for Consul service registry and discovery has been added. 







