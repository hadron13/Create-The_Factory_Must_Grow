package com.drmangotea.createindustry.blocks.electricity.cable_blocks.copycat_cable_block;

import com.drmangotea.createindustry.registry.TFMGBlockEntities;
import com.drmangotea.createindustry.registry.TFMGBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.decoration.copycat.CopycatModel;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelDataManager;
//import net.minecraftforge.client.model.data.ModelDataManager;

import javax.annotation.Nullable;

public class CopycatCableBlock extends Block implements IBE<CopycatCableBlockEntity>, IWrenchable {

    public CopycatCableBlock(Properties pProperties) {
        super(pProperties);
    }



    @Override
    public InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        onWrenched(state, context);
        return IWrenchable.super.onSneakWrenched(state, context);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return onBlockEntityUse(context.getLevel(), context.getClickedPos(), ufte -> {
            ItemStack consumedItem = ufte.getConsumedItem();
            if (!ufte.hasCustomMaterial())
                return InteractionResult.PASS;
            Player player = context.getPlayer();
            if (!player.isCreative())
                player.getInventory()
                        .placeItemBackInInventory(consumedItem);
            context.getLevel()
                    .levelEvent(2001, context.getClickedPos(), Block.getId(ufte.getBlockState()));
            ufte.setMaterial(AllBlocks.COPYCAT_BASE.getDefaultState());
            ufte.setConsumedItem(ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        });
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
                                 BlockHitResult pHit) {

        if (pPlayer == null)
            return InteractionResult.PASS;

        Direction face = pHit.getDirection();
        ItemStack itemInHand = pPlayer.getItemInHand(pHand);
        BlockState materialIn = getAcceptedBlockState(pLevel, pPos, itemInHand, face);

        if (materialIn != null)
            materialIn = prepareMaterial(pLevel, pPos, pState, pPlayer, pHand, pHit, materialIn);
        if (materialIn == null)
            return InteractionResult.PASS;

        BlockState material = materialIn;
        return onBlockEntityUse(pLevel, pPos, ufte -> {
            if (ufte.getMaterial()
                    .is(material.getBlock())) {
                if (!ufte.cycleMaterial())
                    return InteractionResult.PASS;
                ufte.getLevel()
                        .playSound(null, ufte.getBlockPos(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, .75f,
                                .95f);
                return InteractionResult.SUCCESS;
            }
            if (ufte.hasCustomMaterial())
                return InteractionResult.PASS;
            if (pLevel.isClientSide())
                return InteractionResult.SUCCESS;

            ufte.setMaterial(material);
            ufte.setConsumedItem(itemInHand);
            ufte.getLevel()
                    .playSound(null, ufte.getBlockPos(), material.getSoundType()
                            .getPlaceSound(), SoundSource.BLOCKS, 1, .75f);

            if (pPlayer.isCreative())
                return InteractionResult.SUCCESS;

            itemInHand.shrink(1);
            if (itemInHand.isEmpty())
                pPlayer.setItemInHand(pHand, ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        });
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        if (pPlacer == null)
            return;
        ItemStack offhandItem = pPlacer.getItemInHand(InteractionHand.OFF_HAND);
        BlockState appliedState =
                getAcceptedBlockState(pLevel, pPos, offhandItem, Direction.orderedByNearest(pPlacer)[0]);

        if (appliedState == null)
            return;
        withBlockEntityDo(pLevel, pPos, ufte -> {
            if (ufte.hasCustomMaterial())
                return;

            ufte.setMaterial(appliedState);
            ufte.setConsumedItem(offhandItem);

            if (pPlacer instanceof Player player && player.isCreative())
                return;
            offhandItem.shrink(1);
            if (offhandItem.isEmpty())
                pPlacer.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        });
    }

    @Nullable
    public BlockState getAcceptedBlockState(Level pLevel, BlockPos pPos, ItemStack item, Direction face) {
        if (!(item.getItem() instanceof BlockItem bi))
            return null;

        Block block = bi.getBlock();
        if (block instanceof com.simibubi.create.content.decoration.copycat.CopycatBlock||block instanceof CopycatCableBlock)
            return null;

        BlockState appliedState = block.defaultBlockState();
        boolean hardCodedAllow = isAcceptedRegardless(appliedState);

        if (!AllTags.AllBlockTags.COPYCAT_ALLOW.matches(block) && !hardCodedAllow) {

            if (AllTags.AllBlockTags.COPYCAT_DENY.matches(block))
                return null;
            if (block instanceof EntityBlock)
                return null;
            if (block instanceof StairBlock)
                return null;

            if (pLevel != null) {
                VoxelShape shape = appliedState.getShape(pLevel, pPos);
                if (shape.isEmpty() || !shape.bounds()
                        .equals(Shapes.block()
                                .bounds()))
                    return null;

                VoxelShape collisionShape = appliedState.getCollisionShape(pLevel, pPos);
                if (collisionShape.isEmpty())
                    return null;
            }
        }

        if (face != null) {
            Direction.Axis axis = face.getAxis();

            if (appliedState.hasProperty(BlockStateProperties.FACING))
                appliedState = appliedState.setValue(BlockStateProperties.FACING, face);
            if (appliedState.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && axis != Direction.Axis.Y)
                appliedState = appliedState.setValue(BlockStateProperties.HORIZONTAL_FACING, face);
            if (appliedState.hasProperty(BlockStateProperties.AXIS))
                appliedState = appliedState.setValue(BlockStateProperties.AXIS, axis);
            if (appliedState.hasProperty(BlockStateProperties.HORIZONTAL_AXIS) && axis != Direction.Axis.Y)
                appliedState = appliedState.setValue(BlockStateProperties.HORIZONTAL_AXIS, axis);
        }

        return appliedState;
    }

    public boolean isAcceptedRegardless(BlockState material) {
        return false;
    }

    public BlockState prepareMaterial(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer,
                                      InteractionHand pHand, BlockHitResult pHit, BlockState material) {
        return material;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.hasBlockEntity() || pState.getBlock() == pNewState.getBlock())
            return;
        if (!pIsMoving)
            withBlockEntityDo(pLevel, pPos, ufte -> Block.popResource(pLevel, pPos, ufte.getConsumedItem()));
        pLevel.removeBlockEntity(pPos);
    }

    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
        if (pPlayer.isCreative())
            withBlockEntityDo(pLevel, pPos, ufte -> ufte.setConsumedItem(ItemStack.EMPTY));
    }

