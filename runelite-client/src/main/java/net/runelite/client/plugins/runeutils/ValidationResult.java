package net.runelite.client.plugins.runeutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ValidationResult
{
	private int totalRequirements;
	private int matchedCount;
	private int mismatchedCount;
	private Map<Integer, Boolean> slotResults = new HashMap<>();
	private List<TrackedItemState> unmatchedStates = new ArrayList<>();

	public boolean satisfies(PresenceMode mode, int threshold)
	{
		switch (mode)
		{
			case ALL:
				return mismatchedCount == 0;
			case ANY:
				return matchedCount > 0;
			case AT_LEAST_N:
				return matchedCount >= threshold;
			default:
				return false;
		}
	}

	public void recordMatch(int slot)
	{
		matchedCount++;
		totalRequirements++;
		slotResults.put(slot, true);
	}

	public void recordMismatch(int slot, TrackedItemState unmatched)
	{
		mismatchedCount++;
		totalRequirements++;
		slotResults.put(slot, false);
		if (unmatched != null)
		{
			unmatchedStates.add(unmatched);
		}
	}
}
