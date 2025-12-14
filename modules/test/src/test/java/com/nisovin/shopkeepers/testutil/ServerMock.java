package com.nisovin.shopkeepers.testutil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_21_R4.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R4.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_21_R4.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_21_R4.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_21_R4.util.Versioning;

import com.nisovin.shopkeepers.api.internal.util.Unsafe;
import com.nisovin.shopkeepers.util.java.Validate;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry.PendingTags;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.tags.TagLoader;

/**
 * Mocks the Server (at least the functions required for our tests).
 * <p>
 * Adopted from CraftBukkit: See AbstractTestingBase and DummyServer.
 */
class ServerMock extends ProxyHandler<Server> {

	private static final RegistryAccess.Frozen REGISTRY_CUSTOM;

	// Static initializer: Ensures that this is only setup once across all tests.
	static {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		// Populate available packs:
		PackRepository packRepository = ServerPacksSource.createVanillaTrustedRepository();
		packRepository.reload();
		// Set up resource manager:
		MultiPackResourceManager resourceManager = new MultiPackResourceManager(
				PackType.SERVER_DATA,
				packRepository.getAvailablePacks().stream().map(Pack::open).toList()
		);
		LayeredRegistryAccess<RegistryLayer> layers = Unsafe.castNonNull(RegistryLayer.createRegistryAccess());
		List<PendingTags<?>> tags = Unsafe.castNonNull(TagLoader.loadTagsForExistingRegistries(
				resourceManager,
				layers.getLayer(RegistryLayer.STATIC)
		));
		RegistryAccess.Frozen worldgenAccess = layers.getAccessForLoading(RegistryLayer.WORLDGEN);
		List<RegistryLookup<?>> lookups = Unsafe.castNonNull(
				TagLoader.buildUpdatedLookups(worldgenAccess, Unsafe.castNonNull(tags))
		);
		RegistryAccess.Frozen loadedWorldgen = RegistryDataLoader.load(
				resourceManager,
				Unsafe.castNonNull(lookups),
				RegistryDataLoader.WORLDGEN_REGISTRIES
		);
		layers = Unsafe.castNonNull(
				layers.replaceFrom(RegistryLayer.WORLDGEN, loadedWorldgen)
		);
		REGISTRY_CUSTOM = Unsafe.castNonNull(layers.compositeAccess().freeze());

		// Set up the server mock as Bukkit API provider:
		Server serverMock = new ServerMock().newProxy();
		Bukkit.setServer(serverMock);

		CraftRegistry.setMinecraftRegistry(REGISTRY_CUSTOM);
	}

	// Calling this method ensures that the static initializer is invoked.
	public static void setup() {
	}

	private final Map<Class<?>, Registry<?>> registers = new HashMap<>();

	private ServerMock() {
		super(Server.class);
	}

	@Override
	protected void setupMethodHandlers() throws Exception {
		this.addHandler(Server.class.getMethod("getItemFactory"), (proxy, args) -> {
			return Unsafe.assertNonNull(CraftItemFactory.instance());
		});

		this.addHandler(Server.class.getMethod("getRegistry", Class.class), (proxy, args) -> {
			assert args != null;
			Class<? extends Keyed> clazz = Unsafe.castNonNull(args[0]);
			return registers.computeIfAbsent(
					clazz,
					key -> CraftRegistry.createRegistry(clazz, REGISTRY_CUSTOM)
			);
		});

		this.addHandler(Server.class.getMethod("getName"), (proxy, args) -> {
			return ServerMock.class.getName();
		});

		this.addHandler(Server.class.getMethod("getVersion"), (proxy, args) -> {
			Package pkg = Unsafe.assertNonNull(ServerMock.class.getPackage());
			return pkg.getImplementationVersion();
		});

		this.addHandler(Server.class.getMethod("getBukkitVersion"), (proxy, args) -> {
			return Versioning.getBukkitVersion();
		});

		final Logger logger = Logger.getLogger(ServerMock.class.getCanonicalName());
		this.addHandler(Server.class.getMethod("getLogger"), (proxy, args) -> {
			return logger;
		});

		this.addHandler(Server.class.getMethod("getUnsafe"), (proxy, args) -> {
			return CraftMagicNumbers.INSTANCE;
		});

		this.addHandler(
				Server.class.getMethod("createBlockData", Material.class),
				(proxy, args) -> {
					Validate.notNull(args, "args is null");
					assert args != null;
					Material material = Unsafe.castNonNull(args[0]);
					return CraftBlockData.newData(material.asBlockType(), Unsafe.uncheckedNull());
				}
		);
	}
}
