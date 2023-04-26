package tk.pandadev.dragonwings;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragonWings implements ModInitializer {
    private static final String MOD_ID = "dragonwings";
    private static final Logger LOGGER = LoggerFactory.getLogger("dragonwings");

    @Override
    public void onInitialize() {


        LOGGER.info("Hello Fabric world");
    }
}
