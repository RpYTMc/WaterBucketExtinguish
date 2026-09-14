package water.bucket;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaterBucket implements ModInitializer {

	public static final String MOD_ID = "waterbucket";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// The mod is client-side only; everything lives in WaterBucketClient.
	}
}
