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

import icy.common.AcceptListener;
import icy.common.EventHierarchicalChecker;
import icy.common.WeakAcceptListener;
import icy.gui.inspector.InspectorPanel;
import icy.gui.menu.ApplicationMenu;
import icy.gui.menu.ToolRibbonTask;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.painter.Painter;
import icy.plugin.abstract_.Plugin;
import icy.preferences.XMLPreferences;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.swimmingPool.SwimmingPool;

import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 * MainInterface
 * 
 * @see icy.gui.main.MainInterfaceGui
 * @author Fabrice de Chaumont & Stephane
 */
public interface MainInterface
{
    /**
     * Creates the windows in the Icy.getMainInterface()
     */
    public abstract void init();

    /**
     * check if exit is allowed from registered listeners
     */
    public abstract boolean canExitExternal();

    /**
     * Return all internal frames
     */
    public abstract ArrayList<JInternalFrame> getInternalFrames();

    /**
     * Return all external frames
     */
    public abstract ArrayList<JFrame> getExternalFrames();

    public abstract XMLPreferences getPreferences();

    /**
     * Return the inspector object (right informations panel)
     */
    public abstract InspectorPanel getInspector();

    /**
     * Return the currently active plugins
     */
    public abstract ArrayList<Plugin> getActivePlugins();

    /**
     * @return the current focused viewer
     */
    public abstract Viewer getFocusedViewer();

    /**
     * @return the current focused sequence
     */
    public abstract Sequence getFocusedSequence();

    /**
     * @return the current focused image
     */
    public abstract IcyBufferedImage getFocusedImage();

    /**
     * Return all active viewers
     */
    public abstract ArrayList<Viewer> getViewers();

    /**
     * Return all active viewers(HashSet form)
     */
    public abstract HashSet<Viewer> getViewerSet();

    /**
     * Set focus on specified viewer
     * 
     * @param viewer
     *        viewer which received focus
     */
    public abstract void setFocusedViewer(Viewer viewer);

    /**
     * Add the frame to the Desktop pane and change its layer value to make it over the other
     * internal frames.
     * 
     * @param internalFrame
     */
    public abstract void addToDesktopPane(JInternalFrame internalFrame);

    public abstract IcyDesktopPane getDesktopPane();

    public abstract ApplicationMenu getApplicationMenu();

    public abstract TaskFrameManager getTaskWindowManager();

    @Deprecated
    public abstract void registerExternalFrame(JFrame frame);

    @Deprecated
    public abstract void unRegisterExternalFrame(JFrame frame);

    public abstract void registerPlugin(Plugin plugin);

    public abstract void unRegisterPlugin(Plugin plugin);

    public abstract void registerViewer(Viewer viewer);

    public abstract void unRegisterViewer(Viewer viewer);

    // public abstract void registerStreamPlugin(PluginStreamGenerator pluginStreamGenerator);

    public abstract MainFrame getFrame();

    /**
     * Close viewers attached to specified sequence
     */
    public abstract void closeViewersOfSequence(Sequence sequence);

    /**
     * Close all viewers
     */
    public abstract void closeAllViewers();

    /**
     * Return first viewer for the sequence containing specified ROI
     */
    public abstract Viewer getFirstViewerContaining(ROI roi);

    /**
     * Return first viewer for the sequence containing specified Painter
     */
    public abstract Viewer getFirstViewerContaining(Painter painter);

    /**
     * Return first viewer attached to specified sequence
     */
    public abstract Viewer getFirstViewer(Sequence sequence);

    /**
     * Return viewers attached to specified sequence
     */
    public abstract ArrayList<Viewer> getViewers(Sequence sequence);

    /**
     * Return true if specified viewer is the unique viewer for its attached sequence
     */
    public abstract boolean isUniqueViewer(Viewer viewer);

    /**
     * Return the active viewer (should be the same as focused one)
     */
    public abstract Viewer getActiveViewer();

