package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.misc.KillWithersTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.item.Items;

public class CollectNetherStarsTask extends ResourceTask {
    private Task _killWithers;
    private final int _count;
    public CollectNetherStarsTask(int count) {
        super(Items.NETHER_STAR, count);
        _count = count;
        _killWithers = new KillWithersTask(count);
    }

    @Override
    protected void onResourceStart(AltoClef mod) {

    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        setDebugState("Killing wither");
        return _killWithers;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectNetherStarsTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting Nether Stars";
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }
}
