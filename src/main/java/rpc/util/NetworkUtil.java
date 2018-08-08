package rpc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class NetworkUtil {
    private static Logger LOG = LoggerFactory.getLogger(NetworkUtil.class);

    public static String getLocalIp() {
        try(final DatagramSocket socket = new DatagramSocket()){
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            LOG.error("Unexpected error: {}", e);
        }
        return null;
    }
}
