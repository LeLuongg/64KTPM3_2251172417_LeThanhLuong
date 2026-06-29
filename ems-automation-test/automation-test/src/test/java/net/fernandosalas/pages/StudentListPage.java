package net.fernandosalas.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.JavascriptExecutor;
public class StudentListPage {

    private final WebDriver     driver;
    private final WebDriverWait wait;
    private static final int    TIMEOUT_SECONDS = 10;

    // Dynamic XPath template – [locator-dynamic-exception]
    // CSS Selector không hỗ trợ ancestor:: nên XPath là bắt buộc ở đây
    private static final String XPATH_BTN_BY_EMAIL =
        "//td[normalize-space(text())='%s']/ancestor::tr//*[contains(@class,'%s')]";

    private static final String XPATH_EMAIL_CELL =
        "//td[normalize-space(text())='%s']";

    // ── Static Locators – [locator-css-over-xpath] ────────────────
    private final By btnAddStudent = By.cssSelector("a[href='/add-student']");
    private final By studentTable  = By.cssSelector("table");
    private final By tableRows     = By.cssSelector("table tbody tr");
    private final By emailCells    = By.cssSelector("table tbody tr td:nth-child(3)");
    private final By toastMessage  = By.cssSelector(".Toastify__toast-body");
    private final By pageHeading   = By.cssSelector("h2");

    // ── Constructor ───────────────────────────────────────────────
    public StudentListPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
    }

    // ── Navigation ────────────────────────────────────────────────
    public void clickAddStudent() {
        wait.until(ExpectedConditions.elementToBeClickable(btnAddStudent)).click();
    }

    // ── Dynamic XPath Methods – [locator-dynamic-exception] ───────
    // Duyệt DOM: <td>[email] → ancestor::<tr> → <button>[class]

    // public void clickUpdateByEmail(String email) {
    //     wait.until(ExpectedConditions.visibilityOfElementLocated(studentTable));
    //     String xpath = String.format(XPATH_BTN_BY_EMAIL, email, "btn-info");
    //     wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath))).click();
    // }
    public void clickUpdateByEmail(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentTable));
        String xpath = String.format(XPATH_BTN_BY_EMAIL, email, "btn-outline-info");
        WebElement updateBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", updateBtn);
        // ĐỔI DÒNG DƯỚI THÀNH LỆNH NÀY:
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", updateBtn);
    }

    public void clickDeleteByEmail(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentTable));
        String xpath = String.format(XPATH_BTN_BY_EMAIL, email, "btn-outline-danger");
        WebElement deleteBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", deleteBtn);
        // ĐỔI DÒNG DƯỚI THÀNH LỆNH NÀY:
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
    }

    public void clickFirstDeleteButton() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentTable));
        WebElement firstDeleteBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("(//*[contains(@class,'btn-outline-danger')])[1]")
        ));
        
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", firstDeleteBtn);
        executor.executeScript("arguments[0].click();", firstDeleteBtn);
    }
    // ── State Retrieval (phục vụ Assert) ─────────────────────────
    // [wait-explicit-only] Mọi getter đều có Wait
    // [wait-no-implicit-mix] Không dùng implicitlyWait()

    public int getStudentCount() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentTable));
        return driver.findElements(tableRows).size();
    }

    public boolean isStudentEmailDisplayed(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentTable));
        String xpath = String.format(XPATH_EMAIL_CELL, email);
        // findElements() trả về List rỗng thay vì ném NoSuchElementException
        // → An toàn khi phần tử có thể không tồn tại
        return !driver.findElements(By.xpath(xpath)).isEmpty();
    }

    public int countEmailOccurrences(String email) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentTable));
        String xpath = String.format(XPATH_EMAIL_CELL, email);
        return driver.findElements(By.xpath(xpath)).size();
    }

    public String getToastMessage() {
        return wait.until(
            ExpectedConditions.visibilityOfElementLocated(toastMessage)).getText();
    }

    public String getPageHeading() {
        return wait.until(
            ExpectedConditions.visibilityOfElementLocated(pageHeading)).getText();
    }

    public List<String> getAllDisplayedEmails() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentTable));
        return driver.findElements(emailCells)
                     .stream()
                     .map(WebElement::getText)
                     .toList();
    }

    public boolean isTableEmpty() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentTable));
        return driver.findElements(tableRows).isEmpty();
    }

    // ── Bổ sung vào StudentListPage – phục vụ nhóm TC_VIEW ───────

    /** Locator thông báo lỗi khi không tải được danh sách (TC_VIEW_04, 05) */
    private final By errorMessage = By.cssSelector(".alert-danger, .error-message");

    /**
     * Lấy nội dung thông báo lỗi khi không tải được danh sách.
     * Phục vụ TC_VIEW_04 (Backend mất kết nối) và TC_VIEW_05 (HTTP 500).
     *
     * @return Chuỗi thông báo lỗi, ví dụ: "Unable to load student list"
     */
    public String getErrorMessage() {
        return wait.until(
            ExpectedConditions.visibilityOfElementLocated(errorMessage)
        ).getText();
    }

    /**
     * Kiểm tra mỗi dòng trong bảng có đủ 2 nút Update và Delete không.
     * Phục vụ TC_VIEW_01 và TC_VIEW_06.
     *
     * Logic:
     *   - Lấy tất cả <tr> trong <tbody>
     *   - Với mỗi <tr>, kiểm tra có button.btn-info (Update) và button.btn-danger (Delete)
     *
     * @return true nếu TẤT CẢ dòng đều có đủ 2 nút, false nếu có ít nhất 1 dòng thiếu
     */
    public boolean doesEachRowHaveActionButtons() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(studentTable));
        List<WebElement> rows = driver.findElements(tableRows);

        if (rows.isEmpty()) return false;

        for (WebElement row : rows) {
            // Kiểm tra nút Update (btn-info) trong dòng này
            List<WebElement> updateBtns = row.findElements(
                By.cssSelector(".btn-outline-info")
            );
            // Kiểm tra nút Delete (btn-danger) trong dòng này
            List<WebElement> deleteBtns = row.findElements(
                By.cssSelector(".btn-outline-danger")
            );

            // Nếu thiếu bất kỳ nút nào → trả về false ngay
            if (updateBtns.isEmpty() || deleteBtns.isEmpty()) {
                return false;
            }
        }

        // Tất cả dòng đều có đủ 2 nút
        return true;
    }

    public boolean isDeleteConfirmationPopupDisplayed(String email) {
        try {
            WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//td[text()='" + email + "']/following-sibling::td//button[contains(@class,'btn-outline-danger')]")
            ));
            
            JavascriptExecutor executor = (JavascriptExecutor) driver;
            executor.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", deleteBtn);
            executor.executeScript("arguments[0].click();", deleteBtn);
            
            WebDriverWait popupWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement popup = popupWait.until(ExpectedConditions.visibilityOfElementLocated(By.className("modal-dialog"))); 
            
            return popup.isDisplayed();
            
        } catch (Exception e) {
            return false;
        }
    }
}