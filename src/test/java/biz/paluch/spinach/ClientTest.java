package biz.paluch.spinach;

import static com.google.code.tempusfugit.temporal.Duration.seconds;
import static com.google.code.tempusfugit.temporal.WaitFor.waitOrTimeout;
import static com.lambdaworks.redis.ScriptOutputType.STATUS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

import com.google.code.tempusfugit.temporal.Condition;
import com.google.code.tempusfugit.temporal.Timeout;
import com.lambdaworks.redis.*;
import com.lambdaworks.redis.protocol.CommandHandler;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class ClientTest extends AbstractCommandTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Override
    public void openConnection() throws Exception {
        Logger logger = LogManager.getLogger(CommandHandler.class);
        logger.setLevel(Level.ALL);
        super.openConnection();
    }

    @Override
    public void closeConnection() throws Exception {
        super.closeConnection();
        Logger logger = LogManager.getLogger(CommandHandler.class);
        logger.setLevel(Level.INFO);
    }

    @Test(expected = RedisException.class)
    public void close() throws Exception {
        disque.close();
        disque.auth("");
    }

    @Test
    public void listenerTest() throws Exception {

        final TestConnectionListener listener = new TestConnectionListener();

        RedisClient client = new RedisClient(host, port);
        client.addListener(listener);

        assertThat(listener.onConnected).isNull();
        assertThat(listener.onDisconnected).isNull();
        assertThat(listener.onException).isNull();

        RedisAsyncConnection<String, String> connection = client.connectAsync();
        waitOrTimeout(new Condition() {

            @Override
            public boolean isSatisfied() {
                return listener.onConnected != null;
            }
        }, Timeout.timeout(seconds(2)));

        assertThat(listener.onConnected).isEqualTo(connection);
        assertThat(listener.onDisconnected).isNull();

        connection.set(key, value).get();
        connection.close();

        waitOrTimeout(new Condition() {

            @Override
            public boolean isSatisfied() {
                return listener.onDisconnected != null;
            }
        }, Timeout.timeout(seconds(2)));

        assertThat(listener.onConnected).isEqualTo(connection);
        assertThat(listener.onDisconnected).isEqualTo(connection);

    }

    @Test
    public void listenerTestWithRemoval() throws Exception {

        final TestConnectionListener removedListener = new TestConnectionListener();
        final TestConnectionListener retainedListener = new TestConnectionListener();

        RedisClient client = new RedisClient(host, port);
        client.addListener(removedListener);
        client.addListener(retainedListener);
        client.removeListener(removedListener);

        RedisAsyncConnection<String, String> connection = client.connectAsync();
        waitOrTimeout(new Condition() {

            @Override
            public boolean isSatisfied() {
                return retainedListener.onConnected != null;
            }
        }, Timeout.timeout(seconds(2)));

        assertThat(retainedListener.onConnected).isNotNull();

        assertThat(removedListener.onConnected).isNull();
        assertThat(removedListener.onDisconnected).isNull();
        assertThat(removedListener.onException).isNull();

    }

    @Test
    public void reconnect() throws Exception {
        /*
         * disque.set(key, value); disque.quit(); Thread.sleep(100); assertThat(disque.get(key)).isEqualTo(value);
         * disque.quit(); Thread.sleep(100); assertThat(disque.get(key)).isEqualTo(value); disque.quit(); Thread.sleep(100);
         * assertThat(disque.get(key)).isEqualTo(value);
         */
    }

    /*
     * @Test(expected = RedisCommandInterruptedException.class, timeout = 10) public void interrupt() throws Exception {
     * Thread.currentThread().interrupt(); disque.blpop(0, key); }
     */

    @Test
    public void connectFailure() throws Exception {
        RedisClient client = new RedisClient("invalid");
        exception.expect(RedisException.class);
        exception.expectMessage("Unable to connect");
        client.connect();
    }

    @Test
    public void connectPubSubFailure() throws Exception {
        RedisClient client = new RedisClient("invalid");
        exception.expect(RedisException.class);
        exception.expectMessage("Unable to connect");
        client.connectPubSub();
    }

    private class TestConnectionListener implements RedisConnectionStateListener {

        public RedisChannelHandler<?, ?> onConnected;
        public RedisChannelHandler<?, ?> onDisconnected;
        public RedisChannelHandler<?, ?> onException;

        @Override
        public void onRedisConnected(RedisChannelHandler<?, ?> connection) {
            onConnected = connection;
        }

        @Override
        public void onRedisDisconnected(RedisChannelHandler<?, ?> connection) {
            onDisconnected = connection;
        }

        @Override
        public void onRedisExceptionCaught(RedisChannelHandler<?, ?> connection, Throwable cause) {
            onException = connection;

        }
    }

    @Test
    public void emptyClient() throws Exception {

        RedisClient client = new RedisClient();
        try {
            client.connect();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("RedisURI");
        }

        try {
            client.connectAsync();
        } catch (IllegalStateException e) {
            assertThat(e).hasMessageContaining("RedisURI");
        }

        try {
            client.connect((RedisURI) null);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageContaining("RedisURI");
        }

        try {
            client.connectAsync((RedisURI) null);
        } catch (IllegalArgumentException e) {
            assertThat(e).hasMessageContaining("RedisURI");
        }
        client.shutdown();
    }

    @Test
    public void testExceptionWithCause() throws Exception {
        RedisException e = new RedisException(new RuntimeException());
        assertThat(e).hasCauseExactlyInstanceOf(RuntimeException.class);
    }

}
