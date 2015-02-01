# Java network listeners

## Blocking listener

Blocking listener is very simple but in can't be interrupted by calling thread. So there's no point in that:

```java
Future<Socket> future = executor.submit( Listeners.createListener( PORT ) );
try {
    Socket socket = future.get( 1, TimeUnit.SECONDS );
} catch( TimeoutException e ) {
    future.cancel( true );
}
```

Listener will stay active and PORT will be bound still. The reason is in usage of the uninterruptible [ServerSocket.accept()](http://docs.oracle.com/javase/7/docs/api/java/net/ServerSocket.html#accept()).

## Non-blocking listener

In contrary non-blocking listener is more sophisticated but it can be interrupted by calling thread. So you can do that:

```java
Future<Socket> future = executor.submit( Listeners.createListener( PORT ) );
try {
    Socket socket = future.get( 1, TimeUnit.SECONDS );
} catch( TimeoutException e ) {
    future.cancel( true );
}
```

Listener will be terminated and PORT will be freed.

## NetCat listener

May be useful for Linux/MacOS users :) It's fully interruptible too.

```java
Process process = Listeners.createNetCatListener( PORT );
...
process.destroy();
```

## Maven usage

Add following to your pom.xml:
```xml
<repositories>
    <repository>
        <id>central</id>
        <name>bintray</name>
        <url>http://dl.bintray.com/dddpaul/maven</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.dddpaul</groupId>
        <artifactId>listeners</artifactId>
        <version>1.0</version>
    </dependency>
</dependencies>
```
