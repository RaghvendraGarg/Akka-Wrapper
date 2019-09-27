package com.akka.wrapper.service;

import org.springframework.stereotype.Component;

@Component
public class EchoService {

	private String echoString = "default";

	public String getEchoString() {
		return echoString;
	}

}