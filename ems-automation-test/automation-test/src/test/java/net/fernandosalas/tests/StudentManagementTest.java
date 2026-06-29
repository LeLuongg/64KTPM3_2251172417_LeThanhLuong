package net.fernandosalas.tests;

import net.fernandosalas.base.BaseTest;
import net.fernandosalas.pages.StudentFormPage;
import net.fernandosalas.pages.StudentListPage;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Lớp kịch bản kiểm thử – Student Management System (Chức năng Thêm Sinh Viên)
 *
 * <p>
 * Kiến trúc phân tầng:
 * 
 * <pre>
 * StudentManagementTest  (lớp này)
 * │  extends
 * BaseTest               → Quản lý WebDriver, Setup, Teardown, Screenshot
 * │  uses
 * StudentListPage        → Page Object: trang danh sách sinh viên
 * StudentFormPage        → Page Object: biểu mẫu thêm sinh viên
 * </pre>
 *
 * <p>
 * Nguyên tắc áp dụng:
 * <ul>
 * <li>[data-driven] : Dữ liệu (33 Test Cases) được bơm từ @DataProvider.</li>
 * <li>[page-object] : Mọi tương tác UI đi qua Page Object.</li>
 * <li>[single-assert] : Tách biệt Assert cho Toast (thành công) và Validation
 * (lỗi).</li>
 * </ul>
 */
public class StudentManagementTest extends BaseTest {

    // ═══════════════════════════════════════════════════════════════
    // HẰNG SỐ CẤU HÌNH & THÔNG BÁO MONG ĐỢI
    // ═══════════════════════════════════════════════════════════════

    private static final String BASE_URL = "http://localhost:3000/students";

    // Các thông báo hệ thống được trích xuất từ Bảng Test Case
    private static final String MSG_ADDED_SUCCESS = "Student added successfully!";
    private static final String MSG_FILL_ALL_FIELDS = "Please fill in all the fields!";
    private static final String MSG_DEPT_ERROR = "An error occurred. Please try again.";
    private static final String MSG_INVALID_EMAIL = "Invalid email format!";
    private static final String MSG_DUPLICATE_EMAIL = "Email already exists.";
    private static final String MSG_INVALID_INPUT = "Invalid input detected";
    private static final String MSG_REQUIRED_FIELD = "This field is required";

    // ═══════════════════════════════════════════════════════════════
    // PHẦN 1 – DATA PROVIDERS (NẠP DỮ LIỆU TỪ BẢNG TEST CASE)
    // ═══════════════════════════════════════════════════════════════

    /**
     * DỮ LIỆU HAPPY PATH (Các luồng hợp lệ)
     * Bao gồm: TC_ADD_01, 02, 03, 18, 19, 22, 30, 31
     * Ghi chú: Ký tự dài 255/2 được đại diện bằng hàm repeat() cho code sạch.
     */
    @DataProvider(name = "addStudentValidData")
    public Object[][] addStudentValidData() {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        return new Object[][] {
                // { Mã TC, firstName, lastName, email, department }
                { "TC_ADD_01", "Nguyen", "Van A", "vana_" + uniqueId + "@gmail.com", "Computer Science" },
                { "TC_ADD_02", "Le", "Thi B", "b.le_" + uniqueId + "@student.hust.edu.vn", "Engineering" },
                { "TC_ADD_03", "Nguyễn", "Văn Bình", "binh_" + uniqueId + "@gmail.com", "Mathematics" },
                { "TC_ADD_18", "An", "Nguyen", "an_" + uniqueId + "@gmail.com", "Computer Science" },
                { "TC_ADD_19", "A".repeat(255), "Nguyen", "a255_" + uniqueId + "@gmail.com", "Computer Science" },
                { "TC_ADD_22", "Nguyen", "An", "an_last_" + uniqueId + "@gmail.com", "Biology" },
                { "TC_ADD_30", "  Nguyen  ", "Van A", "trim1_" + uniqueId + "@gmail.com", "History" },
                { "TC_ADD_31", "Nguyen", "  Van A  ", "trim2_" + uniqueId + "@gmail.com", "History" }
        };
    }

    /**
     * DỮ LIỆU BỎ TRỐNG TRƯỜNG BẮT BUỘC
     * Bao gồm: TC_ADD_04, 05, 06, 07
     */
    @DataProvider(name = "missingRequiredFieldsData")
    public Object[][] missingRequiredFieldsData() {
        return new Object[][] {
                // { Mã TC, firstName, lastName, email, department, expectedAlert }
                { "TC_ADD_04", "", "", "", "", MSG_FILL_ALL_FIELDS },
                { "TC_ADD_05", "", "Van C", "vanc@gmail.com", "Biology", MSG_FILL_ALL_FIELDS },
                { "TC_ADD_06", "Tran", "", "tran@gmail.com", "Biology", MSG_FILL_ALL_FIELDS },
                { "TC_ADD_07", "Pham", "Van D", "", "History", MSG_FILL_ALL_FIELDS }
        };
    }

    /**
     * DỮ LIỆU LỖI ĐỊNH DẠNG EMAIL & SQL INJECTION VÀO EMAIL
     * Bao gồm: TC_ADD_09, 10, 11, 12, 13, 14, 16
     */
    @DataProvider(name = "invalidEmailData")
    public Object[][] invalidEmailData() {
        return new Object[][] {
                // { Mã TC, invalidEmail, expectedValidationError }
                { "TC_ADD_09", "vanagmail.com", MSG_INVALID_EMAIL }, // Thiếu @
                { "TC_ADD_10", "vana@@gmail.com", MSG_INVALID_EMAIL }, // Dư @
                { "TC_ADD_11", "vana@", MSG_INVALID_EMAIL }, // Thiếu domain
                { "TC_ADD_12", "vana@gmailcom", MSG_INVALID_EMAIL }, // Sai domain (không chấm)
                { "TC_ADD_13", "van a@gmail.com", MSG_INVALID_EMAIL }, // Chứa khoảng trắng
                { "TC_ADD_14", "vana@gmail..com", MSG_INVALID_EMAIL }, // 2 dấu chấm
                { "TC_ADD_16", "' OR '1'='1", MSG_INVALID_EMAIL } // SQL Injection vào Email
        };
    }

