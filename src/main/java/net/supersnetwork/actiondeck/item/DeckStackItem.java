package net.supersnetwork.actiondeck.item;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import net.supersnetwork.actiondeck.block.DeckStackBlockEntity;

import java.util.List;

public class DeckStackItem extends BlockItem {
	public DeckStackItem(Block block, Settings settings) {
		super(block, settings.maxCount(1));
	}

	@Override
	public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
		super.appendTooltip(stack, world, tooltip, context);
		int cardCount = DeckStackBlockEntity.readCardsFromStack(stack).size();
		tooltip.add(Text.literal("Cards: ")
			.formatted(Formatting.GRAY)
			.append(Text.literal(Integer.toString(cardCount)).formatted(Formatting.WHITE)));
	}
}
