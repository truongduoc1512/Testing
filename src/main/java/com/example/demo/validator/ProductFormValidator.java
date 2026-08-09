package com.example.demo.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.example.demo.dao.ProductDAO;
import com.example.demo.entity.Product;
import com.example.demo.form.ProductForm;

@Component
public class ProductFormValidator implements Validator {

   private static final int MAX_CODE_LENGTH = 20;
   private static final int MAX_NAME_LENGTH = 255;

   private final ProductDAO productDAO;

   @Autowired
   public ProductFormValidator(ProductDAO productDAO) {
      this.productDAO = productDAO;
   }

   // This validator only checks for the ProductForm.
   @Override
   public boolean supports(Class<?> clazz) {
      return clazz == ProductForm.class;
   }

   @Override
   public void validate(Object target, Errors errors) {
      ProductForm productForm = (ProductForm) target;

      validateLocalRules(productForm, errors);
      if (errors.hasErrors() || !productForm.isNewProduct()) {
         return;
      }

      Product product = productDAO.findProduct(productForm.getCode());
      if (product != null) {
         errors.rejectValue("code", "Duplicate.productForm.code", "Mã sản phẩm đã tồn tại");
      }
   }

   public void validateLocalRules(ProductForm productForm, Errors errors) {
      normalize(productForm);

      // Check the fields of ProductForm.
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "code", "NotEmpty.productForm.code",
            "Mã sản phẩm không được để trống");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotEmpty.productForm.name",
            "Tên sản phẩm không được để trống");

      if (!errors.hasFieldErrors("code") && productForm.getCode().length() > MAX_CODE_LENGTH) {
         errors.rejectValue("code", "Length.productForm.code", "Mã sản phẩm tối đa 20 ký tự");
      }
      if (!errors.hasFieldErrors("name") && productForm.getName().length() > MAX_NAME_LENGTH) {
         errors.rejectValue("name", "Length.productForm.name", "Tên sản phẩm tối đa 255 ký tự");
      }
      if (!errors.hasFieldErrors("stockQuantity") && productForm.getStockQuantity() < 0) {
         errors.rejectValue("stockQuantity", "Min.productForm.stockQuantity", "Số lượng tồn kho không được âm!");
      }
      if (!errors.hasFieldErrors("price")
            && (!Double.isFinite(productForm.getPrice()) || productForm.getPrice() <= 0)) {
         errors.rejectValue("price", "Min.productForm.price", "Giá sản phẩm phải lớn hơn 0!");
      }
      if (!errors.hasFieldErrors("discountPercent")
            && (productForm.getDiscountPercent() < 0 || productForm.getDiscountPercent() > 100)) {
         errors.rejectValue("discountPercent", "Range.productForm.discountPercent",
               "Phần trăm giảm giá phải trong khoảng 0 đến 100!");
      }
   }

   private void normalize(ProductForm productForm) {
      productForm.setCode(trim(productForm.getCode()));
      productForm.setName(trim(productForm.getName()));
   }

   private String trim(String value) {
      return value == null ? null : value.trim();
   }
}
