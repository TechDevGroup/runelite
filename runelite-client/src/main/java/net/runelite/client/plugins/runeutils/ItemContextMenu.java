package net.runelite.client.plugins.runeutils;

import net.runelite.client.ui.ColorScheme;
import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ItemContextMenu extends JPopupMenu
{
	private final TrackedItemState itemState;
	private final Consumer<TrackedItemState> onUpdate;
	private final Runnable onRemove;

	public ItemContextMenu(TrackedItemState itemState, Consumer<TrackedItemState> onUpdate, Runnable onRemove)
	{
		this.itemState = itemState;
		this.onUpdate = onUpdate;
		this.onRemove = onRemove;

		buildMenu();
	}

	private void buildMenu()
	{
		JMenu quantityMenu = new JMenu("Set Quantity");

		JMenuItem exactItem = new JMenuItem("Exact...");
		exactItem.addActionListener(e -> showQuantityDialog(QuantityCondition.EXACT));
		quantityMenu.add(exactItem);

		JMenuItem atLeastItem = new JMenuItem("At Least...");
		atLeastItem.addActionListener(e -> showQuantityDialog(QuantityCondition.AT_LEAST));
		quantityMenu.add(atLeastItem);

		JMenuItem atMostItem = new JMenuItem("At Most...");
		atMostItem.addActionListener(e -> showQuantityDialog(QuantityCondition.AT_MOST));
		quantityMenu.add(atMostItem);

		JMenuItem betweenItem = new JMenuItem("Between...");
		betweenItem.addActionListener(e -> showBetweenDialog());
		quantityMenu.add(betweenItem);

		JMenuItem anyAmountItem = new JMenuItem("Any Amount");
		anyAmountItem.addActionListener(e -> updateCondition(QuantityCondition.ANY, 1, 1));
		quantityMenu.add(anyAmountItem);

		add(quantityMenu);

		addSeparator();

		JCheckBoxMenuItem requireSlotItem = new JCheckBoxMenuItem("Require Specific Slot");
		requireSlotItem.setSelected(itemState.hasValidationFlag(ValidationFlag.REQUIRE_POSITION));
		requireSlotItem.addActionListener(e -> toggleRequirePosition(requireSlotItem.isSelected()));
		add(requireSlotItem);

		addSeparator();

		JMenuItem removeItem = new JMenuItem("Remove from Profile");
		removeItem.setForeground(Color.RED);
		removeItem.addActionListener(e -> {
			Component parentWindow = SwingUtilities.getWindowAncestor(this.getInvoker());
			int result = JOptionPane.showConfirmDialog(
				parentWindow,
				"Remove this item from the profile?",
				"Confirm Removal",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE
			);

			if (result == JOptionPane.YES_OPTION)
			{
				onRemove.run();
			}
		});
		add(removeItem);
	}

	private void showQuantityDialog(QuantityCondition condition)
	{
		String prompt;
		switch (condition)
		{
			case EXACT:
				prompt = "Enter exact quantity:";
				break;
			case AT_LEAST:
				prompt = "Enter minimum quantity:";
				break;
			case AT_MOST:
				prompt = "Enter maximum quantity:";
				break;
			default:
				return;
		}

		Component parentWindow = SwingUtilities.getWindowAncestor(this.getInvoker());
		String input = JOptionPane.showInputDialog(
			parentWindow,
			prompt,
			"Set Quantity",
			JOptionPane.QUESTION_MESSAGE
		);

		if (input != null && !input.trim().isEmpty())
		{
			try
			{
				int quantity = Integer.parseInt(input.trim());
				if (quantity > 0)
				{
					updateCondition(condition, quantity, quantity);
				}
				else
				{
					JOptionPane.showMessageDialog(
						parentWindow,
						"Quantity must be greater than 0",
						"Invalid Input",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
			catch (NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(
					parentWindow,
					"Please enter a valid number",
					"Invalid Input",
					JOptionPane.ERROR_MESSAGE
				);
			}
		}
	}

	private void showBetweenDialog()
	{
		JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
		panel.add(new JLabel("Minimum:"));
		JTextField minField = new JTextField(String.valueOf(itemState.getQuantity()));
		panel.add(minField);

		panel.add(new JLabel("Maximum:"));
		JTextField maxField = new JTextField(String.valueOf(itemState.getQuantityMax()));
		panel.add(maxField);

		Component parentWindow = SwingUtilities.getWindowAncestor(this.getInvoker());
		int result = JOptionPane.showConfirmDialog(
			parentWindow,
			panel,
			"Set Quantity Range",
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.QUESTION_MESSAGE
		);

		if (result == JOptionPane.OK_OPTION)
		{
			try
			{
				int min = Integer.parseInt(minField.getText().trim());
				int max = Integer.parseInt(maxField.getText().trim());

				if (min > 0 && max > 0 && min <= max)
				{
					updateCondition(QuantityCondition.BETWEEN, min, max);
				}
				else
				{
					JOptionPane.showMessageDialog(
						parentWindow,
						"Invalid range. Minimum must be ≤ Maximum, and both must be > 0",
						"Invalid Input",
						JOptionPane.ERROR_MESSAGE
					);
				}
			}
			catch (NumberFormatException ex)
			{
				JOptionPane.showMessageDialog(
					parentWindow,
					"Please enter valid numbers",
					"Invalid Input",
					JOptionPane.ERROR_MESSAGE
				);
			}
		}
	}

	private void updateCondition(QuantityCondition condition, int minQuantity, int maxQuantity)
	{
		TrackedItemState updatedState = new TrackedItemState(itemState.getItemId(), itemState.getItemName());
		updatedState.setSlot(itemState.getSlot());
		updatedState.setQuantity(minQuantity);
		updatedState.setQuantityMax(maxQuantity);
		updatedState.setQuantityCondition(condition);

		// Copy validation flags
		for (ValidationFlag flag : itemState.getValidationFlags())
		{
			updatedState.addValidationFlag(flag);
		}

		onUpdate.accept(updatedState);
	}

	private void toggleRequirePosition(boolean require)
	{
		TrackedItemState updatedState = new TrackedItemState(itemState.getItemId(), itemState.getItemName());
		updatedState.setSlot(itemState.getSlot());
		updatedState.setQuantity(itemState.getQuantity());
		updatedState.setQuantityMax(itemState.getQuantityMax());
		updatedState.setQuantityCondition(itemState.getQuantityCondition());

		// Copy validation flags
		for (ValidationFlag flag : itemState.getValidationFlags())
		{
			updatedState.addValidationFlag(flag);
		}

		// Toggle REQUIRE_POSITION flag
		if (require)
		{
			updatedState.addValidationFlag(ValidationFlag.REQUIRE_POSITION);
		}
		else
		{
			updatedState.removeValidationFlag(ValidationFlag.REQUIRE_POSITION);
		}

		onUpdate.accept(updatedState);
	}
}
