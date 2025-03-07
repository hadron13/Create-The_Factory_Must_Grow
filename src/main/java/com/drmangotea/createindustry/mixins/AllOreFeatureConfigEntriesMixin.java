package com.drmangotea.createindustry.mixins;


import com.drmangotea.createindustry.worldgen.TFMGLayeredPatterns;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.DynamicDataProvider;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.infrastructure.worldgen.AllLayerPatterns;
import com.simibubi.create.infrastructure.worldgen.AllOreFeatureConfigEntries;
import com.simibubi.create.infrastructure.worldgen.OreFeatureConfigEntry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.ForgeConfigSpec;
//import net.minecraftforge.common.world.BiomeModifier;
//import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * really goofy way to do worldgen but it works
 */
@Mixin(AllOreFeatureConfigEntries.class)
public class AllOreFeatureConfigEntriesMixin {


	@Shadow @Final private static Predicate<BiomeLoadingEvent> NETHER_BIOMES;
	@Shadow @Final private static Predicate<BiomeLoadingEvent> OVERWORLD_BIOMES;
	@Shadow
	public static final OreFeatureConfigEntry STRIATED_ORES_OVERWORLD =
			create("striated_ores_overworld", 32, 1 / 18f, -30, 70)
					.biomeExt()
					.predicate(OVERWORLD_BIOMES)
					.parent()
					.layeredDatagenExt()
					.withLayerPattern(TFMGLayeredPatterns.BAUXITE)
					.withLayerPattern(TFMGLayeredPatterns.LIGNITE)
					.withLayerPattern(TFMGLayeredPatterns.FIRECLAY)
					.withLayerPattern(TFMGLayeredPatterns.GALENA)
					.withLayerPattern(AllLayerPatterns.SCORIA)
					.withLayerPattern(AllLayerPatterns.CINNABAR)
					.withLayerPattern(AllLayerPatterns.MAGNETITE)
					.withLayerPattern(AllLayerPatterns.MALACHITE)
					.withLayerPattern(AllLayerPatterns.LIMESTONE)
					.withLayerPattern(AllLayerPatterns.OCHRESTONE)

					//.biomeTag(BiomeTags.IS_OVERWORLD)
					.parent();
	@Shadow
	public static final OreFeatureConfigEntry STRIATED_ORES_NETHER =
			create("striated_ores_nether", 32, 1 / 18f, 40, 90)
					.biomeExt()
					.predicate(NETHER_BIOMES)
					.parent()
					.layeredDatagenExt()
					.withLayerPattern(TFMGLayeredPatterns.SULFUR)
					.withLayerPattern(AllLayerPatterns.SCORIA_NETHER)
					.withLayerPattern(AllLayerPatterns.SCORCHIA_NETHER)
//					.biomeTag(BiomeTags.IS_NETHER)
					.parent();

	//

	private static OreFeatureConfigEntry create(String name, int clusterSize, float frequency,
												int minHeight, int maxHeight) {
		ResourceLocation id = Create.asResource(name);
		OreFeatureConfigEntry configDrivenFeatureEntry = new OreFeatureConfigEntry(id, clusterSize, frequency, minHeight, maxHeight);
		return configDrivenFeatureEntry;
	}
	@Shadow
	public static void fillConfig(ForgeConfigSpec.Builder builder, String namespace) {
		OreFeatureConfigEntry.ALL
				.forEach((id, entry) -> {
					if (id.getNamespace().equals(namespace)) {
						builder.push(entry.getName());
						entry.addToConfig(builder);
						builder.pop();
					}
				});
	}
	@Shadow
	public static void init() {}
	@Shadow
	public static void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();
		RegistryAccess registryAccess = RegistryAccess.BUILTIN.get();

		//

		Map<ResourceLocation, ConfiguredFeature<?, ?>> configuredFeatures = new HashMap<>();
		for (Map.Entry<ResourceLocation, OreFeatureConfigEntry> entry : OreFeatureConfigEntry.ALL.entrySet()) {
			OreFeatureConfigEntry.DatagenExtension datagenExt = entry.getValue().datagenExt();
			if (datagenExt != null) {
				configuredFeatures.put(entry.getKey(), datagenExt.createConfiguredFeature(registryAccess));
			}
		}

		DynamicDataProvider<ConfiguredFeature<?, ?>> configuredFeatureProvider = DynamicDataProvider.create(generator, "Create's Configured Features", registryAccess, Registry.CONFIGURED_FEATURE_REGISTRY, configuredFeatures);
		if (configuredFeatureProvider != null) {
			generator.addProvider( configuredFeatureProvider);
		}

		//

		Map<ResourceLocation, PlacedFeature> placedFeatures = new HashMap<>();
		for (Map.Entry<ResourceLocation, OreFeatureConfigEntry> entry : OreFeatureConfigEntry.ALL.entrySet()) {
			OreFeatureConfigEntry.DatagenExtension datagenExt = entry.getValue().datagenExt();
			if (datagenExt != null) {
				placedFeatures.put(entry.getKey(), datagenExt.createPlacedFeature(registryAccess));
			}
		}

		DynamicDataProvider<PlacedFeature> placedFeatureProvider = DynamicDataProvider.create(generator, "Create's Placed Features", registryAccess, Registry.PLACED_FEATURE_REGISTRY, placedFeatures);
		if (placedFeatureProvider != null) {
			generator.addProvider(placedFeatureProvider);
		}


		//TODO: re-add this?
//		Map<ResourceLocation, BiomeModifier> biomeModifiers = new HashMap<>();
//		for (Map.Entry<ResourceLocation, OreFeatureConfigEntry> entry : OreFeatureConfigEntry.ALL.entrySet()) {
//			OreFeatureConfigEntry.DatagenExtension datagenExt = entry.getValue().datagenExt();
//			if (datagenExt != null) {
//				biomeModifiers.put(entry.getKey(), datagenExt.createBiomeModifier(registryAccess));
//			}
//		}
//
//		DynamicDataProvider<BiomeModifier> biomeModifierProvider = DynamicDataProvider.create(generator, "Create's Biome Modifiers", registryAccess, ForgeRegistries.Keys.BIOME_MODIFIERS, biomeModifiers);
//		if (biomeModifierProvider != null) {
//			generator.addProvider(true, biomeModifierProvider);
//		}
	}
}

