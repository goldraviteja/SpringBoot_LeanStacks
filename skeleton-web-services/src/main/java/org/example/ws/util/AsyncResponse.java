package org.example.ws.util;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncResponse<V> implements Future<V> {

    private static final long BLOCK_INDEFINITELY = 0;
    private V value;
    private Exception executionException;
    private boolean isCompletedExceptionally = false;
    private boolean isCancelled = false;
    private boolean isDone = false;
    private long checkCompletedInterval = 100;


    public AsyncResponse() {

    }

    public AsyncResponse(V val) {
        this.value = val;
        this.isDone = true;
    }


    /**
     * Create a new, completed AsyncResponse with the supplied Exception. The
     * AsyncResponse is marked as completed exceptionally. When the client
     * invokes one of the <code>get</code> methods, an ExecutionException will
     * be thrown using the supplied Exception as the cause of the
     * ExecutionException.
     * 
     * @param ex A Throwable.
     */
    public AsyncResponse(Throwable ex) {
        this.executionException = new ExecutionException(ex);
        this.isCompletedExceptionally = true;
        this.isDone = true;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.isCancelled = true;
        this.isDone = true;
        return false;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    public boolean isCompletedExceptionally() {
        return this.isCompletedExceptionally;
    }

    @Override
    public boolean isDone() {
        return this.isDone;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        block(BLOCK_INDEFINITELY);
        if (isCancelled()) {
            throw new CancellationException();
        }
        if (isCompletedExceptionally()) {
            throw new ExecutionException(this.executionException);
        }
        if (isDone()) {
            return this.value;
        }
        throw new InterruptedException();
    }

    @Override
    public V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {

        long timeoutInMillis = unit.toMillis(timeout);
        block(timeoutInMillis);

        if (isCancelled()) {
            throw new CancellationException();
        }
        if (isCompletedExceptionally()) {
            throw new ExecutionException(this.executionException);
        }

        if (isDone()) {
            return this.value;
        }
        throw new InterruptedException();
    }



    /**
     * Mark this AsyncResponse as finished (completed) and set the supplied
     * value V as the task return value.
     * @param val An object of type V.
     * @return A boolean that when TRUE indicates the AsyncResponse state was
     *         successfully updated. A response of FALSE indicates the
     *         AsyncResponse state could not be set correctly.
     */

    public boolean complete(V val) {
        this.value = val;
        this.isDone = true;
        return true;
    }


    /**

     * Mark this AsyncResposne as finished (completed) with an exception. The
     * AsyncResponse value (V) is set to null. The supplied Throwable will be
     * used as the Cause of an ExceptionException thrown when any
     * <code>get</code> method is called.
     * 
     * @param ex A Throwable.
     * @return A boolean that when TRUE indicates the AsyncResponse state was
     *         successfully updated. A response of FALSE indicates the
     *         AsyncResponse state could not be set correctly.
     */

    public boolean completeExceptionally(Throwable ex) {
        this.value = null;
        this.executionException = new ExecutionException(ex);
        this.isCompletedExceptionally = true;
        this.isDone = true;
        return true;
    }

    /**
     * Set the interval at which any <code>get</code> method evaluates if the
     * AsyncResponse is complete or cancelled.
     * @param millis A long number of milliseconds.
     */
    public void setCheckCompletedInterval(long millis) {
        this.checkCompletedInterval = millis;
    }

    /**
     * Pauses the current thread until the AsyncResponse is in a completed or
     * cancelled status OR the specified timeout (in milliseconds) has elapsed.
     * If the timeout value is zero (0), then wait indefinitely for the
     * AsyncResponse to be completed or cancelled.
     * 
     * @param timeout A long number of milliseconds after which the process
     *        ceases to wait for state change.
     * @throws InterruptedException Thrown when the blocking operation is
     *         interrupted.
     */

    private void block(long timeout) throws InterruptedException {

        long start = System.currentTimeMillis();

        // Block until done, cancelled, or the timeout is exceeded

        while (!isDone() && !isCancelled()) {
            if (timeout > BLOCK_INDEFINITELY) {
                long now = System.currentTimeMillis();
                if (now > start + timeout) {
                    break;
                }
            }
            Thread.sleep(checkCompletedInterval);
        }
    }
}