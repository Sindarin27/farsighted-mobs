package net.sindarin27.farsightedmobs;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.sindarin27.farsightedmobs.mixin.MobMixin;
import net.sindarin27.farsightedmobs.mixin.NearestAttackableTargetGoalMixin;

public class AttributeUtility {
    // Change the base value of the given attribute on the given entity to the given value
    public static void ChangeBaseAttributeValue(Mob mob, Holder<Attribute> attribute, double value) {
        AttributeInstance attributeInstance = mob.getAttribute(attribute);
        // Safety goes first
        if (attributeInstance == null) {
            FarsightedMobs.LOGGER.warn("No attribute instance found for " + attribute.value().getDescriptionId());
            return;
        }
        attributeInstance.setBaseValue(value);
    }

    public static void AddAttributeModifier(Mob mob, Holder<Attribute> attribute, AttributeModifier modifier) {
        AttributeInstance attributeInstance = mob.getAttribute(attribute);
        // Safety goes first
        if (attributeInstance == null) {
            FarsightedMobs.LOGGER.warn("No attribute instance found for " + attribute.value().getDescriptionId());
            return;
        }
        attributeInstance.addOrReplacePermanentModifier(modifier);
    }

    // Workaround for MC-145656 (https://bugs.mojang.com/browse/MC-145656)
    // Should no longer be needed from 1.21.1 onwards, but does no harm either
    public static void FixFollowRange(Mob mob) {
        double followRange = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        for (net.minecraft.world.entity.ai.goal.WrappedGoal wrappedGoal : ((MobMixin) mob).getTargetSelector().getAvailableGoals()) {
            Goal goal = wrappedGoal.getGoal();
            if (goal instanceof NearestAttackableTargetGoal<?> natGoal) {
                ((NearestAttackableTargetGoalMixin) natGoal).getTargetConditions().range(followRange);
            }
        }
    }
}
