package net.erzekawek.veyl.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class VeylDoublePlantBlock extends DoublePlantBlock {
    private final TagKey<Block> placeableOn;

    public VeylDoublePlantBlock(TagKey<Block> placeableOn, Properties properties) {
        super(properties);
        this.placeableOn = placeableOn;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return level.getBlockState(pos.below()).is(placeableOn);
        }
        return super.canSurvive(state, level, pos);
    }
}
