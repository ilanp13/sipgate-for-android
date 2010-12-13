package com.sipgate.api.types;

/**
 * RegisteredMobileDevice
 * 
 * @author graef
 * @version 2.21.0
 */
public class RegisteredMobileDevice
{
	private String deviceNumberE164;
	private String deviceVendor;
	private String deviceModel;
	private String deviceFirmware;

	public String getDeviceNumberE164()
	{
		return deviceNumberE164;
	}

	public void setDeviceNumberE164(String deviceNumberE164)
	{
		this.deviceNumberE164 = deviceNumberE164;
	}

	public String getDeviceVendor()
	{
		return deviceVendor;
	}

	public void setDeviceVendor(String deviceVendor)
	{
		this.deviceVendor = deviceVendor;
	}

	public String getDeviceModel()
	{
		return deviceModel;
	}

	public void setDeviceModel(String deviceModel)
	{
		this.deviceModel = deviceModel;
	}

	public String getDeviceFirmware()
	{
		return deviceFirmware;
	}

	public void setDeviceFirmware(String deviceFirmware)
	{
		this.deviceFirmware = deviceFirmware;
	}
}
