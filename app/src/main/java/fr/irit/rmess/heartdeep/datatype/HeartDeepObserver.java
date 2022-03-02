/***********************************************************************
 Name............ : HeartDeepObserver.java
 Description..... : A custom interface to define the behavior of a class that can listen to some events. (The Observer/Listerner in Observer pattern)
 Author.......... : Kevin Jiokeng for IRIT, RMESS Team
 Creation Date... : 31/10/2019
 ************************************************************************/

package fr.irit.rmess.heartdeep.datatype;

/**
 * A custom interface to define the behavior of a class that can listen to some events. (The Observer/Listerner in Observer pattern)
 *
 * @param <T> The type of data that must be delivered in a notification
 * @see HeartDeepObservable
 */
public interface HeartDeepObserver<T> {
    /**
     * Called when the eventSource notifies this object
     *
     * @param eventSource The object sending the notification
     * @param newValue    The value delivered in the notification
     */
    void update(HeartDeepObservable<T> eventSource, T newValue);
}
