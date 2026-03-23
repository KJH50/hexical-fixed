package miyucomics.hexical.mixin;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.GarbageIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem;
import at.petrak.hexcasting.common.casting.actions.rw.OpRead;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import miyucomics.hexical.inits.HexicalItems;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(value = OpRead.class, remap = false)
public class OpReadMixin {
	
	// Wrap the original execute method to add Curio Compass support
	@WrapMethod(method = "execute")
	private List<Iota> readFromCompassCurio(List<Iota> args, CastingEnvironment env, Operation<List<Iota>> original) throws MishapBadOffhandItem {
		try {
			return original.call(args, env);
		} catch (Throwable exception) {
			CastingEnvironment.HeldItemInfo data = env.getHeldItemToOperateOn(
				item -> item.isOf(HexicalItems.CURIO_COMPASS)
					&& item.hasNbt()
					&& item.getNbt().contains("needle", NbtElement.INT_ARRAY_TYPE)
					&& item.getNbt().getIntArray("needle").length >= 3
			);
			if (data == null)
				 throw MishapBadOffhandItem.of(null, "iota.read");

			LivingEntity caster = env.getCastingEntity();
			if (caster == null)
				return List.of(new GarbageIota());

			int[] coordinates = data.stack().getNbt().getIntArray("needle");
			return List.of(new Vec3Iota(new Vec3d(coordinates[0], coordinates[1], coordinates[2]).subtract(caster.getEyePos()).normalize()));
		}
	}
}