package net.supersnetwork.actiondeck.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.supersnetwork.actiondeck.data.ActionDeckCardDefinitions;
import net.supersnetwork.actiondeck.item.ActionDeckItems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DeckStackBlockEntity extends BlockEntity {
	public static final String CARDS_KEY = "Cards";

	private final List<Identifier> cards = new ArrayList<>();

	public DeckStackBlockEntity(BlockPos pos, BlockState state) {
		super(ActionDeckBlockEntities.DECK_STACK, pos, state);
	}

	public int size() {
		return cards.size();
	}

	public boolean isEmpty() {
		return cards.isEmpty();
	}

	public List<Identifier> getCards() {
		return List.copyOf(cards);
	}

	public Optional<Identifier> getTopCardId() {
		return cards.isEmpty() ? Optional.empty() : Optional.of(cards.get(cards.size() - 1));
	}

	public void addCard(Identifier cardId) {
		cards.add(cardId);
		sync();
	}

	public void addCards(List<Identifier> cardIds) {
		cards.addAll(cardIds);
		sync();
	}

	public ItemStack popTopCard() {
		if (cards.isEmpty()) {
			return ItemStack.EMPTY;
		}

		Identifier cardId = cards.remove(cards.size() - 1);
		sync();
		return net.supersnetwork.actiondeck.item.Card.createCard(cardId);
	}

	public void shuffle() {
		Collections.shuffle(cards);
		sync();
	}

	public ItemStack createDeckStack() {
		ItemStack stack = new ItemStack(ActionDeckBlocks.DECK_STACK);
		writeCardsToStack(stack, cards);
		return stack;
	}

	public static boolean isCard(ItemStack stack) {
		return stack.isOf(ActionDeckItems.CARD) && net.supersnetwork.actiondeck.item.Card.getCardId(stack).isPresent();
	}

	public static boolean isDeckStack(ItemStack stack) {
		return stack.isOf(ActionDeckBlocks.DECK_STACK.asItem());
	}

	public static List<Identifier> readCardsFromStack(ItemStack stack) {
		List<Identifier> result = new ArrayList<>();
		NbtCompound nbt = stack.getNbt();

		if (nbt == null || !nbt.contains(CARDS_KEY)) {
			return result;
		}

		NbtList list = nbt.getList(CARDS_KEY, NbtElement.STRING_TYPE);
		for (int i = 0; i < list.size(); i++) {
			parseIdentifier(list.getString(i)).ifPresent(result::add);
		}

		return result;
	}

	public static void writeCardsToStack(ItemStack stack, List<Identifier> cards) {
		NbtCompound nbt = stack.getOrCreateNbt();
		NbtList list = new NbtList();

		for (Identifier card : cards) {
			list.add(NbtString.of(card.toString()));
		}

		nbt.put(CARDS_KEY, list);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		cards.clear();

		NbtList list = nbt.getList(CARDS_KEY, NbtElement.STRING_TYPE);
		for (int i = 0; i < list.size(); i++) {
			parseIdentifier(list.getString(i)).ifPresent(cards::add);
		}
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		writeCards(nbt);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}

	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	private void sync() {
		markDirty();
		if (world != null && !world.isClient) {
			world.updateListeners(pos, getCachedState(), getCachedState(), 3);
		}
	}

	private void writeCards(NbtCompound nbt) {
		NbtList list = new NbtList();
		for (Identifier card : cards) {
			list.add(NbtString.of(card.toString()));
		}

		nbt.put(CARDS_KEY, list);
	}

	private static Optional<Identifier> parseIdentifier(String value) {
		try {
			Identifier id = new Identifier(value);
			if (ActionDeckCardDefinitions.contains(id)) {
				return Optional.of(id);
			}
			return Optional.of(id);
		} catch (Exception ignored) {
			return Optional.empty();
		}
	}
}
