package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.movement.GetCloseToBlockTask;
import adris.altoclef.tasks.movement.GetToOuterEndIslandsTask;
import adris.altoclef.tasks.movement.SearchEndIslandsTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.item.Items;

public class CollectShulkerShellsTask extends ResourceTask {
    private final int _count;
    private final Task _getToOuterEndIslandsTask;
    private final Task _exploreEndIslandsTask;

    public CollectShulkerShellsTask(int count) {
        super(Items.SHULKER_SHELL, count);
        _count = count;
        _exploreEndIslandsTask = new SearchEndIslandsTask();
        _getToOuterEndIslandsTask = new GetToOuterEndIslandsTask();
    }
    @Override
    protected void onResourceStart(AltoClef mod) {
        mod.getBlockTracker().trackBlock(Blocks.PURPUR_PILLAR);
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        if (mod.getEntityTracker().entityFound(ShulkerEntity.class)) {
            return new KillAndLootTask(ShulkerEntity.class, new ItemTarget(Items.SHULKER_SHELL, _count));
        }
        if (!_getToOuterEndIslandsTask.isFinished(mod)) {
            setDebugState("Getting to outer end islands");
            return _getToOuterEndIslandsTask;
        }
        if (mod.getBlockTracker().anyFound(Blocks.PURPUR_PILLAR)) {
            setDebugState("Moving towards end city");
            return new GetCloseToBlockTask(mod.getBlockTracker().getNearestTracking(Blocks.PURPUR_PILLAR).get());
        }
        setDebugState("Exploring end islands");
        return _exploreEndIslandsTask;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(Blocks.PURPUR_PILLAR);
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectShulkerShellsTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting Shulker Shells";
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }
}
