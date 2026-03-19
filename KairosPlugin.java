package com.kairos;

import com.kairos.commands.framework.DisastersCommandManager;
import com.kairos.core.*;
import com.kairos.handlers.protection.ProtectionManager;
import com.kairos.hooks.KairosExpansion;
import com.kairos.listeners.*;
import com.kairos.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.kairos.seasons.effects.*;
import com.kairos.api.entities.CustomEntityType;
import com.kairos.api.seasons.Season;
import com.kairos.api.DisasterType;

// Entities
import com.kairos.entities.mobs.BloodZombie;
import com.kairos.entities.mobs.AncientMummy;
import com.kairos.entities.mobs.Vampire;
import com.kairos.entities.mobs.Scarecrow;
import com.kairos.entities.mobs.AncientSkeleton;
import com.kairos.entities.mobs.Psyco;
import com.kairos.entities.mobs.ZombieKnight;
import com.kairos.entities.mobs.SkeletonKnight;
import com.kairos.entities.mobs.LostSoul;
import com.kairos.entities.mobs.Ghoul;

// Items
import com.kairos.items.WindStaff;

// Disasters
import com.kairos.events.disasters.ExtremeWinds;
import com.kairos.events.disasters.Earthquake;
import com.kairos.events.disasters.Sandstorm;
import com.kairos.events.disasters.Blizzard;
import com.kairos.events.disasters.CaveIn;
import com.kairos.events.disasters.Tornado;
import com.kairos.events.disasters.Hurricane;
import com.kairos.events.disasters.Infestation;
import com.kairos.events.disasters.NetherPurge;
import com.kairos.events.disasters.BanditRaid;
import com.kairos.events.disasters.DeathParade;

