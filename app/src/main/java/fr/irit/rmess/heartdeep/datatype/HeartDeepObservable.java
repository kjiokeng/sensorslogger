/***********************************************************************
 Name............ : HeartDeepObservable
 Description..... : A custom interface to define the behavior of a class that can send some events. (The Observable in Observer pattern)
 Author.......... : Kevin Jiokeng for IRIT, RMESS Team
 Creation Date... : 31/10/2019
 ************************************************************************/

package fr.irit.rmess.heartdeep.datatype;

/**
 * A custom interface to define the behavior of a class that can send some events. (The Observable in Observer pattern)
 *
 * @param <T> The type of data that is delivered in a notification.
 * @see HeartDeepObserver
 */
public interface HeartDeepObservable<T> {
    /**
     * Registers an observer as beeing able to handle notification from this object
     * If this observer is already registered, nothing is done
     * @param observer The observer to be added
     */
    void registerObserver(HeartDeepObserver<T> observer);

    /**
     * Unregister an observer
     * @param observer The observer to be added
     */
    void unRegisterObserver(HeartDeepObserver<T> observer);

    /**
     * Sends the notification to an given observer, with a given value.
     * This will result in calling {@link HeartDeepObserver#update(fr.irit.rmess.heartdeep.datatype.HeartDeepObservable, Object)}
     * @param observer The observer to send the notification to
     * @param newValue The value to be delivered in the notification
     */
    void notifyObserver(HeartDeepObserver<T> observer, T newValue);

    /**
     * Notifies all the registered listeners
     * @param newValue The value to be delivered in the notifications
     */
    void notifyAllObservers(T newValue);
}
