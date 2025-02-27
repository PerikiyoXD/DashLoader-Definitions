package dev.quantumfusion.dashloader.def.mixin.accessor;

import net.minecraft.client.gl.GlBlendState;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.Program;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map;

@Mixin(Shader.class)
public interface ShaderAccessor {

	@Accessor
	Map<String, Object> getSamplers();

	@Accessor
	@Mutable
	void setSamplers(Map<String, Object> samplers);

	@Accessor
	GlBlendState getBlendState();

	@Accessor
	@Mutable
	void setBlendState(GlBlendState blendState);

	@Accessor
	List<Integer> getLoadedAttributeIds();


	@Accessor
	Map<String, GlUniform> getLoadedUniforms();

	@Accessor
	@Mutable
	void setLoadedAttributeIds(List<Integer> loadedAttributeIds);

	@Accessor
	List<String> getAttributeNames();

	@Accessor
	@Mutable
	void setAttributeNames(List<String> attributeNames);

	@Accessor
	List<String> getSamplerNames();

	@Accessor
	@Mutable
	void setSamplerNames(List<String> samplerNames);

	@Accessor
	@Mutable
	void setLoadedSamplerIds(List<Integer> loadedSamplerIds);

	@Accessor
	@Mutable
	void setUniforms(List<GlUniform> uniforms);

	@Accessor
	@Mutable
	void setLoadedUniformIds(List<Integer> loadedUniformIds);

	@Accessor
	@Mutable
	void setLoadedUniforms(Map<String, GlUniform> loadedUniforms);

	@Accessor
	@Mutable
	void setProgramId(int programId);

	@Accessor
	@Mutable
	void setName(String name);

	@Accessor
	@Mutable
	void setVertexShader(Program vertexShader);

	@Accessor
	@Mutable
	void setFragmentShader(Program fragmentShader);

	@Accessor
	@Mutable
	void setFormat(VertexFormat format);

	@Invoker("loadReferences")
	void loadref();
}