public class KairosPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private WorldManager worldManager;
    private DisasterRegistry disasterRegistry;
    private PlayerDataManager playerDataManager;
    private ProtectionManager protectionManager;
    private CustomEntityRegistry customEntityRegistry;
    private CustomItemRegistry customItemRegistry;
    private DataFileManager dataFileManager;
    private DisasterEventHandler eventHandler;
    private ScheduledEventManager scheduledEventManager;
    private SeasonManager seasonManager;
    private DiseaseManager diseaseManager;

    @Override
    public void onEnable() {
        // 1. Initialize Configuration Managers FIRST
        this.configManager = new ConfigManager(this);
        this.dataFileManager = new DataFileManager(this, "data.yml");
        
        // 1.5 Load Dynamic Difficulties
        com.kairos.world.Difficulty.loadConfig(configManager.getConfig("config.yml"));
        
        // 2. Initialize Core Systems
        this.playerDataManager = new PlayerDataManager(this);
        this.protectionManager = new ProtectionManager(this);
        this.worldManager = new WorldManager(this);
        this.disasterRegistry = new DisasterRegistry(configManager.getConfig("disasters.yml"));
        this.customEntityRegistry = new CustomEntityRegistry(this);
        this.customItemRegistry = new CustomItemRegistry(this);
        this.eventHandler = new DisasterEventHandler();
        this.scheduledEventManager = new ScheduledEventManager();
        this.seasonManager = new SeasonManager(this);
        this.diseaseManager = new DiseaseManager(this);

        // 3. Initialize Mechanics (Temperature, Thirst, Disease)
        TemperatureManager tempManager = new TemperatureManager(this, seasonManager, configManager);
        ThirstManager thirstManager = new ThirstManager(this, tempManager, configManager);

        // 4. Hook into PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new KairosExpansion(this, tempManager, thirstManager).register();
            getLogger().info("Successfully hooked into PlaceholderAPI!");
        }

        // 5. Start Global Schedulers
        getServer().getScheduler().runTaskTimer(this, new DisasterScheduler(this), 20L, 20L);
        getServer().getScheduler().runTaskTimer(this, new SeasonTask(this), 20L, 20L);
        getServer().getScheduler().runTaskTimer(this, new ScheduledEventTask(this), 20L, 20L);
        getServer().getScheduler().runTaskTimer(this, new CustomEntityTask(this), 1L, 1L);
        getServer().getScheduler().runTaskTimer(this, new PlayerHUDTask(this, tempManager, thirstManager), 20L, 20L);
        getServer().getScheduler().runTaskTimer(this, new DiseaseTask(diseaseManager), 60L, 60L);
        getServer().getScheduler().runTaskTimer(this, new TimeCycleTask(this), 1L, 1L);

        // 6. Register Commands
        if (getCommand("disasters") != null) {
            DisastersCommandManager cmdManager = new DisastersCommandManager(this);
            getCommand("disasters").setExecutor(cmdManager);
            getCommand("disasters").setTabCompleter(cmdManager);
        }
        
        // 7. Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new DisasterDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityPersistenceListener(this), this);
        getServer().getPluginManager().registerEvents(new ArmorListener(this), this);
        getServer().getPluginManager().registerEvents(new CustomItemListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);
        getServer().getPluginManager().registerEvents(new CropGrowthListener(this), this);
        getServer().getPluginManager().registerEvents(new SeasonalMobSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new SeasonalHarvestListener(this), this);
        getServer().getPluginManager().registerEvents(new SeasonalFishingListener(this), this);
        getServer().getPluginManager().registerEvents(new WildlifeSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new DiseaseListener(this, diseaseManager), this);
        new LootManager(this); // Registers its own listener
        
        // 8. Register Season Effects
        getSeasonManager().registerBiomeEffect(new WinterSnowEffect());
        getSeasonManager().registerBiomeEffect(new WinterTaigaEffect());
        getSeasonManager().registerBiomeEffect(new WinterDesertEffect());
        getSeasonManager().registerBiomeEffect(new SummerScorchingEffect());
        getSeasonManager().registerBiomeEffect(new SpringThawEffect());
        getSeasonManager().registerBiomeEffect(new SummerSandEffect());
        getSeasonManager().registerBiomeEffect(new MudSlownessEffect(Season.SPRING));
        getSeasonManager().registerBiomeEffect(new MudSlownessEffect(Season.AUTUMN));
        getSeasonManager().registerBiomeEffect(new WinterFreezeEffect());

        // 9. Register Entities
        CustomEntityType.register(BloodZombie.TYPE);
        CustomEntityType.register(AncientMummy.TYPE);
        CustomEntityType.register(Vampire.TYPE);
        CustomEntityType.register(Scarecrow.TYPE);
        CustomEntityType.register(LostSoul.TYPE);
        CustomEntityType.register(SkeletonKnight.TYPE);
        CustomEntityType.register(ZombieKnight.TYPE);
        CustomEntityType.register(Psyco.TYPE);
        CustomEntityType.register(AncientSkeleton.TYPE);
        CustomEntityType.register(Ghoul.TYPE);
        
        // 10. Register Items
        getItemRegistry().registerItem(new WindStaff());

        // 11. Register Disasters
        getDisasterRegistry().registerDisaster(DisasterType.EXTREME_WINDS, ExtremeWinds::new);
        getDisasterRegistry().registerDisaster(DisasterType.BLIZZARD, Blizzard::new);
        getDisasterRegistry().registerDisaster(DisasterType.SANDSTORM, Sandstorm::new);
        getDisasterRegistry().registerDisaster(DisasterType.EARTHQUAKE, Earthquake::new);
        getDisasterRegistry().registerDisaster(DisasterType.CAVE_IN, CaveIn::new);
        getDisasterRegistry().registerDisaster(DisasterType.TORNADO, Tornado::new);
        getDisasterRegistry().registerDisaster(DisasterType.HURRICANE, Hurricane::new);

        getDisasterRegistry().registerDisaster(DisasterType.INFESTATION, Infestation::new);
        getDisasterRegistry().registerDisaster(DisasterType.NETHER_PURGE, NetherPurge::new);
        getDisasterRegistry().registerDisaster(DisasterType.BANDIT_RAID, BanditRaid::new);
        getDisasterRegistry().registerDisaster(DisasterType.DEATH_PARADE, DeathParade::new);

        // 12. Link Disasters to Seasons
        getDisasterRegistry().linkDisasterToSeasons(DisasterType.BLIZZARD, Season.WINTER);
        getDisasterRegistry().linkDisasterToSeasons(DisasterType.SANDSTORM, Season.SUMMER, Season.AUTUMN);
        getDisasterRegistry().linkDisasterToSeasons(DisasterType.EXTREME_WINDS, Season.SPRING, Season.AUTUMN, Season.WINTER);
        getDisasterRegistry().linkDisasterToSeasons(DisasterType.TORNADO, Season.SPRING, Season.SUMMER);
        getDisasterRegistry().linkDisasterToSeasons(DisasterType.HURRICANE, Season.SUMMER, Season.AUTUMN);
        getDisasterRegistry().linkDisasterToSeasons(DisasterType.INFESTATION, Season.SPRING, Season.SUMMER);
        getDisasterRegistry().linkDisasterToSeasons(DisasterType.NETHER_PURGE, Season.SUMMER);
    }

    @Override
    public void onDisable() {
        if (seasonManager != null) seasonManager.saveAllStates();
        if (playerDataManager != null) playerDataManager.saveAll(); 
    }

    // Getters
    public ConfigManager getConfigManager() { return configManager; }
    public WorldManager getWorldManager() { return worldManager; }
    public DisasterRegistry getDisasterRegistry() { return disasterRegistry; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public ProtectionManager getProtectionManager() { return protectionManager; }
    public CustomEntityRegistry getEntityRegistry() { return customEntityRegistry; }
    public CustomItemRegistry getItemRegistry() { return customItemRegistry; }
    public DataFileManager getDataFileManager() { return dataFileManager; }
    public DisasterEventHandler getEventHandler() { return eventHandler; }
    public ScheduledEventManager getScheduledEventManager() { return scheduledEventManager; }
    public SeasonManager getSeasonManager() { return seasonManager; }
    public DiseaseManager getDiseaseManager() { return diseaseManager; }
}