    /**
     * DỮ LIỆU KIỂM TRA RÀNG BUỘC KÝ TỰ (BVA, Số, Ký tự đặc biệt, XSS, Space)
     * Bao gồm: TC_ADD_17, 20, 21, 23, 24, 25, 26, 27, 28, 29, 32, 33
     */
    @DataProvider(name = "fieldValidationData")
    public Object[][] fieldValidationData() {
        return new Object[][] {
                // { Mã TC, firstName, lastName, expectedValidationError }
                { "TC_ADD_17", "A", "Nguyen", "First Name must be at least 2 characters" },
                { "TC_ADD_20", "A".repeat(256), "Nguyen", "First Name must not exceed 255 characters" },
                { "TC_ADD_21", "Nguyen", "B", "Last Name must be at least 2 characters" },
                { "TC_ADD_23", "Nguyen", "B".repeat(256), "Last Name must not exceed 255 characters" },
                { "TC_ADD_24", "Nguyen@#", "Van A", "First Name cannot contain special characters" },
                { "TC_ADD_25", "Nguyen", "Van A*", "Last Name cannot contain special characters" },
                { "TC_ADD_26", "Van123", "Nguyen", "First Name cannot contain numbers" },
                { "TC_ADD_27", "Van", "Van123", "Last Name cannot contain numbers" },
                { "TC_ADD_28", "     ", "Nguyen", MSG_REQUIRED_FIELD }, // 5 spaces
                { "TC_ADD_29", "Nguyen", "     ", MSG_REQUIRED_FIELD }, // 5 spaces
                { "TC_ADD_32", "<script>alert('xss')</script>", "Nguyen", MSG_INVALID_INPUT }, // XSS First Name
                { "TC_ADD_33", "Nguyen", "<script>alert('xss')</script>", MSG_INVALID_INPUT } // XSS Last Name
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // PHẦN 2 – PHƯƠNG THỨC HỖ TRỢ NỘI BỘ (Private Helpers)
    // ═══════════════════════════════════════════════════════════════

    private StudentListPage navigateToListPage() {
        getDriver().get(BASE_URL);
        return new StudentListPage(getDriver());
    }

    private StudentFormPage navigateToAddFormPage() {
        StudentListPage listPage = navigateToListPage();
        listPage.clickAddStudent();
        return new StudentFormPage(getDriver());
    }

    // ═══════════════════════════════════════════════════════════════
    // PHẦN 3 – THỰC THI KIỂM THỬ (TEST EXECUTION)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Nhóm Happy Path: Chạy qua các TC hợp lệ và kiểm tra Toast thành công.
     */
    @Test(dataProvider = "addStudentValidData", description = "Thực thi các luồng Thêm sinh viên thành công (Happy Path & BVA Valid)")
    public void executeValidAddStudentCases(String tcId, String firstName, String lastName, String email,
            String department) {
        StudentFormPage formPage = navigateToAddFormPage();
        formPage.fillStudentForm(firstName, lastName, email, department);

        StudentListPage listPage = new StudentListPage(getDriver());
        Assert.assertEquals(
                listPage.getToastMessage(),
                MSG_ADDED_SUCCESS,
                String.format("[%s] Toast thông báo không khớp kết quả mong đợi.", tcId));
    }

    /**
     * Nhóm Bỏ trống trường: Kiểm tra hiển thị Form Alert.
     */
    @Test(dataProvider = "missingRequiredFieldsData", description = "TC_ADD_04 ~ TC_ADD_07: Bỏ trống các trường bắt buộc")
    public void executeMissingFieldsCases(String tcId, String firstName, String lastName, String email,
            String department, String expectedAlert) {
        StudentFormPage formPage = navigateToAddFormPage();
        formPage.fillStudentForm(firstName, lastName, email, department);

        Assert.assertEquals(
                formPage.getToastMessage(),
                expectedAlert,
                String.format("[%s] Thông báo form không khớp khi bỏ trống trường.", tcId));
    }

    /**
     * TC_ADD_08: Ngoại lệ riêng khi không chọn Department.
     */
    @Test(description = "TC_ADD_08 - Không chọn Department")
    public void TC_ADD_08_missingDepartment() {
        StudentFormPage formPage = navigateToAddFormPage();
        formPage.fillStudentForm("Hoang", "Thi E", "e@gmail.com", ""); // Truyền rỗng để bỏ qua dropdown

        Assert.assertEquals(
                formPage.getAlertMessage(),
                MSG_DEPT_ERROR,
                "[TC_ADD_08] Thông báo lỗi không chọn phòng ban không chính xác.");
    }

    /**
     * Nhóm Sai định dạng Email: Kiểm tra Inline Validation của trường Email.
     */
    @Test(dataProvider = "invalidEmailData", description = "TC_ADD_09 ~ 14, 16: Nhập email sai định dạng hoặc chứa mã SQL Injection")
    public void executeInvalidEmailCases(String tcId, String invalidEmail, String expectedValidationError) {
        StudentFormPage formPage = navigateToAddFormPage();
        formPage.fillStudentForm("Nguyen", "Van A", invalidEmail, "Computer Science");

        Assert.assertEquals(
                formPage.getValidationErrorMessage(),
                expectedValidationError,
                String.format("[%s] Inline validation cho trường email không chính xác.", tcId));
    }

    /**
     * Nhóm Validation Ký tự (Tên/Họ): Kiểm tra các ràng buộc BVA, XSS, khoảng
     * trắng, số, ký tự đặc biệt.
     */
    @Test(dataProvider = "fieldValidationData", description = "TC_ADD_17 ~ 33 (Trừ Email): Kiểm tra validation độ dài, ký tự đặc biệt và bảo mật XSS")
    public void executeFieldValidationCases(String tcId, String firstName, String lastName,
            String expectedValidationError) {
        StudentFormPage formPage = navigateToAddFormPage();
        formPage.fillStudentForm(firstName, lastName, "valid@gmail.com", "Computer Science");

        Assert.assertEquals(
                formPage.getValidationErrorMessage(),
                expectedValidationError,
                String.format("[%s] Inline validation cho trường First/Last Name không chính xác.", tcId));
    }

    /**
     * TC_ADD_15: Trùng lặp Email. Yêu cầu phải tạo dữ liệu mồi (seed data) trước.
     */
    @Test(description = "TC_ADD_15 - Nhập email đã tồn tại trong hệ thống (Duplicate)")
    public void TC_ADD_15_duplicateEmail() {
        final String duplicateEmail = "vana@gmail.com";

        // Bước 1: Seed dữ liệu (Giả lập trong CSDL đã có)
        StudentFormPage formPage = navigateToAddFormPage();
        formPage.fillStudentForm("Nguyen", "Van A", duplicateEmail, "Computer Science");

        // Bước 2: Thử nhập lại email đó
        StudentListPage listPage = new StudentListPage(getDriver());
        listPage.clickAddStudent();
        formPage = new StudentFormPage(getDriver());
        formPage.fillStudentForm("Le", "Van B", duplicateEmail, "Mathematics");

        // Bước 3: Assert
        Assert.assertEquals(
                formPage.getAlertMessage(),
                MSG_DUPLICATE_EMAIL,
                "[TC_ADD_15] Không hiển thị cảnh báo email trùng lặp.");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PHẦN BỔ SUNG VÀO LỚP StudentManagementTest.java
    // Thêm sau phần khai báo hằng số MSG_ đã có, bổ sung hằng số Update/Delete
    // ═══════════════════════════════════════════════════════════════════════

    // ─── Hằng số bổ sung cho Update & Delete ────────────────────────────
    private static final String MSG_UPDATED_SUCCESS = "Student updated successfully!";
    private static final String MSG_DELETED_SUCCESS = "Student deleted successfully!";

    // Email mồi cố định dùng làm dữ liệu seed cho toàn bộ nhóm TC_UPD
    // Sinh viên này phải tồn tại trong CSDL trước khi chạy các TC_UPD
    private static final String SEED_EMAIL_FOR_UPDATE = "luisfernandosalasg@gmail.com";
    private static final String SEED_FIRST_NAME = "Fernando";
    private static final String SEED_LAST_NAME = "Salas";
    private static final String SEED_DEPARTMENT = "Computer Science";

    // ═══════════════════════════════════════════════════════════════
    // PHẦN 4 – DATA PROVIDERS CHO NHÓM TC_UPD
    // ═══════════════════════════════════════════════════════════════

    /**
     * DỮ LIỆU HAPPY PATH – CẬP NHẬT THÀNH CÔNG
     * Bao gồm: TC_UPD_01, 03, 04, 05, 06, 18, 19, 30, 31
     *
     * Cấu trúc mỗi dòng:
     * { tcId, newFirstName, newLastName, newEmail, newDepartment }
     *
     * Quy ước: null = giữ nguyên giá trị cũ đang có trong form
     * (Xử lý trong navigateToUpdateForm() – không gọi clear()+sendKeys() nếu null)
     */
    @DataProvider(name = "updateStudentValidData")
    public Object[][] updateStudentValidData() {
        String ts = String.valueOf(System.currentTimeMillis());
        return new Object[][] {
                // tcId firstName lastName email department
                { "TC_UPD_01", "Minh", "Tuan", "minhtuan_" + ts + "@gmail.com", "Biology" },
                { "TC_UPD_03", null, null, "update_" + ts + "@gmail.com", null }, // Chỉ đổi Email
                { "TC_UPD_04", null, null, null, "History" }, // Chỉ đổi Department
                { "TC_UPD_05", "Thanh Huong", null, null, null }, // Chỉ đổi First Name
                { "TC_UPD_06", null, "Nguyen", null, null }, // Chỉ đổi Last Name
                { "TC_UPD_18", "An", null, null, null }, // First Name 2 ký tự (BVA)
                { "TC_UPD_19", null, "An", null, null }, // Last Name 2 ký tự (BVA)
                { "TC_UPD_30", "  Minh  ", null, null, null }, // Trim First Name
                { "TC_UPD_31", null, "  Nguyen  ", null, null }, // Trim Last Name
        };
    }

    /**
     * DỮ LIỆU NEGATIVE – BỎ TRỐNG TRƯỜNG BẮT BUỘC KHI CẬP NHẬT
     * Bao gồm: TC_UPD_07, 08, 09
     *
     * Cấu trúc: { tcId, clearFirstName, clearLastName, clearEmail, expectedMsg }
     * true = xóa trắng trường đó trước khi Submit
     */
    @DataProvider(name = "updateMissingRequiredFieldsData")
    public Object[][] updateMissingRequiredFieldsData() {
        return new Object[][] {
                // tcId clrFirst clrLast clrEmail expectedMsg
                { "TC_UPD_07", true, false, false, MSG_FILL_ALL_FIELDS },
                { "TC_UPD_08", false, true, false, MSG_FILL_ALL_FIELDS },
                { "TC_UPD_09", false, false, true, MSG_FILL_ALL_FIELDS },
        };
    }

    /**
     * DỮ LIỆU NEGATIVE – EMAIL SAI ĐỊNH DẠNG KHI CẬP NHẬT
     * Bao gồm: TC_UPD_11, 12, 13, 15
     *
     * Cấu trúc: { tcId, invalidEmail, expectedValidationMsg }
     */
    @DataProvider(name = "updateInvalidEmailData")
    public Object[][] updateInvalidEmailData() {
        return new Object[][] {
                // tcId invalidEmail expectedValidationMsg
                { "TC_UPD_11", "vanagmail.com", MSG_INVALID_EMAIL }, // Thiếu @
                { "TC_UPD_12", "vana@", MSG_INVALID_EMAIL }, // Thiếu domain
                { "TC_UPD_13", "van a@gmail.com", MSG_INVALID_EMAIL }, // Khoảng trắng
                { "TC_UPD_15", "' OR '1'='1", MSG_INVALID_EMAIL }, // SQL Injection
        };
    }

    /**
     * DỮ LIỆU NEGATIVE – VALIDATION KÝ TỰ TRƯỜNG TÊN KHI CẬP NHẬT
     * Bao gồm: TC_UPD_16, 17, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29
     *
     * Cấu trúc: { tcId, newFirstName, newLastName, expectedValidationMsg }
     * null = giữ nguyên trường đó (không thay đổi)
     */
    @DataProvider(name = "updateFieldValidationData")
    public Object[][] updateFieldValidationData() {
        return new Object[][] {
                // tcId newFirstName newLastName expectedMsg
                { "TC_UPD_16", "A", null, "First Name must be at least 2 characters" }, // BVA min-1
                { "TC_UPD_17", null, "A", "Last Name must be at least 2 characters" }, // BVA min-1
                { "TC_UPD_20", "A".repeat(256), null, "First Name must not exceed 255 characters" }, // BVA max+1
                { "TC_UPD_21", null, "A".repeat(256), "Last Name must not exceed 255 characters" }, // BVA max+1
                { "TC_UPD_22", "Nguyen@#", null, "First Name cannot contain special characters" },
                { "TC_UPD_23", null, "Nguyen@#", "Last Name cannot contain special characters" },
                { "TC_UPD_24", "Nguyen123", null, "First Name cannot contain numbers" },
                { "TC_UPD_25", null, "Van123", "Last Name cannot contain numbers" },
                { "TC_UPD_26", "<script>alert('xss')</script>", null, MSG_INVALID_INPUT }, // XSS First Name
                { "TC_UPD_27", null, "<script>alert('xss')</script>", MSG_INVALID_INPUT }, // XSS Last Name
                { "TC_UPD_28", "     ", null, MSG_REQUIRED_FIELD }, // 5 spaces First
                { "TC_UPD_29", null, "     ", MSG_REQUIRED_FIELD }, // 5 spaces Last
        };
    }

    // ═══════════════════════════════════════════════════════════════
    // PHẦN 5 – PHƯƠNG THỨC HỖ TRỢ NỘI BỘ CHO UPDATE (Private Helpers)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Điều hướng đến form Update của sinh viên theo email.
     * Sau khi gọi phương thức này, driver đang ở trang Update Student Form
     * với dữ liệu cũ đã được điền sẵn vào tất cả các trường.
     *
     * @param email Email của sinh viên cần cập nhật (dùng Dynamic XPath)
     * @return StudentFormPage đang hiển thị form Update với dữ liệu cũ
     */
    private StudentFormPage navigateToUpdateForm(String email) {
        StudentListPage listPage = navigateToListPage();
        listPage.clickUpdateByEmail(email);
        return new StudentFormPage(getDriver());
    }

    /**
     * Đảm bảo bảng danh sách sinh viên hoàn toàn trống trước khi chạy Test.
     * Quét toàn bộ email đang hiển thị và xóa sạch từng dòng một.
     */
    private void ensureTableIsEmpty() {
        StudentListPage listPage = navigateToListPage();
        
        // Vòng lặp mới: Cứ thấy bảng chưa trống thì bấm nút xóa ở dòng trên cùng
        while (!listPage.isTableEmpty()) {
            listPage.clickFirstDeleteButton(); // <--- Chú ý dòng này phải là clickFirstDeleteButton
            try {
                listPage.getToastMessage();
            } catch (Exception e) {
                // Bỏ qua lỗi
            }
        }
    }
    

    /**
     * Điền dữ liệu mới vào form Update và Submit.
     *
     * Quy tắc null: Nếu tham số là null → KHÔNG clear() + sendKeys() trường đó
     * → Giữ nguyên giá trị cũ đang có trong form (phục vụ TC chỉ đổi 1 trường).
     *
     * Nếu tham số KHÔNG null → Bắt buộc gọi clear() trước sendKeys()
     * → Xóa dữ liệu cũ trước khi ghi mới, tránh ghép chuỗi.
     *
     * @param formPage      Đối tượng StudentFormPage đang mở form Update
     * @param newFirstName  First Name mới, null = giữ nguyên
     * @param newLastName   Last Name mới, null = giữ nguyên
     * @param newEmail      Email mới, null = giữ nguyên
     * @param newDepartment Department mới, null = giữ nguyên
     */
    private void fillUpdateFormAndSubmit(StudentFormPage formPage,
            String newFirstName,
            String newLastName,
            String newEmail,
            String newDepartment) {
        // Chỉ ghi đè trường nào có giá trị mới – null = bỏ qua (giữ nguyên)
        // clear() được gọi BÊN TRONG enterFirstName/enterLastName/enterEmail
        // (đã có sẵn trong StudentFormPage) → không cần gọi lại ở đây
        if (newFirstName != null)
            formPage.enterFirstName(newFirstName);
        if (newLastName != null)
            formPage.enterLastName(newLastName);
        if (newEmail != null)
            formPage.enterEmail(newEmail);
        if (newDepartment != null)
            formPage.selectDepartment(newDepartment);
        formPage.clickSubmit();
    }

    /**
     * Đảm bảo sinh viên seed tồn tại trong CSDL trước khi chạy TC_UPD.
     * Nếu email seed chưa tồn tại → thêm mới.
     * Nếu đã tồn tại → bỏ qua (idempotent).
     */
    private void ensureSeedStudentExists() {
        StudentListPage listPage = navigateToListPage();
        if (!listPage.isStudentEmailDisplayed(SEED_EMAIL_FOR_UPDATE)) {
            listPage.clickAddStudent();
            StudentFormPage seedForm = new StudentFormPage(getDriver());
            seedForm.fillStudentForm(
                    SEED_FIRST_NAME,
                    SEED_LAST_NAME,
                    SEED_EMAIL_FOR_UPDATE,
                    SEED_DEPARTMENT);
            seedForm.getToastMessage();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PHẦN 6 – THỰC THI KIỂM THỬ UPDATE (Test Execution – Update)
    // ═══════════════════════════════════════════════════════════════

    /**
     * TC_UPD_02: Xác nhận form Update điền sẵn đúng dữ liệu cũ.
     * Không thay đổi gì – chỉ kiểm tra giá trị hiển thị trong form.
     */
    @Test(priority = 100, description = "TC_UPD_02: Form Update hiển thị đúng dữ liệu cũ được điền sẵn")
    public void TC_UPD_02_verifyPrefilledData() {
        ensureSeedStudentExists();
        StudentFormPage formPage = navigateToUpdateForm(SEED_EMAIL_FOR_UPDATE);

        // Assert từng trường được điền sẵn đúng dữ liệu gốc
        Assert.assertEquals(
                formPage.getFieldValue("firstName"),
                SEED_FIRST_NAME,
                "[TC_UPD_02] Trường First Name không hiển thị đúng dữ liệu cũ.");
        Assert.assertEquals(
                formPage.getFieldValue("lastName"),
                SEED_LAST_NAME,
                "[TC_UPD_02] Trường Last Name không hiển thị đúng dữ liệu cũ.");
        Assert.assertEquals(
                formPage.getFieldValue("email"),
                SEED_EMAIL_FOR_UPDATE,
                "[TC_UPD_02] Trường Email không hiển thị đúng dữ liệu cũ.");
        Assert.assertEquals(
                formPage.getSelectedDepartment(),
                SEED_DEPARTMENT,
                "[TC_UPD_02] Dropdown Department không hiển thị đúng giá trị cũ.");
    }

    /**
     * TC_UPD_10: Không chọn Department (bỏ chọn) rồi Submit.
     * TC riêng lẻ vì logic đặc biệt – cần reset dropdown về trạng thái trống.
     */
    @Test(priority = 101, description = "TC_UPD_10: Bỏ chọn Department rồi Submit – form phải bị chặn")
    public void TC_UPD_10_missingDepartment() {
        ensureSeedStudentExists();
        StudentFormPage formPage = navigateToUpdateForm(SEED_EMAIL_FOR_UPDATE);

        // Truyền chuỗi rỗng → selectDepartment() bỏ qua (guard trong Page Object)
        // → Department giữ nguyên giá trị cũ nhưng form thiếu giá trị hợp lệ
        formPage.selectDepartment("");
        formPage.clickSubmit();

        Assert.assertEquals(
                formPage.getAlertMessage(),
                MSG_DEPT_ERROR,
                "[TC_UPD_10] Thông báo lỗi không chọn Department không chính xác.");
    }

    /**
     * TC_UPD_14: Cập nhật email trùng với sinh viên khác.
     * TC riêng lẻ vì cần chuẩn bị 2 sinh viên seed.
     */
    @Test(priority = 102, description = "TC_UPD_14: Cập nhật email trùng với sinh viên khác – hệ thống phải từ chối")
    public void TC_UPD_14_duplicateEmail() {
        final String existingEmail = "existing_seed@gmail.com";

        // Chuẩn bị: Đảm bảo sinh viên 1 (seed chính) và sinh viên 2 (existing) đều tồn
        // tại
        ensureSeedStudentExists();

        StudentListPage listPage = navigateToListPage();
        if (!listPage.isStudentEmailDisplayed(existingEmail)) {
            listPage.clickAddStudent();
            new StudentFormPage(getDriver()).fillStudentForm(
                    "Existing", "Student", existingEmail, "Biology");
        }

        // Thực thi: Cập nhật sinh viên seed → đổi email thành existingEmail
        StudentFormPage formPage = navigateToUpdateForm(SEED_EMAIL_FOR_UPDATE);
        fillUpdateFormAndSubmit(formPage, null, null, existingEmail, null);

        Assert.assertEquals(
                formPage.getAlertMessage(),
                MSG_DUPLICATE_EMAIL,
                "[TC_UPD_14] Không hiển thị cảnh báo email trùng lặp.");
    }

    /**
     * Nhóm Happy Path – Cập nhật thành công.
     * Tái sử dụng SEED_EMAIL_FOR_UPDATE: sau mỗi TC thành công, email có thể thay
     * đổi.
     * Do đó mỗi TC trong nhóm này chạy độc lập với ensureSeedStudentExists().
     */
    @Test(dataProvider = "updateStudentValidData", priority = 103, description = "TC_UPD_01,03~06,18,19,30,31: Cập nhật sinh viên thành công (Happy Path & BVA Valid)")
    public void executeValidUpdateCases(String tcId,
            String newFirstName,
            String newLastName,
            String newEmail,
            String newDepartment) {
        ensureSeedStudentExists();
        StudentFormPage formPage = navigateToUpdateForm(SEED_EMAIL_FOR_UPDATE);

        // Lệnh clear() được gọi BÊN TRONG StudentFormPage.enterXxx() trước sendKeys()
        // → Đảm bảo xóa dữ liệu cũ trước khi ghi mới, không bị ghép chuỗi
        fillUpdateFormAndSubmit(formPage, newFirstName, newLastName, newEmail, newDepartment);

        StudentListPage listPage = new StudentListPage(getDriver());
        Assert.assertEquals(
                listPage.getToastMessage(),
                MSG_UPDATED_SUCCESS,
                String.format("[%s] Toast thông báo cập nhật không khớp kết quả mong đợi.", tcId));
    }

    /**
     * Nhóm Negative – Bỏ trống trường bắt buộc khi cập nhật.
     * TC_UPD_07, 08, 09.
     */
    @Test(dataProvider = "updateMissingRequiredFieldsData", priority = 104, description = "TC_UPD_07~09: Xóa trắng trường bắt buộc khi Update – form phải bị chặn")
    public void executeUpdateMissingFieldsCases(String tcId,
            boolean clearFirst,
            boolean clearLast,
            boolean clearEmail,
            String expectedMsg) {
        ensureSeedStudentExists();
        StudentFormPage formPage = navigateToUpdateForm(SEED_EMAIL_FOR_UPDATE);

        // Xóa trắng đúng trường cần test – clear() xóa dữ liệu cũ, sendKeys("") để rỗng
        if (clearFirst)
            formPage.enterFirstName("");
        if (clearLast)
            formPage.enterLastName("");
        if (clearEmail)
            formPage.enterEmail("");
        formPage.clickSubmit();

        Assert.assertEquals(
                formPage.getAlertMessage(),
                expectedMsg,
                String.format("[%s] Thông báo lỗi khi bỏ trống trường bắt buộc không chính xác.", tcId));
    }

    /**
     * Nhóm Negative – Sai định dạng Email khi cập nhật.
     * TC_UPD_11, 12, 13, 15.
     */
    @Test(dataProvider = "updateInvalidEmailData", priority = 105, description = "TC_UPD_11,12,13,15: Email sai định dạng / SQL Injection khi Update")
    public void executeUpdateInvalidEmailCases(String tcId,
            String invalidEmail,
            String expectedValidationMsg) {
        ensureSeedStudentExists();
        StudentFormPage formPage = navigateToUpdateForm(SEED_EMAIL_FOR_UPDATE);

        // clear() được gọi bên trong enterEmail() → xóa email cũ trước khi nhập sai
        formPage.enterEmail(invalidEmail);
        formPage.clickSubmit();

        Assert.assertEquals(
                formPage.getValidationErrorMessage(),
                expectedValidationMsg,
                String.format("[%s] Inline validation cho trường Email không chính xác.", tcId));
    }

    /**
     * Nhóm Negative – Validation ký tự trường Tên/Họ khi cập nhật.
     * TC_UPD_16, 17, 20~29.
     */
    @Test(dataProvider = "updateFieldValidationData", priority = 106, description = "TC_UPD_16,17,20~29: Validation BVA, ký tự đặc biệt, số, XSS, khoảng trắng")
    public void executeUpdateFieldValidationCases(String tcId,
            String newFirstName,
            String newLastName,
            String expectedValidationMsg) {
        ensureSeedStudentExists();
        StudentFormPage formPage = navigateToUpdateForm(SEED_EMAIL_FOR_UPDATE);

        // null = giữ nguyên trường đó; không null = clear() bên trong sẽ xóa dữ liệu cũ
        if (newFirstName != null)
            formPage.enterFirstName(newFirstName);
        if (newLastName != null)
            formPage.enterLastName(newLastName);
        formPage.clickSubmit();

        Assert.assertEquals(
                formPage.getValidationErrorMessage(),
                expectedValidationMsg,
                String.format("[%s] Inline validation cho trường First/Last Name không chính xác.", tcId));
    }

    // ═══════════════════════════════════════════════════════════════
    // HẰNG SỐ – NHÓM DELETE & VIEW
    // ═══════════════════════════════════════════════════════════════

    // private static final String MSG_DELETED_SUCCESS = "Student deleted
    // successfully!";
    private static final String MSG_UNABLE_TO_LOAD = "Unable to load student list";
    private static final String MSG_INTERNAL_SERVER_ERROR = "Internal server error";
    private static final String EXPECTED_PAGE_HEADING = "List of Students";
    private static final String STUDENTS_URL = "http://localhost:3000/students";

    // ═══════════════════════════════════════════════════════════════
    // PHẦN – THỰC THI KIỂM THỬ XÓA SINH VIÊN (TC_DEL)
    // ═══════════════════════════════════════════════════════════════

    /**
     * TC_DEL_01: Xóa một sinh viên đang tồn tại.
     *
     * Luồng thực thi:
     * Seed sinh viên → Xác định trong danh sách → Nhấn Delete
     * → Toast thành công → Email biến mất khỏi bảng
     */
    @Test(priority = 300, description = "TC_DEL_01: Xóa sinh viên tồn tại – Toast thành công, bản ghi biến mất")
    public void TC_DEL_01_deleteExistingStudent() {
        // Seed: Tạo sinh viên mới để xóa – email động tránh xung đột với dữ liệu khác
        final String emailToDelete = "del01_" + System.currentTimeMillis() + "@gmail.com";

        StudentListPage listPage = navigateToListPage();
        listPage.clickAddStudent();
        new StudentFormPage(getDriver())
                .fillStudentForm("Delete", "Target01", emailToDelete, "Biology");

        // Xác nhận sinh viên đã xuất hiện trong danh sách trước khi xóa
        listPage = navigateToListPage();
        Assert.assertTrue(
                listPage.isStudentEmailDisplayed(emailToDelete),
                "[TC_DEL_01] Sinh viên seed chưa xuất hiện trong danh sách – tiền điều kiện thất bại.");

        // Thực thi: Nhấn Delete trên đúng dòng chứa email (Dynamic XPath)
        listPage.clickDeleteByEmail(emailToDelete);

        // Assert 1: Toast "Student deleted successfully!" hiển thị
        Assert.assertEquals(
                listPage.getToastMessage(),
                MSG_DELETED_SUCCESS,
                "[TC_DEL_01] Toast thông báo xóa không khớp kết quả mong đợi.");

        // Assert 2: Email đã biến mất khỏi danh sách sau khi xóa
        Assert.assertFalse(
                listPage.isStudentEmailDisplayed(emailToDelete),
                "[TC_DEL_01] Sinh viên vẫn còn hiển thị trong danh sách sau khi xóa.");
    }

    /**
     * TC_DEL_02: Xóa sinh viên và xác nhận các bản ghi khác không bị ảnh hưởng.
     *
     * Luồng thực thi:
     * Seed 2 sinh viên → Ghi nhận count → Xóa 1 → Assert count-1,
     * email bị xóa mất, email còn lại vẫn hiển thị
     */
    @Test(priority = 301, description = "TC_DEL_02: Xóa sinh viên – N-1 bản ghi còn lại không bị ảnh hưởng")
    public void TC_DEL_02_deleteDoesNotAffectOthers() {
        final long ts = System.currentTimeMillis();
        final String emailToDelete = "del02_target_" + ts + "@gmail.com";
        final String emailToKeep = "del02_keep_" + ts + "@gmail.com";

        // Seed: Tạo 2 sinh viên độc lập – 1 để xóa, 1 để xác nhận còn nguyên
        StudentListPage listPage = navigateToListPage();
        listPage.clickAddStudent();
        new StudentFormPage(getDriver())
                .fillStudentForm("Delete", "Target02", emailToDelete, "Biology");

        navigateToListPage().clickAddStudent();
        new StudentFormPage(getDriver())
                .fillStudentForm("Keep", "Student02", emailToKeep, "History");

        // Ghi nhận số lượng sinh viên TRƯỚC khi xóa
        listPage = navigateToListPage();
        int countBefore = listPage.getStudentCount();

        // Thực thi: Xóa đúng 1 sinh viên theo email
        listPage.clickDeleteByEmail(emailToDelete);

        // Chờ Toast xác nhận xóa thành công
        Assert.assertEquals(
                listPage.getToastMessage(),
                MSG_DELETED_SUCCESS,
                "[TC_DEL_02] Toast thông báo xóa không chính xác.");

        // Assert 1: Số lượng giảm đúng 1 (N → N-1)
        Assert.assertEquals(
                listPage.getStudentCount(),
                countBefore - 1,
                "[TC_DEL_02] Số lượng sinh viên sau khi xóa không giảm đúng 1.");

        // Assert 2: Sinh viên cần xóa không còn trong danh sách
        Assert.assertFalse(
                listPage.isStudentEmailDisplayed(emailToDelete),
                "[TC_DEL_02] Sinh viên cần xóa vẫn còn hiển thị – xóa thất bại.");

        // Assert 3: Sinh viên KHÔNG liên quan vẫn giữ nguyên
        Assert.assertTrue(
                listPage.isStudentEmailDisplayed(emailToKeep),
                "[TC_DEL_02] Sinh viên không liên quan bị xóa theo – thao tác xóa bị lỗi.");
    }

    /**
     * TC_DEL_03: Xóa sinh viên CUỐI CÙNG trong danh sách (BVA – boundary).
     *
     * Luồng thực thi:
     *   Seed 1 sinh viên duy nhất → Nhấn Delete → Toast thành công
     *   → Danh sách hiển thị trạng thái trống → Không crash
     *
     * Lưu ý: TC này yêu cầu môi trường kiểm soát – chỉ có đúng 1 sinh viên.
     * Trong thực tế cần dọn dẹp dữ liệu trước (xóa hết rồi seed 1 bản ghi).
     */
    @Test(
        priority    = 302,
        description = "TC_DEL_03: Xóa sinh viên cuối cùng (BVA) – bảng trống, không crash"
    )
    public void TC_DEL_03_deleteLastStudent() {
        // Gọi hàm dọn dẹp để đảm bảo CSDL trắng trước khi test
        ensureTableIsEmpty(); 

        final String lastEmail = "del03_last_" + System.currentTimeMillis() + "@gmail.com";
        
        // Thêm 1 sinh viên duy nhất
        StudentListPage listPage = navigateToListPage();
        listPage.clickAddStudent();
        new StudentFormPage(getDriver())
            .fillStudentForm("Last", "Student03", lastEmail, "Mathematics");

        // Lấy lại trang List (Chỉ gán lại biến, không khai báo lại biến listPage)
        listPage = navigateToListPage(); 
        
        // Xóa đúng sinh viên đó
        listPage.clickDeleteByEmail(lastEmail);

        // Assert 1: Toast thành công
        Assert.assertEquals(
            listPage.getToastMessage(),
            MSG_DELETED_SUCCESS,
            "[TC_DEL_03] Toast thông báo xóa sinh viên cuối không chính xác."
        );

        // Assert 2: Email đã biến mất
        Assert.assertFalse(
            listPage.isStudentEmailDisplayed(lastEmail),
            "[TC_DEL_03] Sinh viên cuối vẫn hiển thị sau khi xóa."
        );
    }

    /**
     * TC_DEL_04: Xóa sinh viên rồi thêm lại với email đã xóa (tái sử dụng email).
     *
     * Luồng thực thi:
     * Seed → Xóa → Xác nhận đã xóa → Thêm mới với cùng email
     * → Unique constraint phải được giải phóng → Thêm thành công
     */
    @Test(priority = 303, description = "TC_DEL_04: Xóa rồi thêm lại với email đã xóa – unique constraint giải phóng")
    public void TC_DEL_04_reuseEmailAfterDelete() {
        final String reusedEmail = "del04_reuse_" + System.currentTimeMillis() + "@gmail.com";

        // Bước 1: Seed – Tạo sinh viên với email cần tái sử dụng
        StudentListPage listPage = navigateToListPage();
        listPage.clickAddStudent();
        new StudentFormPage(getDriver())
                .fillStudentForm("First", "Owner04", reusedEmail, "Computer Science");

        // Bước 2: Xóa sinh viên vừa tạo
        listPage = navigateToListPage();
        listPage.clickDeleteByEmail(reusedEmail);
        Assert.assertEquals(
                listPage.getToastMessage(),
                MSG_DELETED_SUCCESS,
                "[TC_DEL_04] Bước xóa thất bại – không thể tiếp tục test tái sử dụng email.");

        // Xác nhận email đã biến mất hoàn toàn trước khi thêm lại
        Assert.assertFalse(
                listPage.isStudentEmailDisplayed(reusedEmail),
                "[TC_DEL_04] Email vẫn còn trong bảng sau khi xóa – tiền điều kiện bước 3 thất bại.");

        // Bước 3: Thêm mới sinh viên với CHÍNH email vừa xóa
        listPage = navigateToListPage();
        listPage.clickAddStudent();
        new StudentFormPage(getDriver())
                .fillStudentForm("Second", "Owner04", reusedEmail, "Engineering");

        // Assert 1: Toast thêm mới thành công (unique constraint đã giải phóng)
        listPage = new StudentListPage(getDriver());
        Assert.assertEquals(
                listPage.getToastMessage(),
                MSG_ADDED_SUCCESS,
                "[TC_DEL_04] Không thể tái sử dụng email sau khi xóa – unique constraint chưa giải phóng.");

        // Assert 2: Email mới xuất hiện trong danh sách
        Assert.assertTrue(
                listPage.isStudentEmailDisplayed(reusedEmail),
                "[TC_DEL_04] Email tái sử dụng không hiển thị trong danh sách sau khi thêm lại.");
    }

    @Test(
        priority = 304,
        description = "TC_DEL_05: Hệ thống phải hiển thị Popup xác nhận trước khi xóa"
    )
    public void TC_DEL_05_verifyDeleteConfirmationPopup() {
        // Mồi 1 sinh viên có sẵn để test
        ensureSeedStudentExists();
        StudentListPage listPage = navigateToListPage();
        
        // Gọi hàm kiểm tra Popup
        boolean isPopupShown = listPage.isDeleteConfirmationPopupDisplayed(SEED_EMAIL_FOR_UPDATE);
        
        // Đoạn này dùng để bắt Bug: Nếu isPopupShown là false, nó sẽ giơ cờ Đỏ (Fail)
        Assert.assertTrue(
            isPopupShown, 
            "[BUG] Hệ thống rủi ro mất dữ liệu: Không hiển thị Popup xác nhận khi người dùng bấm nút Xóa!"
        );
    }
    // ═══════════════════════════════════════════════════════════════
    // PHẦN – THỰC THI KIỂM THỬ XEM DANH SÁCH SINH VIÊN (TC_VIEW)
    // ═══════════════════════════════════════════════════════════════

    /**
     * TC_VIEW_01: Xem danh sách sinh viên khi có dữ liệu.
     *
     * Luồng thực thi:
     * Nhấn "Students" → Bảng hiển thị đúng đủ cột và dữ liệu khớp CSDL
     */
    @Test(priority = 400, description = "TC_VIEW_01: Danh sách sinh viên hiển thị đúng và đầy đủ các cột")
    public void TC_VIEW_01_viewStudentListWithData() {
        // Seed: Đảm bảo có ít nhất 1 sinh viên trong CSDL
        final String seedEmail = "view01_" + System.currentTimeMillis() + "@gmail.com";
        StudentListPage listPage = navigateToListPage();
        if (!listPage.isStudentEmailDisplayed(seedEmail)) {
            listPage.clickAddStudent();
            new StudentFormPage(getDriver())
                    .fillStudentForm("View", "Student01", seedEmail, "Computer Science");
        }

        // Thực thi: Điều hướng về trang danh sách qua thanh nav
        listPage = navigateToListPage();

        // Assert 1: Tiêu đề trang đúng
        Assert.assertEquals(
                listPage.getPageHeading(),
                EXPECTED_PAGE_HEADING,
                "[TC_VIEW_01] Tiêu đề trang không khớp – không đang ở trang danh sách sinh viên.");

        // Assert 2: Bảng có ít nhất 1 dòng dữ liệu
        Assert.assertTrue(
                listPage.getStudentCount() > 0,
                "[TC_VIEW_01] Bảng không hiển thị dữ liệu dù CSDL có sinh viên.");

        // Assert 3: Email seed hiển thị đúng trong cột Email (cột thứ 3)
        Assert.assertTrue(
                listPage.isStudentEmailDisplayed(seedEmail),
                "[TC_VIEW_01] Email seed không hiển thị đúng trong cột Email của bảng.");

        // Assert 4: Mỗi dòng có đúng 2 nút hành động (Update + Delete)
        Assert.assertTrue(
                listPage.doesEachRowHaveActionButtons(),
                "[TC_VIEW_01] Một hoặc nhiều dòng không có đủ nút Update và Delete.");
    }

    /**
     * TC_VIEW_02: Xem danh sách sinh viên khi CSDL trống.
     *
     * Lưu ý: TC này yêu cầu môi trường kiểm soát – CSDL phải trống.
     * Trong thực tế cần @BeforeMethod dọn dẹp toàn bộ dữ liệu.
     */
    @Test(
        priority    = 401,
        description = "TC_VIEW_02: Danh sách trống khi CSDL không có sinh viên – không crash"
    )
    public void TC_VIEW_02_viewEmptyStudentList() {
        // Dọn sạch bảng
        ensureTableIsEmpty(); 

        // Chỉ khai báo biến listPage 1 lần duy nhất ở đây
        StudentListPage listPage = navigateToListPage();
        
        Assert.assertEquals(
            listPage.getPageHeading(), 
            EXPECTED_PAGE_HEADING, 
            "[TC_VIEW_02] Sai tiêu đề trang."
        );
        
        Assert.assertTrue(
            listPage.isTableEmpty(), 
            "[TC_VIEW_02] Bảng không ở trạng thái trống dù CSDL không có sinh viên."
        );
    }

    /**
     * TC_VIEW_03: Điều hướng trực tiếp đến URL trang danh sách sinh viên.
     *
     * Luồng thực thi:
     * Nhập trực tiếp URL → Trang tải thành công → Dữ liệu hiển thị đúng
     */
    @Test(priority = 402, description = "TC_VIEW_03: Truy cập trực tiếp URL /students – trang tải thành công")
    public void TC_VIEW_03_directUrlNavigation() {
        // Thực thi: Nhập URL trực tiếp vào thanh địa chỉ trình duyệt
        getDriver().get(STUDENTS_URL);
        StudentListPage listPage = new StudentListPage(getDriver());

        // Assert 1: URL hiện tại đúng
        Assert.assertTrue(
                getDriver().getCurrentUrl().contains("students"),
                "[TC_VIEW_03] URL hiện tại không chứa 'students' sau khi điều hướng trực tiếp.");

        // Assert 2: Tiêu đề trang hiển thị đúng
        Assert.assertEquals(
                listPage.getPageHeading(),
                EXPECTED_PAGE_HEADING,
                "[TC_VIEW_03] Tiêu đề trang không đúng khi truy cập trực tiếp URL.");

        // Assert 3: Bảng hiển thị đúng (có heading, không crash)
        Assert.assertNotNull(
                listPage.getPageHeading(),
                "[TC_VIEW_03] Trang không tải được khi truy cập trực tiếp URL.");
    }

    /**
     * TC_VIEW_04: Xem danh sách khi Backend mất kết nối.
     *
     * Lưu ý: TC này kiểm thử điều kiện ngoại lệ hạ tầng.
     * Trong môi trường tự động, cần mock hoặc dừng Backend service trước khi chạy.
     * Nếu không thể dừng Backend programmatically, TC này nên chạy thủ công
     * hoặc dùng WireMock để giả lập lỗi.
     */
    @Test(priority = 403, description = "TC_VIEW_04: Backend mất kết nối – hiển thị thông báo lỗi, không crash", enabled = false 
    )
    public void TC_VIEW_04_backendConnectionLost() {
        // Tiền điều kiện: Backend service đã được tắt trước khi chạy TC này
        // (Thực hiện thủ công hoặc qua script dừng service)

        // Thực thi: Điều hướng đến trang danh sách
        getDriver().get(STUDENTS_URL);
        StudentListPage listPage = new StudentListPage(getDriver());

        // Assert 1: Thông báo lỗi "Unable to load student list" hiển thị
        Assert.assertEquals(
                listPage.getErrorMessage(),
                MSG_UNABLE_TO_LOAD,
                "[TC_VIEW_04] Thông báo lỗi khi Backend mất kết nối không chính xác.");

        // Assert 2: Bảng không hiển thị dữ liệu
        Assert.assertTrue(
                listPage.isTableEmpty(),
                "[TC_VIEW_04] Bảng vẫn hiển thị dữ liệu dù Backend mất kết nối.");

        // Assert 3: Không crash – URL vẫn ở trang students
        Assert.assertTrue(
                getDriver().getCurrentUrl().contains("students"),
                "[TC_VIEW_04] Trang bị crash khi Backend mất kết nối.");
    }

    /**
     * TC_VIEW_05: Xem danh sách khi xảy ra lỗi truy vấn Backend (HTTP 500).
     *
     * Lưu ý: Tương tự TC_VIEW_04 – cần WireMock hoặc môi trường mock để
     * giả lập Backend trả về HTTP 500 Internal Server Error.
     */
    @Test(priority = 404, description = "TC_VIEW_05: Backend trả về HTTP 500 – thông báo lỗi, không crash", enabled = false // Tắt
                                                                                                                            // mặc
                                                                                                                            // định
                                                                                                                            // –
                                                                                                                            // cần
                                                                                                                            // WireMock
                                                                                                                            // giả
                                                                                                                            // lập
                                                                                                                            // HTTP
                                                                                                                            // 500
    )
    public void TC_VIEW_05_backendInternalServerError() {
        // Tiền điều kiện: WireMock hoặc server mock đã cấu hình trả về HTTP 500
        // cho endpoint GET /api/students

        getDriver().get(STUDENTS_URL);
        StudentListPage listPage = new StudentListPage(getDriver());

        // Assert 1: Thông báo "Internal server error" hiển thị
        Assert.assertEquals(
                listPage.getErrorMessage(),
                MSG_INTERNAL_SERVER_ERROR,
                "[TC_VIEW_05] Thông báo lỗi HTTP 500 không chính xác.");

        // Assert 2: Giao diện không crash – trang vẫn hiển thị
        Assert.assertTrue(
                getDriver().getCurrentUrl().contains("students"),
                "[TC_VIEW_05] Trang bị crash khi Backend trả về HTTP 500.");
    }

    /**
     * TC_VIEW_06: Kiểm tra nút Update và Delete hiển thị đúng trên mỗi dòng.
     *
     * Luồng thực thi:
     * Seed ít nhất 1 sinh viên → Vào danh sách → Kiểm tra từng dòng
     * có đủ 2 nút Update (xanh) và Delete (đỏ)
     */
    @Test(priority = 405, description = "TC_VIEW_06: Mỗi dòng trong bảng có đủ nút Update và Delete đúng vị trí")
    public void TC_VIEW_06_verifyActionButtonsOnEachRow() {
        // Seed: Đảm bảo có ít nhất 1 sinh viên trong bảng
        final String seedEmail = "view06_" + System.currentTimeMillis() + "@gmail.com";
        StudentListPage listPage = navigateToListPage();
        listPage.clickAddStudent();
        new StudentFormPage(getDriver())
                .fillStudentForm("View", "Button06", seedEmail, "History");

        // Điều hướng về danh sách
        listPage = navigateToListPage();

        // Assert 1: Bảng có ít nhất 1 dòng
        Assert.assertTrue(
                listPage.getStudentCount() > 0,
                "[TC_VIEW_06] Bảng trống – không có dòng nào để kiểm tra nút.");

        // Assert 2: Mỗi dòng đều có đủ nút Update (btn-info) và Delete (btn-danger)
        Assert.assertTrue(
                listPage.doesEachRowHaveActionButtons(),
                "[TC_VIEW_06] Một hoặc nhiều dòng không có đủ nút Update và Delete.");
    }
}