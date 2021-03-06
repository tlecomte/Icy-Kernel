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
package icy.gui.preferences;

import icy.plugin.PluginDescriptor;
import icy.plugin.PluginInstaller;
import icy.plugin.PluginInstaller.PluginInstallerListener;
import icy.plugin.PluginLoader;
import icy.plugin.PluginLoader.PluginLoaderEvent;
import icy.plugin.PluginLoader.PluginLoaderListener;
import icy.plugin.PluginRepositoryLoader;
import icy.plugin.PluginRepositoryLoader.PluginRepositoryLoaderListener;
import icy.plugin.PluginUpdater;

import java.util.ArrayList;

/**
 * @author Stephane
 */
public class PluginLocalPreferencePanel extends PluginListPreferencePanel implements PluginLoaderListener,
        PluginInstallerListener, PluginRepositoryLoaderListener
{
    private enum PluginLocalState
    {
        NULL, UPDATING, HAS_UPDATE, NO_UPDATE, CHECKING_UPDATE
    }

    /**
     * 
     */
    private static final long serialVersionUID = -6732331255351202922L;

    public static final String NODE_NAME = "Local Plugin";

    PluginLocalPreferencePanel(PreferenceFrame parent)
    {
        super(parent, NODE_NAME, PluginPreferencePanel.NODE_NAME);

        PluginRepositoryLoader.addListener(this);
        PluginLoader.addListener(this);
        PluginInstaller.addListener(this);

        // remove last column not used here
        table.removeColumn(table.getColumn(columnIds[4]));

        action1Button.setText("Delete");
        action1Button.setVisible(true);
        action2Button.setText("Check update");
        action2Button.setVisible(true);

        pluginsChanged();
    }

    @Override
    protected void closed()
    {
        super.closed();

        PluginRepositoryLoader.removeListener(this);
        PluginInstaller.removeListener(this);
        PluginLoader.removeListener(this);
    }

    private PluginLocalState getPluginLocalState(PluginDescriptor plugin)
    {
        if (plugin != null)
        {
            if (!PluginRepositoryLoader.isBasicLoaded())
                return PluginLocalState.CHECKING_UPDATE;

            // get online version
            final PluginDescriptor onlinePlugin = PluginUpdater.getUpdate(plugin);

            // udpate available ?
            if (onlinePlugin != null)
            {
                if (PluginInstaller.isInstallingPlugin(onlinePlugin))
                    return PluginLocalState.UPDATING;

                return PluginLocalState.HAS_UPDATE;
            }

            return PluginLocalState.NO_UPDATE;
        }

        return PluginLocalState.NULL;
    }

    @Override
    protected void doAction1(PluginDescriptor plugin)
    {
        // desinstall udpate
        PluginInstaller.desinstall(plugin, true);
        // refresh state
        refreshTableData();
        updateButtonsState();
    }

    @Override
    protected void doAction2(PluginDescriptor plugin)
    {
        switch (getPluginLocalState(plugin))
        {
            case HAS_UPDATE:
                final PluginDescriptor onlinePlugin = PluginUpdater.getUpdate(plugin);

                // install udpate
                PluginInstaller.install(onlinePlugin, true);
                // refresh state
                refreshTableData();
                updateButtonsState();
                break;
        }
    }

    @Override
    protected void repositoryChanged()
    {
        // do nothing here
    }

    @Override
    protected void reloadPlugins()
    {
        PluginLoader.reloadAsynch();
        updateButtonsState();
    }

    @Override
    protected String getStateValue(PluginDescriptor plugin)
    {
        if (plugin == null)
            return "";

        // special case where plugin is currently begin removed
        if (PluginInstaller.isDesinstallingPlugin(plugin))
            return "Deleting...";

        switch (getPluginLocalState(plugin))
        {
            case CHECKING_UPDATE:
                return "checking...";

            case UPDATING:
                return "updating...";

            case HAS_UPDATE:
                return "update available";

            case NO_UPDATE:
                return "up to date";
        }

        return "";
    }

    @Override
    protected ArrayList<PluginDescriptor> getPlugins()
    {
        final ArrayList<PluginDescriptor> result = PluginLoader.getPlugins();

        // only display installed plugins (this hide inner plugins)
        for (int i = result.size() - 1; i >= 0; i--)
            if (!result.get(i).isInstalled())
                result.remove(i);

        return result;
    }

    @Override
    protected void updateButtonsStateInternal()
    {
        super.updateButtonsStateInternal();

        if (PluginLoader.isLoading())
        {
            refreshButton.setText("Reloading...");
            refreshButton.setEnabled(false);
        }
        else
        {
            refreshButton.setText("Reload list");
            refreshButton.setEnabled(true);
        }

        final PluginDescriptor plugin = getSelectedPlugin();

        if (plugin == null)
        {
            action1Button.setEnabled(false);
            action2Button.setEnabled(false);
            return;
        }

        // special case where plugin is currently begin removed
        if (PluginInstaller.isDesinstallingPlugin(plugin))
        {
            action1Button.setText("Deleting...");
            action1Button.setEnabled(false);
        }
        else
        {
            action1Button.setText("Delete");
            action1Button.setEnabled(true);
        }

        switch (getPluginLocalState(plugin))
        {
            case CHECKING_UPDATE:
                action1Button.setEnabled(false);
                action2Button.setText("Checking...");
                action2Button.setEnabled(false);
                break;

            case UPDATING:
                action1Button.setEnabled(false);
                action2Button.setText("Updating...");
                action2Button.setEnabled(false);
                break;

            case HAS_UPDATE:
                action2Button.setText("Update");
                action2Button.setEnabled(true);
                break;

            case NO_UPDATE:
                action2Button.setText("No update");
                action2Button.setEnabled(false);
                break;

            case NULL:
                action1Button.setEnabled(false);
                action2Button.setEnabled(false);
                break;
        }

        // keep delete button enabled only if we can actually delete the plugin
        if (action1Button.isEnabled())
            action1Button.setEnabled(plugin.isInstalled());
    }

    @Override
    public void pluginLoaderChanged(PluginLoaderEvent e)
    {
        pluginsChanged();
    }

    @Override
    public void pluginInstalled(boolean success)
    {
        updateButtonsState();
    }

    @Override
    public void pluginRemoved(boolean success)
    {
        updateButtonsState();
    }

    @Override
    public void pluginRepositeryLoaderChanged()
    {
        refreshTableData();
    }
}
