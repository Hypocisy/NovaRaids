package me.unariginal.novaraids.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.unariginal.novaraids.NovaRaids;
import me.unariginal.novaraids.data.Location;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LocationsConfig {
    private final NovaRaids nr = NovaRaids.INSTANCE;

    List<Location> locations = new ArrayList<>();

    public LocationsConfig() {
        try {
            loadLocations();
        } catch (IOException | NullPointerException | UnsupportedOperationException e) {
            nr.loaded_properly = false;
            nr.logError("[RAIDS] Failed to load locations file. " + e.getMessage());
            for (StackTraceElement element : e.getStackTrace()) {
                nr.logError("  " + element.toString());
            }
        }
    }

    public void loadLocations() throws IOException, NullPointerException, UnsupportedOperationException {
        File rootFolder = FabricLoader.getInstance().getConfigDir().resolve("NovaRaids").toFile();
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }

        File file = FabricLoader.getInstance().getConfigDir().resolve("NovaRaids/locations.json").toFile();
        if (file.createNewFile()) {
            InputStream stream = NovaRaids.class.getResourceAsStream("/raid_config_files/locations.json");
            assert stream != null;
            OutputStream out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = stream.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            stream.close();
            out.close();
        }

        JsonElement root = JsonParser.parseReader(new FileReader(file));
        assert root != null;
        JsonObject config = root.getAsJsonObject();

        List<Location> locations = new ArrayList<>();
        for (String key : config.keySet()) {
            JsonObject location_object = config.getAsJsonObject(key);
            double x;
            double y;
            double z;
            ServerWorld world = nr.server().getOverworld();
            int border_radius;
            int boss_pushback_radius;
            float boss_facing_direction;
            boolean use_join_location = false;
            double join_x = 0;
            double join_y = 0;
            double join_z = 0;
            float yaw = 0;
            float pitch = 0;

            if (checkProperty(location_object, "x_pos")) {
                x = location_object.get("x_pos").getAsDouble();
            } else {
                continue;
            }
            if (checkProperty(location_object, "y_pos")) {
                y = location_object.get("y_pos").getAsDouble();
            } else {
                continue;
            }
            if (checkProperty(location_object, "z_pos")) {
                z = location_object.get("z_pos").getAsDouble();
            } else {
                continue;
            }
            Vec3d pos = new Vec3d(x, y, z);

            if (checkProperty(location_object, "world")) {
                String world_path = location_object.get("world").getAsString();
                for (ServerWorld w : nr.server().getWorlds()) {
                    if (w.getRegistryKey().toString().equals(world_path)) {
                        world = w;
                        break;
                    }
                }
            }

            if (checkProperty(location_object, "border_radius")) {
                border_radius = location_object.get("border_radius").getAsInt();
            } else {
                continue;
            }
            if (checkProperty(location_object, "boss_pushback_radius")) {
                boss_pushback_radius = location_object.get("boss_pushback_radius").getAsInt();
            } else {
                continue;
            }
            if (checkProperty(location_object, "boss_facing_direction")) {
                boss_facing_direction = location_object.get("boss_facing_direction").getAsFloat();
            } else {
                continue;
            }
            if (checkProperty(location_object, "use_join_location")) {
                use_join_location = location_object.get("use_join_location").getAsBoolean();
            }
            if (use_join_location) {
                if (checkProperty(location_object, "join_location")) {
                    JsonObject join_location_object = location_object.get("join_location").getAsJsonObject();
                    if (checkProperty(join_location_object, "x_pos")) {
                        join_x = join_location_object.get("x_pos").getAsDouble();
                    } else {
                        continue;
                    }
                    if (checkProperty(join_location_object, "y_pos")) {
                        join_y = join_location_object.get("y_pos").getAsDouble();
                    } else {
                        continue;
                    }
                    if (checkProperty(join_location_object, "z_pos")) {
                        join_z = join_location_object.get("z_pos").getAsDouble();
                    } else {
                        continue;
                    }
                    if (checkProperty(join_location_object, "yaw")) {
                        yaw = join_location_object.get("yaw").getAsFloat();
                    } else {
                        continue;
                    }
                    if (checkProperty(join_location_object, "pitch")) {
                        pitch = join_location_object.get("pitch").getAsFloat();
                    } else {
                        continue;
                    }
                }
            }
            Vec3d join_pos = new Vec3d(join_x, join_y, join_z);
            locations.add(new Location(key, pos, world, border_radius, boss_pushback_radius, boss_facing_direction, use_join_location, join_pos, yaw, pitch));
        }
        this.locations = locations;
    }

    public boolean checkProperty(JsonObject section, String property) {
        if (section.has(property)) {
            return true;
        }
        nr.logError("[RAIDS] Missing " + property + " property in locations.json. Using default value(s) or skipping.");
        return false;
    }

    public Location getLocation(String key) {
        for (Location loc : locations) {
            if (loc.name().equalsIgnoreCase(key)) {
                return loc;
            }
        }
        return null;
    }
}
