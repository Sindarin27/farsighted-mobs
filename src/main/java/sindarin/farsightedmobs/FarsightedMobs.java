package sindarin.farsightedmobs;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import sindarin.farsightedmobs.config.ModConfig;
import sindarin.farsightedmobs.mixin.ActiveTargetGoalAccessor;
import sindarin.farsightedmobs.mixin.MobEntityAccessor;

import java.util.Objects;

public class FarsightedMobs implements ModInitializer {
    public static ModConfig CONFIG = new ModConfig();

    @Override
    public void onInitialize() {
        AutoConfig.register(ModConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    public static Entity upgradeEntity(MobEntity e) {
        Identifier type = EntityType.getId(e.getType());
        if (FarsightedMobs.CONFIG.followRanges.containsKey(type.toString())) {
            int range = FarsightedMobs.CONFIG.followRanges.get(type.toString());
            e.getAttributeInstance(EntityAttributes.FOLLOW_RANGE).setBaseValue(range);
            FixFollowRange(e);
            return e;
        }
        return e;
    }

    public static void FixFollowRange(LivingEntity livingEntity) {
        if (livingEntity instanceof MobEntity) {
            ((MobEntityAccessor) livingEntity).getTargetSelector().getGoals().forEach(prioritizedGoal -> {
                Goal goal = prioritizedGoal.getGoal();
                if (goal instanceof ActiveTargetGoal) {
                    ActiveTargetGoalAccessor activeTargetGoal = (ActiveTargetGoalAccessor) goal;
                    activeTargetGoal.setTargetPredicate(activeTargetGoal.getTargetPredicate()
                            .setBaseMaxDistance(livingEntity.getAttributeValue(EntityAttributes.FOLLOW_RANGE)));
                }
            });
        }
    }
}
