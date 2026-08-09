package com.example.demo.validator;

import java.util.Locale;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.example.demo.form.CustomerForm;

@Component
public class CustomerFormValidator implements Validator {

   private static final int MAX_NAME_LENGTH = 255;
   private static final int MAX_ADDRESS_LENGTH = 255;
   private static final int MAX_EMAIL_LENGTH = 128;
   private static final int MAX_PHONE_LENGTH = 128;

   private final EmailValidator emailValidator = EmailValidator.getInstance();

   // This validator only checks for the CustomerForm.
   @Override
   public boolean supports(Class<?> clazz) {
      return clazz == CustomerForm.class;
   }

   @Override
   public void validate(Object target, Errors errors) {
      CustomerForm custInfo = (CustomerForm) target;

      normalize(custInfo);

      // Check the fields of CustomerForm.
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotEmpty.customerForm.name");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "NotEmpty.customerForm.email");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "address", "NotEmpty.customerForm.address");
      ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phone", "NotEmpty.customerForm.phone");

      if (!isBlank(custInfo.getEmail()) && custInfo.getEmail().length() > MAX_EMAIL_LENGTH) {
         errors.rejectValue("email", "Length.customerForm.email", "Email tối đa 128 ký tự");
      } else if (!isBlank(custInfo.getEmail()) && !emailValidator.isValid(custInfo.getEmail())) {
         errors.rejectValue("email", "Pattern.customerForm.email");
      }
      if (custInfo.getName() != null && custInfo.getName().length() > MAX_NAME_LENGTH) {
         errors.rejectValue("name", "Length.customerForm.name", "Tên người nhận tối đa 255 ký tự");
      }
      if (custInfo.getAddress() != null && custInfo.getAddress().length() > MAX_ADDRESS_LENGTH) {
         errors.rejectValue("address", "Length.customerForm.address", "Địa chỉ tối đa 255 ký tự");
      }
      if (custInfo.getPhone() != null && custInfo.getPhone().length() > MAX_PHONE_LENGTH) {
         errors.rejectValue("phone", "Length.customerForm.phone", "Số điện thoại quá dài");
      }
   }

   private void normalize(CustomerForm customerForm) {
      customerForm.setName(trim(customerForm.getName()));
      customerForm.setAddress(trim(customerForm.getAddress()));
      customerForm.setEmail(normalizeEmail(customerForm.getEmail()));
      customerForm.setPhone(trim(customerForm.getPhone()));
   }

   private String trim(String value) {
      return value == null ? null : value.trim();
   }

   private String normalizeEmail(String value) {
      String trimmed = trim(value);
      return trimmed == null ? null : trimmed.toLowerCase(Locale.ROOT);
   }

   private boolean isBlank(String value) {
      return value == null || value.isEmpty();
   }
}
