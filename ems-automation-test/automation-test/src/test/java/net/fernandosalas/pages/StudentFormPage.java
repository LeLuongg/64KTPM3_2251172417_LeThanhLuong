package net.fernandosalas.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class StudentFormPage {

    private final WebDriver     driver;
    private final WebDriverWait wait;
    private static final int    TIMEOUT_SECONDS = 10;

    // ── Locators ──────────────────────────────────────────────────
    // [locator-name-first] By.name() cho input có thuộc tính name
    private final By txtFirstName    = By.name("firstName");
    private final By txtLastName     = By.name("lastName");
    private final By txtEmail        = By.name("email");
    // [locator-css-over-xpath] cssSelector cho select và button
    private final By ddlDepartment   = By.cssSelector("select.form-select");
    private final By btnSubmit       = By.cssSelector("button.btn.btn-outline-success");
    private final By toastMessage    = By.cssSelector(".Toastify__toast-body");
    private final By validationError = By.cssSelector(".invalid-feedback");
    private final By alertMessage    = By.cssSelector(".alert");

    // ── Constructor ───────────────────────────────────────────────
    public StudentFormPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
    }

    // ── Atomic Action Methods ─────────────────────────────────────
    // [wait-explicit-only] Mọi thao tác đều qua wait.until()
    // [wait-no-thread-sleep] Không có Thread.sleep()
    // clear() trước sendKeys() để xóa dữ liệu cũ (quan trọng với form Update)

    public void enterFirstName(String firstName) {
        WebElement el = wait.until(
            ExpectedConditions.visibilityOfElementLocated(txtFirstName));
        el.clear();
        el.sendKeys(firstName); // Truyền "" để test trường rỗng
    }

    public void enterLastName(String lastName) {
        WebElement el = wait.until(
            ExpectedConditions.visibilityOfElementLocated(txtLastName));
        el.clear();
        el.sendKeys(lastName);
    }

    public void enterEmail(String email) {
        WebElement el = wait.until(
            ExpectedConditions.visibilityOfElementLocated(txtEmail));
        el.clear();
        el.sendKeys(email);
    }

    public void selectDepartment(String departmentName) {
        // Guard: null/"" → bỏ qua (phục vụ TC không chọn Department)
        if (departmentName == null || departmentName.trim().isEmpty()) return;
        WebElement el = wait.until(
            ExpectedConditions.elementToBeClickable(ddlDepartment));
        new Select(el).selectByVisibleText(departmentName);
    }

    public void selectDepartmentByValue(String value) {
        WebElement el = wait.until(
            ExpectedConditions.elementToBeClickable(ddlDepartment));
        new Select(el).selectByValue(value);
    }

    public void clickSubmit() {
        wait.until(ExpectedConditions.elementToBeClickable(btnSubmit)).click();
    }

    // ── Business Method ───────────────────────────────────────────
    public void fillStudentForm(String firstName, String lastName,
                                String email, String departmentName) {
        enterFirstName(firstName);
        enterLastName(lastName);
        enterEmail(email);
        selectDepartment(departmentName);
        clickSubmit();
    }

    // ── Getter Methods (phục vụ Assert) ──────────────────────────

    public String getToastMessage() {
        return wait.until(
            ExpectedConditions.visibilityOfElementLocated(toastMessage)).getText();
    }

    public String getValidationErrorMessage() {
        return wait.until(
            ExpectedConditions.visibilityOfElementLocated(validationError)).getText();
    }

    public String getAlertMessage() {
        return wait.until(
            ExpectedConditions.visibilityOfElementLocated(alertMessage)).getText();
    }

    public boolean isSubmitButtonEnabled() {
        try {
            WebElement btn = wait.until(
                ExpectedConditions.elementToBeClickable(btnSubmit));
            return btn.isDisplayed() && btn.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }

    /** Lấy tên Department đang chọn – phục vụ TC_UPD_02 */
    public String getSelectedDepartment() {
        WebElement el = wait.until(
            ExpectedConditions.visibilityOfElementLocated(ddlDepartment));
        return new Select(el).getFirstSelectedOption().getText();
    }

    
    /** Lấy value của input theo name – phục vụ TC_UPD_02 */
    public String getFieldValue(String fieldName) {
        By locator = By.name(fieldName); 
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        
        try {
            // Chờ thông minh: Trình duyệt sẽ quét liên tục, ngay khi value có chữ (không rỗng) là đi tiếp ngay lập tức
            wait.until(d -> !el.getAttribute("value").isEmpty());
        } catch (Exception e) {
            // Bỏ qua Exception nếu ô đó thực sự trống (phục vụ các test case khác nếu có)
        }
        
        return el.getAttribute("value");
    }
    
}