package com.gikk.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class GikkThreadFactory implements ThreadFactory {
    //***********************************************************************************************
    //											VARIABLES
    //***********************************************************************************************

    private final static AtomicInteger poolCounter = new AtomicInteger(0);

    private final AtomicInteger threadCounter = new AtomicInteger(0);
    private final String threadPrefix;
    private final boolean isDaemon;

    private final ThreadGroup group;

    //***********************************************************************************************
    //											CONSTRUCTOR
    //***********************************************************************************************
    private GikkThreadFactory(Builder builder) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();

        threadPrefix = builder.threadsPrefix != null
                ? builder.threadsPrefix
                : "pool-" + poolCounter.getAndIncrement() + "-thread-";
        isDaemon = builder.isDaemon;
    }

    //***********************************************************************************************
    //											PUBLIC
    //***********************************************************************************************
    /**
     * {@inheritDoc}
     */
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, threadPrefix + threadCounter.getAndIncrement(), 0);
        t.setDaemon(isDaemon);

        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }

        return t;
    }

    //***********************************************************************************************
    //											BUILDER CLASS
    //***********************************************************************************************
    /**
     * Class for creating GikkThreadFactory. <br>
     * To create a valid GikkThreadFactory, you must call the {@code .build()}
     * method. The setter methods are optional and available for convenience.
     *
     * @author Simon
     *
     */
    public static class Builder {

        private String threadsPrefix = null;
        private boolean isDaemon = false;

        /**
         * Creates a basic GikkThreadFactory builder. To create an instance of a
         * GikkThreadFactory, call the {@code .build()} method on this
         * object.<br>
         * You may want to set certain parameters of the GikkThreadFactory. In
         * that case, use the setter methods <b>before</b> calling
         * {@code .build()}. Setters methods do not affect already created
         * GikkThreadFactory instances.
         */
        public Builder() {
        }

        /**
         * Sets the prefix for threads created by the GikkThreadFactory built by
         * this builder. <br>
         * Each thread built by the GikkThreadFactory will have a number
         * appended to its prefix, so if for example you set the prefix to
         * "foo", the first thread will be "foo0", the second "foo1" and so on.
         *
         * @param prefix The prefix each thread will be given
         * @return This object. Allows for chaining setters and {@code .build()}
         */
        public Builder setThreadsPrefix(String prefix) {
            threadsPrefix = prefix;
            return this;
        }

        /**
         * Decides whether threads created by this GikkThreadFactory should be
         * daemon threads or not.
         *
         * @param daemon If {@code true}, threads created by the built
         * GikkThreadFactory will be daemon threads.
         * @return This object. Allows for chaining setters and {@code .build()}
         */
        public Builder setThreadsDaemon(boolean daemon) {
            isDaemon = daemon;
            return this;
        }

        /**
         * Builds a GikkThreadFactory with the parameters that have been
         * assigned in this Builder's setter methods.
         *
         * @return A GikkThreadFactory, using the assigned parameters.
         */
        public GikkThreadFactory build() {
            return new GikkThreadFactory(this);
        }
    }
}
