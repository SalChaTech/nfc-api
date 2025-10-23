package com.salcatech.nfc_api.service;

import com.salcatech.nfc_api.exception.UserProductNotFoundException;
import com.salcatech.nfc_api.model.UserProduct;
import com.salcatech.nfc_api.repository.UserProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserProductService {

    private final UserProductRepository userProductRepository;

    public UserProductService(UserProductRepository userProductRepository) {
        this.userProductRepository = userProductRepository;
    }

    public List<UserProduct> getAll() {
        return userProductRepository.findAll();
    }

    public UserProduct getById(String id) throws UserProductNotFoundException {
        return userProductRepository.findById(id).orElseThrow(UserProductNotFoundException::new);
    }

    public UserProduct save(UserProduct userProduct) {
        return userProductRepository.save(userProduct);
    }

    public void delete(String id) {
        userProductRepository.deleteById(id);
    }
}
