package com.devsuperior.dscommerce.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscommerce.dto.ProductDTO;
import com.devsuperior.dscommerce.dto.ProductMinDTO;
import com.devsuperior.dscommerce.entities.Product;
import com.devsuperior.dscommerce.repositories.ProductRepository;
import com.devsuperior.dscommerce.services.exceptions.DatabaseException;
import com.devsuperior.dscommerce.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscommerce.tests.ProductFactory;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

	@InjectMocks
	private ProductService service;

	@Mock
	private ProductRepository repository;

	private long existingId;
	private long nonExistingId;
	private long dependentId;
	private Product product;
	private PageImpl<Product> page;
	private ProductDTO productDTO;
	private String productName;

	@BeforeEach
	void setup() {

		existingId = 1L;
		nonExistingId = 1000L;
		dependentId = 2L;
		product = ProductFactory.createProduct();
		page = new PageImpl<>(List.of(product));
		productName = "PS5";

		product = ProductFactory.createProduct(productName);
		productDTO = new ProductDTO(product);

		Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
		
		Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

		
		Mockito.when(repository.searchByName(eq(productName), any())).thenReturn(page);
		Mockito.when(repository.getReferenceById(existingId)).thenReturn(product);
		Mockito.when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);
		Mockito.when(repository.save(any())).thenReturn(product);
		

		Mockito.doNothing().when(repository).deleteById(existingId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
		
		Mockito.when(repository.existsById(existingId)).thenReturn(true);
		Mockito.when(repository.existsById(nonExistingId)).thenReturn(false);
		Mockito.when(repository.existsById(dependentId)).thenReturn(true);

	}

	@Test
	public void findByShouldReturnProductDTOWhenIdExists() {

		ProductDTO result = service.findById(existingId);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), existingId);
		Assertions.assertEquals(result.getName(), product.getName());

	}

	@Test
	public void findByShouldReturnResourceNotFoundExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});
	}	

	@Test
	public void findAllShouldReturnPageable() {

		Pageable pageable = PageRequest.of(0, 5);
		String name = "PS5";

		Page<ProductMinDTO> result = service.findAll(name, pageable);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getSize(), 1);
		Assertions.assertEquals(result.iterator().next().getName(), productName);
	}

	@Test
	public void updateShouldProductDTOWhenExistingId() {

		ProductService serviceSpy = Mockito.spy(service);
		ProductDTO result = serviceSpy.update(existingId, productDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(productDTO.getId(), existingId);

	}
	
	@Test
	public void updateShouldResourceNotFoundExceptionWhenIdOrProductDoesNotExisting() {
		
		ProductService serviceSpy = Mockito.spy(service);
		
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			@SuppressWarnings("unused")
			ProductDTO result = serviceSpy.update(nonExistingId, productDTO);

		});		
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() { 
		
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(dependentId);
		});
		
		Mockito.verify(repository, times(1)).deleteById(dependentId);
	} 	
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
	}

	@Test
	public void deleteShouldProductDTOWhenExistingId() {

		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});

		Mockito.verify(repository, times(1)).deleteById(existingId);

	}
	
	@Test
	public void insertShouldProductDTOWhenIdExisting() {
		
		ProductService serviceSpy = Mockito.spy(service);
		ProductDTO result = serviceSpy.insert(productDTO);

		Assertions.assertNotNull(result);
		Assertions.assertEquals(productDTO.getId(), existingId);
	}
}
