package wraith.fabricaeexnihilo.modules.sieves;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import wraith.fabricaeexnihilo.FabricaeExNihilo;
import wraith.fabricaeexnihilo.modules.base.IHasColor;
import wraith.fabricaeexnihilo.util.Color;

public class MeshItem extends Item implements IHasColor {

    private final Color color;
    private final int enchantability;
    private final String displayName;

    public MeshItem(Color color, int enchantability, String displayName, FabricItemSettings settings) {
        super(settings);
        this.color = color;
        this.enchantability = enchantability;
        this.displayName = displayName;
    }

    public MeshItem(Color color, int enchantability, String displayName) {
        this(color, enchantability, displayName, ITEM_SETTINGS);
    }

    protected static final FabricItemSettings ITEM_SETTINGS = new FabricItemSettings().group(FabricaeExNihilo.ITEM_GROUP).maxCount(FabricaeExNihilo.CONFIG.modules.sieves.meshStackSize);

    @Override
    public int getColor(int index) {
        return color.toInt();
    }

    @Override
    public int getColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        return getColor(tintIndex);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantability() {
        return this.enchantability;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public Text getName() {
        return new TranslatableText("item.fabricaeexnihilo.mesh", new TranslatableText(displayName));
    }

    public Text getName(ItemStack stack) {
        return getName();
    }

}