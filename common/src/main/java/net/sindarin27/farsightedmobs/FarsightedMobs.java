package net.sindarin27.farsightedmobs;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.sindarin27.farsightedmobs.predicates.DifficultyPredicate;
import net.sindarin27.farsightedmobs.predicates.MoonBrightnessPredicate;
import net.sindarin27.farsightedmobs.predicates.MoonPhasePredicate;
import net.sindarin27.farsightedmobs.predicates.entity.MonsterEntityPredicate;
import net.sindarin27.farsightedmobs.predicates.origin.ClampedLocalDifficultyPredicate;
import net.sindarin27.farsightedmobs.predicates.origin.LocalDifficultyPredicate;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Supplier;

import static net.sindarin27.farsightedmobs.AttributeUtility.FixFollowRange;

public final class FarsightedMobs {
    public static final String MOD_ID = "farsightedmobs";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Supplier<RegistrarManager> MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static final AttributeRulesManager attributeRulesManager = new AttributeRulesManager();
    static final boolean DEBUG = Platform.isDevelopmentEnvironment();
    public static RegistrySupplier<LootItemConditionType> DIFFICULTY_PREDICATE;
    public static RegistrySupplier<LootItemConditionType> LOCAL_DIFFICULTY_PREDICATE;
    public static RegistrySupplier<LootItemConditionType> CLAMPED_LOCAL_DIFFICULTY_PREDICATE;
    public static RegistrySupplier<LootItemConditionType> MOON_PHASE_PREDICATE;
    public static RegistrySupplier<LootItemConditionType> MOON_BRIGHTNESS_PREDICATE;

    public static void init() {
        // Write common init code here.
        LOGGER.info("Starting initialisation for {}", MOD_ID);

        // Make the attributes rules manager listen to datapack reloads with its logical namespace. 
        // For fabric: it is dependent on all built-in (vanilla) registries
        ReloadListenerRegistry.register(PackType.SERVER_DATA,
                attributeRulesManager,
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "farsightedmobs_spawn_attributes"));

        Registrar<MapCodec<? extends EntitySubPredicate>> entitySubPredicates = MANAGER.get().get(Registries.ENTITY_SUB_PREDICATE_TYPE);

        //RegistrySupplier<MapCodec<? extends EntitySubPredicate>> MONSTER_PREDICATE = 
        entitySubPredicates.register(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "monster"),
                () -> MonsterEntityPredicate.CODEC);

        Registrar<LootItemConditionType> lootConditionTypes = MANAGER.get().get(Registries.LOOT_CONDITION_TYPE);
        DIFFICULTY_PREDICATE = lootConditionTypes.register(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "difficulty"),
                () -> new LootItemConditionType(DifficultyPredicate.CODEC)
        );
        LOCAL_DIFFICULTY_PREDICATE = lootConditionTypes.register(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "local_difficulty"),
                () -> new LootItemConditionType(LocalDifficultyPredicate.CODEC)
        );
        CLAMPED_LOCAL_DIFFICULTY_PREDICATE = lootConditionTypes.register(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "clamped_local_difficulty"),
                () -> new LootItemConditionType(ClampedLocalDifficultyPredicate.CODEC)
        );
        MOON_PHASE_PREDICATE = lootConditionTypes.register(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "moon_phase"),
                () -> new LootItemConditionType(MoonPhasePredicate.CODEC)
        );
        MOON_BRIGHTNESS_PREDICATE = lootConditionTypes.register(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "moon_brightness"),
                () -> new LootItemConditionType(MoonBrightnessPredicate.CODEC)
        );

        EntityPredicate.Builder predicateBuilder = new EntityPredicate.Builder();
        predicateBuilder.subPredicate(MonsterEntityPredicate.INSTANCE);

        LOGGER.info("Finished initialisation for {}", MOD_ID);
//        attributeRulesManager.rules.add(new AttributeRule(predicateBuilder.build(), Attributes.FOLLOW_RANGE, 32));
    }

    public static void OnMobSpawn(ServerLevel level, Mob mob) {
        Player closestPlayer = level.getNearestPlayer(mob, 2048);

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .withParameter(LootContextParams.ORIGIN, mob.position())
                .withLuck(closestPlayer != null ? closestPlayer.getLuck() : 0f)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, closestPlayer)
                .create(AttributeRule.SPAWN_CONDITION);

        LootContext context = new LootContext.Builder(params).create(Optional.empty());

        for (AttributeRule rule : attributeRulesManager.GetRules()) {
            boolean applied = rule.Apply(mob, context);
            if (DEBUG)
                LOGGER.info("Rule {} with priority {} on mob {} evaluated as {}", rule.identifier, rule.getPriority(), mob.getType().getDescriptionId(), applied);
        }

        // Fix the minecraft bug that causes entities to never update their follow range by updating it once they spawn
        FixFollowRange(mob);
    }


}
