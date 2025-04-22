package com.pcclub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PcClubApplicationTests {

	@Test
	void contextLoads() {
		// Проверяет, что контекст приложения загружается корректно
	}

}