# ĐẶC TẢ KIỂM THỬ HỘP TRẮNG: `CartController`
## Tầng: Web MVC Controller | Phân hệ: Quản lý Giỏ hàng & Thanh toán Session
* **Tổng số Test Case:** **37 Test Cases**
* **Mục đích tài liệu:** Bảng ánh xạ chi tiết giữa các hàm nghiệp vụ trong `CartController.java` và các hàm kiểm thử trong `CartControllerCoverageTest.java` & `ShoppingCartFinalizeTemplateTest.java`, phục vụ tra cứu nhanh giá trị test đầu vào, nhánh logic mục tiêu và kết quả mong đợi.

---

## 📌 PHẦN 1: BẢNG TỔNG HỢP CÁC HÀM TRONG `CartController.java`

Lớp `CartController` quản lý 10 hàm nghiệp vụ trên giao diện Web Thymeleaf và AJAX giỏ hàng:

| STT | Tên hàm trong `CartController.java` | Mục đích nghiệp vụ | Số Test Case đối ứng |
| :---: | :--- | :--- | :---: |
| 1 | `initBinder(...)` | Đăng ký Custom Validator chỉ dành cho CustomerForm | **1** |
| 2 | `buyProductHandler(...)` | Mua nhanh sản phẩm (thêm vào giỏ và redirect sang `/shoppingCart`) | **4** |
| 3 | `addToCartHandler(...)` | Thêm sản phẩm vào giỏ hàng và giữ nguyên trang kèm thông báo | **3** |
| 4 | `removeProductHandler(...)` | Xóa một dòng sản phẩm khỏi giỏ hàng Session | **2** |
| 5 | `updateQuantity(...)` | Cập nhật số lượng sản phẩm (giới hạn theo tồn kho thực tế) | **3** |
| 6 | `shoppingCartHandler(...)` | Hiển thị trang giỏ hàng Thymeleaf và gợi ý sản phẩm liên quan | **2** |
| 7 | `shoppingCartCustomerForm(...)` | Hiển thị form nhập thông tin giao hàng (chặn khi giỏ rỗng) | **2** |
| 8 | `shoppingCartCustomerSave(...)` | Validate và lưu thông tin khách hàng vào giỏ hàng Session | **2** |
| 9 | `shoppingCartConfirmationReview/Save(...)` | Xác nhận đơn hàng và lưu đơn hàng vào CSDL (chuyển sang lastOrder) | **6** |
| 10 | `shoppingCartFinalize(...)` & AJAX APIs | Trang hoàn tất đơn hàng và 2 API AJAX cập nhật/xóa giỏ hàng | **12** |

---

## 📑 PHẦN 2: BẢNG MA TRẬN ÁNH XẠ CHI TIẾT 37 TEST CASES

