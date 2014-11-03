/*
 * JFileSync
 * Copyright (C) 2002-2007, Jens Heidrich
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA, 02110-1301, USA
 */

package jfs.sync;

import java.util.Vector;
import jfs.JFileSync;
import jfs.conf.JFSConst;
import jfs.conf.JFSSettings;
import jfs.conf.JFSText;

/**
 * Visits selected steps of the file comparison and synchronization algorithm in order to compute the state of the
 * progression.
 * 
 * @author Jens Heidrich
 * @version $Id: JFSProgress.java,v 1.24 2007/07/20 12:27:52 heidrich Exp $
 */
public class JFSProgress {

    /** Stores the only instance of the class. */
    private static JFSProgress instance = null;

    /** The current activity */
    private ProgressActivity activity = ProgressActivity.INITIALIZATION;

    /** The current state */
    private ProgressState state = ProgressState.ACTIVE;

    /** Duration to compute the table. */
    private long duration = 0;

    /** Starting time. */
    private long startTime = 0;

    /** Determines whether the algorithm is canceled or not. */
    private boolean isCanceled = false;

    /** Vector with all oberservers of the alogorithm's progress. */
    private Vector<JFSProgressObserver> observers = new Vector<JFSProgressObserver>();

    /** The time when the observers were updated last. */
    private long updateTime = 0;


    /** The different activities performed by the synchronization algorithm. */
    public enum ProgressActivity {
        INITIALIZATION("progress.init.title"), COMPARISON("progress.comparison.title"), SYNCHRONIZATION_DELETE(
                "progress.delete.title"), SYNCHRONIZATION_COPY("progress.copy.title");
        private String name;


        ProgressActivity(String name) {
            this.name = name;
        }


        public String getName() {
            return name;
        }
    }

    /** The states of the progress object. */
    public enum ProgressState {
        PREPARATION, ACTIVE, DONE
    }

    /**
     * Creates a new progress object.
     */
    private JFSProgress() {
    }


    /**
     * Returns the reference of the only instance.
     * 
     * @return The only instance.
     */
    public final static JFSProgress getInstance() {
        if (instance==null) {
            instance = new JFSProgress();
        }

        return instance;
    }


    /**
     * Prepares starting a progress computation for a certain type of activity.
     * 
     * @param activity
     *            The type of activity to start.
     */
    final void prepare(ProgressActivity activity) {
        this.activity = activity;
        this.state = ProgressState.PREPARATION;
        isCanceled = false;
        updateTime = 0;
        duration = 0;
        update();
    }


    /**
     * Starts a progress computation.
     */
    final void start() {
        state = ProgressState.ACTIVE;
        startTime = System.currentTimeMillis();
        update();
    }


    /**
     * Ends a progress computation.
     */
    final void end() {
        state = ProgressState.DONE;
        duration = System.currentTimeMillis()-startTime;
        update();
        if (JFSSettings.getInstance().isDebug()) {
            System.out.println("..."+duration+"ms");
        }
    }


    /**
     * @return Returns the current activity.
     */
    public final ProgressActivity getActivity() {
        return activity;
    }


    /**
     * @return Returns the current state of the current activity.
     */
    public final ProgressState getState() {
        return state;
    }


    /**
     * @return Returns duration for the last performed activity.
     */
    public final String getDuration() {
        return getTime(duration);
    }


    /**
     * @return Returns the predicted remaining time for completing the currently performed algorithm.
     */
    public final String getRemainingTime() {
        int ratio = getCompletionRatio();

        if (ratio>0) {
            long time = (System.currentTimeMillis()-startTime)*(100-ratio)/ratio;
            return getTime(time);
        }
        return "";
    }


