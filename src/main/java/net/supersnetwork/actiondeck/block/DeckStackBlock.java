package net.supersnetwork.actiondeck.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.supersnetwork.actiondeck.item.Card;

public class DeckStackBlock extends Block implements BlockEntityProvider {
	public static final IntProperty LEVEL = IntProperty.of("level", 0, 16);

	public DeckStackBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(LEVEL, 0));
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new DeckStackBlockEntity(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		int size = DeckStackBlockEntity.readCardsFromStack(ctx.getStack()).size();
		return getDefaultState().with(LEVEL, getLevel(size));
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, net.minecraft.entity.LivingEntity placer, ItemStack itemStack) {
		if (world.getBlockEntity(pos) instanceof DeckStackBlockEntity deck) {
			deck.setDeck(
				DeckStackBlockEntity.readCardsFromStack(itemStack),
				DeckStackBlockEntity.isFaceDown(itemStack)
			);
			updateLevel(world, pos, state, deck.size());
		}
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (hand != Hand.MAIN_HAND) {
			return ActionResult.PASS;
		}
		if (!(world.getBlockEntity(pos) instanceof DeckStackBlockEntity deck)) {
			return ActionResult.PASS;
		}

		ItemStack held = player.getStackInHand(hand);
		if (DeckStackBlockEntity.isCard(held)) {
			if (deck.isFaceDown()) {
				return ActionResult.FAIL;
			}
			if (!world.isClient) {
				Card.getCardId(held).ifPresent(deck::addCard);
				if (!player.getAbilities().creativeMode) {
					held.decrement(1);
				}
				updateLevel(world, pos, state, deck.size());
				world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, 0.5f, 1.0f);
			}
			return ActionResult.success(world.isClient);
		}

		if (player.isSneaking() && held.isEmpty()) {
			if (deck.isFaceDown()) {
				return ActionResult.FAIL;
			}
			if (!world.isClient) {
				deck.shuffle();
				world.playSound(null, pos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 0.8f, 1.2f);
			}
			return ActionResult.success(world.isClient);
		}

		if (held.isEmpty() && !world.isClient) {
			ItemStack card = deck.popExposedCard();
			if (!card.isEmpty()) {
				if (!player.getInventory().insertStack(card)) {
					ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, card);
				}
				updateLevel(world, pos, state, deck.size());
				world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.5f, 1.0f);
			}
		}

		return ActionResult.success(world.isClient);
	}

	@Override
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (!world.isClient && world.getBlockEntity(pos) instanceof DeckStackBlockEntity deck && !deck.isEmpty()) {
			ItemStack stack = deck.createDeckStack();
			ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5, stack);
			world.spawnEntity(entity);
		}
		super.onBreak(world, pos, state, player);
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		return state;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		int height = Math.max(1, state.get(LEVEL));
		return VoxelShapes.cuboid(0.1875, 0.0, 0.0625, 0.8125, height / 16.0, 0.9375);
	}

	public static int getLevel(int cardCount) {
		return cardCount <= 0 ? 0 : Math.min(16, cardCount);
	}

	private static void updateLevel(World world, BlockPos pos, BlockState state, int cardCount) {
		if (cardCount <= 0) {
			world.removeBlock(pos, false);
			return;
		}

		BlockState updated = state.with(LEVEL, getLevel(cardCount));
		if (state != updated) {
			world.setBlockState(pos, updated, Block.NOTIFY_ALL);
		}
	}
}
