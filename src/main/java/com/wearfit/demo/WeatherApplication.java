package com.wearfit.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WeatherApplication {

	public static void main(String[] args) {
		System.out.println("开始启动项目...");
		SpringApplication.run(WeatherApplication.class, args);
		System.out.println("项目启动完毕！");
	}

}
