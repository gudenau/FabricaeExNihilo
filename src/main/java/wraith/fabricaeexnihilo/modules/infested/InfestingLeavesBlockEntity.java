package wraith.fabricaeexnihilo.modules.infested;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import wraith.fabricaeexnihilo.FabricaeExNihilo;
import wraith.fabricaeexnihilo.modules.ModBlocks;
import wraith.fabricaeexnihilo.modules.base.BaseBlockEntity;
import wraith.fabricaeexnihilo.modules.base.IHasColor;
import wraith.fabricaeexnihilo.util.Color;

public class InfestingLeavesBlockEntity extends BaseBlockEntity implements BlockEntityClientSerializable, IHasColor {

    private InfestedLeavesBlock infestedBlock;
    private double progress = 0.0;

    private int tickCounter;

    public static Identifier BLOCK_ENTITY_ID = FabricaeExNihilo.ID("infesting");
    public static final BlockEntityType<InfestingLeavesBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(
            InfestingLeavesBlockEntity::new,
            ModBlocks.INFESTING_LEAVES.values().toArray(new InfestingLeavesBlock[0])
    ).build(null);

    public InfestingLeavesBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
        tickCounter = world == null ? 0 : world.random.nextInt(FabricaeExNihilo.CONFIG.modules.barrels.tickRate);
    }

    public static void ticker(World world, BlockPos blockPos, BlockState blockState, InfestingLeavesBlockEntity infestedLeavesEntity) {
        infestedLeavesEntity.tick(world, blockPos, blockState);
    }

    public void tick(World world, BlockPos blockPos, BlockState blockState) {
        // Don't update every single tick
        tickCounter += 1;
        if (tickCounter < FabricaeExNihilo.CONFIG.modules.silkworms.updateFrequency) {
            return;
        }
        tickCounter = 0;

        // Advance
        progress += FabricaeExNihilo.CONFIG.modules.silkworms.progressPerUpdate;

        if (progress < 1f) {
            markDirty();
            if (progress > FabricaeExNihilo.CONFIG.modules.silkworms.minimumSpreadPercent && world != null) {
                InfestedHelper.tryToSpreadFrom(world, blockPos, FabricaeExNihilo.CONFIG.modules.silkworms.infestingSpreadAttempts);
            }
            return;
        }

        // Done Transforming
        if (world == null) {
            return;
        }
        var curState = world.getBlockState(blockPos);
        var newState = infestedBlock.getDefaultState()
                .with(LeavesBlock.DISTANCE, curState.get(LeavesBlock.DISTANCE))
                .with(LeavesBlock.PERSISTENT, curState.get(LeavesBlock.PERSISTENT));
        world.setBlockState(blockPos, newState);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt == null) {
            FabricaeExNihilo.LOGGER.warn("An infesting leaves block at " + pos + " is missing data.");
            return;
        }
        readNbtWithoutWorldInfo(nbt);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return toNBTWithoutWorldInfo(super.writeNbt(nbt));
    }

    @Override
    public void fromClientTag(NbtCompound nbt) {
        readNbt(nbt);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound nbt) {
        return writeNbt(nbt);
    }

    public NbtCompound toNBTWithoutWorldInfo(NbtCompound nbt) {
        nbt.putString("block", Registry.BLOCK.getId(infestedBlock).toString());
        nbt.putDouble("progress", progress);
        return nbt;
    }

    public void readNbtWithoutWorldInfo(NbtCompound nbt) {
        infestedBlock = (InfestedLeavesBlock) Registry.BLOCK
                .getOrEmpty(new Identifier(nbt.getString("block")))
                .orElse(ModBlocks.INFESTED_LEAVES
                        .values()
                        .stream()
                        .findFirst()
                        .get());
        progress = nbt.getDouble("progress");
    }

    public InfestedLeavesBlock getInfestedBlock() {
        return infestedBlock;
    }

    public void setInfestedBlock(InfestedLeavesBlock infestedBlock) {
        this.infestedBlock = infestedBlock;
    }

    @Override
    public int getColor(int index) {
        return getColor(infestedBlock.getLeafBlock().getDefaultState(), world, pos, index);
    }

    @Override
    public int getColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        var originalColor = MinecraftClient.getInstance().getBlockColors().getColor(infestedBlock.getLeafBlock().getDefaultState(), world, pos, tintIndex);
        return Color.average(Color.WHITE, new Color(originalColor), progress).toInt();
    }

    public double getProgress() {
        return this.progress;
    }

}