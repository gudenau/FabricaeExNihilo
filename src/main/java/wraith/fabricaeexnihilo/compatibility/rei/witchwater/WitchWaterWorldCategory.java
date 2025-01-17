package wraith.fabricaeexnihilo.compatibility.rei.witchwater;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import wraith.fabricaeexnihilo.FabricaeExNihilo;
import wraith.fabricaeexnihilo.compatibility.rei.GlyphWidget;
import wraith.fabricaeexnihilo.compatibility.rei.PluginEntry;
import wraith.fabricaeexnihilo.modules.witchwater.WitchWaterFluid;
import wraith.fabricaeexnihilo.util.ItemUtils;

import java.util.ArrayList;
import java.util.List;

public class WitchWaterWorldCategory implements DisplayCategory<WitchWaterWorldDisplay> {

    @Override
    public CategoryIdentifier<? extends WitchWaterWorldDisplay> getCategoryIdentifier() {
        return PluginEntry.WITCH_WATER_WORLD;
    }

    @Override
    public Renderer getIcon() {
        return ItemUtils.asREIEntry(WitchWaterFluid.BUCKET);
    }

    @Override
    public Text getTitle() {
        return new LiteralText("Witch Water Fluid Interactions");
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public int getDisplayWidth(WitchWaterWorldDisplay display) {
        return WIDTH;
    }

    @Override
    public List<Widget> setupDisplay(WitchWaterWorldDisplay display, Rectangle bounds) {
        var widgets = new ArrayList<Widget>();
        widgets.add(Widgets.createRecipeBase(bounds));

        var glyphPlus = new GlyphWidget(bounds, bounds.getMinX() + GLYPH_PLUS_X, bounds.getMinY() + GLYPH_PLUS_Y, GLYPH_WIDTH, GLYPH_HEIGHT, GLYPHS, GLYPH_PLUS_U, GLYPH_PLUS_V);
        var glypgArrow = new GlyphWidget(bounds, bounds.getMinX() + GLYPH_ARROW_X, bounds.getMinY() + GLYPH_ARROW_Y, GLYPH_WIDTH, GLYPH_HEIGHT, GLYPHS, GLYPH_ARROW_U, GLYPH_ARROW_V);
        widgets.add(glyphPlus);
        widgets.add(glypgArrow);

        var witchWaterwitchWater = display.getInputEntries().get(0);
        var fluids = display.getInputEntries().get(1);
        var results = display.getOutputEntries();

        widgets.add(Widgets.createSlot(new Point(bounds.getMinX() + WITCH_X, bounds.getMinY() + WITCH_Y)).entries(witchWaterwitchWater));
        widgets.add(Widgets.createSlot(new Point(bounds.getMinX() + FLUID_X, bounds.getMinY() + FLUID_Y)).entries(fluids));

        for (int i = 0; i < results.size(); i++) {
            var result = results.get(i);
            widgets.add(
                    Widgets.createSlot(new Point(
                            bounds.getMinX() + OUTPUT_X + (i % OUTPUT_SLOTS_X) * 18,
                            bounds.getMinY() + OUTPUT_Y + (i / OUTPUT_SLOTS_X) * 18)).entries(result));

        }
        // Fill in the empty spots
        for (int i = 0; i < results.size() && i < MAX_OUTPUTS; i++) {
            widgets.add(
                    Widgets.createSlot(new Point(
                            bounds.getMinX() + OUTPUT_X + (i % OUTPUT_SLOTS_X) * 18,
                            bounds.getMinY() + OUTPUT_Y + (i / OUTPUT_SLOTS_X) * 18)));
        }

        return widgets;
    }

    public static Identifier GLYPHS = FabricaeExNihilo.ID("textures/gui/rei/glyphs.png");

    public static final int OUTPUT_SLOTS_X = Math.max(FabricaeExNihilo.CONFIG.modules.REI.witchwaterworldCols, 1);
    public static final int OUTPUT_SLOTS_Y = Math.max(FabricaeExNihilo.CONFIG.modules.REI.witchwaterworldRows, 3);

    public static final int MAX_OUTPUTS = OUTPUT_SLOTS_X * OUTPUT_SLOTS_Y;

    public static final int MARGIN = 6;

    public static final int WIDTH = (OUTPUT_SLOTS_X + 2) * 18 + MARGIN * 2;
    public static final int HEIGHT = OUTPUT_SLOTS_Y * 18 + MARGIN * 2;

    public static final int GLYPH_PLUS_X = MARGIN;
    public static final int GLYPH_PLUS_Y = HEIGHT / 2 - 9;
    public static final int GLYPH_ARROW_X = MARGIN + 18;
    public static final int GLYPH_ARROW_Y = GLYPH_PLUS_Y;

    public static final int OUTPUT_X = GLYPH_ARROW_X + 18;
    public static final int OUTPUT_Y = MARGIN;

    public static final int FLUID_X = MARGIN;
    public static final int FLUID_Y = GLYPH_PLUS_Y + 18;
    public static final int WITCH_X = MARGIN;
    public static final int WITCH_Y = GLYPH_PLUS_Y - 18;

    public static final int GLYPH_WIDTH = 16;
    public static final int GLYPH_HEIGHT = 16;
    public static final int GLYPH_PLUS_U = 3 * 16;
    public static final int GLYPH_PLUS_V = 0;
    public static final int GLYPH_ARROW_U = 0;
    public static final int GLYPH_ARROW_V = 0;

}