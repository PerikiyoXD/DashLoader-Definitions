package dev.quantumfusion.dashloader.def.mixin.accessor;

import dev.quantumfusion.hyphen.thr.HyphenException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.state.StateManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelLoader.class)
public interface ModelLoaderAccessor {

	@Accessor
	void setSpriteAtlasManager(SpriteAtlasManager spriteAtlasManager);

	@Accessor
	@Mutable
	Map<Identifier, UnbakedModel> getModelsToBake();

	@Accessor
	@Mutable
	void setResourceManager(ResourceManager resourceManager);


	@Accessor("ITEM_FRAME_STATE_FACTORY")
	static StateManager<Block, BlockState> getTheItemFrameThing() {
		throw new HyphenException("froge", "your dad");
	}

}
