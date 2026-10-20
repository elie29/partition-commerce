package com.commerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

@SpringBootTest
class CommerceApplicationTests {

	@Test
	void contextLoads() {
        Assert.isTrue(true, "Context loads successfully");
	}

}
