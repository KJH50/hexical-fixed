package miyucomics.hexical.inits

import miyucomics.hexical.HexicalMain
import miyucomics.hexical.inits.HexicalBlocks.PERIWINKLE_FLOWER
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback
import net.minecraft.client.gl.ShaderProgram
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats

object HexicalRenderLayers {
	val PERLIN_NOISE_TEXTURE: RenderPhase.Textures = RenderPhase.Textures.create().add(HexicalMain.id("textures/misc/perlin.png"), false, false).build()

	private lateinit var mageBlockShader: ShaderProgram
	lateinit var mageBlockRenderLayer: RenderLayer
		private set

	private lateinit var mediaJarShader: ShaderProgram
	lateinit var mediaJarRenderLayer: RenderLayer
		private set

	/**
	 * 检查渲染层是否已初始化（用于 Mixin 中避免循环初始化）
	 */
	fun isInitialized(): Boolean = ::mageBlockRenderLayer.isInitialized && ::mediaJarRenderLayer.isInitialized

	fun registerLayers() {
		// 防止重复初始化导致 lateinit 异常
		if (::mageBlockRenderLayer.isInitialized) return

		mageBlockRenderLayer = RenderLayer.of(
			"mage_block",
			VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL,
			VertexFormat.DrawMode.QUADS,
			2097152,
			true,
			false,
			MultiPhaseParameters.builder()
				.lightmap(RenderPhase.ENABLE_LIGHTMAP)
				.program(RenderPhase.ShaderProgram { mageBlockShader })
				.texture(PERLIN_NOISE_TEXTURE)
				.build(true)
		)

		mediaJarRenderLayer = RenderLayer.of(
			"media_jar",
			VertexFormats.POSITION_TEXTURE_COLOR_NORMAL,
			VertexFormat.DrawMode.QUADS,
			512,
			MultiPhaseParameters.builder()
				.program(RenderPhase.ShaderProgram { mediaJarShader })
				.texture(PERLIN_NOISE_TEXTURE)
				.transparency(RenderPhase.NO_TRANSPARENCY)
				.cull(RenderPhase.ENABLE_CULLING)
				.lightmap(RenderPhase.DISABLE_LIGHTMAP)
				.overlay(RenderPhase.DISABLE_OVERLAY_COLOR)
				.build(true)
		)
	}

	fun clientInit() {
		CoreShaderRegistrationCallback.EVENT.register { context ->
			context.register(HexicalMain.id("media_jar"), VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL) { mageBlockShader = it }
			context.register(HexicalMain.id("media_jar"), VertexFormats.POSITION_TEXTURE_COLOR_NORMAL) { mediaJarShader = it }
		}

		// 延迟渲染层创建，避免与 Oculus 等模组的 Mixin 冲突
		registerLayers()

		BlockRenderLayerMap.INSTANCE.putBlock(HexicalBlocks.MAGE_BLOCK, mageBlockRenderLayer)
		BlockRenderLayerMap.INSTANCE.putBlock(PERIWINKLE_FLOWER, RenderLayer.getCutout())
	}
}