package com.kairos.events.disasters;

import com.kairos.api.DisasterType;
import com.kairos.api.events.DisasterPreStartEvent;
import com.kairos.events.base.ActiveDisaster;
import com.kairos.utils.MessageUtils;
import com.kairos.utils.RepeatingTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

public class CaveIn extends ActiveDisaster {

    private RepeatingTask task;
    private int radius;
    private int maxBlocks;
    private int blocksFallen = 0;
    private Location epicenter;
    
    private static final Set<Material> CAVE_MATERIALS = EnumSet.of(
            Material.STONE, Material.DEEPSLATE, Material.DIRT, Material.GRAVEL,
            Material.ANDESITE, Material.DIORITE, Material.GRANITE, Material.TUFF,
            Material.COBBLESTONE, Material.COBBLED_DEEPSLATE, Material.DRIPSTONE_BLOCK,
            Material.SAND, Material.SANDSTONE, Material.RED_SANDSTONE, Material.NETHERRACK,
            Material.BASALT, Material.BLACKSTONE
    );

    public CaveIn(int level, World world) {
        super(DisasterType.CAVE_IN, level, world);
        this.radius = getIntOrDefault("cavein.radius.level_" + level, 5 + (level * 2));
        this.maxBlocks = getIntOrDefault("cavein.max_blocks.level_" + level, 20 * level);
    }

    @Override
    public void start(World world, Player p, boolean broadcastAllowed) {
        if (p.getLocation().getBlockY() > 50) return; 

        DisasterPreStartEvent event = new DisasterPreStartEvent(this, world, level, p);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        this.epicenter = p.getLocation();
        ongoingDisasters.add(this);
        triggerStartCommands(p); // NEW!

        if (broadcastAllowed && isWarningEnabled() && (boolean) worldProfile.getSettings().getOrDefault("event_broadcast", true)) {
            MessageUtils.send(p, "messages.weather.cavein", "&4&lThe cavern roof is collapsing!");
        }
        
        startTask();
    }

    private void startTask() {
        List<Block> potentialCeilingBlocks = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Block highest = getCeilingBlock(epicenter.clone().add(x, 0, z));
                if (highest != null && CAVE_MATERIALS.contains(highest.getType()) && !hasSupport(highest)) {
                    if (!plugin.getProtectionManager().isProtected(highest.getLocation(), com.kairos.handlers.protection.ProtectionType.BLOCK_DAMAGE)) {
                        potentialCeilingBlocks.add(highest);
                    }
                }
            }
        }

        Collections.shuffle(potentialCeilingBlocks); 

        this.task = new RepeatingTask(plugin, 0, 2) { 
            @Override
            public void run() {
                if (potentialCeilingBlocks.isEmpty() || blocksFallen >= maxBlocks) {
                    triggerEndCommands(); // NEW!
                    ongoingDisasters.remove(CaveIn.this);
                    cancel();
                    return;
                }

                for (int i = 0; i < 3; i++) {
                    if (potentialCeilingBlocks.isEmpty()) break;
                    
                    Block b = potentialCeilingBlocks.remove(0);
                    Location spawnLoc = b.getLocation().add(0.5, -0.1, 0.5);
                    
                    FallingBlock fb = world.spawnFallingBlock(spawnLoc, b.getBlockData());
                    fb.setHurtEntities(true); 
                    fb.setDropItem(false);
                    fb.setVelocity(new Vector(0, -0.5, 0));
                    
                    b.setType(Material.AIR);
                    world.spawnParticle(Particle.BLOCK_DUST, spawnLoc, 15, 0.5, 0.2, 0.5, fb.getBlockData());
                    world.playSound(spawnLoc, Sound.BLOCK_STONE_BREAK, 1.0f, 0.5f);
                    
                    blocksFallen++;
                }
            }
        };
    }

    private Block getCeilingBlock(Location loc) {
        for (int y = 0; y < 15; y++) { 
            Block b = loc.clone().add(0, y, 0).getBlock();
            if (b.getType().isSolid()) return b;
        }
        return null;
    }

    private boolean hasSupport(Block center) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Material m = center.getRelative(x, y, z).getType();
                    if (m.name().contains("FENCE") || m.name().contains("LOG") || 
                        m.name().contains("PLANKS") || m.name().contains("WALL")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void clear() {
        triggerEndCommands(); // NEW!
        if (this.task != null) this.task.cancel();
    }
}
