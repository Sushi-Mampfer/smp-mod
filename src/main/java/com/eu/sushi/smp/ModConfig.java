package com.eu.sushi.smp;

public class ModConfig {
    public boolean noSleep = false;
    public boolean noNether = false;
    public boolean noEnd = false;
    public boolean noRockets = false;
    public SpawnElytra spawnElytra = new SpawnElytra();
    public boolean speedyGhast = false;
    public Whitelist whitelist = new Whitelist();

    public static class SpawnElytra {
        public boolean enabled = false;
        public int radius = 0;
    }

    public static class Whitelist {
        public boolean enabled = false;
        public boolean verification = false;
        public String token = "";
        public String channel = "";
        public String whitelist_role = "";
        public String webhook_id = "";
    }
}