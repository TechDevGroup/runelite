package net.runelite.client.plugins.runeutils;

import java.awt.Color;
import java.awt.Component;
import java.util.function.Consumer;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * Right-click context menu for profile management
 */
public class ProfileContextMenu extends JPopupMenu
{
	private final ProfileState profile;
	private final Consumer<ProfileState> onUpdate;
	private final Runnable onDelete;

	public ProfileContextMenu(ProfileState profile, Consumer<ProfileState> onUpdate, Runnable onDelete)
	{
		this.profile = profile;
		this.onUpdate = onUpdate;
		this.onDelete = onDelete;

		buildMenu();
	}

	private void buildMenu()
	{
		JMenuItem toggleItem = new JMenuItem(profile.isEnabled() ? "Disable" : "Enable");
		toggleItem.addActionListener(e -> handleToggleEnabled());
		add(toggleItem);

		addSeparator();

		JMenuItem renameItem = new JMenuItem("Rename...");
		renameItem.addActionListener(e -> handleRename());
		add(renameItem);

		addSeparator();

		JMenu interfaceStateMenu = new JMenu("Interface State");
		for (InterfaceState state : InterfaceState.values())
		{
			JCheckBoxMenuItem stateItem = new JCheckBoxMenuItem(state.getDisplayName());
			stateItem.setSelected(profile.getRequiredInterfaceState() == state);
			stateItem.addActionListener(e -> handleSetInterfaceState(state));
			interfaceStateMenu.add(stateItem);
		}
		add(interfaceStateMenu);

		addSeparator();

		JCheckBoxMenuItem previewItem = new JCheckBoxMenuItem("Preview Mode");
		previewItem.setSelected(profile.isPreviewMode());
		previewItem.setToolTipText("Show overlays even when interface state doesn't match");
		previewItem.addActionListener(e -> handleTogglePreviewMode(previewItem.isSelected()));
		add(previewItem);

		addSeparator();

		JCheckBoxMenuItem prioritizedItem = new JCheckBoxMenuItem("Mark as Prioritized");
		prioritizedItem.setSelected(profile.isPrioritized());
		prioritizedItem.addActionListener(e -> handleTogglePrioritized(prioritizedItem.isSelected()));
		add(prioritizedItem);

		addSeparator();

		JMenuItem deleteItem = new JMenuItem("Delete Profile");
		deleteItem.setForeground(Color.RED);
		deleteItem.addActionListener(e -> handleDelete());
		add(deleteItem);
	}

	private void handleToggleEnabled()
	{
		profile.setEnabled(!profile.isEnabled());
		onUpdate.accept(profile);
	}

	private void handleRename()
	{
		Component parentWindow = SwingUtilities.getWindowAncestor(this.getInvoker());
		String newName = (String) JOptionPane.showInputDialog(
			parentWindow,
			"Enter new profile name:",
			"Rename Profile",
			JOptionPane.PLAIN_MESSAGE,
			null,
			null,
			profile.getName()
		);

		if (newName != null && !newName.trim().isEmpty())
		{
			profile.setName(newName.trim());
			onUpdate.accept(profile);
		}
	}

	private void handleTogglePrioritized(boolean prioritized)
	{
		profile.setPrioritized(prioritized);
		onUpdate.accept(profile);
	}

	private void handleSetInterfaceState(InterfaceState state)
	{
		profile.setRequiredInterfaceState(state);
		onUpdate.accept(profile);
	}

	private void handleTogglePreviewMode(boolean preview)
	{
		profile.setPreviewMode(preview);
		onUpdate.accept(profile);
	}

	private void handleDelete()
	{
		Component parentWindow = SwingUtilities.getWindowAncestor(this.getInvoker());
		int result = JOptionPane.showConfirmDialog(
			parentWindow,
			"Delete profile \"" + profile.getName() + "\"?",
			"Confirm Deletion",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE
		);

		if (result == JOptionPane.YES_OPTION)
		{
			onDelete.run();
		}
	}
}
