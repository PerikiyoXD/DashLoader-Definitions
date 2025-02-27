package dev.quantumfusion.dashloader.def.mixin.option.cache.model;

import com.mojang.datafixers.util.Pair;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.api.option.Option;
import dev.quantumfusion.dashloader.def.fallback.model.MissingDashModel;
import dev.quantumfusion.dashloader.def.fallback.model.UnbakedBakedModel;
import dev.quantumfusion.dashloader.def.util.mixins.MixinThings;
import dev.quantumfusion.dashloader.def.util.mixins.SpriteAtlasTextureDuck;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(value = ModelLoader.class, priority = 69420)
public abstract class ModelLoaderMixin {

	@Mutable
	@Shadow
	@Final
	private Map<Identifier, UnbakedModel> unbakedModels;

	@Shadow
	protected abstract void method_4716(BlockState blockState);

	@Mutable
	@Shadow
	@Final
	private Set<Identifier> modelsToLoad;

	@Mutable
	@Shadow
	@Final
	private Map<Identifier, UnbakedModel> modelsToBake;

	@Shadow
	@Final
	private Map<Identifier, Pair<SpriteAtlasTexture, SpriteAtlasTexture.Data>> spriteAtlasData;

	@Inject(
			method = "<init>(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiler/Profiler;I)V",
			at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=static_definitions", shift = At.Shift.AFTER)
	)
	private void injectLoadedModels(ResourceManager resourceManager, BlockColors blockColors, Profiler profiler, int i, CallbackInfo ci) {
		if (DashLoader.isRead()) {
			var data = DashLoader.getData();
			var dashModels = data.bakedModels.getCacheResultData();
			DashLoader.LOGGER.info("Injecting {} Cached Models", dashModels.size());
			this.unbakedModels = new Object2ObjectOpenHashMap<>(this.unbakedModels);
			this.modelsToBake = new Object2ObjectOpenHashMap<>(this.modelsToBake);
			this.modelsToLoad = new ObjectOpenHashSet<>(this.modelsToLoad);
			dashModels.forEach((identifier, bakedModel) -> {
				if (!(bakedModel instanceof MissingDashModel) && !unbakedModels.containsKey(identifier) && !modelsToBake.containsKey(identifier)) {
					UnbakedBakedModel unbakedBakedModel = new UnbakedBakedModel(bakedModel, identifier);
					this.unbakedModels.put(identifier, unbakedBakedModel);
					this.modelsToBake.put(identifier, unbakedBakedModel);
				}
			});
		}
	}

	/**
	 * We want to not load all of the blockstate models as we have a list of them available on which ones to load to save a lot of computation
	 */
	@Redirect(
			method = "<init>(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiler/Profiler;I)V",
			at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 0)
	)
	private boolean loadMissingModels(Iterator instance) {
		if (DashLoader.isRead()) {
			final Object2ObjectMap<BlockState, Identifier> missingModelsRead = DashLoader.getData().getReadContextData().missingModelsRead;
			for (BlockState blockState : missingModelsRead.keySet()) {
				// load thing lambda
				method_4716(blockState);
			}
			DashLoader.LOGGER.info("Loaded {} unsupported models.", missingModelsRead.size());
			return false;
		}
		return instance.hasNext();
	}

	@Inject(
			method = "<init>(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Lnet/minecraft/util/profiler/Profiler;I)V",
			at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;", shift = At.Shift.BEFORE),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onFinishAddingModels(ResourceManager resourceManager, BlockColors blockColors, Profiler profiler, int mipmap, CallbackInfo ci,
			Set<Pair<String, String>> thing,
			Set<SpriteIdentifier> thing2,
			Map<Identifier, List<SpriteIdentifier>> map) {
		if (DashLoader.isRead()) {
			DashLoader.LOGGER.info("Injecting Atlases");
			DashLoader.getData().getReadContextData().dashAtlasManager.consumeAtlases(Option.CACHE_MODEL_LOADER, (pair) ->  {
				SpriteAtlasTexture atlas = pair.getLeft();
				Identifier id = atlas.getId();
				DashLoader.LOGGER.info("Injected {} atlas.", id);
				spriteAtlasData.put(id, Pair.of(atlas, atlas.stitch(resourceManager, ((SpriteAtlasTextureDuck) atlas).getCachedSprites().keySet().stream(), profiler, pair.getRight().mipLevel())));
			});
			map.clear();
		}
	}

	@Inject(
			method = "upload",
			at = @At(
					value = "INVOKE_STRING",
					target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V",
					args = "ldc=atlas",
					shift = At.Shift.AFTER
			)
	)
	private void atlasInject(TextureManager textureManager, Profiler profiler, CallbackInfoReturnable<SpriteAtlasManager> cir) {
		if (DashLoader.isRead()) {
			// Cache stats
			int cachedModels = 0;
			int fallbackModels = 0;
			for (UnbakedModel value : this.modelsToBake.values()) {
				if (value instanceof UnbakedBakedModel) {
					cachedModels += 1;
				} else {
					fallbackModels += 1;
				}
			}
			MixinThings.CACHED_MODELS_COUNT = cachedModels;
			MixinThings.FALLBACK_MODELS_COUNT = fallbackModels;
		}
	}
}
