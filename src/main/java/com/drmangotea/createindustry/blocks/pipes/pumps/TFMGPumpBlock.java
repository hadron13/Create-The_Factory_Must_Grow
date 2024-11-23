package com.drmangotea.createindustry.blocks.pipes.pumps;


import com.drmangotea.createindustry.registry.TFMGBlockEntities;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TFMGPumpBlock extends PumpBlock {
    public TFMGPumpBlock(Properties p_i48415_1_) {
        super(p_i48415_1_);
    }




    @Override
    public Class<PumpBlockEntity> getBlockEntityClass() {
        return PumpBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PumpBlockEntity> getBlockEntityType() {
        return TFMGBlockEntities.TFMG_MECHANICAL_PUMP.get();
    }
}