    /**
     * Return list of active sequence (displayed in a viewer)
     */
    public abstract ArrayList<Sequence> getSequences();

    /**
     * Return list of active sequence (displayed in a viewer) in HashSet form
     */
    public abstract HashSet<Sequence> getSequenceSet();

    /**
     * Return true if specified sequence is currently opened (displayed in a viewer)
     */
    public abstract boolean isOpened(Sequence sequence);

    /**
     * Use {@link #getFirstSequenceContaining(ROI)} instead
     * 
     * @deprecated
     */
    @Deprecated
    public abstract Sequence getFirstSequencesContaining(ROI roi);

    /**
     * Use {@link #getFirstSequenceContaining(Painter)} instead
     * 
     * @deprecated
     */
    @Deprecated
    public abstract Sequence getFirstSequencesContaining(Painter painter);

    /**
     * Return the first active sequence containing the specified ROI
     */
    public abstract Sequence getFirstSequenceContaining(ROI roi);

    /**
     * Return the first active sequence containing the specified Painter
     */
    public abstract Sequence getFirstSequenceContaining(Painter painter);

    /**
     * Return all active sequence containing the specified ROI
     */
    public abstract ArrayList<Sequence> getSequencesContaining(ROI roi);

    /**
     * Return all active sequence containing the specified Painter
     */
    public abstract ArrayList<Sequence> getSequencesContaining(Painter painter);

    /**
     * Return all active ROI
     */
    public abstract ArrayList<ROI> getROIs();

    /**
     * Return all active ROI (HashSet form)
     */
    public abstract HashSet<ROI> getROISet();

    /**
     * Return the ROI containing the specified painter (if any)
     */
    public abstract ROI getROI(Painter painter);

    /**
     * Return all active Painter
     */
    public abstract ArrayList<Painter> getPainters();

    /**
     * Return all active Painter (HashSet form)
     */
    public abstract HashSet<Painter> getPainterSet();

    public abstract SwimmingPool getSwimmingPool();

    public abstract String getSelectedTool();

    public abstract void setSelectedTool(String command);

    public abstract ToolRibbonTask getToolRibbon();

    public abstract boolean isAlwaysOnTop();

    public abstract void setAlwaysOnTop(boolean value);

    /**
     * Add main listener
     */
    public abstract void addListener(MainListener listener);

    /**
     * Remove main listener
     */
    public abstract void removeListener(MainListener listener);

    /**
     * Add "can exit" listener.<br>
     * <br>
     * CAUTION : A weak reference is used to reference the listener for easier release<br>
     * so you should have a hard reference to your listener to keep it alive
     */
    public abstract void addCanExitListener(AcceptListener listener);

    /**
     * Remove "can exit" listener
     */
    public abstract void removeCanExitListener(AcceptListener listener);

    /**
     * Internal remove "can exit" listener (used internally only)
     */
    public abstract void internalRemoveCanExitListener(WeakAcceptListener listener);

    /**
     * Add focused viewer listener.<br>
     * This permit to receive events of focused viewer only.<br>
     * It can also be used to detect viewer focus change.
     */
    public abstract void addFocusedViewerListener(FocusedViewerListener listener);

    /**
     * Remove focused viewer listener.
     */
    public abstract void removeFocusedViewerListener(FocusedViewerListener listener);

    /**
     * Add focused sequence listener.<br>
     * This permit to receive events of focused sequence only.<br>
     * It can also be used to detect sequence focus change.
     */
    public abstract void addFocusedSequenceListener(FocusedSequenceListener listener);

    /**
     * Remove focused sequence listener.
     */
    public abstract void removeFocusedSequenceListener(FocusedSequenceListener listener);

    /**
     * Start update.
     */
    public abstract void beginUpdate();

    /**
     * End update.
     */
    public abstract void endUpdate();

    /**
     * Return true if main interface is currently begin in update state
     */
    public abstract boolean isUpdating();

    public abstract void onChanged(EventHierarchicalChecker object);

}