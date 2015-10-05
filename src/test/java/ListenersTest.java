import com.github.dddpaul.Listeners;
import org.jooq.lambda.Unchecked;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;
import java.util.function.Function;

public class ListenersTest extends Assert
{
    private static final int PORT = 54321;
    private static ExecutorService executor;

    @BeforeClass
    public static void init()
    {
        executor = Executors.newSingleThreadExecutor();
    }

    @Test
    public void testCreateListener() throws Exception
    {
        Callable<Socket> listener = Listeners.createListener( PORT );
        executor.submit( listener );
        connect();
        assertTrue( Listeners.isAvailable( PORT ) );
    }

    /**
     * Blocking listener is non-interruptible
     */
    @Test
    public void testCancelListener() throws Exception
    {
        Callable<Socket> listener = Listeners.createListener(PORT);
        Future<Socket> future = executor.submit( listener );
        try {
            future.get( 1, TimeUnit.SECONDS ).close();
        } catch( TimeoutException e ) {
            future.cancel( true );
            Thread.sleep( 250 );
            assertFalse( Listeners.isAvailable( PORT ) );
            connect(); // To terminate listener
            assertTrue( Listeners.isAvailable( PORT ) );
        }
    }

    @Test
    public void testCreateNonBlockingListener() throws Exception
    {
        Callable<Socket> listener = Listeners.createNonBlockingListener( PORT );
        executor.submit( listener );
        connect();
        assertTrue( Listeners.isAvailable( PORT ) );
    }

    /**
     * Non-blocking listener is interruptible
     */
    @Test
    public void testCancelNonBlockingListener() throws Exception
    {
        Callable<Socket> listener = Listeners.createNonBlockingListener( PORT );
        Future<Socket> future = executor.submit( listener );
        try {
            future.get( 1, TimeUnit.SECONDS ).close();
        } catch( TimeoutException e ) {
            future.cancel( true );
            Thread.sleep( 250 );
            assertTrue( Listeners.isAvailable( PORT ) );
        }
    }

    @Test
    public void testCreateCompletableListener() throws Exception
    {
        CountDownLatch latch = new CountDownLatch(1);
        Listeners.createCompletableListener( PORT ).thenAccept( Unchecked.consumer( socket -> {
            socket.close();
            latch.countDown();
        }));
        connect();
        latch.await();
        assertTrue( Listeners.isAvailable( PORT ) );
    }

    /**
     * CompletableFuture is non-interruptible, see
     * <a href="http://www.nurkiewicz.com/2015/03/completablefuture-cant-be-interrupted.html">CompletableFuture can't be interrupted</a>
     */
    @Test
    public void testCancelCompletableListener() throws Exception
    {
        CompletableFuture<Socket> listener = Listeners.createCompletableListener( PORT );
        try {
            listener.get( 1, TimeUnit.SECONDS ).close();
        } catch( TimeoutException e ) {
            listener.cancel( true );
            Thread.sleep( 250 );
            assertFalse(Listeners.isAvailable(PORT));
            connect(); // To terminate listener
            assertTrue( Listeners.isAvailable( PORT ) );
        }
        assertTrue(listener.isCompletedExceptionally());
    }

    @Test
    public void testCreateNetCatListener() throws Exception
    {
        Process process = Listeners.createNetCatListener( PORT );
        connect();
        assertTrue( Listeners.isAvailable( PORT ) );
        process.destroy();
    }

    /**
     * NetCat listener is interruptible
     */
    @Test
    public void testCancelNetCatListener() throws Exception
    {
        Process process = Listeners.createNetCatListener( PORT );
        process.destroy();
        Thread.sleep( 250 );
        assertTrue( Listeners.isAvailable( PORT ) );
    }

    private static void connect() throws IOException, InterruptedException
    {
        Thread.sleep( 250 );
        SocketChannel channel = SocketChannel.open();
        boolean result = channel.connect( new InetSocketAddress( "localhost", PORT ) );
        assertTrue( result );
        channel.close();
        Thread.sleep( 250 );
    }
}
