package com.devsuperior.dscommerce.controllers.IT;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.entities.Category;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.tests.ProductFactory;
import com.devsuperior.dscommerce.tests.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ProductControllerIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private TokenUtil tokenUtil;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private String productName;
	private String adminUserName, adminPassword;
	private String clientusername, clientPassword;
	
	private String adminToken, invalidToken, clientToken;
	
	private Product product;
	private ProductDTO productDTO;
	
	
	@BeforeEach
	void setup() throws Exception {
		productName = "Macbook";
		
		adminUserName = "alex@gmail.com";
		adminPassword = "123456";
		clientusername = "maria@gmail.com";
		clientPassword = "123456";
		
		adminToken = tokenUtil.obtainAccessToken(mockMvc, adminUserName, adminPassword);
		clientToken = tokenUtil.obtainAccessToken(mockMvc, clientusername, clientPassword);
		invalidToken = adminToken + "xpto";
		
		product = new Product(null, "PS5", "Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit"
				, 3999.90, "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
		Category category = new Category(2L, "Eletro");
		product.getCategories().add(category);
		productDTO = new ProductDTO(product);
		
	}

	@Test
	public void findAllShouldReturnPageWhenNameParamIsNotEmpty() throws Exception {
		
		ResultActions result = mockMvc.perform(get("/products?name={productName}", productName)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.content[0].id").value(3L));
		result.andExpect(jsonPath("$.content[0].name").value("Macbook Pro"));
		result.andExpect(jsonPath("$.content[0].price").value(1250.0));
		result.andExpect(jsonPath("$.content[0].imgUrl").value("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/3-big.jpg"));
	}

	@Test
	public void findAllShouldReturnPageWhenNameParamIsEmpty() throws Exception {
		
		ResultActions result = mockMvc.perform(get("/products", productName)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.content[0].id").value(1L));
		result.andExpect(jsonPath("$.content[0].price").value(90.5));
	}
	
	@Test
	public void insertShouldReturnProductDTOValidateDateWhenLoggedAdmin() throws Exception {
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result =
				mockMvc.perform(post("/products")
						.header("Authorization", "Bearer " + adminToken)
						.content(jsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
						.andDo(MockMvcResultHandlers.print());
		
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").value(26L));
		result.andExpect(jsonPath("$.name").value("PS5"));
		result.andExpect(jsonPath("$.description").value("Lorem ipsum, dolor sit amet consectetur adipisicing elit. Qui ad, adipisci illum ipsam velit"));
		result.andExpect(jsonPath("$.price").value(3999.90));
		result.andExpect(jsonPath("$.imgUrl").value("https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg"));
		result.andExpect(jsonPath("$.categories[0].id").value(2L));
	}
	
	@Test
	public void insertShouldReturnProduct422WhenAdminLoggedFieldNameIsEmpty() throws Exception {
		
		product.setName("ab");
		productDTO = new ProductDTO(product);
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result =
				mockMvc.perform(post("/products")
						.header("Authorization", "Bearer " + adminToken)
						.content(jsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableEntity());
		result.andExpect(jsonPath("$.errors[0].fieldName").value("name"));
		result.andExpect(jsonPath("$.errors[0].message").value("Nome precisar ter de 3 a 80 caracteres"));
	}
	
	@Test
	public void insertShouldReturnProduct422WhenAdminLoggedFieldDescriptionIsInvalid() throws Exception {
		
		product.setDescription("ab");
		productDTO = new ProductDTO(product);
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result =
				mockMvc.perform(post("/products")
						.header("Authorization", "Bearer " + adminToken)
						.content(jsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableEntity());
		result.andExpect(jsonPath("$.errors[0].fieldName").value("description"));
		result.andExpect(jsonPath("$.errors[0].message").value("Descrição precisa ter no mínimo 10 caracteres"));	
	}
	@Test
	public void insertShouldReturnProduct422WhenAdminLoggedFieldAndPriceIsNegative() throws Exception {
		
		product.setPrice(-5.0);
		productDTO = new ProductDTO(product);
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result =
				mockMvc.perform(post("/products")
						.header("Authorization", "Bearer " + adminToken)
						.content(jsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableEntity());
	}
	@Test
	public void insertShouldReturnProduct422WhenAdminLoggedFieldAndPriceIsZero() throws Exception {
		
		product.setPrice(0.0);
		productDTO = new ProductDTO(product);
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result =
				mockMvc.perform(post("/products")
						.header("Authorization", "Bearer " + adminToken)
						.content(jsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableEntity());
	}
	@Test
	public void insertShouldReturnProduct422WhenAdminLoggedFieldAndProductHasCategoriesNotExistis() throws Exception {
		
		product.getCategories().clear();
		productDTO = new ProductDTO(product);
		
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result =
				mockMvc.perform(post("/products")
						.header("Authorization", "Bearer " + adminToken)
						.content(jsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableEntity());
	}
}
