package sindarin.farsightedmobs.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.util.Identifier;

import java.util.HashMap;

@Config(name="farsighted_mobs")
public class ModConfig implements ConfigData {
    @ConfigEntry.BoundedDiscrete(min = 0, max=2048)
    @Comment("Default follow range for all hostile mobs. Between 0 and 2048, vanilla = 16.")
    public int hostileDefaultRange = 32;
    @Comment("Follow ranges per mob. Vanilla zombie = 35.")
    public HashMap<String, Integer> followRanges = new HashMap<String, Integer>() {{
        put("minecraft:zombie", 35);
    }};
}
