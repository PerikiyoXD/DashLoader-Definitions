package dev.quantumfusion.dashloader.def.data.image;

import dev.quantumfusion.dashloader.core.Dashable;
import dev.quantumfusion.dashloader.core.api.DashObject;
import dev.quantumfusion.dashloader.core.registry.RegistryReader;
import dev.quantumfusion.dashloader.def.mixin.accessor.NativeImageAccessor;
import dev.quantumfusion.hyphen.scan.annotations.Data;
import net.minecraft.client.texture.NativeImage;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.nio.ByteBuffer;

@Data
@DashObject(NativeImage.class)
public class DashImage implements Dashable<NativeImage> {
	public final byte[] image;
	public final NativeImage.Format format;
	public final boolean useSTB;
	public final int width;
	public final int height;

	public DashImage(NativeImage nativeImage) {
		try {
			NativeImageAccessor nativeImageAccess = (NativeImageAccessor) (Object) nativeImage;
			this.format = nativeImage.getFormat();
			this.width = nativeImage.getWidth();
			this.height = nativeImage.getHeight();
			this.image = write(nativeImageAccess.getPointer());
			this.useSTB = nativeImageAccess.getIsStbImage();
		} catch (IOException e) {
			throw new RuntimeException("Failed to create image. Reason: ", e);
		}
	}

	public DashImage(byte[] image, NativeImage.Format format, boolean useSTB, int width, int height) {
		this.image = image;
		this.format = format;
		this.useSTB = useSTB;
		this.width = width;
		this.height = height;
	}


	private byte[] write(long pointer) throws IOException {
		final int capacity = width * height * format.getChannelCount();
		final var byteBuffer = MemoryUtil.memByteBuffer(pointer, capacity);
		final byte[] bytes = new byte[capacity];
		byteBuffer.get(bytes);
		return bytes;
	}

	/**
	 * <h2>I can bet that next dashloader version will change this again. This method needs some serious over engineering.</h2>
	 *
	 * @param registry da registry
	 * @return da image
	 */
	@Override
	public final NativeImage export(final RegistryReader registry) {
		final ByteBuffer buf = MemoryUtil.memAlloc(image.length);
		buf.put(image);
		buf.rewind();
		return NativeImageAccessor.init(format, this.width, this.height, useSTB, MemoryUtil.memAddress(buf));
	}
}
