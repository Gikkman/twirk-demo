package com.gikk;

import com.gikk.util.Log;
import com.gikk.util.Scheduler;
import com.gikk.util.SchedulerBuilder;
import com.gikk.util.Touple;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import javax.annotation.PreDestroy;
import org.springframework.stereotype.Service;

/**
 *
 * @author Gikkman
 */
@Service
public class SchedulerService {

    /**
     * *************************************************************************
     * VARIABLES
	 **************************************************************************
     */
    private final Map<String, Runnable> permanentOnTick = new ConcurrentHashMap<>();
    private final Scheduler scheduler;
    private Queue<Touple<String, Runnable>> queue = new ConcurrentLinkedQueue<>();

    /**
     * *************************************************************************
     * CONSTRUCTOR
	 *************************************************************************
     */
    private SchedulerService() {
        scheduler = new SchedulerBuilder(3).setThreadsPrefix("Scheduler-").setThreadsDaemon(true).build();
        repeatedTask(1000, 1000, this::tickAction);
    }
    
    @PreDestroy
    private void preDestroy() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
        scheduler.terminate();
    }

    private static Runnable tryCatchWrap(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Throwable t) {
                Log.error("A scheduled runnable threw an uncaught exception", t);
            }
        };
    }

    /* *************************************************************************
     * TASK MANAGEMENT
    ***************************************************************************/
     
    public final Future executeTask(Runnable task) {
        if (scheduler.isDisposed()) {
            return null;
        }
        return scheduler.executeTask(tryCatchWrap(task));
    }

    public final Future delayedTask(int delayMillis, Runnable task) {
        if (scheduler.isDisposed()) {
            return null;
        }
        return scheduler.scheduleDelayedTask(delayMillis, tryCatchWrap(task));
    }

    /* *************************************************************************
     * TICK MANAGEMENT
     **************************************************************************/
    public final Future repeatedTask(int initialDelayMillis, int periodMillis, Runnable task) {
        if (scheduler.isDisposed()) {
            return null;
        }
        return scheduler.scheduleRepeatedTask(initialDelayMillis, periodMillis, tryCatchWrap(task));
    }

    /**
     * Adds a runnable to the Tick-batch. A Runnable in the Tick-batch are
     * executed every 1000 ms
     *
     * @param identifier name for the runnable
     * @param runnable the runnable
     */
    public void addTick(String identifier, Runnable runnable) {
        queue.add(new Touple<>(identifier, runnable));
    }

    /**
     * Removes a runnable to the Tick-batch.
     *
     * @param identifier name for the runnable
     */
    public void removeTick(String identifier) {
        permanentOnTick.remove(identifier);
    }

    /**
     * *********************
     * Singleton internals *
	 **********************
     */
    private void tickAction() {
        Queue<Touple<String, Runnable>> oldQueue = queue;
        queue = new ConcurrentLinkedQueue<>();

        Iterator<Entry<String, Runnable>> itr = permanentOnTick.entrySet().iterator();
        while (itr.hasNext()) {
            Entry<String, Runnable> e = itr.next();
            try {
                e.getValue().run();
            } catch (Exception ex) {
                Log.error("A tick task has failed", e.getKey(), ex.getMessage());
            }
        }

        oldQueue.forEach((t) -> {
            permanentOnTick.put(t.left, t.right);
        });
    }
}
