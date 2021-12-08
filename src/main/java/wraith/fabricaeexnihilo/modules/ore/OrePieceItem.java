package wraith.fabricaeexnihilo.modules.ore;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import wraith.fabricaeexnihilo.modules.base.IHasColor;
import wraith.fabricaeexnihilo.util.Color;

public class OrePieceItem extends Item implements IHasColor {

    private final OreProperties properties;

    public OrePieceItem(OreProperties properties, Settings settings) {
        super(settings);
        this.properties = properties;
    }

    @Override
    public int getColor(int index) {
        return index == 1 ? properties.getColor().toInt() : Color.WHITE.toInt();
    }

    @Override
    public int getColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        return getColor(tintIndex);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public Text getName() {
        return new TranslatableText("item.fabricaeexnihilo.piece", new TranslatableText(properties.getDisplayName()));
    }

    @Override
    public Text getName(ItemStack stack) {
        return new TranslatableText("item.fabricaeexnihilo.piece", new TranslatableText(properties.getDisplayName()));
    }

}
