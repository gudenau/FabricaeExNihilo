package wraith.fabricaeexnihilo.modules.base;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public interface IHasColor {
    int getColor(int index);
    int getColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex);
}
