package com.example.apps.wishlists.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.apps.auth.entities.User;
import com.example.apps.auth.repositories.IUserRepository;
import com.example.apps.products.dtos.ProductDTO;
import com.example.apps.products.entities.Product;
import com.example.apps.products.repositories.ProductRepository;
import com.example.apps.wishlists.entities.WishList;
import com.example.apps.wishlists.repositories.WishListRepository;
import com.example.apps.wishlists.services.IWishListService;
import com.example.tfs.exceptions.ProductNotFound;
import com.example.tfs.exceptions.UserNotFoundException;
import com.example.tfs.exceptions.WishListAlreadyContainsProduct;
import com.example.tfs.exceptions.WishListNotFound;

@Service
public class WishListServiceImpl implements IWishListService {

    @Autowired
    private WishListRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private IUserRepository userRepository;

    private void checkOwnership(User user, String username) throws AccessDeniedException {
        if (!user.getUsername().equals(username)) {
            throw new AccessDeniedException("Access Denied!");
        }
    }

    private void checkWishList(User user) {
        if (user.getWishlist() == null) {
            throw new WishListNotFound("Wishlist not found");
        }
    }

    private void checkDuplicateProduct(Product product, WishList wishList) {
        if (wishList.getProducts().contains(product)) {
            throw new WishListAlreadyContainsProduct("Product already exists in the wishlist");
        }

    }

    @Override
    public List<ProductDTO> getById(Long userId, String actualUserUsername) throws AccessDeniedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        checkOwnership(user, actualUserUsername);
        checkWishList(user);
        WishList wishList = wishlistRepository.findById(user.getWishlist().getId())
                .orElseThrow(() -> new WishListNotFound("WishList not found"));
        List<Product> products = wishList.getProducts();
        List<ProductDTO> productDTOs = new ArrayList<>();

        for (Product product : products) {
            ProductDTO ProductDTO = new ProductDTO();
            BeanUtils.copyProperties(product, ProductDTO);
            productDTOs.add(ProductDTO);
        }

        return productDTOs;

    }

    @Override
    @Transactional
    public void putProductToWishlist(Long userId, Long productId, String actualUserUsername)
            throws AccessDeniedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        checkOwnership(user, actualUserUsername);
        checkWishList(user);
        WishList wishList = wishlistRepository.findById(user.getWishlist().getId())
                .orElseThrow(() -> new WishListNotFound("WishList not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFound("Product not found"));
        checkDuplicateProduct(product, wishList);

        user.getWishlist().getProducts().add(product);

        wishlistRepository.save(wishList);

    }

    @Override
    @Transactional
    public void deleteProductFromWishlist(Long userId, Long productId, String actualUserUsername)
            throws AccessDeniedException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        checkOwnership(user, actualUserUsername);
        checkWishList(user);
        WishList wishList = wishlistRepository.findById(user.getWishlist().getId())
                .orElseThrow(() -> new WishListNotFound("WishList not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFound("Product not found"));

        user.getWishlist().getProducts().remove(product);

        wishlistRepository.save(wishList);

    }

}
