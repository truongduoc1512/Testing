package com.example.demo.dao;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

import javax.persistence.LockModeType;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
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
        Session session = this.sessionFactory.getCurrentSession();
        return session.find(Product.class, code);
    }

    public Product findActiveProduct(String code) {
        Product product = findProduct(code);
        return product != null && "ACTIVE".equalsIgnoreCase(product.getStatus()) ? product : null;
    }

    public Product findProductForUpdate(String code) {
        Session session = this.sessionFactory.getCurrentSession();
        return session.find(Product.class, code, LockModeType.PESSIMISTIC_WRITE);
    }

    public ProductInfo findProductInfo(String code) {
        Product product = this.findActiveProduct(code);
        if (product == null) {
            return null;
        }
        return new ProductInfo(product);
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(ProductForm productForm) {
        validateProductForm(productForm);
        Session session = this.sessionFactory.getCurrentSession();
        String code = productForm.getCode().trim();
        productForm.setCode(code);
        productForm.setName(productForm.getName().trim());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new AccessDeniedException("Bạn cần đăng nhập để lưu sản phẩm.");
        }
        String currentUsername = auth.getName();

        Product product = null;

        boolean isNew = false;
        if (code != null) {
            product = this.findProductForUpdate(code);
        }
        if (product == null) {
            isNew = true;
            product = new Product();
            product.setCreateDate(new Date());
        } else if (!currentUsername.equals(product.getOwnerUsername())) {
            throw new AccessDeniedException("Bạn không có quyền cập nhật sản phẩm này.");
        }
        product.setCode(code);
        product.setName(productForm.getName());
        product.setPrice(productForm.getPrice());
        product.setDiscountPercent(productForm.getDiscountPercent());
        product.setStockQuantity(productForm.getStockQuantity());
        product.setStatus("ACTIVE");

        if (productForm.getFileData() != null) {
            try {
                byte[] image = productForm.getFileData().getBytes();
                if (image != null && image.length > 0) {
                    product.setImage(image);
                }
            } catch (java.io.IOException e) {
                throw new IllegalArgumentException("Không thể đọc file ảnh sản phẩm.", e);
            }
        }

        if (isNew) {
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

    private void validateProductForm(ProductForm productForm) {
        if (productForm == null || productForm.getCode() == null || productForm.getCode().trim().isEmpty()
                || productForm.getCode().trim().length() > 20) {
            throw new IllegalArgumentException("Mã sản phẩm không hợp lệ.");
        }
        if (productForm.getName() == null || productForm.getName().trim().isEmpty()
                || productForm.getName().trim().length() > 255) {
            throw new IllegalArgumentException("Tên sản phẩm không hợp lệ.");
        }
        if (!Double.isFinite(productForm.getPrice()) || productForm.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0.");
        }
        if (productForm.getDiscountPercent() < 0 || productForm.getDiscountPercent() > 100) {
            throw new IllegalArgumentException("Phần trăm giảm giá phải trong khoảng 0 đến 100.");
        }
        if (productForm.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho không được âm.");
        }
    }

    public PaginationResult<ProductInfo> queryProducts(int page, int maxResult, int maxNavigationPage,
            String likeName, String ownerUsername, String sort, Double minPrice, Double maxPrice,
            String location, String brand, Boolean isMall, Boolean isFavored, Integer rating, String category) {

        boolean hasLikeName = likeName != null && likeName.length() > 0;
        boolean hasOwner = ownerUsername != null && ownerUsername.length() > 0;
        boolean hasCategory = category != null && category.trim().length() > 0;

        StringBuilder sql = new StringBuilder("Select new ")
                .append(ProductInfo.class.getName())
                .append("(p.code, p.name, p.price, p.discountPercent, p.salesCount, p.location, ")
                .append("p.brand, p.rating, p.isMall, p.isFavored, p.reviewCount, p.stockQuantity, ")
                .append("p.category, p.status) ")
                .append(" from ").append(Product.class.getName()).append(" p Where 1=1 ");

        if (!hasOwner) {
            sql.append(" and p.status = :activeStatus ");
        }

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
            sql.append(" and (p.price * (100 - p.discountPercent) / 100.0) >= :minPrice ");
        }
        if (maxPrice != null) {
            sql.append(" and (p.price * (100 - p.discountPercent) / 100.0) <= :maxPrice ");
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
            sql.append(" order by (p.price * (100 - p.discountPercent) / 100.0) asc ");
        } else if ("priceDesc".equals(sort)) {
            sql.append(" order by (p.price * (100 - p.discountPercent) / 100.0) desc ");
        } else {
            // Mới nhất.
            sql.append(" order by p.createDate desc ");
        }

        Session session = this.sessionFactory.getCurrentSession();
        Query<ProductInfo> query = session.createQuery(sql.toString(), ProductInfo.class);

        if (!hasOwner) {
            query.setParameter("activeStatus", "ACTIVE");
        }

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
            query.setParameter("rating", rating.doubleValue());
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

    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(String code) {
        Session session = this.sessionFactory.getCurrentSession();
        Product product = this.findProductForUpdate(code);
        if (product != null) {
            product.setStatus("INACTIVE");
            session.update(product);
            session.flush();
        }
    }

}
