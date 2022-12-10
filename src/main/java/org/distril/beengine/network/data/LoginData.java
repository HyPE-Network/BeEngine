package org.distril.beengine.network.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.nukkitx.protocol.bedrock.data.skin.SerializedSkin;
import com.nukkitx.protocol.bedrock.util.EncryptionUtils;
import io.netty.util.AsciiString;
import lombok.Getter;
import org.distril.beengine.util.SkinUtil;

import java.util.UUID;

@Getter
public class LoginData {

	private static final Gson GSON = new Gson();

	private final String xuid;
	private final String identityPublicKey;
	private final UUID uuid;
	private final String username;
	private final String languageCode;
	private final Device device;
	private final SerializedSkin skin;
	private final boolean authenticated;


	private LoginData(String xuid, String identityPublicKey, UUID uuid, String username, String languageCode,
	                  Device device, SerializedSkin skin, boolean authenticated) {
		this.xuid = xuid;
		this.identityPublicKey = identityPublicKey;
		this.uuid = uuid;
		this.username = username;
		this.languageCode = languageCode;
		this.device = device;
		this.skin = skin;
		this.authenticated = authenticated;
	}

	public static LoginData extract(AsciiString chainData, AsciiString skinData) {
		try {
			JsonObject chainJSON = GSON.fromJson(chainData.toString(), JsonObject.class);

			JSONArray chains = new JSONArray();
			chainJSON.getAsJsonArray("chain").forEach(chain -> chains.add(chain.getAsString()));

			var authenticated = EncryptionUtils.verifyChain(chains);

			// Retrieve xuid, uuid, and username
			String chainPayload = JWSObject.parse(((String) chains.get(chains.size() - 1))).getPayload().toString();
			JsonObject jsonPayload = GSON.fromJson(chainPayload, JsonObject.class);
			JsonObject extraData = jsonPayload.getAsJsonObject("extraData");

			var xuid = extraData.get("XUID").getAsString();
			var uuid = UUID.fromString(extraData.get("identity").getAsString());
			var username = extraData.get("displayName").getAsString();
			var identityPublicKey = jsonPayload.get("identityPublicKey").getAsString();

			// Extract data from skin string
			JWSObject skinJWS = JWSObject.parse(skinData.toString());
			JsonObject skinJSON = GSON.fromJson(skinJWS.getPayload().toString(), JsonObject.class);

			// Retrieve device and language code
			var device = Device.fromOS(skinJSON.get("DeviceOS").getAsInt());
			var languageCode = skinJSON.get("LanguageCode").getAsString();

			// Retrieve skin
			var skin = SkinUtil.fromToken(skinJSON);
			return new LoginData(xuid, identityPublicKey, uuid, username, languageCode, device, skin, authenticated);
		} catch (Exception exception) {
			return null;
		}
	}
}
