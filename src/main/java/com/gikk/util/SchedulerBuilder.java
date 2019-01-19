package com.gikk.util;

/**
 * Class for building instances of {@link Scheduler} objects.<br><br>
 *
 * The only requirement a {@link Scheduler} has is that a capacity is
 * designated, i.e. how many threads it should have in its availible thread
 * pool. The higher a schedulers capacity, the more tasks can be executed
 * concurrently, but the more memory is required to hold the threads in
 * memory.<br>
 * Other settings, such as the threads names and whether they are daemon threads
 * or not, are optional.<br><br>
 *
 * To construct an instance of a {@link Scheduler}, create an instance of a
 * SchedulerBuilder, then call the appropriate (or none) of the setters, then
 * call {@code .build()}.<br><br>
 *
 * <b>Usage example</b><br>
 * {@code Scheduler sched = new SchedulerBuilder(5).setThreadsPrefix("MyThreads-").build();}<br>
 * This will create a Scheduler with 5 threads in it, where the threads are
 * named MyThreads-0 to MyThreads-4
 *
 * @author Gikkman
 *
 */
public class SchedulerBuilder {
    //***********************************************************
    // 				VARIABLES
    //***********************************************************

    final int capacity;

    String threadsPrefix = null;
    boolean daemonThreads = false;

    //***********************************************************
    // 				CONSTRUCTOR
    //***********************************************************	
    /**
     * Constructs a basic SchedulerBuilder.<br>
     *
     * @param schedulerCapacity Decides how many threads the resulting
     * {@link Scheduler} will have
     */
    public SchedulerBuilder(int schedulerCapacity) {
        capacity = schedulerCapacity;
    }
    //***********************************************************
    // 				PUBLIC
    //***********************************************************	

    /**
     * Sets the prefix for threads held by the {@link Scheduler}. <br>
     * Each thread will have a number appended to its prefix, so if for example
     * you set the prefix to "foo", the first thread will be "foo0", the second
     * "foo1" and so on.
     *
     * @param prefix The prefix each thread will be given
     * @return This object. Allows for chaining setters and {@code .build()}
     */
    public SchedulerBuilder setThreadsPrefix(String prefix) {
        threadsPrefix = prefix;
        return this;
    }

    /**
     * Decides whether threads held by the {@link Scheduler} should be daemon
     * threads or not.
     *
     * @param isDaemon If {@code true}, threads in the Scheduler will be daemon
     * threads.
     * @return This object. Allows for chaining setters and {@code .build()}
     */
    public SchedulerBuilder setThreadsDaemon(boolean isDaemon) {
        daemonThreads = isDaemon;
        return this;
    }

    /**
     * Builds a {@link Scheduler} with the parameters that have been assigned in
     * this Builder's setter methods.<br>
     * Parameters can be assigned by using the various setter methods on this
     * builder object <b>before</b> {@code .build()} is called. Subsequent calls
     * to setters will not affect an already created {@link Scheduler}.
     *
     * @return A Scheduler, using the assigned parameters.
     */
    public Scheduler build() {
        return new Scheduler(this);
    }
}
