package com.kairos.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public enum HeadUtils {
    
    // You can add all your old Base64 strings here!
    ROTTEN_ZOMBIE("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzZhYWU4NmRhMGNkMzE3YTQ3ZmE2NjY4ZmQ0Nzg1YjVhN2E3ZTRlZDllN2JjNjg2NTJiYWUyNzk4NGI4NGMifX19"),
    SOUL_1("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWM0ZDIxOTk2ZjcwYjU0NWZiYTdkZjM2YTkyNzg5MWNiZDJmNTcwYmM3Y2IzY2E5ZGU1MTcxOGE4NGMwYTU2ZSJ9fX0="),
    SOUL_2("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODliYjRiNDI1NTRjODlkMTdkYjFmZDdkMTkyNmM4OWNlOWFhYjNmM2EzMTMyM2Y3ODQ5NDI5YzJlMDQ4YWU3YyJ9fX0="),
    SOUL_3("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTFhYjRjZGFmNWIzNGZmZjEzODdjOWE2MzA4NzYyMTU4MmZlNzNiZGEyYTgzNmI2OGM1Y2ZmYzlhYTRmYzMifX19"),
    SOUL_4("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmFiYmUzZGZjY2ViYWIyNDk1OTk0ZGY4MzVhYmZiMjk2YmUwNDE4ZDJmNjIyODUwYmRlMTRjOWU4MjQyMmYxYyJ9fX0="),
    SKELETON_KNIGHT("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTNlMzJhNmM0MTQ3ZGY0MGQwMjM4NTQ2ZDM2ZDVjMWVlYWZjMmVhOWNlYWMwMzUzZmNiODNiMGVlYTJkMmNmMSJ9fX0="),
    ZOMBIE_KNIGHT("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWNjMzcwMjdlNTZmYWJkZDA0YmQzNTc0ZmUwZjQxM2JjMTY1Y2RmOWY4ZGY5NmFiZjdmYzA1M2E3ZDJlOTZjYiJ9fX0="),
    STALKER("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNjYjE5MjQxNjQ4NTEwYmNjMTVjMTgwMjhiYjM1YTQ0ZWE2MDQ5ODhmMmFhOTQ1MmMzZmM2MmVkNWZkYzAxOSJ9fX0="),
    ANCIENT_MUMMY("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTllNjk1MThjYzFhMzM0NGI2OTc3M2EwOWEyMzdjNjYzODFiODUyNzkxN2Y0YTM4NTBlZThhY2Y0ZWY0MjAzYiJ9fX0="),
    ANCIENT_SKELETON("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzFkZDdlZTdkYzdmYmNiNWMxYWVlN2EyNTc5MTdmMDM0ZWViYTFlMDkzNzI3ZDcxMmRhYjBmYzM1ZmIwZTM4In19fQ=="),
    GHOUL("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjc4YTJlYmI3MThlZjdhODM3NjNkZWY0ZDkzMjI0ZjU0MDc2NDUwM2FhODYxNTQ1NjU4MDZhZjFiYWI0NTFkMCJ9fX0="),
    MUMMY("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTllNjk1MThjYzFhMzM0NGI2OTc3M2EwOWEyMzdjNjYzODFiODUyNzkxN2Y0YTM4NTBlZThhY2Y0ZWY0MjAzYiJ9fX0="),
    SCARECROW("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZlNjUwMmFjNGM4NDdiMWFjMzc4MTBkNjZkMjhjOTFhOGIxOGZkN2Y2MzgzMTI4MjI4NzU1YWE4YzhmNSJ9fX0=");
    
    
    
    
    private final String base64;
    private ItemStack headItem;

    HeadUtils(String base64) {
        this.base64 = base64;
    }

    public ItemStack getHead() {
        if (headItem != null) return headItem.clone(); // Cache it for performance

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
        PlayerTextures textures = profile.getTextures();

        try {
            String decoded = new String(Base64.getDecoder().decode(base64));
            String url = decoded.substring("{\"textures\":{\"SKIN\":{\"url\":\"".length(), decoded.length() - "\"}}}".length());
            textures.setSkin(new URL(url));
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
            item.setItemMeta(meta);
            
            this.headItem = item; // Save to cache
            return item.clone();
        } catch (MalformedURLException | StringIndexOutOfBoundsException e) {
            return new ItemStack(Material.ZOMBIE_HEAD); // Fallback
        }
    }
}