package miyucomics.hexical.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import miyucomics.hexical.inits.HexicalRenderLayers;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(RenderLayer.class)
public class RenderLayerMixin {
	@WrapMethod(method = "getBlockLayers")
	private static List<RenderLayer> alterBlockLayers(Operation<List<RenderLayer>> original) {
		List<RenderLayer> mutableCopy = new ArrayList<>(original.call());
		// 检查渲染层是否已初始化，避免在 RenderType.<clinit> 期间访问未初始化的字段
		if (HexicalRenderLayers.INSTANCE.isInitialized()) {
			mutableCopy.add(HexicalRenderLayers.INSTANCE.getMageBlockRenderLayer());
		}
		return Collections.unmodifiableList(mutableCopy);
	}
}