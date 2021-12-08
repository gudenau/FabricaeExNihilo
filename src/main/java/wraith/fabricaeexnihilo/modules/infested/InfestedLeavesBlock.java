package wraith.fabricaeexnihilo.modules.infested;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import wraith.fabricaeexnihilo.FabricaeExNihilo;
import wraith.fabricaeexnihilo.modules.base.IHasColor;
import wraith.fabricaeexnihilo.util.Color;

import java.util.Random;

public class InfestedLeavesBlock extends LeavesBlock implements IHasColor, NonInfestableLeavesBlock {
    private final LeavesBlock leafBlock;

    public InfestedLeavesBlock(LeavesBlock leafBlock, FabricBlockSettings settings) {
        super(settings);
        this.leafBlock = leafBlock;
    }

    @Override
    public int getColor(int index) {
        return Color.WHITE.toInt();
    }

    @Override
    public int getColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        return getColor(tintIndex);
    }

    public LeavesBlock getLeafBlock() {
        return leafBlock;
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        if(world.isClient) {
            return;
        }
        InfestedHelper.tryToSpreadFrom(world, pos, FabricaeExNihilo.CONFIG.modules.silkworms.infestedSpreadAttempts);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public MutableText getName() {
        return new TranslatableText("block.fabricaeexnihilo.infested", leafBlock.getName());
    }

}