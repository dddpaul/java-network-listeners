import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;

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
        Callable<Socket> listener = Listeners.createListener( PORT );
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
