package com.github.dddpaul;

import org.jooq.lambda.Unchecked;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

public class Listeners {
    /**
     * Blocking listener
     */
    public static Callable<Socket> createListener(final int port) {
        return () -> {
            ServerSocket serverSocket = new ServerSocket(port);
            Socket socket = serverSocket.accept();
            serverSocket.close();
            return socket;
        };
    }

    /**
     * Non-blocking listener
     */
    public static Callable<Socket> createNonBlockingListener(final int port) {
        return () -> createNonBlockingSocket(port);
    }

    /**
     * Non-blocking listener created through {@link CompletableFuture}
     */
    public static CompletableFuture<Socket> createCompletableListener(final int port) {
        return CompletableFuture.supplyAsync(Unchecked.supplier(() -> createNonBlockingSocket(port)));
    }


    /**
     * NetCat listener (requires installed nc binary)
     */
    public static Process createNetCatListener(final int port) throws IOException, InterruptedException {
        List<String> cmd = Arrays.asList("nc", "-l", String.valueOf(port));
        return new ProcessBuilder(cmd).redirectErrorStream(true).start();
    }

    /**
     * Check local port usage
     *
     * @return <tt>true</tt> if port is available to listen on, <tt>false</tt> if port is used
     */
    public static boolean isAvailable(int port) throws IOException {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (BindException ignored) {
            return false;
        }
    }

    private static Socket createNonBlockingSocket(final int port) throws IOException, InterruptedException {
        Socket socket;
        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(port));
            while (true) {
                SocketChannel channel = serverChannel.accept();
                Thread.sleep(100);
                if (channel != null) {
                    socket = channel.socket();
                    break;
                }
            }
        }
        return socket;
    }
}
