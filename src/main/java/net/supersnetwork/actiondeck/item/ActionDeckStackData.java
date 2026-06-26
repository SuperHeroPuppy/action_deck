package net.supersnetwork.actiondeck.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.function.Consumer;

public final class ActionDeckStackData {
	private ActionDeckStackData() {
	}

	public static NbtCompound get(ItemStack stack) {
		NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
		return component == null ? null : component.copyNbt();
	}

	public static NbtCompound getOrCreate(ItemStack stack) {
		NbtCompound nbt = get(stack);
		return nbt == null ? new NbtCompound() : nbt;
	}

	public static void set(ItemStack stack, NbtCompound nbt) {
		stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
	}

	public static void edit(ItemStack stack, Consumer<NbtCompound> editor) {
		NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, editor);
	}
}
