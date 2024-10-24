package net.sindarin27.farsightedmobs.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.sindarin27.farsightedmobs.FarsightedMobs;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record DifficultyPredicate(IntRange value) implements LootItemCondition {
    public static final MapCodec<DifficultyPredicate> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(IntRange.CODEC.fieldOf("value").forGetter(DifficultyPredicate::value))
                    .apply(instance, DifficultyPredicate::new)
    );
    @Override
    public @NotNull LootItemConditionType getType() { return FarsightedMobs.DIFFICULTY_PREDICATE.get(); }

    @Override
    public @NotNull Set<LootContextParam<?>> getReferencedContextParams() {
        return value.getReferencedContextParams();
    }

    @Override
    public boolean test(LootContext lootContext) {
        return this.value.test(lootContext, lootContext.getLevel().getDifficulty().getId());
    }
}
