/*
 * Copyright 2010, 2011 Institut Pasteur.
 * 
 * This file is part of ICY.
 * 
 * ICY is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * ICY is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ICY. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.gui.main;

import java.util.EventListener;

public interface MainListener extends EventListener
{
    /**
     * A plugin has been started
     */
    public void pluginOpened(MainEvent event);

    /**
     * A plugin has ended
     */
    public void pluginClosed(MainEvent event);

    /**
     * A viewer has been opened
     */
    public void viewerOpened(MainEvent event);

    /**
     * A viewer just got the focus
     */
    public void viewerFocused(MainEvent event);

    /**
     * A viewer has been closed
     */
    public void viewerClosed(MainEvent event);

    /**
     * A sequence has been opened
     */
    public void sequenceOpened(MainEvent event);

    /**
     * A sequence just got the focus
     */
    public void sequenceFocused(MainEvent event);

    /**
     * A sequence has been closed
     */
    public void sequenceClosed(MainEvent event);

    /**
     * A ROI has been added to its first sequence
     */
    public void roiAdded(MainEvent event);

    /**
     * A ROI has been removed from its last sequence
     */
    public void roiRemoved(MainEvent event);

    /**
     * A painter has been added to its first sequence
     */
    public void painterAdded(MainEvent event);

    /**
     * A painter has been removed from its last sequence
     */
    public void painterRemoved(MainEvent event);
}
