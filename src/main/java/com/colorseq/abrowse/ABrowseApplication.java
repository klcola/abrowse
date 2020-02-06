package com.colorseq.abrowse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author Lei Kong
 */
/*@EntityScan("com.colorseq.cscore")
@ComponentScan("com.colorseq.cscore")
@EnableJpaRepositories("com.colorseq.cscore")*/
@EntityScan("com.colorseq")
@EnableJpaRepositories("com.colorseq")
@SpringBootApplication
public class ABrowseApplication {

	public static void main(String[] args) {
		SpringApplication.run(ABrowseApplication.class, args);
	}
}

