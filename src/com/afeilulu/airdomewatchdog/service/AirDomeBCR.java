package com.afeilulu.airdomewatchdog.service;

public class AirDomeBCR
extends ALongRunningReceiver
{
	@Override
	public Class getLRSUploadClass() {
		return UploadService.class;
	}

	@Override
	public Class getLRSFtp() {
		return FtpFile2JsonService.class;
	}

	@Override
	public Class getRebootClass() {
		return RebootService.class;
	}

	@Override
	public Class getLRSClass() {
		// TODO Auto-generated method stub
		return null;
	}

}
