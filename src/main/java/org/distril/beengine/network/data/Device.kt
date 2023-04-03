package org.distril.beengine.network.data

enum class Device(
	val osId: Int,
	val osName: String
) {

	UNKNOWN(-1, "Unknown"),
	ANDROID(1, "Android"),
	IOS(2, "IOS"),
	OSX(3, "OSX"),
	AMAZON(4, "Amazon"),
	GEAR_VR(5, "Gear VR"),
	HOLOLENS(6, "HoloLens"),
	WINDOWS_10(7, "Windows 10"),
	WINDOWS_32(8, "Windows 32"),
	DEDICATED(9, "Dedicated"),
	TV_OS(10, "TVOS"),
	PLAYSTATION(11, "PlayStation"),
	NINTENDO(12, "Nintendo"),
	XBOX(13, "Xbox"),
	WINDOWS_PHONE(14, "Windows Phone");

	companion object {

		fun fromOSId(osId: Int): Device {
			return Device.values().firstOrNull { it.osId == osId } ?: UNKNOWN
		}
	}
}
