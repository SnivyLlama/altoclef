package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.construction.compound.ConstructWitherKillingChamberTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.RunAwayFromPositionTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasks.speedrun.BeatMinecraft2Task;
import adris.altoclef.tasks.squashed.CataloguedResourceTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class KillWithersTask extends Task {
    private Task _beatTheGame;
    private Task _foodTask;
    private final int TARGET_FOOD = 220;
    private final int MIN_FOOD = 50;
    private int _count;
    private final ItemTarget[] TARGET_ARMOR = {
            new ItemTarget(Items.DIAMOND_HELMET, 1),
            new ItemTarget(Items.DIAMOND_CHESTPLATE, 1),
            new ItemTarget(Items.DIAMOND_LEGGINGS, 1),
            new ItemTarget(Items.GOLDEN_BOOTS, 1)
    };
    private final ItemTarget[] TARGET_GEAR = {
            new ItemTarget(Items.DIAMOND_SWORD, 1),
            new ItemTarget(Items.DIAMOND_PICKAXE, 1)
    };
    public KillWithersTask(int count) {
        _count = count;
    }

    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();
        mod.getBehaviour().addProtectedItems(Items.WITHER_SKELETON_SKULL, Items.SOUL_SAND, Items.OBSIDIAN);
        mod.getBlockTracker().trackBlock(Blocks.END_PORTAL);
        _beatTheGame = new BeatMinecraft2Task();
    }

    @Override
    protected Task onTick(AltoClef mod) {
        switch (WorldHelper.getCurrentDimension()) {
            case OVERWORLD -> {
                if (mod.getItemStorage().getItemCountInventoryOnly(Items.SOUL_SAND) < 4*_count ||
                        mod.getItemStorage().getItemCountInventoryOnly(Items.WITHER_SKELETON_SKULL) < 3*_count) {
                    if (_foodTask != null && _foodTask.isActive() && !_foodTask.isFinished(mod))
                        return _foodTask;
                    for (ItemTarget piece : TARGET_ARMOR) {
                        if (!StorageHelper.isArmorEquipped(mod, piece.getMatches()[0])) {
                            setDebugState("Aquiring armor");
                            return new EquipArmorTask(TARGET_ARMOR);
                        }
                    }
                    if (!StorageHelper.itemTargetsMetInventory(mod, TARGET_GEAR)) {
                        setDebugState("Aquiring gear");
                        return new CataloguedResourceTask(TARGET_GEAR);
                    }
                    if (StorageHelper.calculateInventoryFoodScore(mod) < MIN_FOOD) {
                        setDebugState("Aquiring food");
                        _foodTask = new CollectFoodTask(TARGET_FOOD);
                        return _foodTask;
                    }
                    setDebugState("Let's go to the nether bois");
                    return new DefaultGoToDimensionTask(Dimension.NETHER);
                }
                setDebugState("Beating the Game to get to the end");
                return _beatTheGame;
            }
            case NETHER -> {
                if (mod.getItemStorage().getItemCountInventoryOnly(Items.SOUL_SAND) < 4*_count) {
                    setDebugState("Get soul sand for wither");
                    return new CataloguedResourceTask(new ItemTarget(Items.SOUL_SAND, 4*_count));
                }
                if (mod.getItemStorage().getItemCountInventoryOnly(Items.WITHER_SKELETON_SKULL) < 3*_count) {
                    mod.getMobDefenseChain().doWeAvoidWitherSkeletons(false);
                    setDebugState("Get wither skulls for wither");
                    return new CataloguedResourceTask(new ItemTarget(Items.WITHER_SKELETON_SKULL, 3*_count));
                }
                mod.getMobDefenseChain().doWeAvoidWitherSkeletons(true);
                setDebugState("Beating the Game to get to the end");
                return _beatTheGame;
            }
            case END -> {
                if (!mod.getBlockTracker().anyFound(Blocks.END_PORTAL)) {
                    setDebugState("Killing the dragon because it's annoying");
                    return _beatTheGame;
                }
                Optional<Entity> wither = mod.getEntityTracker().getClosestEntity(new Vec3d(0, 0, 0), WitherEntity.class);
                if (wither.isEmpty()) {
                    setDebugState("Constructing the chamber");
                    return new ConstructWitherKillingChamberTask();
                } else {
                    if (wither.get().getPos().isInRange(mod.getPlayer().getPos(), 5)) {
                        setDebugState("Keeping our distance...");
                        return new RunAwayFromPositionTask(5, wither.get().getBlockPos());
                    }
                    setDebugState("Waiting for wither to die... (" + ((LivingEntity) wither.get()).getHealth() + " health)");
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof KillWithersTask;
    }

    @Override
    protected String toDebugString() {
        return "Killing Wither";
    }
}