    @Override
    public Class<CopycatCableBlockEntity> getBlockEntityClass() {
        return CopycatCableBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CopycatCableBlockEntity> getBlockEntityType() {
        return TFMGBlockEntities.COPYCAT_CABLE.get();
    }

    // Connected Textures

    // TODO: re-add this
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
//                                    BlockState queryState, BlockPos queryPos) {
//
//        if (isIgnoredConnectivitySide(level, state, side, pos, queryPos))
//            return state;
//   state     ModelDataManager modelDataManager = ;
//        if (modelDataManager == null)
//            return getMaterial(level, pos);
//        return CopycatModel.getMaterial(modelDataManager.getAt(pos));
//    }

    public boolean isIgnoredConnectivitySide(BlockAndTintGetter reader, BlockState state, Direction face,
                                             BlockPos fromPos, BlockPos toPos) {
        return false;
    }

    public boolean canConnectTexturesToward(BlockAndTintGetter reader, BlockPos fromPos, BlockPos toPos,
                                            BlockState state) {
        return true;
    }

    //

    public static BlockState getMaterial(BlockGetter reader, BlockPos targetPos) {
        if (reader.getBlockEntity(targetPos) instanceof CopycatCableBlockEntity cbe)
            return cbe.getMaterial();
        return Blocks.AIR.defaultBlockState();
    }

    public boolean canFaceBeOccluded(BlockState state, Direction face) {
        return false;
    }

    public boolean shouldFaceAlwaysRender(BlockState state, Direction face) {
        return false;
    }

    // Wrapped properties

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
        return getMaterial(level, pos).getSoundType();
    }

    @Override
    public float getFriction(BlockState state, LevelReader level, BlockPos pos, Entity entity) {
        return getMaterial(level, pos).getFriction(level, pos, entity);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return getMaterial(level, pos).getLightEmission(level, pos);
    }

    @Override
    public boolean canHarvestBlock(BlockState state, BlockGetter level, BlockPos pos, Player player) {
        return getMaterial(level, pos).canHarvestBlock(level, pos, player);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return getMaterial(level, pos).getExplosionResistance(level, pos, explosion);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos,
                                       Player player) {
        BlockState material = getMaterial(level, pos);
        if (TFMGBlocks.COPYCAT_CABLE_BASE.has(material) || player != null && player.isShiftKeyDown())
            return new ItemStack(this);
        return material.getCloneItemStack(target, level, pos, player);
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2,
                                     LivingEntity entity, int numberOfParticles) {
        return getMaterial(level, pos).addLandingEffects(level, pos, state2, entity, numberOfParticles);
    }

    @Override
    public boolean addRunningEffects(BlockState state, Level level, BlockPos pos, Entity entity) {
        return getMaterial(level, pos).addRunningEffects(level, pos, entity);
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return getMaterial(level, pos).getEnchantPowerBonus(level, pos);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        return getMaterial(level, pos).canEntityDestroy(level, pos, entity);
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos, SpawnPlacements.Type type,
                                EntityType<?> entityType) {
        return false;
    }

    @Override
    public void fallOn(Level pLevel, BlockState pState, BlockPos pPos, Entity pEntity, float pFallDistance) {
        BlockState material = getMaterial(pLevel, pPos);
        material.getBlock()
                .fallOn(pLevel, material, pPos, pEntity, pFallDistance);
    }

    @Override
    public float getDestroyProgress(BlockState pState, Player pPlayer, BlockGetter pLevel, BlockPos pPos) {
        return getMaterial(pLevel, pPos).getDestroyProgress(pPlayer, pLevel, pPos);
    }

    //

    @OnlyIn(Dist.CLIENT)
    public static BlockColor wrappedColor() {
        return new WrappedBlockColor();
    }

    @OnlyIn(Dist.CLIENT)
    public static class WrappedBlockColor implements BlockColor {

        @Override
        public int getColor(BlockState pState, @Nullable BlockAndTintGetter pLevel, @Nullable BlockPos pPos,
                            int pTintIndex) {
            if (pLevel == null || pPos == null)
                return GrassColor.get(0.5D, 1.0D);
            return Minecraft.getInstance()
                    .getBlockColors()
                    .getColor(getMaterial(pLevel, pPos), pLevel, pPos, pTintIndex);
        }

    }

}
