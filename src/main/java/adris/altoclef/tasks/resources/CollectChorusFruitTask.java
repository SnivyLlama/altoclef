package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.movement.GetToOuterEndIslandsTask;
import adris.altoclef.tasks.movement.SearchEndIslandsTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.MiningRequirement;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;

public class CollectChorusFruitTask extends ResourceTask {
    private final int _count;
    private final Task _getToOuterEndIslandsTask;
    private final Task _exploreEndIslandsTask;

    public CollectChorusFruitTask(int count) {
        super(Items.CHORUS_FRUIT, count);
        _count = count;
        _exploreEndIslandsTask = new SearchEndIslandsTask();
        _getToOuterEndIslandsTask = new GetToOuterEndIslandsTask();
    }
    @Override
    protected void onResourceStart(AltoClef mod) {
        mod.getBlockTracker().trackBlock(Blocks.CHORUS_PLANT);
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        if (mod.getBlockTracker().anyFound(Blocks.CHORUS_PLANT)) {
            setDebugState("Mining chorus fruit");
            return new MineAndCollectTask(Items.CHORUS_FRUIT, _count, new Block[]{Blocks.CHORUS_PLANT}, MiningRequirement.HAND);
        }
        if (!_getToOuterEndIslandsTask.isFinished(mod)) {
            setDebugState("Getting to outer end islands");
            return _getToOuterEndIslandsTask;
        }
        setDebugState("Exploring end islands");
        return _exploreEndIslandsTask;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
        mod.getBlockTracker().stopTracking(Blocks.CHORUS_PLANT);
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectChorusFruitTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting Chorus Fruit";
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }
}
