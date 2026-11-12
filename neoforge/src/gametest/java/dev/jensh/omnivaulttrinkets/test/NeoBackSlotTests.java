package dev.jensh.omnivaulttrinkets.test;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

/** Test mod: installs the client test driver when the clientTest run asks for it. */
@Mod("omnivault_trinkets_compat_test")
public final class NeoBackSlotTests {
	public NeoBackSlotTests() {
		if (Boolean.getBoolean("omnivault_trinkets_compat.client_test") && FMLEnvironment.getDist() == Dist.CLIENT) {
			NeoClientTest.install();
		}
	}
}
