package net.sindarin27.farsightedmobs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class AttributeRulesManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private List<AttributeRule> rules = new ArrayList<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    // Initialise json loader for a folder named 
    public AttributeRulesManager() {
        super(GSON, "farsightedmobs_spawn_attributes");
    }

    @Override
    // Apply rule-reading to datapacks
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        DynamicOps<JsonElement> jsonOps = JsonOps.INSTANCE;
        try {
            RegistryOps<JsonElement> registryOps = VanillaRegistries.createLookup().createSerializationContext(JsonOps.INSTANCE);
            jsonOps = registryOps;
        } catch (IllegalStateException exception) {
            LOGGER.warn("Failed to build vanilla registry lookup, falling back to plain JSON parsing for attribute rules", exception);
        }
        rules = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata. Just in case.
            try {
                AttributeRule decoded = AttributeRule.CODEC.codec().parse(jsonOps, entry.getValue()).getOrThrow(JsonParseException::new);
                decoded.identifier = resourcelocation;
                rules.add(decoded);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                LOGGER.error("Parsing error loading rule {}", resourcelocation, jsonparseexception);
            }
        }
        
        rules.sort(Comparator.comparing(AttributeRule::getPriority));
    }
    
    public Stream<AttributeRule> GetRules() {
        return rules.stream();
    }
}
