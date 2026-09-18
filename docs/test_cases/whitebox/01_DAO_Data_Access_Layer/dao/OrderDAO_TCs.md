# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `OrderDAO`
## Tầng: Data Access Object (DAO) | Phân hệ: Quản lý Đơn hàng & Thanh toán
* **Tổng số Test Case:** **43 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `OrderDAO.java` và các hàm kiểm thử trong `OrderDAOTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `OrderDAO.java`

Lớp `OrderDAO` quản lý 13 hàm nghiệp vụ chính được bao phủ bởi 43 test case trong `OrderDAOTest.java`:

| STT | Tên hàm trong `OrderDAO.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `saveOrder(CartInfo, Authentication)` | Đặt hàng, trừ tồn kho, tăng lượt bán, khóa SP chống Deadlock | **18** |
| 2 | `listOrderInfo(page, maxResult, maxNav, auth)` | Lấy danh sách đơn hàng có phân trang theo scope quyền | **2** |
| 3 | `findOrder(String orderId)` | Tìm kiếm entity đơn hàng theo ID | **1** |
| 4 | `findOrderForUpdate(String orderId)` | Tìm đơn hàng kèm khóa bi quan (`PESSIMISTIC_WRITE`) | **1** |
| 5 | `canAccessOrder(String orderId, Authentication)`| Kiểm tra quyền xem chi tiết đơn hàng theo vai trò | **3** |
| 6 | `canManageOrder(String orderId, Authentication)`| Kiểm tra quyền điều phối đơn hàng của Seller | **3** |
| 7 | `isOrderCustomer(String orderId, String user)` | Kiểm tra quyền sở hữu đơn hàng của Customer | **2** |
| 8 | `getOrderInfo(String orderId)` | Lấy DTO thông tin đơn hàng `OrderInfo` | **2** |
| 9 | `listOrderDetailInfos(String orderId)` | Lấy toàn bộ danh sách dòng sản phẩm trong đơn | **1** |
| 10 | `listOrderDetailInfosForPrincipal(orderId, auth)`| Lấy danh sách dòng sản phẩm lọc theo Seller/Customer | **3** |
| 11 | `updateOrderStatus(String orderId, String status)`| Cập nhật trạng thái đơn hàng theo FSM hợp lệ | **3** |
| 12 | `getTotalOrdersCount(Authentication auth)` | Thống kê tổng số lượng đơn hàng theo scope quyền | **2** |
| 13 | `getTotalRevenue(Authentication auth)` | Thống kê tổng doanh thu bán hàng theo scope quyền | **2** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 43 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_ORD_01` | `saveOrder` | `saveOrder_rejectsNullCart` | `cartInfo = null` | Nhánh `if (cartInfo == null)` = True | Ném `IllegalArgumentException` |
| `TC_ORD_02` | `saveOrder` | `saveOrder_rejectsEmptyCart` | `cartLines.isEmpty() = true` | Nhánh `if (cartInfo.isEmpty())` = True | Ném `IllegalArgumentException` |
| `TC_ORD_03` | `saveOrder` | `saveOrder_rejectsInvalidCustomer` | `customerInfo.isValid() = false` | Nhánh `if (!customerInfo.isValid())` = True | Ném `IllegalArgumentException` |
| `TC_ORD_04` | `saveOrder` | `saveOrder_rejectsMissingLineStructure` | `cartLine.getProductInfo() = null` | Nhánh `if (line.getProductInfo() == null)` = True | Ném `IllegalArgumentException` |
| `TC_ORD_05` | `saveOrder` | `saveOrder_rejectsQuantityBelowOne` | `quantity = 0` (Dưới biên dưới) | Nhánh `if (quantity <= 0)` = True | Ném `IllegalArgumentException` |
| `TC_ORD_06` | `saveOrder` | `saveOrder_rejectsMissingOrInactiveProduct` | Mock `product.isActive() = false` | Nhánh `if (!product.isActive())` = True | Báo lỗi SP ngừng kinh doanh |
| `TC_ORD_07` | `saveOrder` | `saveOrder_enforcesStockBoundary` | `cartQuantity = 11`, `stock = 10` | Nhánh `if (quantity > product.getStock())` = True | Báo lỗi vượt quá tồn kho |
| `TC_ORD_08` | `saveOrder` | `saveOrder_refreshesCartLineFromServerProduct` | DB: `price = 150k`, Cart: `100k` | Dòng lệnh cập nhật lại giá từ entity CSDL | Giá đơn chốt theo CSDL (150k) |
| `TC_ORD_09` | `saveOrder` | `saveOrder_deductsInventoryAndIncreasesSalesCount` | `stock = 10`, `qty = 2`, `sales = 5` | Lệnh `stock = stock - qty; sales = sales + qty;` | Tồn kho còn 8, Lượt bán tăng 7 |
| `TC_ORD_10` | `saveOrder` | `saveOrder_createsPendingGuestOrderWithNextNumber` | `auth = null`, `maxOrderNum = 100` | Nhánh tạo đơn khách vãng lai (Guest) | `status = "PENDING"`, `num = 101` |
| `TC_ORD_11` | `saveOrder` | `saveOrder_recordsAuthenticatedCustomer` | `auth.getName() = "alice"` | Lệnh gán `order.setCustomerUsername("alice")` | Đơn gán đúng username `"alice"` |
| `TC_ORD_12` | `saveOrder` | `saveOrder_treatsUnauthenticatedAuthenticationAsGuest` | `auth.isAuthenticated() = false` | Nhánh `if (!auth.isAuthenticated())` = True | Xử lý như khách vãng lai (Guest) |
| `TC_ORD_13` | `saveOrder` | `saveOrder_treatsAnonymousAuthenticationAsGuest` | `auth.getPrincipal() = "anonymousUser"` | Nhánh `if ("anonymousUser".equals(principal))` | Xử lý như khách vãng lai (Guest) |
| `TC_ORD_14` | `saveOrder` | `saveOrder_appliesNormalizedVoucherAndRecordsUsage` | `voucherCode = "  sale10  "` hợp lệ | Chuỗi chuẩn hóa mã & gọi `recordVoucherUsage` | Giảm tiền và lưu log voucher |
| `TC_ORD_15` | `saveOrder` | `saveOrder_treatsBlankVoucherAsAbsent` | `voucherCode = "   "` (Chuỗi trắng) | Nhánh `if (code == null \|\| code.trim().isEmpty())` | Bỏ qua bước áp dụng voucher |
| `TC_ORD_16` | `saveOrder` | `saveOrder_rejectsInvalidVoucherBeforeCreatingOrder` | `voucherCode = "EXPIRED"` | Bắt kết quả voucher `isValid() = false` | Hủy đơn hàng, ném ngoại lệ |
| `TC_ORD_17` | `saveOrder` | `saveOrder_locksLinesInStableProductCodeOrder` | Giỏ hàng chứa mã `["P02", "P01"]` | Sắp xếp mảng mã SP trước khi gọi Lock | Khóa lần lượt: `P01` ➔ `P02` |
| `TC_ORD_18` | `saveOrder` | `saveOrder_treatsNullMaxOrderNumberAsZero` | CSDL chưa có đơn nào (`maxOrderNum == null`) | Toán tử `maxOrder == null ? 0 : ...` | Đơn đầu tiên có số `orderNum = 1` |
| `TC_ORD_19` | `listOrderInfo` | `listOrderInfo_buildsExpectedScope` | `role = "ROLE_USER"`, `page = 1` | Mệnh đề `WHERE customerUsername = :user` | Trả về danh sách đơn của user |
| `TC_ORD_20` | `listOrderInfo` | `listOrderInfo_noPrincipalOverloadBuildsUnscopedQuery` | Không truyền Authentication | HQL query không chứa mệnh đề WHERE scope | Trả về toàn bộ đơn hệ thống |
| `TC_ORD_21` | `findOrder` | `findOrder_delegatesLookup` | `orderId = "ORD_01"` | Lệnh `session.get(Order.class, id)` | Trả về entity `Order` |
| `TC_ORD_22` | `findOrderForUpdate` | `findOrderForUpdate_usesPessimisticLock` | `orderId = "ORD_01"` | Lệnh `session.get(..., LockMode.PESSIMISTIC_WRITE)` | Khóa ghi bản ghi đơn hàng |
| `TC_ORD_23` | `canAccessOrder` | `canAccessOrder_rejectsMissingKeys` | `orderId = null` hoặc `auth = null` | Nhánh kiểm tra null tham số | Trả về `false` |
| `TC_ORD_24` | `canAccessOrder` | `canAccessOrder_usesRoleSpecificOwnershipQuery` | Role Admin vs Role User | Phân nhánh kiểm tra quyền theo vai trò | Admin=`true`, User check owner |
| `TC_ORD_25` | `canAccessOrder` | `canAccessOrder_rejectsUnknownRoleWithoutQuery` | `role = "ROLE_ANONYMOUS"` | Nhánh `default` trong kiểm tra Role | Trả về `false` không query CSDL |
| `TC_ORD_26` | `canManageOrder` | `canManageOrder_rejectsMissingKeys` | Thiếu tham số đầu vào | Nhánh kiểm tra null tham số | Trả về `false` |
| `TC_ORD_27` | `canManageOrder` | `canManageOrder_returnsFalseForOrderWithoutLines` | Đơn không có dòng sản phẩm nào | Nhánh `if (lines.isEmpty())` = True | Trả về `false` |
| `TC_ORD_28` | `canManageOrder` | `canManageOrder_requiresSellerToOwnEveryLine` | Seller chỉ sở hữu 1 trong 2 dòng SP | Điều kiện `allMatch(sellerIsOwner)` = False | Trả về `false` |
| `TC_ORD_29` | `isOrderCustomer` | `isOrderCustomer_rejectsMissingKeys` | `orderId = null` | Nhánh kiểm tra null tham số | Trả về `false` |
| `TC_ORD_30` | `isOrderCustomer` | `isOrderCustomer_mapsCountToBoolean` | Kết quả query `count = 1` | Biểu thức `return count > 0` | Trả về `true` |
| `TC_ORD_31` | `getOrderInfo` | `getOrderInfo_returnsNullWhenOrderMissing` | `orderId = "NOT_EXIST"` | Nhánh `if (order == null)` = True | Trả về `null` |
| `TC_ORD_32` | `getOrderInfo` | `getOrderInfo_mapsOrderFields` | Entity Order hợp lệ | Khối lệnh ánh xạ Entity ➔ DTO | DTO `OrderInfo` đầy đủ trường |
| `TC_ORD_33` | `listOrderDetailInfos` | `listOrderDetailInfos_returnsAllLinesForOrder` | `orderId = "ORD_01"` | Câu HQL query `OrderDetail` theo đơn | Trả về danh sách đầy đủ item |
| `TC_ORD_34` | `listOrderDetailInfosForPrincipal` | `listOrderDetailInfosForPrincipal_filtersSellerWhenNotCustomer` | User đóng vai trò Seller | Lọc `WHERE product.owner = :seller` | Chỉ thấy SP do mình bán |
| `TC_ORD_35` | `listOrderDetailInfosForPrincipal` | `listOrderDetailInfosForPrincipal_returnsAllLinesForCustomerSeller` | Vừa là người mua vừa là người bán | Bỏ qua điều kiện lọc Seller | Thấy trọn vẹn chi tiết đơn |
| `TC_ORD_36` | `listOrderDetailInfosForPrincipal` | `listOrderDetailInfosForPrincipal_nonSellerRoleDoesNotCheckCustomer` | Role Quản trị viên (Admin) | Không kích hoạt lọc theo Customer | Thấy trọn vẹn chi tiết đơn |
| `TC_ORD_37` | `updateOrderStatus` | `updateOrderStatus_doesNothingWhenOrderMissing` | `orderId = "NOT_EXIST"` | Nhánh `if (order == null)` = True | Bỏ qua, không ném lỗi |
| `TC_ORD_38` | `updateOrderStatus` | `updateOrderStatus_normalizesValidTransition` | `PENDING` ➔ `CONFIRMED` | Nhánh chuyển trạng thái hợp lệ | Trạng thái chuyển thành công |
| `TC_ORD_39` | `updateOrderStatus` | `updateOrderStatus_rejectsInvalidTransition` | `CANCELLED` ➔ `DELIVERED` | Nhánh chuyển trạng thái bất hợp pháp | Ném ngoại lệ chuyển sai quy trình |
| `TC_ORD_40` | `getTotalOrdersCount` | `getTotalOrdersCount_scopesAndMapsNullableAggregate` | Scope User có 0 đơn | Toán tử gán `res == null ? 0 : ...` | Trả về 0 an toàn |
| `TC_ORD_41` | `getTotalRevenue` | `getTotalRevenue_scopesAndMapsNullableAggregate` | Doanh thu aggregate = `null` | Toán tử gán `res == null ? 0.0 : ...` | Trả về `0.0` an toàn |
| `TC_ORD_42` | `getTotalOrdersCount` | `getTotalOrdersCount_withoutScopeReturnsGlobalAggregate` | Không truyền scope | HQL đếm toàn sàn | Trả về tổng đơn toàn sàn |
| `TC_ORD_43` | `getTotalRevenue` | `getTotalRevenue_withoutScopeReturnsGlobalAggregate` | Không truyền scope | HQL tính tổng doanh thu toàn sàn | Trả về tổng doanh thu toàn sàn |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.dao.OrderDAO`
* **Statement Coverage (Instructions):** **674 / 682 (98.8%)**
* **Branch Coverage (Branches):** **88 / 88 (100.0%)**
* **Đánh giá:** Đạt độ phủ nhánh tuyệt đối 100%. Phủ kín toàn bộ các luồng kiểm tra giỏ hàng, xác thực khách hàng, trừ kho, voucher, phân quyền và máy trạng thái đơn hàng.
