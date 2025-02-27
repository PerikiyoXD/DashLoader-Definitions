package dev.quantumfusion.dashloader.def.mixin.option.cache.font;

import com.google.common.collect.Lists;
import dev.quantumfusion.dashloader.def.DashLoader;
import dev.quantumfusion.dashloader.def.mixin.accessor.FontManagerAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.FontStorageAccessor;
import dev.quantumfusion.dashloader.def.mixin.accessor.UnicodeTextureFontAccessor;
import dev.quantumfusion.dashloader.def.util.mixins.MixinThings;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.font.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

@Mixin(targets = "net/minecraft/client/font/FontManager$1")
public class FontManagerOverride {
	private Map<Identifier, Pair<Int2ObjectMap<IntList>, List<Font>>> cache;

	@Inject(
			method = {"method_18638", "prepare*"},
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void overridePrepare(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<Map<Identifier, List<Font>>> cir) {
		var fonts = DashLoader.getData().fonts;
		if (fonts.dataAvailable() && DashLoader.isRead()) {
			var cacheResultData = fonts.getCacheResultData();
			cacheResultData.forEach((identifier, list) -> list.forEach(font -> {
						if (font instanceof UnicodeTextureFont) {
							((UnicodeTextureFontAccessor) font).setResourceManager(resourceManager);
						}
					}
			));

			prepareFonts(cacheResultData);
			cir.setReturnValue(cacheResultData);
		}
	}

	@Inject(
			method = {"method_18638", "prepare*"},
			at = @At(value = "TAIL"),
			cancellable = true
	)
	private void prepareOffthread(ResourceManager resourceManager, Profiler profiler, CallbackInfoReturnable<Map<Identifier, List<Font>>> cir) {
		prepareFonts(cir.getReturnValue());
	}

	private void prepareFonts(Map<Identifier, List<Font>> map) {
		DashLoader.LOGGER.info("Preparing fonts off-thread");
		Map<Identifier, Pair<Int2ObjectMap<IntList>, List<Font>>> cache = new Object2ObjectOpenHashMap<>();
		map.forEach((identifier, fonts) -> cache.put(identifier, computeFonts(Lists.reverse(fonts))));
		this.cache = cache;
	}


	@Inject(
			method = {"method_18635", "apply*"},
			at = @At(value = "HEAD"),
			cancellable = true
	)
	private void overrideApply(Map<Identifier, List<Font>> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
		var fonts = DashLoader.getData().fonts;
		if (fonts.dataAvailable() && DashLoader.isRead()) {
			profiler.startTick();
			profiler.push("closing");
			final FontManagerAccessor fontManagerAccessor = (FontManagerAccessor) MixinThings.FONTMANAGER;
			final Map<Identifier, FontStorage> fontStorages = fontManagerAccessor.getFontStorages();

			fontStorages.values().forEach(FontStorage::close);
			fontStorages.clear();

			DashLoader.LOGGER.info("Applying fonts off-thread");
			profiler.swap("reloading");
			this.cache.forEach((identifier, entry) -> {
				FontStorage fontStorage = new FontStorage(fontManagerAccessor.getTextureManager(), identifier);
				FontStorageAccessor access = (FontStorageAccessor) fontStorage;
				access.callCloseFonts();
				access.callCloseGlyphAtlases();
				access.getGlyphRendererCache().clear();
				access.getGlyphCache().clear();
				access.getCharactersByWidth().clear();
				access.setBlankGlyphRenderer(access.callGetGlyphRenderer(BlankGlyph.INSTANCE));
				access.setWhiteRectangleGlyphRenderer(access.callGetGlyphRenderer(WhiteRectangleGlyph.INSTANCE));

				access.getCharactersByWidth().putAll(entry.getKey());
				access.getFonts().addAll(entry.getValue());
				fontStorages.put(identifier, fontStorage);
			});

			profiler.pop();
			profiler.endTick();
			ci.cancel();
		}
	}

	@Inject(method = {"method_18635", "apply*"}, at = @At(value = "TAIL"))
	private void applyInject(Map<Identifier, List<Font>> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
		if (DashLoader.isWrite()) {
			DashLoader.getData().fonts.setMinecraftData(map);
		}
	}

	private Pair<Int2ObjectMap<IntList>, List<Font>> computeFonts(List<Font> fonts) {
		Int2ObjectMap<IntList> charactersByWidth = new Int2ObjectOpenHashMap<>();
		List<Font> fontsOut = new ArrayList<>();

		final Glyph space = FontStorageAccessor.getSPACE();

		final IntFunction<IntList> creatIntArrayListFunc = (i) -> new IntArrayList();

		fonts.forEach(currentFont -> currentFont.getProvidedGlyphs().forEach((IntConsumer) codePoint -> {
			for (int i = 0, fontsSize = fonts.size(); i < fontsSize; i++) {
				Font font = fonts.get(i);
				Glyph glyph = codePoint == 32 ? space : font.getGlyph(codePoint);
				if (glyph != null) {
					if (glyph != BlankGlyph.INSTANCE) {
						charactersByWidth.computeIfAbsent(MathHelper.ceil(glyph.getAdvance(false)), creatIntArrayListFunc).add(codePoint);
					}
					fontsOut.add(font);
					break;
				}
			}
		}));


		return Pair.of(charactersByWidth, fontsOut);
	}


	@Mixin(FontManager.class)
	private static class LeoFontSolution {
		@Inject(method = "<init>", at = @At(value = "TAIL"))
		private void initInject(TextureManager manager, CallbackInfo ci) {
			MixinThings.FONTMANAGER = ((FontManager) (Object) this);
		}
	}

}
