package com.gikk.util;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * This class handles scheduling of tasks that should be executed one or more times sometime in the future.<br><br>
 *
 * This class uses a ScheduledThreadPoolExecutor to execute the different tasks. That has the consequence that a thread
 * might be running for a while after the program tells the Scheduler to terminate. <b>This is normal and may take up to 60 seconds.</b>
 * After 60 seconds the Scheduler force-terminates all remaining tasks.<br><br>
 *
 * Schedulers are created via the {@link SchedulerBuilder#build()} method.
 *
 * @author Gikkman
 *
 */
public class Scheduler
{
	//***********************************************************************************************
	//											VARIABLES
	//***********************************************************************************************
	private final Thread shutdownThread;
	private final ScheduledThreadPoolExecutor executor;
	private boolean disposed = false;

	//***********************************************************************************************
	//											CONSTRUCTOR
	//***********************************************************************************************
	Scheduler(SchedulerBuilder builder)
	{
		shutdownThread = createShutdownThread();
		ThreadFactory threadFactory = new GikkThreadFactory.Builder()
			.setThreadsPrefix(builder.threadsPrefix)
			.setThreadsDaemon(builder.daemonThreads)
			.build();
		executor = new ScheduledThreadPoolExecutor(builder.capacity, threadFactory);
	}
	//***********************************************************************************************
	//											PUBLIC
	//***********************************************************************************************

	/**Gets the {@link ScheduledThreadPoolExecutor}'s thread pool size. See {@link ScheduledThreadPoolExecutor#getPoolSize()}
	 *
	 * @return The ScheduledThreadPoolExecutor's thread pool size
	 */
	public long getCapacity()
	{
		return executor.getCorePoolSize();
	}

	/**Gets the approximate number of scheduled tasks. The number is approximate, since the number might
	 * change during computation.<br> 
	 * The number is computed by fetching the queue from {@link ScheduledThreadPoolExecutor} and retrieving its size
	 *
	 * @return The approximate number of scheduled tasks.
	 */
	public long getScheduledQueueSize()
	{
		return executor.getQueue().size();
	}

	/**Gets the approximate number of completed tasks. The number is approximate, since the number might
	 * change during computation.<br>
	 * See {@link ScheduledThreadPoolExecutor#getCompletedTaskCount()}
	 *
	 * @return The approximate number of completed tasks
	 */
	public long getCompletedTaskCount()
	{
		return executor.getCompletedTaskCount();
	}

	/**Check the status of the Scheduler. If it has been disposed, no more {@code Tasks} will be executed. To dispose
	 * of the Scheduler, call {@link #terminate()}
	 *
	 * @return {@code True} if the Scheduler has been disposed (or is in the process of disposing).
	 */
	public boolean isDisposed()
	{
		return disposed;
	}

	/**Returns the approximate number of threads that are actively executing tasks.
	 *
	 * @return The approximate number of tasks executing at the moment
	 */
	public long getActiveCount()
	{
		return executor.getActiveCount();
	}

	/**Schedules a Task to be executed once every {@code periodMillis}. The Task's {@code onUpdate()} will be called
	 * the first time after initDelayMillis. If any execution of the task encounters an exception, subsequent executions 
	 * are suppressed. Otherwise, the task will only terminate via cancellation or termination of the executor. <br>
	 * If any execution of this task takes longer than its period, then subsequent executions may start late, but will not 
	 * concurrently execute
	 *
	 * @param initDelayMillis How many milliseconds we wait until we start trying to execute this task
	 * @param periodMillis How many milliseconds we wait until we start trying to execute this task from the previous time it executed
	 * @param task The task to the executed repeatedly
	 * @return A {@code ScheduledFuture}, which may be used to interact with the scheduled task (say for canceling or interruption)
	 */
	public ScheduledFuture<?> scheduleRepeatedTask(int initDelayMillis, int periodMillis, Runnable task)
	{
		return executor.scheduleAtFixedRate(task, initDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
	}

	/**Postpones a OneTimeTask for delayMillis. After the assigned delay, the task will be performed as soon as possible. 
	 * The task might have to wait for longer, if no threads are availible after the stated delay.<br>
	 * The task will be executed only once, and then removed from the scheduler. Results from the task may be recovered from
	 * the {@code ScheduledFuture} object after completion.
	 *
	 * @param delayMillis How many milliseconds we wait until we start trying to execute this task
	 * @param task The task to be executed
	 * @return A {@code ScheduledFuture}, which may be used to interact with the scheduled task (say for canceling or interruption)
	 */
	public ScheduledFuture<?> scheduleDelayedTask(int delayMillis, Runnable task)
	{
		return executor.schedule(task, delayMillis, TimeUnit.MILLISECONDS);
	}

	/**Tells the scheduler to perform a  certain task as soon as possible. This might be immediately (if there are threads
	 * availible) or sometime in the future. There are no guarantees for when the task will be performed, just as soon as possible.
	 *
	 * @param task The task that should be performed
	 * @return A {@code ScheduledFuture}, which may be used to interact with the scheduled task (say for canceling or interruption)
	 */
	public ScheduledFuture<?> executeTask(Runnable task)
	{
		return executor.schedule(task, 0, TimeUnit.MILLISECONDS);
	}
	//***********************************************************************************************
	//											TERMINATION
	//***********************************************************************************************

	/**This method will create a new thread, that will try to shut down execution of all tasks.<br><br>
	 *
	 * First, the Thread will initiates an orderly shutdown in which previously submitted tasks are executed, 
	 * but no new tasks will be accepted. Submitted in this case means that the task has begun execution. The thread
	 * will then allow up to 60 seconds for Tasks to complete.<br>
	 * After waiting for 60 seconds, the Thread will attempt to stop all actively executing tasks and halt the process 
	 * of waiting tasks.<br><br>
	 *
	 * The created thread is not a daemon thread, so if it is not possible to halt execution of all tasks, the new thread
	 * might never finish. Thus, it is important that created tasks can be halted.
	 *
	 */
	public void terminate()
	{
		synchronized (this)
		{
			if (disposed == true)
			{
				return;
			}
			disposed = true;
		}
		shutdownThread.start();
	}

	//***********************************************************************************************
	//											PRIVATE
	//***********************************************************************************************

	private Thread createShutdownThread()
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					System.out.println();
					System.out.println("\tThere are currently " + executor.getQueue().size() + " task scheduled.\n"
						+ "\tThere are currently " + executor.getActiveCount() + " tasks executing.\n"
						+ "\tAttempting shutdown. Please allow up to a minute...");

					executor.shutdown();
					if (executor.awaitTermination(60, TimeUnit.SECONDS))
					{
						System.out.println("\tShutdown completed");
						return;
					}

					System.out.println("\tThere are still " + executor.getActiveCount() + " tasks executing.\n"
						+ "\tForcing shutdown...");
					executor.shutdownNow();
					System.out.println("\tForce shutdown successful");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});

		thread.setDaemon(false);
		return thread;
	}
}