package de.schulung.customers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StaticResourcesTests {

	@Autowired
	MockMvc mvc;

	@Test
	void when_get_index_then_return_index_html() throws Exception {
		mvc.perform(get("/index.html"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML));
	}

  @Test
  void when_get_openapi_then_return_openapi_yaml() throws Exception {
    mvc.perform(get("/openapi.yaml"))
      .andExpect(status().isOk());
  }

}
