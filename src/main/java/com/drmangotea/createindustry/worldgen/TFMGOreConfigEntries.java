package com.drmangotea.createindustry.worldgen;

import com.drmangotea.createindustry.registry.TFMGBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.DynamicDataProvider;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.infrastructure.worldgen.OreFeatureConfigEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;


public class TFMGOreConfigEntries {

    private static final Predicate<BiomeLoadingEvent> OVERWORLD_BIOMES = event -> {
        Biome.BiomeCategory category = event.getCategory();
        return category != Biome.BiomeCategory.NETHER && category != Biome.BiomeCategory.THEEND && category != Biome.BiomeCategory.NONE;
    };

    private static final Predicate<BiomeLoadingEvent> NETHER_BIOMES = event -> {
        Biome.BiomeCategory category = event.getCategory();
        return category == Biome.BiomeCategory.NETHER;
    };

    public static final OreFeatureConfigEntry LEAD_ORE =
            create("lead_ore", 10, 5, -63, 60)
                    .biomeExt()
                    .predicate(OVERWORLD_BIOMES)
                    .parent()
                    .standardDatagenExt()

                    .withBlocks(Couple.create(TFMGBlocks.LEAD_ORE, TFMGBlocks.DEEPSLATE_LEAD_ORE))
                    .parent();
    public static final OreFeatureConfigEntry NICKEL_ORE =
            create("nickel_ore", 8, 4, -63, 20)
                    .biomeExt()
                    .predicate(OVERWORLD_BIOMES)
                    .parent()
                    .standardDatagenExt()
                    .withBlocks(Couple.create(TFMGBlocks.NICKEL_ORE, TFMGBlocks.DEEPSLATE_NICKEL_ORE))
                    .parent();


    public static final OreFeatureConfigEntry LITHIUM_ORE =
            create("lithium_ore", 12, 2, -63, 0)
                    .biomeExt()
                    .predicate(OVERWORLD_BIOMES)
                    .parent()
                    .standardDatagenExt()
                    .withBlocks(Couple.create(TFMGBlocks.LITHIUM_ORE, TFMGBlocks.DEEPSLATE_LITHIUM_ORE))
                    .parent();


    ////
    private static OreFeatureConfigEntry create(String name, int clusterSize, float frequency,
                                                int minHeight, int maxHeight) {
        ResourceLocation id = Create.asResource(name);
        OreFeatureConfigEntry configDrivenFeatureEntry = new OreFeatureConfigEntry(id, clusterSize, frequency, minHeight, maxHeight);
        return configDrivenFeatureEntry;
    }


    public static void init() {}

}
