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
	public static final String FACE_DOWN_KEY = "FaceDown";

	private final List<Identifier> cards = new ArrayList<>();
	private boolean faceDown;

	public DeckStackBlockEntity(BlockPos pos, BlockState state) {
		super(ActionDeckBlockEntities.DECK_STACK, pos, state);
	}

	public int size() {
		return cards.size();
	}

	public boolean isEmpty() {
		return cards.isEmpty();
	}

	public boolean isFaceDown() {
		return faceDown;
	}

	public List<Identifier> getCards() {
		return List.copyOf(cards);
	}

	public Optional<Identifier> getTopCardId() {
		return getExposedCardId();
	}

	public Optional<Identifier> getExposedCardId() {
		if (cards.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(faceDown ? cards.get(0) : cards.get(cards.size() - 1));
	}

	public void setDeck(List<Identifier> cardIds, boolean faceDown) {
		cards.clear();
		cards.addAll(cardIds);
		this.faceDown = faceDown;
		sync();
	}

	public void setFaceDown(boolean faceDown) {
		if (this.faceDown == faceDown) {
			return;
		}
		this.faceDown = faceDown;
		sync();
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
		return popExposedCard();
	}

	public ItemStack popExposedCard() {
		if (cards.isEmpty()) {
			return ItemStack.EMPTY;
		}

		Identifier cardId = faceDown ? cards.remove(0) : cards.remove(cards.size() - 1);
		sync();
		return net.supersnetwork.actiondeck.item.Card.createCard(cardId);
	}

	public void shuffle() {
		Collections.shuffle(cards);
		sync();
	}

	public ItemStack createDeckStack() {
		ItemStack stack = new ItemStack(ActionDeckBlocks.DECK_STACK);
		writeCardsToStack(stack, cards, faceDown);
		return stack;
	}

	public static boolean isCard(ItemStack stack) {
		return stack.isOf(ActionDeckItems.CARD) && net.supersnetwork.actiondeck.item.Card.getCardId(stack).isPresent();
	}

	public static boolean isDeckStack(ItemStack stack) {
		return stack.isOf(ActionDeckBlocks.DECK_STACK.asItem());
	}

	public static boolean isFaceDown(ItemStack stack) {
		NbtCompound nbt = stack.getNbt();
		return nbt != null && nbt.getBoolean(FACE_DOWN_KEY);
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
		writeCardsToStack(stack, cards, false);
	}

	public static void writeCardsToStack(ItemStack stack, List<Identifier> cards, boolean faceDown) {
		NbtCompound nbt = stack.getOrCreateNbt();
		NbtList list = new NbtList();

		for (Identifier card : cards) {
			list.add(NbtString.of(card.toString()));
		}

		nbt.put(CARDS_KEY, list);
		nbt.putBoolean(FACE_DOWN_KEY, faceDown);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		cards.clear();
		faceDown = nbt.getBoolean(FACE_DOWN_KEY);

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
		nbt.putBoolean(FACE_DOWN_KEY, faceDown);
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