| Mã TC | Hàm ở `src/main` | Hàm kiểm thử ở `src/test` | Giá trị Test đầu vào (Inputs / Mock Data) | Nhánh logic / Điều kiện mục tiêu | Kết quả mong đợi (Assertion) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `TC_CART_01` | `initBinder` | `initBinder_setsValidatorOnlyForCustomerForm` | Target = `CustomerForm.class` | Nhánh `validator.supports(target)` = True | Gán validator thành công |
| `TC_CART_02` | `buyProductHandler` | `buyProduct_rejectsMissingCode` | `code = null` hoặc `""` | Nhánh `if (code == null \|\| code.isEmpty())` | Redirect về `/productList` |
| `TC_CART_03` | `buyProductHandler` | `buyProduct_reportsUnknownProduct` | Mã SP không có trong CSDL | Nhánh `if (productInfo == null)` | Redirect về `/productList` kèm flash attr |
| `TC_CART_04` | `buyProductHandler` | `buyProduct_redirectsSoldOutProductToProductList` | Sản phẩm hết hàng (`stock <= 0`) | Nhánh `if (productInfo.getStockQuantity() <= 0)` | Chặn mua, redirect về `/productList` |
| `TC_CART_05` | `buyProductHandler` | `buyProduct_addsAvailableProductToCart` | Sản phẩm còn hàng (`stock > 0`) | Thêm SP vào Session Cart | Redirect sang `/shoppingCart` |
| `TC_CART_06` | `addToCartHandler` | `addToCart_rejectsMissingCode` | `code = null` | Nhánh `if (code == null)` = True | Redirect về `/productList` |
| `TC_CART_07` | `addToCartHandler` | `addToCart_rejectsSoldOutProduct` | Sản phẩm `stock = 0` | Nhánh kiểm tra hết hàng | Báo lỗi hết hàng |
| `TC_CART_08` | `addToCartHandler` | `addToCart_addsAvailableProductAndSuccessMessage`| SP hợp lệ | Thêm vào giỏ thành công | Giữ nguyên trang và hiển thị message |
| `TC_CART_09` | `removeProductHandler` | `removeProduct_rejectsMissingCode` | `code = null` khi xóa | Nhánh kiểm tra mã SP null | Redirect về `/shoppingCart` |
| `TC_CART_10` | `removeProductHandler` | `removeProduct_removesExistingCartLine` | Mã SP có trong giỏ | Xóa dòng sản phẩm khỏi giỏ Session | Giỏ hàng cập nhật số lượng và tổng tiền |
| `TC_CART_11` | `updateQuantity` | `updateQuantity_rejectsMissingProduct` | Mã SP không có trong giỏ | Nhánh không tìm thấy SP trong giỏ | Bỏ qua, redirect về `/shoppingCart` |
| `TC_CART_12` | `updateQuantity` | `updateQuantity_rejectsSoldOutProduct` | SP trong kho bị cập nhật về `stock = 0` | Nhánh kho hết hàng | Báo lỗi SP tạm hết |
| `TC_CART_13` | `updateQuantity` | `updateQuantity_capsQuantityAtAvailableStock` | Nhập số lượng 15, kho chỉ còn 10 | Nhánh `requestedQty > availableStock` | Tự động hạ số lượng xuống đúng tồn kho (10) |
| `TC_CART_14` | `shoppingCartHandler` | `shoppingCartView_omitsRecommendationsWhenQueryReturnsNull`| Query gợi ý trả về `null` | Nhánh kiểm tra danh sách recommendation null | Trang giỏ hàng không lỗi, bỏ qua gợi ý |
| `TC_CART_15` | `shoppingCartHandler` | `shoppingCartView_addsReturnedRecommendations` | Query gợi ý có dữ liệu | Thêm danh sách vào model attribute | View có dữ liệu gợi ý sản phẩm |
| `TC_CART_16` | `shoppingCartCustomerForm`| `customerForm_redirectsEmptyCart` | Giỏ hàng rỗng (`cart.isEmpty()`) | Nhánh `if (cartInfo.isEmpty())` = True | Chặn checkout, redirect về `/shoppingCart` |
| `TC_CART_17` | `shoppingCartCustomerForm`| `customerForm_mapsExistingCustomer` | Giỏ hàng đã có thông tin khách | Nhánh nạp form từ CustomerInfo có sẵn | Form hiển thị đúng dữ liệu đã lưu |
| `TC_CART_18` | `shoppingCartCustomerSave`| `customerSave_returnsFormForBindingErrors` | Form nhập sai (email sai định dạng) | Nhánh `if (result.hasErrors())` = True | Trả về view `shoppingCartCustomer` |
| `TC_CART_19` | `shoppingCartCustomerSave`| `customerSave_storesValidatedCustomerInCart` | Form hợp lệ | Lưu `CustomerInfo` vào Session Cart | Redirect sang `/shoppingCartConfirmation` |
| `TC_CART_20` | `shoppingCartConfirmationReview`| `confirmationReview_redirectsEmptyCart` | Giỏ rỗng lúc xem xác nhận | Nhánh kiểm tra giỏ rỗng | Redirect về `/shoppingCart` |
| `TC_CART_21` | `shoppingCartConfirmationReview`| `confirmationReview_redirectsCartWithInvalidCustomer`| Khách hàng chưa nhập đủ info | Nhánh `if (!customerInfo.isValid())` | Redirect về `/shoppingCartCustomer` |
| `TC_CART_22` | `shoppingCartConfirmationReview`| `confirmationReview_showsValidCart` | Giỏ và khách hợp lệ | Hiển thị thông tin chốt đơn | Render view `shoppingCartConfirmation` |
| `TC_CART_23` | `shoppingCartConfirmationSave`| `confirmationSave_redirectsEmptyCart` | Giỏ rỗng lúc bấm Chốt đơn | Nhánh kiểm tra giỏ rỗng | Redirect về `/shoppingCart` |
| `TC_CART_24` | `shoppingCartConfirmationSave`| `confirmationSave_redirectsCartWithInvalidCustomer`| Khách thiếu thông tin lúc Chốt đơn | Nhánh kiểm tra thông tin khách | Redirect về `/shoppingCartCustomer` |
| `TC_CART_25` | `confirmationSave` | `confirmationSave_preservesCartWhenOrderSaveFails` | Lỗi DB khi gọi `orderDAO.saveOrder` | Nhánh bắt ngoại lệ khi lưu đơn | Giữ nguyên giỏ hàng, báo lỗi trên view |
| `TC_CART_26` | `confirmationSave` | `confirmationSave_movesSuccessfulCartToLastOrder` | Đặt hàng thành công | Lưu giỏ vào `lastOrderedCart`, reset giỏ hiện tại | Redirect sang `/shoppingCartFinalize` |
| `TC_CART_27` | `shoppingCartFinalize` | `finalize_redirectsWithoutLastOrder` | Chưa đặt đơn nào (`lastOrder == null`) | Nhánh `if (lastOrderedCart == null)` | Chặn xem, redirect về `/shoppingCart` |
| `TC_CART_28` | `shoppingCartFinalize` | `finalize_showsStoredLastOrder` | Có `lastOrderedCart` | Đưa đơn hàng cuối vào Model | Render view `shoppingCartFinalize` |
| `TC_CART_29` | `ajaxQuantity` | `ajaxQuantity_rejectsMissingProduct` | Mã SP AJAX không có trong CSDL | Nhánh `if (productInfo == null)` | Trả về JSON lỗi 400 |
| `TC_CART_30` | `ajaxQuantity` | `ajaxQuantity_rejectsNonPositiveQuantity` | Số lượng gửi lên `<= 0` | Nhánh kiểm tra số lượng không hợp lệ | Trả về JSON báo lỗi số lượng |
| `TC_CART_31` | `ajaxQuantity` | `ajaxQuantity_rejectsSoldOutProduct` | Sản phẩm hết hàng | Nhánh `stock <= 0` | Trả về JSON báo SP hết hàng |
| `TC_CART_32` | `ajaxQuantity` | `ajaxQuantity_capsRequestedQuantityAtAvailableStock` | Yêu cầu số lượng vượt tồn kho | Nhánh tự động hạ số lượng theo tồn kho | Cập nhật số lượng bằng max tồn kho |
| `TC_CART_33` | `ajaxQuantity` | `ajaxQuantity_updatesExistingLineWithoutCap` | Cập nhật số lượng hợp lệ | Cập nhật số lượng trong giỏ | Trả về JSON tổng tiền mới |
| `TC_CART_34` | `ajaxQuantity` | `ajaxQuantity_returnsZeroLineAmountWhenProductIsAbsentFromCart` | SP không có trong giỏ hàng | Nhánh tính tiền dòng sản phẩm null | Trả về số tiền dòng = 0.0 |
| `TC_CART_35` | `ajaxRemove` | `ajaxRemove_reportsMissingProduct` | Xóa AJAX mã không tồn tại | Nhánh `if (productInfo == null)` | Báo lỗi mã SP không hợp lệ |
| `TC_CART_36` | `ajaxRemove` | `ajaxRemove_removesProductAndReturnsUpdatedTotals` | Xóa AJAX SP hợp lệ | Xóa SP khỏi giỏ Session | Trả về JSON tổng tiền giỏ hàng cập nhật |
| `TC_CART_37` | `shoppingCartFinalize` | `finalizeView_rendersTemplateCorrectly` | Gọi template hoàn tất đơn hàng | Kiểm tra Thymeleaf template tồn tại | Render đúng HTML template |

---

## 📊 PHẦN 3: KẾT QUẢ ĐO LƯỜNG ĐỘ BAO PHỦ (JACOCO METRICS)

* **Lớp kiểm thử:** `com.example.demo.controller.CartController`
* **Statement Coverage (Instructions):** **100.0%**
* **Branch Coverage (Branches):** **100.0%**
* **Đánh giá:** Đạt độ phủ tuyệt đối 100% toàn bộ câu lệnh và nhánh logic của luồng giỏ hàng, checkout và AJAX.
