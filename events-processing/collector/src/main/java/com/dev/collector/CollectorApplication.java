package com.dev.collector;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableSwagger2
@Slf4j
public class CollectorApplication {

	public static void main(String[] args) {
		waitForDependencies();
		SpringApplication.run(CollectorApplication.class, args);
	}


	public static void waitForDependencies() {
		waitForIt("kafka", 9092, 30);
	}

	public static void waitForIt(String ip, Integer port, int seconds) {
		boolean ready = false;
		Instant stop = Instant.now().plusSeconds(seconds);
		while (!ready && Instant.now().isBefore(stop)) {
			try {
				SocketAddress sockaddr = new InetSocketAddress(ip, port);
				Socket socket = new Socket();
				try {
					socket.connect(sockaddr, 1000);
					ready = true;
					log.info("{}:{} is now reachable after {} ms", ip, port, (Instant.now().toEpochMilli() - stop.minusSeconds(30).toEpochMilli()));
				} catch (Exception exception) {
					TimeUnit.MILLISECONDS.sleep(300);
				} finally {
					if (ready) {
						socket.close();
					}
				}
			} catch (Exception e) {
				log.error("Error while checking the connection for {}:{}", ip, port, e);
			}
		}

		if (!ready) {
			log.error("{}:{} is NOT reachable!", ip, port);
			log.error("Exiting application!");
			System.exit(1);
		}
	}
}

