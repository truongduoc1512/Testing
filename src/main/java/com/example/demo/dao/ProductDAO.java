package com.example.demo.dao;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

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
        return new ProductInfo(product);
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
        product.setStockQuantity(productForm.getStockQuantity());

        if (productForm.getFileData() != null) {
            try {
                byte[] image = productForm.getFileData().getBytes();
                if (image != null && image.length > 0) {
                    product.setImage(image);
                }
            } catch (java.io.IOException e) {
                // Bỏ qua lỗi đọc ảnh để giữ nguyên dữ liệu ảnh hiện có.
            }
        }

        if (isNew) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = (auth != null) ? auth.getName() : "manager1";
            product.setOwnerUsername(currentUsername);

            // Tạo dữ liệu Shopee mặc định ngẫu nhiên phục vụ phần minh họa.
            // product.setDiscountPercent(randomDiscount);

            ThreadLocalRandom random = ThreadLocalRandom.current();
            product.setSalesCount(random.nextInt(210000));

            String[] locations = {"Thành phố Hồ Chí Minh", "Thành phố Hà Nội", "Tinh An Giang", "Tinh Cà Mau"};
            String randomLoc = locations[random.nextInt(locations.length)];
            product.setLocation(randomLoc);

            String[] brands = {"GEN ALPHA", "COOLMATE", "Originals"};
            String randomBrand = brands[random.nextInt(brands.length)];
            product.setBrand(randomBrand);

            product.setRating(random.nextInt(3) + 3); // 3 to 5 stars
            product.setMall(random.nextDouble() > 0.85);
            product.setFavored(random.nextDouble() > 0.6);

            session.persist(product);
        }
        session.flush();
    }

    public PaginationResult<ProductInfo> queryProducts(int page, int maxResult, int maxNavigationPage,
            String likeName, String ownerUsername, String sort, Double minPrice, Double maxPrice,
            String location, String brand, Boolean isMall, Boolean isFavored, Integer rating, String category) {

        StringBuilder sql = new StringBuilder("Select new ")
                .append(ProductInfo.class.getName())
                .append("(p.code, p.name, p.price, p.discountPercent, p.salesCount, p.location, ")
                .append("p.brand, p.rating, p.isMall, p.isFavored, p.reviewCount, p.stockQuantity) ")
                .append(" from ").append(Product.class.getName()).append(" p Where 1=1 ");

        boolean hasLikeName = likeName != null && likeName.length() > 0;
        boolean hasOwner = ownerUsername != null && ownerUsername.length() > 0;
        boolean hasCategory = category != null && category.trim().length() > 0;

        if (hasLikeName) {
            sql.append(" and lower(p.name) like :likeName ");
        }
        if (hasOwner) {
            sql.append(" and p.ownerUsername = :ownerUsername ");
        }
        if (hasCategory) {
            sql.append(" and (lower(p.category) like :category or lower(p.name) like :category) ");
        }
        if (minPrice != null) {
            sql.append(" and p.price >= :minPrice ");
        }
        if (maxPrice != null) {
            sql.append(" and p.price <= :maxPrice ");
        }

        // Hỗ trợ nhiều địa điểm với cách khớp linh hoạt.
        boolean hasLocation = location != null && location.trim().length() > 0;
        java.util.List<String> locList = null;
        if (hasLocation) {
            String[] locArr = location.split(",");
            locList = java.util.Arrays.stream(locArr).map(String::trim).filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toList());
            if (!locList.isEmpty()) {
                sql.append(" and (");
                for (int i = 0; i < locList.size(); i++) {
                    if (i > 0) sql.append(" or ");
                    sql.append(" lower(p.location) like :loc_").append(i).append(" ");
                }
                sql.append(") ");
            }
        }

        // Hỗ trợ lọc theo nhiều thương hiệu.
        boolean hasBrand = brand != null && brand.trim().length() > 0;
        java.util.List<String> brandList = null;
        if (hasBrand) {
            String[] brandArr = brand.split(",");
            brandList = java.util.Arrays.stream(brandArr).map(String::trim).filter(s -> !s.isEmpty()).collect(java.util.stream.Collectors.toList());
            if (!brandList.isEmpty()) {
                if (brandList.size() == 1) {
                    sql.append(" and p.brand = :brand ");
                } else {
                    sql.append(" and p.brand in (:brandList) ");
                }
            }
        }

        if (isMall != null) {
            sql.append(" and p.isMall = :isMall ");
        }
        if (isFavored != null) {
            sql.append(" and p.isFavored = :isFavored ");
        }
        if (rating != null) {
            sql.append(" and p.rating >= :rating ");
        }

        // Sắp xếp kết quả.
        if ("popular".equals(sort)) {
            sql.append(" order by p.rating desc, p.createDate desc ");
        } else if ("sales".equals(sort)) {
            sql.append(" order by p.salesCount desc ");
        } else if ("priceAsc".equals(sort)) {
            sql.append(" order by p.price/1.0 asc ");
        } else if ("priceDesc".equals(sort)) {
            sql.append(" order by p.price/1.0 desc ");
        } else {
            // Mới nhất.
            sql.append(" order by p.createDate desc ");
        }

        Session session = this.sessionFactory.getCurrentSession();
        Query<ProductInfo> query = session.createQuery(sql.toString(), ProductInfo.class);

        if (hasLikeName) {
            query.setParameter("likeName", "%" + likeName.toLowerCase() + "%");
        }
        if (hasOwner) {
            query.setParameter("ownerUsername", ownerUsername);
        }
        if (hasCategory) {
            query.setParameter("category", "%" + category.trim().toLowerCase() + "%");
        }
        if (minPrice != null) {
            query.setParameter("minPrice", minPrice);
        }
        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }
        if (hasLocation && locList != null && !locList.isEmpty()) {
            for (int i = 0; i < locList.size(); i++) {
                String locVal = locList.get(i).toLowerCase();
                if (locVal.contains("hồ chí minh") || locVal.contains("hcm")) locVal = "%hồ chí minh%";
                else if (locVal.contains("hà nội") || locVal.contains("hn")) locVal = "%hà nội%";
                else if (locVal.contains("an giang")) locVal = "%an giang%";
                else if (locVal.contains("cà mau")) locVal = "%cà mau%";
                else locVal = "%" + locVal + "%";
                query.setParameter("loc_" + i, locVal);
            }
        }
        if (hasBrand && brandList != null && !brandList.isEmpty()) {
            if (brandList.size() == 1) {
                query.setParameter("brand", brandList.get(0));
            } else {
                query.setParameterList("brandList", brandList);
            }
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
            String likeName, String ownerUsername, String sort, Double minPrice, Double maxPrice,
            String location, String brand, Boolean isMall, Boolean isFavored, Integer rating) {
        return queryProducts(page, maxResult, maxNavigationPage, likeName, ownerUsername, sort, minPrice, maxPrice, location, brand, isMall, isFavored, rating, null);
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
            // Xóa chi tiết đơn hàng liên quan để thỏa mãn ràng buộc khóa ngoại.
            String deleteDetailsSql = "Delete from com.example.demo.entity.OrderDetail d Where d.product.code = :code";
            Query<?> query = session.createQuery(deleteDetailsSql);
            query.setParameter("code", code);
            query.executeUpdate();

            // Xóa sản phẩm.
            session.delete(product);
            session.flush();
        }
    }

}
