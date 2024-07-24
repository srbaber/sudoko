package com.baber.fx;

/**
 * defines the interface for handling java fx events
 *
 * @param <T> is the return type of the handle event method
 */
public interface HandlesEvent<T>
{
    /**
     * handle a gui event
     */
    T handleEvent();
}
