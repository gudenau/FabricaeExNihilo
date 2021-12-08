package wraith.fabricaeexnihilo.modules.sieves;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.fabricaeexnihilo.FabricaeExNihilo;
import wraith.fabricaeexnihilo.modules.ModBlocks;
import wraith.fabricaeexnihilo.modules.base.BaseBlockEntity;
import wraith.fabricaeexnihilo.util.ItemUtils;

import java.util.*;

import static wraith.fabricaeexnihilo.api.registry.FabricaeExNihiloRegistries.SIEVE;

public class SieveBlockEntity extends BaseBlockEntity implements BlockEntityClientSerializable {

    private ItemStack mesh = ItemStack.EMPTY;
    private ItemStack contents = ItemStack.EMPTY;
    double progress = 0.0;
    public static final BlockEntityType<SieveBlockEntity> TYPE = FabricBlockEntityTypeBuilder.create(
            SieveBlockEntity::new,
            ModBlocks.SIEVES.values().toArray(new SieveBlock[0])
    ).build(null);
    public static final Identifier BLOCK_ENTITY_ID = FabricaeExNihilo.ID("sieve");

    public SieveBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }

    public ItemStack getMesh() {
        return mesh;
    }

    public ItemStack getContents() {
        return contents;
    }

    public double getProgress() {
        return progress;
    }

    public ActionResult activate(@Nullable BlockState state, @Nullable PlayerEntity player, @Nullable Hand hand, @Nullable BlockHitResult hitResult) {
        if (world == null || world.isClient() || player == null) {
            return ActionResult.PASS;
        }

        var held = player.getStackInHand(hand == null ? player.getActiveHand() : hand);
        if (held == null) {
            held = ItemStack.EMPTY;
        }

        if (held.getItem() instanceof BucketItem) {
            return ActionResult.PASS; // Done for fluid logging
        }

        // Remove/Swap a mesh
        if (SIEVE.isValidMesh(held)) {
            // Removing mesh
            if (!mesh.isEmpty()) {
                player.giveItemStack(mesh.copy());
                mesh = ItemStack.EMPTY;
            }
            // Add mesh
            if (SIEVE.isValidMesh(held)) {
                mesh = ItemUtils.ofSize(held, 1);
                if (!player.isCreative()) {
                    held.decrement(1);
                }
            }
            markDirtyClient();
            return ActionResult.SUCCESS;
        }
        var sieves = getConnectedSieves();
        // Make Progress
        if (!contents.isEmpty()) {
            sieves.forEach(sieve -> sieve.doProgress(player));
            return ActionResult.SUCCESS;
        }

        // Add a block
        if (!held.isEmpty() && !mesh.isEmpty() && SIEVE.isValidRecipe(mesh, getFluid(), held)) {
            ItemStack finalHeld = held;
            sieves.forEach(sieve -> sieve.setContents(finalHeld, !player.isCreative()));
            return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME;
    }

    public void setContents(ItemStack stack, boolean doDrain) {
        if (stack.isEmpty() || !contents.isEmpty()) {
            return;
        }
        contents = ItemUtils.ofSize(stack, 1);
        if (doDrain) {
            stack.decrement(1);
        }
        progress = 0.0;
        markDirtyClient();
    }

    @Nullable
    public Fluid getFluid() {
        if (world == null) {
            return null;
        }
        var state = world.getBlockState(pos);
        if (state == null) {
            return null;
        }
        if (!state.get(Properties.WATERLOGGED)) {
            return Fluids.EMPTY;
        }
        return state.getFluidState().getFluid();
    }

    public void doProgress(PlayerEntity player) {
        if (world == null) {
            return;
        }
        var haste = player.getActiveStatusEffects().get(StatusEffects.HASTE);
        var efficiency = FabricaeExNihilo.CONFIG.modules.sieves.efficiency ? EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, mesh) : 0;
        var hasteLevel = FabricaeExNihilo.CONFIG.modules.sieves.haste ? (haste == null ? -1 : haste.getAmplifier()) + 1 : 0;

        progress += FabricaeExNihilo.CONFIG.modules.sieves.baseProgress
                + FabricaeExNihilo.CONFIG.modules.sieves.efficiencyScaleFactor * efficiency
                + FabricaeExNihilo.CONFIG.modules.sieves.hasteScaleFactor * hasteLevel;

        // TODO spawn some particles

        if (progress > 1.0) {
            SIEVE.getResult(mesh, getFluid(), contents, player, world.random).forEach(player.getInventory()::offerOrDrop);
            progress = 0.0;
            contents = ItemStack.EMPTY;
        }
        markDirtyClient();
    }

    public void dropInventory() {
        ItemScatterer.spawn(world, pos.up(), DefaultedList.copyOf(mesh, contents));
        mesh = ItemStack.EMPTY;
        contents = ItemStack.EMPTY;
    }

    public void dropMesh() {
        ItemScatterer.spawn(world, pos.up(), DefaultedList.copyOf(mesh));
        mesh = ItemStack.EMPTY;
    }


    public void dropContents() {
        ItemScatterer.spawn(world, pos.up(), DefaultedList.copyOf(contents));
        contents = ItemStack.EMPTY;
    }

    public List<SieveBlockEntity> getConnectedSieves() {
        var sieves = new ArrayList<SieveBlockEntity>();

        if (world == null) {
            return sieves;
        }

        var tested = new HashSet<BlockPos>();
        var stack = new Stack<BlockPos>();
        stack.add(this.pos);

        while (!stack.empty()) {
            var popped = stack.pop();
            // Record that this one has been tested
            tested.add(popped);
            if (matchingSieveAt(world, popped)) {
                if (!(this.world.getBlockEntity(popped) instanceof SieveBlockEntity sieve)) {
                    continue;
                }
                sieves.add(sieve);
                // Add adjacent locations to test to the stack
                Arrays.stream(Direction.values())
                        // Horizontals
                        .filter(dir -> dir.getAxis().isHorizontal())
                        // to BlockPos
                        .map(popped::offset)
                        // Remove already tested positions
                        .filter(pos -> !tested.contains(pos) && !stack.contains(pos))
                        // Remove positions too far away
                        .filter(pos ->
                                Math.abs(this.pos.getX() - pos.getX()) <= FabricaeExNihilo.CONFIG.modules.sieves.sieveRadius &&
                                Math.abs(this.pos.getZ() - pos.getZ()) <= FabricaeExNihilo.CONFIG.modules.sieves.sieveRadius)
                        // Add to the stack to be processed
                        .forEach(stack::add);
            }
        }

        return sieves;
    }

    private boolean matchingSieveAt(@Nullable World world, BlockPos pos) {
        if (world == null) {
            return false;
        }
        if (world.getBlockEntity(pos) instanceof SieveBlockEntity sieve) {
            return ItemStack.areItemsEqual(mesh, sieve.mesh);
        }
        return false;
    }

    /**
     * NBT Serialization section
     */

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        return writeNbtWithoutWorldInfo(super.writeNbt(nbt));
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        if (nbt == null) {
            FabricaeExNihilo.LOGGER.warn("A sieve at " + this.pos + " is missing data.");
            return;
        }
        readNbtWithoutWorldInfo(nbt);
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        readNbt(tag);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        return writeNbt(tag);
    }

    public NbtCompound writeNbtWithoutWorldInfo(NbtCompound nbt) {
        nbt.put("mesh", mesh.writeNbt(new NbtCompound()));
        nbt.put("contents", contents.writeNbt(new NbtCompound()));
        nbt.putDouble("progress", progress);
        return nbt;
    }

    public void readNbtWithoutWorldInfo(NbtCompound nbt) {
        mesh = ItemStack.fromNbt(nbt.getCompound("mesh"));
        contents = ItemStack.fromNbt(nbt.getCompound("contents"));
        progress = nbt.getDouble("progress");
    }

}