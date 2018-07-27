## LowRpc

LowRpc is a very toy-like implementation of a RPC framework which is built on Netty, and it mainly serves as my personal practice for network and concurrent programming. 

### Usage

For rpc server, just provide the listening port and service package name which you want to expose
```java
public class RpcServerMain {
    public static void main(String[] args) throws Exception {
        RpcServer server = SimpleRpcServerBuilder.builder()
            .port(8322)
            .beansPackName("example.server.service")
            .build();

        server.start();
    }
}
```

For rpc client, use an interface to create proxy object to send request.
```java
@LowRpcClient(host = "127.0.0.1", port = 8322, clzName = "example.server.service.TestService")
public interface TestServiceClient {
    String testPureWithoutParams();

    Integer testStateWithoutParams();

    Boolean testStateWithParams(Integer num);
}

public class RpcClientMain {
    public static void main(String[] args) {
        TestServiceClient client = RpcClientProxyFactory.createProxy(TestServiceClient.class);
        System.out.println(client.testPureWithoutParams());
        System.out.println(client.testStateWithoutParams());
        System.out.println(client.testStateWithoutParams());
        System.out.println(client.testStateWithParams(2));
    }
}
```



### Something left for improvement

- [ ] Support for service registry and service discovery.
- [ ] Support for both async and sync calls.
- [ ] Handle services which have other dependencies. 
- [ ] More sophisticated error handling.
- [ ] Improve the performance.
- [ ] Add test cases.
- [ ] Make it more easy to use.









