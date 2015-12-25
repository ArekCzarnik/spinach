package biz.paluch.spinach.api.async;

import java.util.List;
import java.util.Map;

import biz.paluch.spinach.api.Job;
import biz.paluch.spinach.api.PauseArgs;
import biz.paluch.spinach.api.QScanArgs;

import com.lambdaworks.redis.KeyScanCursor;
import com.lambdaworks.redis.RedisFuture;
import com.lambdaworks.redis.ScanCursor;

/**
 * Asynchronous executed commands related with Disque Queues.
 * 
 * @param <K> Key type.
 * @param <V> Value type.
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisqueQueueAsyncCommands<K, V> {

    /**
     * Remove the job from the queue.
     *
     * @param jobIds the job Id's
     * @return the number of jobs actually moved from queue to active state
     */
    RedisFuture<Long> dequeue(String... jobIds);

    /**
     * Queue jobs if not already queued.
     *
     * @param jobIds the job Id's
     * @return the number of jobs actually move from active to queued state
     */
    RedisFuture<Long> enqueue(String... jobIds);

    /**
     * Queue jobs if not already queued and increment the nack counter.
     *
     * @param jobIds the job Id's
     * @return the number of jobs actually move from active to queued state
     */
    RedisFuture<Long> nack(String... jobIds);

    /**
     * Change the {@literal PAUSE} pause state to:
     * <ul>
     * <li>Pause a queue</li>
     * <li>Clear the pause state for a queue</li>
     * <li>Query the pause state</li>
     * <li>Broadcast the pause state</li>
     * </ul>
     * 
     * @param queue the queue
     * @param pauseArgs the pause args
     * @return pause state of the queue.
     */
    RedisFuture<String> pause(K queue, PauseArgs pauseArgs);

    /**
     * Return the number of jobs queued.
     * 
     * @param queue the queue
     * @return the number of jobs queued
     */
    RedisFuture<Long> qlen(K queue);

    /**
     * Return an array of at most "count" jobs available inside the queue "queue" without removing the jobs from the queue. This
     * is basically an introspection and debugging command.
     * 
     * @param queue the queue
     * @param count number of jobs to return
     * @return List of jobs.
     */
    RedisFuture<List<Job<K, V>>> qpeek(K queue, long count);

    /**
     * Incrementally iterate the keys space.
     *
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    RedisFuture<KeyScanCursor<K>> qscan();

    /**
     * Incrementally iterate the keys space.
     *
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    RedisFuture<KeyScanCursor<K>> qscan(QScanArgs scanArgs);

    /**
     * Incrementally iterate the keys space.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    RedisFuture<KeyScanCursor<K>> qscan(ScanCursor scanCursor);

    /**
     * Incrementally iterate the keys space.
     *
     * @param scanCursor cursor to resume from a previous scan
     * @param scanArgs scan arguments
     * @return KeyScanCursor&lt;K&gt; scan cursor.
     */
    RedisFuture<KeyScanCursor<K>> qscan(ScanCursor scanCursor, QScanArgs scanArgs);

    /**
     * Retrieve information about a queue as key value pairs.
     * 
     * @param queue the queue
     * @return map containing the statistics (Key-Value pairs)
     */
    RedisFuture<Map<String, Object>> qstat(K queue);

    /**
     * If the job is queued, remove it from queue and change state to active. Postpone the job requeue time in the future so
     * that we'll wait the retry time before enqueueing again.
     * 
     * * Return how much time the worker likely have before the next requeue event or an error:
     * <ul>
     * <li>-ACKED: The job is already acknowledged, so was processed already.</li>
     * <li>-NOJOB We don't know about this job. The job was either already acknowledged and purged, or this node never received
     * a copy.</li>
     * <li>-TOOLATE 50% of the job TTL already elapsed, is no longer possible to delay it.</li>
     * </ul>
     *
     * @param jobId the job Id
     * @return retry count.
     */
    RedisFuture<Long> working(String jobId);
}
