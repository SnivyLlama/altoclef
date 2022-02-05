package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.resources.GetBuildingMaterialsTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SearchEndIslandsTask extends Task {
    // private Task _getEnderPearlTask;
    private final int MAX_SEARCH = 180;
    private Task _getToIslandTask;
    private Task _getBuildingBlocksTask;
    private Task _getToOuterEndIslandsTask;
    public SearchEndIslandsTask() {
        // _getEnderPearlTask = new CataloguedResourceTask(new ItemTarget(Items.ENDER_PEARL, 1));
        _getToOuterEndIslandsTask = new GetToOuterEndIslandsTask();
    }
    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();
        mod.getBehaviour().avoidBlockBreaking(pos -> WorldHelper.isBlock(mod, pos.down(), Blocks.AIR));
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (!_getToOuterEndIslandsTask.isFinished(mod)) {
            return _getToOuterEndIslandsTask;
        }
        if (_getBuildingBlocksTask != null && _getBuildingBlocksTask.isActive() && !_getBuildingBlocksTask.isFinished(mod)) {
            return _getBuildingBlocksTask;
        }
        if (_getToIslandTask != null && _getToIslandTask.isActive() && !_getToIslandTask.isFinished(mod)) {
            return _getToIslandTask;
        }
//        if (!mod.getItemStorage().hasItemInventoryOnly(Items.ENDER_PEARL) && !mod.getEntityTracker().entityFound(EnderPearlEntity.class)) {
//            setDebugState("Getting an ender pearl...");
//            return _getEnderPearlTask;
//        }
        Pair<Integer, BlockPos> travelInfo = islandTravel(mod, findNearestEdge(mod));
        if (StorageHelper.getBuildingMaterialCount(mod) < travelInfo.getLeft()) {
            setDebugState("Getting building materials...");
            _getBuildingBlocksTask = new GetBuildingMaterialsTask(travelInfo.getLeft());
            return _getBuildingBlocksTask;
        }
        if (highestGround(mod) != mod.getPlayer().getBlockPos()) {
            setDebugState("Returning to surface");
            _getToIslandTask = new GetToBlockTask(highestGround(mod));
            return _getToIslandTask;
        }
        setDebugState("Bridging");
        _getToIslandTask = new GetToBlockTask(travelInfo.getRight());
        return _getToIslandTask;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SearchEndIslandsTask;
    }

    @Override
    protected String toDebugString() {
        return "Searching end islands";
    }

    private Direction optimalDirection(AltoClef mod) {
        Direction res;
        int dist;
        if (mod.getPlayer().getBlockX() > 0) {
            res = Direction.EAST;
            dist = mod.getPlayer().getBlockX();
        } else {
            res = Direction.WEST;
            dist = -mod.getPlayer().getBlockX();
        }
        if (mod.getPlayer().getBlockZ() > dist) {
            res = Direction.SOUTH;
        } else if (mod.getPlayer().getBlockZ() < -dist) {
            res = Direction.NORTH;
        }
        return res;
    }

    private BlockPos findNearestEdge(AltoClef mod) {
        BlockPos curr = mod.getPlayer().getBlockPos();
        Direction dir = optimalDirection(mod);
        while (true) {
            if (!WorldHelper.isBlock(mod, curr.offset(dir).down(), Blocks.END_STONE)) {
                if (!WorldHelper.isBlock(mod, curr.offset(dir).down(2), Blocks.END_STONE)) {
                    return curr;
                }
                curr = curr.down();
            } else if (WorldHelper.isBlock(mod, curr.offset(dir).up(), Blocks.END_STONE)) {
                curr = curr.up();
            }
            curr = curr.offset(dir);
        }
    }

    private Pair<Integer, BlockPos> islandTravel(AltoClef mod, BlockPos from) {
        // First, find an island
        Direction dir = optimalDirection(mod);
        BlockPos curr = from;
        boolean done = false;
        int cost = 0;
        int rec = 0;
        while (!done) {
            rec++;
            if (rec > MAX_SEARCH) {
                done = true;
                break;
            }
            curr = curr.offset(dir);
            for (BlockPos pos : WorldHelper.scanRegion(mod, curr.down(), curr.withY(Math.min(curr.getY() + 75, 170)))) {
                if (WorldHelper.isBlock(mod, pos, Blocks.END_STONE)) {
                    curr = pos.up();
                    done = true;
                    break;
                }
            }
        }
        // Then, calculate material costs
        cost += curr.getY() - from.getY();
        cost += Math.abs(curr.getZ() - from.getZ());
        cost += Math.abs(curr.getX() - from.getX());
        return new Pair<>(cost, curr);
    }

    private BlockPos highestGround(AltoClef mod) {
        for (BlockPos pos : WorldHelper.scanRegion(mod, mod.getPlayer().getBlockPos(), mod.getPlayer().getBlockPos().withY(170))) {
            if (WorldHelper.isBlock(mod, pos, Blocks.END_STONE)) {
                return pos.up();
            }
        }
        return mod.getPlayer().getBlockPos();
    }
}
