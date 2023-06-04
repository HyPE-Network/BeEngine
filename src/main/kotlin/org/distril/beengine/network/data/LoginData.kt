package org.distril.beengine.network.data

import com.google.gson.JsonObject
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.shaded.json.JSONArray
import com.nukkitx.protocol.bedrock.data.skin.SerializedSkin
import com.nukkitx.protocol.bedrock.util.EncryptionUtils
import io.netty.util.AsciiString
import org.distril.beengine.util.SkinUtil
import org.distril.beengine.util.Utils.gson
import java.util.*

class LoginData private constructor(
    val xuid: String,
    val identityPublicKey: String,
    val uuid: UUID,
    val username: String,
    val languageCode: String,
    val device: Device,
    val skin: SerializedSkin,
    val authenticated: Boolean
) {

    companion object {

        fun extract(chainData: AsciiString, skinData: AsciiString): LoginData? {
            return try {
                val chainJSON = gson.fromJson(chainData.toString(), JsonObject::class.java)
                val chains = JSONArray()
                chainJSON.getAsJsonArray("chain").forEach { chains.add(it.asString) }

                val authenticated = EncryptionUtils.verifyChain(chains)

                // Retrieve xuid, uuid, and username
                val chainPayload = JWSObject.parse(chains[chains.size - 1] as String).payload.toString()
                val jsonPayload = gson.fromJson(chainPayload, JsonObject::class.java)
                val extraData = jsonPayload.getAsJsonObject("extraData")
                val xuid = extraData["XUID"].asString
                val uuid = UUID.fromString(extraData["identity"].asString)
                val username = extraData["displayName"].asString
                val identityPublicKey = jsonPayload["identityPublicKey"].asString

                // Extract data from skin string
                val skinJWS = JWSObject.parse(skinData.toString())
                val skinJSON = gson.fromJson(skinJWS.payload.toString(), JsonObject::class.java)

                // Retrieve device and language code
                val device = Device.fromOSId(skinJSON["DeviceOS"].asInt)
                val languageCode = skinJSON["LanguageCode"].asString

                // Retrieve skin
                val skin = SkinUtil.fromToken(skinJSON)
                LoginData(xuid, identityPublicKey, uuid, username, languageCode, device, skin, authenticated)
            } catch (exception: Exception) {
                null
            }
        }
    }
}