    /**
     * @return Returns the completion ratio in percent between 0% and 100%.
     */
    public final int getCompletionRatio() {
        JFSComparisonMonitor comparison = JFSComparisonMonitor.getInstance();
        JFSDeleteMonitor delete = JFSDeleteMonitor.getInstance();
        JFSCopyMonitor copy = JFSCopyMonitor.getInstance();

        if (activity==ProgressActivity.COMPARISON) {
            return comparison.getRatio();
        } else if (activity==ProgressActivity.SYNCHRONIZATION_DELETE) {
            return delete.getRatio();
        } else if (activity==ProgressActivity.SYNCHRONIZATION_COPY) {
            return copy.getRatio();
        } else {
            return 0;
        }
    }


    /**
     * This method is called if an object wants to cancel the algorithm. After this request the algorithm stops as soon
     * as possible at predefined milestones.
     */
    public final void cancel() {
        isCanceled = true;

        // Shut down producers at once in order to avoid waiting for time out:
        JFSFileProducerManager pm = JFSFileProducerManager.getInstance();
        if (activity==ProgressActivity.COMPARISON) {
            JFSComparisonMonitor cm = JFSComparisonMonitor.getInstance();
            pm.cancelProducer(cm.getRootUriSrc());
            pm.cancelProducer(cm.getRootUriTgt());
        } else if (activity==ProgressActivity.SYNCHRONIZATION_COPY) {
            JFSCopyMonitor cm = JFSCopyMonitor.getInstance();
            if (cm.getCurrentSrc()!=null) {
                JFSFileProducer producer = cm.getCurrentSrc().getFileProducer();
                pm.cancelProducer(producer.getUri());
            }
            if (cm.getCurrentTgt()!=null) {
                JFSFileProducer producer = cm.getCurrentTgt().getFileProducer();
                pm.cancelProducer(producer.getUri());
            }
        } else if (activity==ProgressActivity.SYNCHRONIZATION_DELETE) {
            JFSDeleteMonitor dm = JFSDeleteMonitor.getInstance();
            if (dm.getCurrentFile()!=null) {
                JFSFileProducer producer = dm.getCurrentFile().getFileProducer();
                pm.cancelProducer(producer.getUri());
            }
        }
    }


    /**
     * Determines whether the algorithm is canceled or not. If so, the algorithm stops as soon as possible at the next
     * predefined milestone.
     * 
     * @return True if and only if the algorithm was cancelled.
     */
    public final boolean isCanceled() {
        return isCanceled;
    }


    /**
     * Attaches an additional observer.
     * 
     * @param observer
     *            The new observer.
     */
    public final void attach(JFSProgressObserver observer) {
        observers.add(observer);
    }


    /**
     * Detaches an existing observer.
     * 
     * @param observer
     *            An old observer.
     */
    public final void detach(JFSProgressObserver observer) {
        observers.remove(observer);
    }


    /**
     * Sends a message to all existing observers that the algorithm's state was updated, if and only if a minimum time
     * period between two subsequent updates is gone.
     */
    final void fireUpdate() {
        if ((System.currentTimeMillis()-updateTime)>=JFSConst.PROGRESS_UPDATE) {
            updateTime = System.currentTimeMillis();
            update();
        }
    }


    /**
     * Updates the current state of the algorithm for all existing observers.
     */
    private final void update() {
        // Wait if debugging is enabled and output progress information:
        if (JFSSettings.getInstance().isDebug()) {
            JFileSync.busyWait(1000);
            System.out.println(getActivity()+", "+getState()+", "+getCompletionRatio()+"%");
        }

        for (JFSProgressObserver po : observers) {
            po.update(this);
        }
    }


    /**
     * Returns the formatted time in hours, minutes, and seconds for a given time distance.
     * 
     * @param time
     *            The time in ms to format.
     * @return The formatted time.
     */
    private final String getTime(long time) {
        JFSText t = JFSText.getInstance();
        String s = "";
        long millis = time%1000;
        long seconds = (time/1000)%60;
        long minutes = (time/60000)%60;
        long hours = time/3600000;

        if (millis>=500) {
            seconds++ ;
        }
        if (hours>0) {
            s += hours+t.get("general.hours");
        }
        if (minutes>0) {
            s += minutes+t.get("general.minutes");
        }
        s += seconds+t.get("general.seconds");

        return s;
    }
    
}