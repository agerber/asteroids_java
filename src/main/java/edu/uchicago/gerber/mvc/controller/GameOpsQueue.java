package edu.uchicago.gerber.mvc.controller;

import edu.uchicago.gerber.mvc.model.Movable;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Effectively a Queue that enqueues and dequeues Game Operations (add/remove)
 */
public class GameOpsQueue extends LinkedList<GameOp> {

    //this data structure is in contention by the "Event Dispatch" thread aka main-swing-thread, and the animation
    // thread.  For example, enqueues may happen either as a result of a keystroke such as a bullet-fire
    // (main-swing-thread) or a collision (animation-thread). We must restrict access to it by one thread at a
    // time by using a Lock.
    private final Lock lock;

    public GameOpsQueue() {
        lock =   new ReentrantLock();
    }


    public void enqueue(Movable mov, GameOp.Action action) {
       try {
            lock.lock();
            addLast(new GameOp(mov, action));
        } finally {
            lock.unlock();
        }
    }


    public GameOp dequeue() {
        try {
            lock.lock();
               return removeFirst();
        } finally {
            lock.unlock();
        }

    }
}
