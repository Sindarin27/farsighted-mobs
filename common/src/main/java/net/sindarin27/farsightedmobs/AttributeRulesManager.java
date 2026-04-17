package net.sindarin27.farsightedmobs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
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

public class AttributeRulesManager extends SimpleJsonResourceReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private volatile List<AttributeRule> rules = new ArrayList<>();
    private HolderLookup.Provider registryLookup = VanillaRegistries.createLookup();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    // Initialise json loader for a folder named 
    public AttributeRulesManager() {
        super(GSON, "farsightedmobs_spawn_attributes");
    }

    public void setRegistryLookup(HolderLookup.Provider lookup) {
        this.registryLookup = lookup;
    }

    @Override
    // Apply rule-reading to datapacks
    protected void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        RegistryOps<JsonElement> registryOps = registryLookup.createSerializationContext(JsonOps.INSTANCE);
        List<AttributeRule> newRules = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            ResourceLocation resourcelocation = entry.getKey();
            if (resourcelocation.getPath().startsWith("_"))
                continue; //Forge: filter anything beginning with "_" as it's used for metadata. Just in case.
            try {
                AttributeRule decoded = AttributeRule.CODEC.codec().parse(registryOps, entry.getValue()).getOrThrow(JsonParseException::new);
                decoded.identifier = resourcelocation;
                newRules.add(decoded);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                LOGGER.error("Parsing error loading rule {}", resourcelocation, jsonparseexception);
            }
        }

        newRules.sort(Comparator.comparing(AttributeRule::getPriority));
        this.rules = newRules;
    }
    
    public List<AttributeRule> GetRules() {
        return rules;
    }
}
