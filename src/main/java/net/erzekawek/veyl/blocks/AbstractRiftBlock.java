package net.erzekawek.veyl.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public class AbstractRiftBlock extends Block {
    private final ResourceKey<Level> targetDimension;
    private final Vec3 targetPosition;
    private final float targetYaw;

    public AbstractRiftBlock(Properties settings, ResourceKey<Level> dimension, Vec3 pos, float yaw) {
        super(settings);
        this.targetDimension = dimension;
        this.targetPosition = pos;
        this.targetYaw = yaw;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean isPrecise) {
        teleport(level, entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        teleport(level, entity);
    }

    private void teleport(Level level, Entity entity) {
        if (level.isClientSide() || entity.isPassenger() || entity.isVehicle()) return;
        if (!(level instanceof ServerLevel current)) return;

        ServerLevel target = current.getServer().getLevel(targetDimension);
        if (target == null) return;

        entity.teleport(new TeleportTransition(
                target, targetPosition, Vec3.ZERO,
                targetYaw, entity.getXRot(),
                TeleportTransition.DO_NOTHING));
    }
}
