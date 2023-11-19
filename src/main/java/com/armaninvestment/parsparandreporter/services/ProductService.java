package com.armaninvestment.parsparandreporter.services;

import com.armaninvestment.parsparandreporter.dtos.ProductDto;
import com.armaninvestment.parsparandreporter.entities.Product;
import com.armaninvestment.parsparandreporter.exceptions.DatabaseIntegrityViolationException;
import com.armaninvestment.parsparandreporter.mappers.ProductMapper;
import com.armaninvestment.parsparandreporter.repositories.ContractRepository;
import com.armaninvestment.parsparandreporter.repositories.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ContractRepository contractRepository;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductMapper productMapper,
                          ContractRepository contractRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.contractRepository = contractRepository;
    }

    // Create a new product
    public ProductDto createProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        Product savedProduct = productRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    // Retrieve all products
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll().stream().map(productMapper::toDto).collect(Collectors.toList());
    }

    public List<ProductDto> searchProductByNameContaining(String searchQuery) {
        return productRepository.findByProductNameContains(searchQuery).stream().map(productMapper::toDto).collect(Collectors.toList());
    }

    // Retrieve a product by ID
    public Optional<ProductDto> getProductById(Long productId) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isPresent()) {
            return product.map(productMapper::toDto);
        }

        return Optional.empty();
    }

    // Update a product by ID
    public Optional<ProductDto> updateProduct(Long productId, ProductDto productDto) {
        Optional<Product> existingProduct = productRepository.findById(productId);

        if (existingProduct.isPresent()) {
            Product productToUpdate = existingProduct.get();
            productMapper.partialUpdate(productDto, productToUpdate);
            Product updatedProduct = productRepository.save(productToUpdate);
            return Optional.of(productMapper.toDto(updatedProduct));
        }

        return Optional.empty();
    }


    public void deleteProduct(Long productId) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isEmpty()) {
            throw new EntityNotFoundException("مشتری ای با شناسه " + productId + "یافت نشد.");
        }
        Product product = optionalProduct.get();

        if (!product.getContractItems().isEmpty()) {
            throw new DatabaseIntegrityViolationException("امکان حذف محصول وجود ندارد چون آیتم های قرارداد مرتبط دارد.");
        }
        if (!product.getWarehouseReceiptItems().isEmpty()) {
            throw new DatabaseIntegrityViolationException("امکان حذف محصول وجود ندارد چون آیتم های حواله مرتبط دارد.");
        }
        contractRepository.deleteById(productId);
    }

    @Transactional
    public void importProductsFromExcel(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row currentRow : sheet) {
                if (currentRow.getRowNum() == 0) {
                    continue;
                }

                String productCode = currentRow.getCell(0).getStringCellValue();
                String productName = currentRow.getCell(1).getStringCellValue();
                String measurementIndex = currentRow.getCell(2).getStringCellValue();


                Product product = new Product();
                product.setProductCode(productCode);
                product.setProductName(productName);
                product.setMeasurementIndex(measurementIndex);
                productRepository.save(product);
            }
        }
    }

    public XSSFWorkbook generateProductListExcel() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Products");

        List<ProductDto> productList = productRepository.findAll().stream().map(productMapper::toDto).toList();

        XSSFRow headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Product ID");
        headerRow.createCell(1).setCellValue("Product Code");
        headerRow.createCell(2).setCellValue("Product Name");
        headerRow.createCell(3).setCellValue("Measurement Index");

        int rowNum = 1;
        for (ProductDto product : productList) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getId());
            row.createCell(1).setCellValue(product.getProductCode());
            row.createCell(2).setCellValue(product.getProductName());
            row.createCell(3).setCellValue(product.getMeasurementIndex());
        }

        return workbook;
    }
}
