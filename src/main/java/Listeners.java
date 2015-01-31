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

public class Listeners
{
    /**
     * Blocking listener
     */
    public static Callable<Socket> createListener( final int port )
    {
        return () -> {
            ServerSocket serverSocket = new ServerSocket( port );
            Socket socket = serverSocket.accept();
            serverSocket.close();
            return socket;
        };
    }

    /**
     * Non-blocking listener
     */
    public static Callable<Socket> createNonBlockingListener( final int port )
    {
        return () -> {
            Socket socket = null;
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking( false );
            serverChannel.socket().bind( new InetSocketAddress( port ) );
            try {
                while( true ) {
                    SocketChannel channel = serverChannel.accept();
                    Thread.sleep( 100 );
                    if( channel != null ) {
                        socket = channel.socket();
                        break;
                    }
                }
            } finally {
                serverChannel.close();
            }
            return socket;
        };
    }

    /**
     * NetCat listener (requires installed nc binary)
     */
    public static Process createNetCatListener( final int port ) throws IOException, InterruptedException
    {
        List<String> cmd = Arrays.asList( "nc", "-l", String.valueOf( port ) );
        return new ProcessBuilder( cmd ).redirectErrorStream( true ).start();
    }

    /**
     * Check local port usage
     *
     * @return <tt>true</tt> if port is available to listen on, <tt>false</tt> if port is used
     */
    public static boolean isAvailable( int port ) throws IOException
    {
        try( ServerSocket ignored = new ServerSocket( port ) ) {
            return true;
        } catch( BindException ignored ) {
            return false;
        }
    }
}
