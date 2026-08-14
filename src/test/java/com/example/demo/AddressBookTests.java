package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dao.UserAddressDAO;
import com.example.demo.entity.UserAddress;
import com.example.demo.form.UserAddressForm;

@SpringBootTest
@Transactional
class AddressBookTests {

    @Autowired
    private UserAddressDAO userAddressDAO;

    @Autowired
    private EntityManager entityManager;

    @Test
    void saveAddress_marksFirstAddressAsDefault() {
        UserAddressForm form = addressForm("Nguyễn Văn A", false);

        UserAddress saved = userAddressDAO.saveAddress("unit_user_1", form);

        assertNotNull(saved);
        assertTrue(saved.isDefault());
    }

    @Test
    void saveAddress_unsetsPreviousDefaultAddress() {
        String username = "unit_user_2";
        UserAddress firstAddress = userAddressDAO.saveAddress(
                username, addressForm("Địa chỉ 1", true));
        UserAddress secondAddress = userAddressDAO.saveAddress(
                username, addressForm("Địa chỉ 2", true));

        entityManager.flush();
        entityManager.clear();

        UserAddress reloadedFirstAddress = userAddressDAO.getAddressById(firstAddress.getId());
        UserAddress reloadedSecondAddress = userAddressDAO.getAddressById(secondAddress.getId());

        assertFalse(reloadedFirstAddress.isDefault());
        assertTrue(reloadedSecondAddress.isDefault());
    }

    @Test
    void deleteAddress_rejectsNonOwnerAndPreservesAddress() {
        UserAddress address = userAddressDAO.saveAddress(
                "owner_user", addressForm("Chính chủ", false));

        boolean deleted = userAddressDAO.deleteAddress("intruder_user", address.getId());

        assertFalse(deleted);
        assertNotNull(userAddressDAO.getAddressById(address.getId()));
    }

    @Test
    void deleteAddress_allowsOwnerAndRemovesAddress() {
        String owner = "owner_user";
        UserAddress address = userAddressDAO.saveAddress(
                owner, addressForm("Chính chủ", false));

        boolean deleted = userAddressDAO.deleteAddress(owner, address.getId());

        assertTrue(deleted);
        assertNull(userAddressDAO.getAddressById(address.getId()));
    }

    private UserAddressForm addressForm(String receiverName, boolean defaultAddress) {
        UserAddressForm form = new UserAddressForm();
        form.setReceiverName(receiverName);
        form.setPhone("0900000000");
        form.setProvince("Hồ Chí Minh");
        form.setDistrict("Quận 1");
        form.setWard("Phường Bến Nghé");
        form.setStreetAddress("1 Lê Lợi");
        form.setDefault(defaultAddress);
        return form;
    }
}
