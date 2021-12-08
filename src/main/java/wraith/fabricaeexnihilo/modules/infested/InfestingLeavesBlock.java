package wraith.fabricaeexnihilo.modules.infested;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fabricaeexnihilo.mixins.BlockWithEntityInvoker;
import wraith.fabricaeexnihilo.modules.base.IHasColor;
import wraith.fabricaeexnihilo.util.BlockGenerator;
import wraith.fabricaeexnihilo.util.Color;

@SuppressWarnings("deprecation")
public class InfestingLeavesBlock extends LeavesBlock implements BlockEntityProvider, NonInfestableLeavesBlock, IHasColor {

    private final Identifier originalIdentifier;

    public InfestingLeavesBlock(Identifier originalIdentifier, FabricBlockSettings settings) {
        super(settings);
        this.originalIdentifier = originalIdentifier;
    }

    public Identifier getOriginalIdentifier() {
        return originalIdentifier;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        var entity = new InfestingLeavesBlockEntity(pos, state);
        var block = Registry.BLOCK.get(BlockGenerator.INSTANCE.createIdentifier("infested", originalIdentifier));
        if (block instanceof InfestedLeavesBlock infestedLeavesBlock) {
            entity.setInfestedBlock(infestedLeavesBlock);
        }
        return entity;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : BlockWithEntityInvoker.checkType(type, InfestingLeavesBlockEntity.TYPE, InfestingLeavesBlockEntity::ticker);
    }

    @Override
    public int getColor(int tintIndex) {
        return Color.WHITE.toInt();
    }

    @Override
    public int getColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        var entity = world.getBlockEntity(pos);
        if (entity instanceof InfestingLeavesBlockEntity infestingLeavesBlockEntity) {
            var originalColor = MinecraftClient.getInstance().getBlockColors().getColor(infestingLeavesBlockEntity.getInfestedBlock().getLeafBlock().getDefaultState(), world, pos, tintIndex);
            return Color.average(Color.WHITE, new Color(originalColor), infestingLeavesBlockEntity.getProgress()).toInt();
        }
        return getColor(tintIndex);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public MutableText getName() {
        return new TranslatableText("block.fabricaeexnihilo.infesting", BlockGenerator.INSTANCE.createIdentifier("infesting", originalIdentifier));
    }

    public static int getColorStatic(BlockState blockState, BlockRenderView world, BlockPos blockPos, int index) {
        if (world.getBlockEntity(blockPos) instanceof InfestingLeavesBlockEntity infestingLeavesBlockEntity) {
            return infestingLeavesBlockEntity.getColor(index);
        }
        return Color.WHITE.toInt();
    }

}