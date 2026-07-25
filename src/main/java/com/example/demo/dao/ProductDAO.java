package com.example.demo.dao;

import java.io.IOException;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Product;
import com.example.demo.form.ProductForm;
import com.example.demo.model.ProductInfo;
import com.example.demo.pagination.PaginationResult;

@Transactional
@Repository
public class ProductDAO {
 
    @Autowired
    private SessionFactory sessionFactory;
 
    public Product findProduct(String code) {
        try {
            Session session = this.sessionFactory.getCurrentSession();
            return session.find(Product.class, code);
        } catch (Exception e) {
            return null;
        }
    }
 
    public ProductInfo findProductInfo(String code) {
        Product product = this.findProduct(code);
        if (product == null) {
            return null;
        }
        return new ProductInfo(product.getCode(), product.getName(), product.getPrice(),
                               product.getDiscountPercent(), product.getSalesCount(), product.getLocation(),
                               product.getBrand(), product.getRating(), product.isMall(), product.isFavored());
    }
 
    @Transactional(rollbackFor = Exception.class)
    public void save(ProductForm productForm) {
        Session session = this.sessionFactory.getCurrentSession();
        String code = productForm.getCode();
 
        Product product = null;
 
        boolean isNew = false;
        if (code != null) {
            product = this.findProduct(code);
        }
        if (product == null) {
            isNew = true;
            product = new Product();
            product.setCreateDate(new Date());
        }
        product.setCode(code);
        product.setName(productForm.getName());
        product.setPrice(productForm.getPrice());
        product.setDiscountPercent(productForm.getDiscountPercent());
 
        if (productForm.getFileData() != null) {
            try {
                byte[] image = productForm.getFileData().getBytes();
                if (image != null && image.length > 0) {
                    product.setImage(image);
                }
            } catch (java.io.IOException e) {
                // Ignore or log
            }
        }
        
        if (isNew) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = (auth != null) ? auth.getName() : "manager1";
            product.setOwnerUsername(currentUsername);
            
            // Populate some random/default Shopee values for mock showcase
            // product.setDiscountPercent(randomDiscount);
            
            product.setSalesCount((int)(Math.random() * 210000));
            
            String[] locations = {"Thành phố Hồ Chí Minh", "Thành phố Hà Nội", "Tinh An Giang", "Tinh Cà Mau"};
            String randomLoc = locations[(int)(Math.random() * locations.length)];
            product.setLocation(randomLoc);
            
            String[] brands = {"GEN ALPHA", "COOLMATE", "Originals"};
            String randomBrand = brands[(int)(Math.random() * brands.length)];
            product.setBrand(randomBrand);
            
            product.setRating((int)(Math.random() * 3) + 3); // 3 to 5 stars
            product.setMall(Math.random() > 0.85);
            product.setFavored(Math.random() > 0.6);
            
            session.persist(product);
        }
        session.flush();
    }
 
    public PaginationResult<ProductInfo> queryProducts(int page, int maxResult, int maxNavigationPage,
            String likeName, String ownerUsername, String sort, Double minPrice, Double maxPrice, 
            String location, String brand, Boolean isMall, Boolean isFavored, Integer rating) {
        
        String sql = "Select new " + ProductInfo.class.getName() //
                + "(p.code, p.name, p.price, p.discountPercent, p.salesCount, p.location, p.brand, p.rating, p.isMall, p.isFavored) "
                + " from " + Product.class.getName() + " p Where 1=1 ";
        
        boolean hasLikeName = likeName != null && likeName.length() > 0;
        boolean hasOwner = ownerUsername != null && ownerUsername.length() > 0;
        
        if (hasLikeName) {
            sql += " and lower(p.name) like :likeName ";
        }
        if (hasOwner) {
            sql += " and p.ownerUsername = :ownerUsername ";
        }
        if (minPrice != null) {
            sql += " and p.price >= :minPrice ";
        }
        if (maxPrice != null) {
            sql += " and p.price <= :maxPrice ";
        }
        if (location != null && location.length() > 0) {
            sql += " and p.location = :location ";
        }
        if (brand != null && brand.length() > 0) {
            sql += " and p.brand = :brand ";
        }
        if (isMall != null) {
            sql += " and p.isMall = :isMall ";
        }
        if (isFavored != null) {
            sql += " and p.isFavored = :isFavored ";
        }
        if (rating != null) {
            sql += " and p.rating >= :rating ";
        }
        
        // Sorting
        if ("popular".equals(sort)) {
            sql += " order by p.rating desc, p.createDate desc ";
        } else if ("sales".equals(sort)) {
            sql += " order by p.salesCount desc ";
        } else if ("priceAsc".equals(sort)) {
            sql += " order by p.price/1.0 asc ";
        } else if ("priceDesc".equals(sort)) {
            sql += " order by p.price/1.0 desc ";
        } else {
            // newest
            sql += " order by p.createDate desc ";
        }
 
        Session session = this.sessionFactory.getCurrentSession();
        Query<ProductInfo> query = session.createQuery(sql, ProductInfo.class);
  
        if (hasLikeName) {
            query.setParameter("likeName", "%" + likeName.toLowerCase() + "%");
        }
        if (hasOwner) {
            query.setParameter("ownerUsername", ownerUsername);
        }
        if (minPrice != null) {
            query.setParameter("minPrice", minPrice);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        if (location != null && location.length() > 0) {
            query.setParameter("location", location);
        }
        if (brand != null && brand.length() > 0) {
            query.setParameter("brand", brand);
        }
        if (isMall != null) {
            query.setParameter("isMall", isMall);
        }
        if (isFavored != null) {
            query.setParameter("isFavored", isFavored);
        }
        if (rating != null) {
            query.setParameter("rating", rating);
        }
        
        return new PaginationResult<ProductInfo>(query, page, maxResult, maxNavigationPage);
    }
 
    public PaginationResult<ProductInfo> queryProducts(int page, int maxResult, int maxNavigationPage,
            String likeName, String ownerUsername) {
        return queryProducts(page, maxResult, maxNavigationPage, likeName, ownerUsername, null, null, null, null, null, null, null, null);
    }

    public PaginationResult<ProductInfo> queryProducts(int page, int maxResult, int maxNavigationPage, String likeName) {
        return queryProducts(page, maxResult, maxNavigationPage, likeName, null);
    }
 
    public PaginationResult<ProductInfo> queryProducts(int page, int maxResult, int maxNavigationPage) {
        return queryProducts(page, maxResult, maxNavigationPage, null, null);
    }
 
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void deleteProduct(String code) {
        Session session = this.sessionFactory.getCurrentSession();
        Product product = this.findProduct(code);
        if (product != null) {
            // Delete associated OrderDetail records to satisfy FK constraints
            String deleteDetailsSql = "Delete from com.example.demo.entity.OrderDetail d Where d.product.code = :code";
            Query<?> query = session.createQuery(deleteDetailsSql);
            query.setParameter("code", code);
            query.executeUpdate();
 
            // Delete product itself
            session.delete(product);
            session.flush();
        }
    }
 
}
