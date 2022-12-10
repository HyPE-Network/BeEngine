package org.distril.beengine.network.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Device {

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

	private final int deviceOS;
	private final String name;

	public static Device fromOS(int deviceOS) {
		return Arrays.stream(Device.values())
				.filter(device -> device.getDeviceOS() == deviceOS)
				.findFirst()
				.orElse(Device.UNKNOWN);
	}
}
