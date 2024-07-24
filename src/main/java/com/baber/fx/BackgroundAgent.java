package com.baber.fx;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

/**
 * Helper class for invoking operations off the gui thread
 * 
 * @param <T> is the return type of event being handled
 */
@SuppressWarnings({
        "PMD.DoNotUseThreads", "PMD.NPathComplexity"
})
public class BackgroundAgent<T>
{
    /**
     * tell someone we're running
     */
    private EventHandler<WorkerStateEvent> runningNotification;

    /**
     * tell someone we've completed
     */
    private EventHandler<WorkerStateEvent> completedNotification;

    /**
     * tell someone we've failed
     */
    private EventHandler<WorkerStateEvent> failedNotification;

    /**
     * don't look at my privates
     */
    static void toggleControlAndNotify(
            final WorkerStateEvent event,
            final EventHandler<WorkerStateEvent> listener,
            final boolean disable)
    {
        if (event == null)
        {
            return;
        }
        if (event.getSource() instanceof Node)
        {
            ( (Node) event.getSource() ).setDisable(disable);
        }
        if (event.getSource() instanceof MenuItem)
        {
            ( (MenuItem) event.getSource() ).setDisable(disable);
        }
        if (listener != null)
        {
            listener.handle(event);
        }
    }

    /**
     * chained method for adding a completed notification listener
     */
    public BackgroundAgent<T> andNotifyWhenComplete(final EventHandler<WorkerStateEvent> anEventHandler)
    {
        completedNotification = anEventHandler;
        return this;
    }

    /**
     * chained method for adding a failed notification listener
     */
    public BackgroundAgent<T> andNotifyWhenFailed(final EventHandler<WorkerStateEvent> anEventHandler)
    {
        failedNotification = anEventHandler;
        return this;
    }

    /**
     * chained method for adding a running notification listener
     */
    public BackgroundAgent<T> andNotifyWhenRunning(final EventHandler<WorkerStateEvent> anEventHandler)
    {
        runningNotification = anEventHandler;
        return this;
    }

    /**
     * execute the event in the background thread
     */
    public void runInBackground(final HandlesEvent<T> process)
    {
        final Task<T> task = runOnThread(process);
        new Thread(task).start();
    }

    /**
     * don't look at my privates
     */
    private Task<T> runOnThread(final HandlesEvent<T> process)
    {
        final Task<T> task = new Task<>()
        {
            @Override
            protected T call()
            {
                final T myState = process.handleEvent();
                if (Boolean.FALSE.equals(myState))
                {
                    final WorkerStateEvent event = new WorkerStateEvent(this, WorkerStateEvent.WORKER_STATE_FAILED);
                    getOnFailed().handle(event);
                }
                return myState;
            }
        };

        final EventHandler<WorkerStateEvent> running =
                runningEvent -> toggleControlAndNotify(runningEvent, runningNotification, true);

        final EventHandler<WorkerStateEvent> completed =
                completedEvent -> toggleControlAndNotify(completedEvent, completedNotification, false);

        final EventHandler<WorkerStateEvent> failed =
                failedEvent -> toggleControlAndNotify(failedEvent, failedNotification, false);

        task.setOnCancelled(failed);
        task.setOnFailed(failed);
        task.setOnRunning(running);
        task.setOnSucceeded(completed);

        return task;
    }

    /**
     * runs the task on the gui thread because you can't modify the gui from another thread
     */
    public void runOnGui(final HandlesEvent<T> process)
    {
        final Task<T> task = runOnThread(process);
        Platform.runLater(task);
    }
}
