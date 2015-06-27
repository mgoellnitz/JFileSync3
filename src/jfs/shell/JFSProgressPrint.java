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
package jfs.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import jfs.conf.JFSLog;
import jfs.conf.JFSText;
import jfs.sync.JFSComparisonMonitor;
import jfs.sync.JFSCopyMonitor;
import jfs.sync.JFSDeleteMonitor;
import jfs.sync.JFSFormatter;
import jfs.sync.JFSProgress;
import jfs.sync.JFSProgress.ProgressActivity;
import jfs.sync.JFSProgressObserver;


/**
 * A simple observer of the algorithm's progress that prints the state to standard out.
 *
 * @author Jens Heidrich
 * @version $Id: JFSProgressPrint.java,v 1.9 2007/07/20 14:07:10 heidrich Exp $
 */
public class JFSProgressPrint implements JFSProgressObserver {

    /**
     * The stream for controling cancel operations.
     */
    private BufferedReader din = new BufferedReader(new InputStreamReader(System.in));


    /**
     * @see JFSProgressObserver#update(JFSProgress)
     */
    @Override
    public final void update(JFSProgress progress) {
        // Get the translation object:
        JFSText t = JFSText.getInstance();

        // Update the command string currently performed:
        String stateString = t.get("progress.processing");

        ProgressActivity activity = progress.getActivity();
        boolean nothingToDisplay = false;
        if (activity==ProgressActivity.COMPARISON) {
            JFSComparisonMonitor m = JFSComparisonMonitor.getInstance();
            if (m.getCurrentSrc()==null&&m.getCurrentTgt()==null) {
                nothingToDisplay = true;
            } else {
                stateString = t.get("progress.compare")+" "+JFSFormatter.adaptPath(m.getCurrentSrc(), 25)+" "
                        +t.get("progress.compare.connector")+" "+JFSFormatter.adaptPath(m.getCurrentTgt(), 25);
            }
        } else if (activity==ProgressActivity.SYNCHRONIZATION_DELETE) {
            JFSDeleteMonitor m = JFSDeleteMonitor.getInstance();
            if (m.getCurrentFile()==null) {
                nothingToDisplay = true;
            } else {
                stateString = t.get("progress.delete")+" "+JFSFormatter.adaptPath(m.getCurrentFile(), 25);
            }
        } else if (activity==ProgressActivity.SYNCHRONIZATION_COPY) {
            JFSCopyMonitor m = JFSCopyMonitor.getInstance();
            if (m.getCurrentSrc()==null&&m.getCurrentTgt()==null) {
                nothingToDisplay = true;
            } else {
                stateString = t.get("progress.copy")+" "+JFSFormatter.adaptPath(m.getCurrentSrc(), 25)+" "
                        +t.get("progress.copy.connector")+" "+JFSFormatter.adaptPath(m.getCurrentTgt(), 25);
            }
        }

        PrintStream out = JFSLog.getOut().getStream();

        if (!nothingToDisplay) {
            int percentageValue = progress.getCompletionRatio();
            String percentage = String.valueOf(percentageValue);

            if (percentageValue<10) {
                percentage = "  "+percentage;
            } else {
                if (percentageValue<100) {
                    percentage = " "+percentage;
                }
            }

            out.print("["+percentage+"%] "+stateString+" ");
            out.println();
        }

        // Ask for canceling the algorithm if 'return' is entered:
        try {
            if (din.ready()) {
                // Skip first enter:
                din.readLine();

                // Read answer:
                String input = "";
                String yes = t.get("cmd.progress.cancel.yes");
                String no = t.get("cmd.progress.cancel.no");

                while (!input.equals(yes)&&!input.equals(no)) {
                    out.println(t.get("cmd.progress.cancel"));
                    out.print(t.get("cmd.progress.cancel.input")+" ");
                    input = din.readLine().toLowerCase();
                }

                if (input.equals(yes)) {
                    out.println(t.get("cmd.progress.cancel.request"));
                    out.println();
                    progress.cancel();
                } else {
                    out.println();
                }
            }
        } catch (IOException e) {
            // Just continue...
        }
    }

}
