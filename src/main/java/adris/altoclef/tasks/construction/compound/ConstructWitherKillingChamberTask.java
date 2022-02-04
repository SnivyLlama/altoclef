package adris.altoclef.tasks.construction.compound;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.resources.CollectObsidianTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class ConstructWitherKillingChamberTask extends Task {
    public static final BlockPos[] OBSIDIAN = {
            new BlockPos(0, -4, 0),
            new BlockPos(1, -4, 0),
            new BlockPos(1, -4, 1),
            new BlockPos(1, -4, -1),
    };
    public static final BlockPos[] SOUL_SAND = {
            new BlockPos(0, -3, 0),
            new BlockPos(1, -3, 0),
            new BlockPos(1, -3, 1),
            new BlockPos(1, -3, -1),
    };
    public final BlockPos[] SKULLS = {
            new BlockPos(2, -3, 0),
            new BlockPos(2, -3, 1),
            new BlockPos(2, -3, -1)
    };
    public ConstructWitherKillingChamberTask() {

    }
    @Override
    protected void onStart(AltoClef mod) {
        mod.getBlockTracker().trackBlock(Blocks.END_PORTAL);
    }

    @Override
    protected Task onTick(AltoClef mod) {
        mod.getClientBaritoneSettings().blocksToAvoidBreaking.value.remove(Blocks.OBSIDIAN);
        mod.getClientBaritoneSettings().blocksToAvoidBreaking.value.remove(Blocks.SOUL_SAND);
        if (mod.getItemStorage().getItemCount(Items.OBSIDIAN) < obsidianNeeded(mod)) {
            setDebugState("Getting obsidian...");
            return new CollectObsidianTask(obsidianNeeded(mod));
        }
        if (!WorldHelper.isBlock(mod, new BlockPos(0, -3, 1).up(mod.getBlockTracker().getNearestTracking(Blocks.END_PORTAL).get().getY()), Blocks.AIR)) {
            setDebugState("Clearing area for wither");
            return new DestroyBlockTask(new BlockPos(0, -3, 1).up(mod.getBlockTracker().getNearestTracking(Blocks.END_PORTAL).get().getY()));
        }
        if (!WorldHelper.isBlock(mod, new BlockPos(0, -3, -1).up(mod.getBlockTracker().getNearestTracking(Blocks.END_PORTAL).get().getY()), Blocks.AIR)) {
            setDebugState("Clearing area for wither");
            return new DestroyBlockTask(new BlockPos(0, -3, -1).up(mod.getBlockTracker().getNearestTracking(Blocks.END_PORTAL).get().getY()));
        }
        mod.getClientBaritoneSettings().blocksToAvoidBreaking.value.add(Blocks.OBSIDIAN);
        for (BlockPos pos : OBSIDIAN) {
            BlockPos obi = pos.up(mod.getBlockTracker().getNearestTracking(Blocks.END_PORTAL).get().getY());
            if (!WorldHelper.isBlock(mod, obi, Blocks.OBSIDIAN)) {
                if (!WorldHelper.isBlock(mod, obi, Blocks.AIR)) {
                    setDebugState("Clearing area for obsidian");
                    return new DestroyBlockTask(obi);
                }
                setDebugState("Placing obsidian to trap wither");
                return new PlaceBlockTask(obi, Blocks.OBSIDIAN);
            }
        }
        mod.getClientBaritoneSettings().blocksToAvoidBreaking.value.add(Blocks.SOUL_SAND);
        for (BlockPos pos : SOUL_SAND) {
            BlockPos sand = pos.up(mod.getBlockTracker().getNearestTracking(Blocks.END_PORTAL).get().getY());
            if (!WorldHelper.isBlock(mod, sand, Blocks.SOUL_SAND)) {
                if (!WorldHelper.isBlock(mod, sand, Blocks.AIR)) {
                    // Code gets scuffy here
                    if (sand.getX() != 0) {
                        if (!mod.getPlayer().getBlockPos().equals(sand.east().down())) {
                            return new GetToBlockTask(sand.east().down());
                        }
                    } else {
                        if (!mod.getPlayer().getBlockPos().equals(sand.west().down())) {
                            return new GetToBlockTask(sand.west().down());
                        }
                    }
                    setDebugState("Clearing area for soul sand");
                    return new DestroyBlockTask(sand);
                }
                setDebugState("Placing soul sand for summoning the wither");
                return new PlaceBlockTask(sand, Blocks.SOUL_SAND);
            }
        }
        for (BlockPos pos : SKULLS) {
            BlockPos skull = pos.up(mod.getBlockTracker().getNearestTracking(Blocks.END_PORTAL).get().getY());
            setDebugState("Get ready...");
            if (!WorldHelper.isBlock(mod, skull, Blocks.WITHER_SKELETON_WALL_SKULL)) {
                if (!WorldHelper.isBlock(mod, skull, Blocks.AIR)) {
                    return new DestroyBlockTask(skull);
                }
                if(!mod.getPlayer().getBlockPos().equals(skull.east().down())) {
                    return new GetToBlockTask(skull.east().down());
                }
                return new InteractWithBlockTask(Items.WITHER_SKELETON_SKULL, Direction.EAST, skull.west(), false);
            }
        }
        setDebugState("What??");
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(Blocks.END_PORTAL);
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof ConstructWitherKillingChamberTask;
    }

    @Override
    protected String toDebugString() {
        return "Construct Wither Killing Chamber";
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        Optional<Entity> nearestWither = mod.getEntityTracker().getClosestEntity(new Vec3d(0, 60, 0), WitherEntity.class);
        return nearestWither.isPresent() &&
                nearestWither.get().getBlockPos().isWithinDistance(new BlockPos(0, 64, 0), 4);
    }

    private int obsidianNeeded(AltoClef mod) {
        // Assuming we are in the end
        int needed = 4;
        for (BlockPos obi : ConstructWitherKillingChamberTask.OBSIDIAN) {
            if (WorldHelper.isBlock(mod, obi.up(mod.getBlockTracker().getNearestTracking(Blocks.END_PORTAL).get().getY()), Blocks.OBSIDIAN)) needed--;
        }
        return needed;
    }
}
