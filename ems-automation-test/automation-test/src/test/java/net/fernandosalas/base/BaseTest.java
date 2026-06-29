package net.fernandosalas.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.File;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * BaseTest — Lớp nền tảng hạ tầng cho toàn bộ framework Selenium.
 *
 * <p>Trách nhiệm duy nhất của lớp này là quản lý vòng đời WebDriver:
 * <ul>
 *   <li>Khởi tạo và cấu hình trình duyệt trước mỗi ca kiểm thử.</li>
 *   <li>Tự động chụp ảnh màn hình khi ca kiểm thử thất bại.</li>
 *   <li>Giải phóng tài nguyên trình duyệt sau mỗi ca kiểm thử.</li>
 * </ul>
 *
 * <p><b>Lưu ý kiến trúc:</b> Lớp này tuyệt đối không chứa bất kỳ
 * Locator hay thao tác tương tác giao diện (UI interaction) nào.
 */
public class BaseTest {

    // -------------------------------------------------------------------------
    // Hằng số cấu hình
    // -------------------------------------------------------------------------

    /**
     * Thư mục lưu ảnh chụp màn hình khi ca kiểm thử thất bại.
     * Đường dẫn tương đối tính từ thư mục gốc của project.
     */
    private static final String SCREENSHOT_DIR = "test-output/screenshots/failures";

    /** Định dạng timestamp dùng để đặt tên file ảnh, tránh trùng lặp. */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    // -------------------------------------------------------------------------
    // Trạng thái WebDriver — ThreadLocal đảm bảo an toàn khi chạy song song
    // -------------------------------------------------------------------------

    /**
     * Lưu trữ WebDriver theo từng luồng (thread).
     * Giúp framework chạy song song (parallel execution) mà không bị xung đột.
     */
    protected ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    // -------------------------------------------------------------------------
    // Phương thức tiện ích
    // -------------------------------------------------------------------------

    /**
     * Trả về WebDriver của luồng hiện tại.
     * Các lớp con (Page Object, Test Class) sử dụng phương thức này
     * thay vì truy cập trực tiếp vào biến {@code driver}.
     *
     * @return WebDriver đang hoạt động trong luồng hiện tại.
     */
    public WebDriver getDriver() {
        return driver.get();
    }

    // -------------------------------------------------------------------------
    // Vòng đời TestNG — Setup
    // -------------------------------------------------------------------------

    /**
     * Thiết lập môi trường kiểm thử trước mỗi ca kiểm thử (@BeforeMethod).
     *
     * <p>Quy trình:
     * <ol>
     *   <li>Dùng WebDriverManager tự động tải và cấu hình ChromeDriver phù hợp.</li>
     *   <li>Khởi tạo một phiên ChromeDriver mới.</li>
     *   <li>Phóng to cửa sổ trình duyệt để đảm bảo giao diện hiển thị đầy đủ.</li>
     * </ol>
     */
    @BeforeMethod
    public void setUp() {
        // Bước 1: WebDriverManager tự động phát hiện phiên bản Chrome
        //         đang cài trên máy và tải ChromeDriver tương ứng.
        WebDriverManager.chromedriver().setup();

        // Bước 2: Khởi tạo ChromeDriver và lưu vào ThreadLocal.
        driver.set(new ChromeDriver());

        // Bước 3: Phóng to cửa sổ trình duyệt.
        getDriver().manage().window().maximize();
    }

    // -------------------------------------------------------------------------
    // Vòng đời TestNG — Teardown
    // -------------------------------------------------------------------------

    /**
     * Thu dọn tài nguyên sau mỗi ca kiểm thử (@AfterMethod).
     *
     * <p>Quy trình:
     * <ol>
     *   <li>Kiểm tra kết quả ca kiểm thử qua {@link ITestResult}.</li>
     *   <li>Nếu thất bại, gọi {@link #captureScreenshot(ITestResult)} để lưu bằng chứng.</li>
     *   <li>Đóng toàn bộ cửa sổ và kết thúc phiên WebDriver bằng {@code driver.quit()}.</li>
     *   <li>Xóa tham chiếu khỏi ThreadLocal để tránh rò rỉ bộ nhớ.</li>
     * </ol>
     *
     * @param result Đối tượng chứa thông tin kết quả của ca kiểm thử vừa chạy,
     *               được TestNG tự động inject vào tham số.
     */
    @AfterMethod
    public void tearDown(ITestResult result) {
        // Bước 1: Xử lý khi ca kiểm thử thất bại.
        if (result.getStatus() == ITestResult.FAILURE) {
            captureScreenshot(result);
        }

        // Bước 2: Đóng trình duyệt và giải phóng tài nguyên.
        //         driver.quit() đóng tất cả cửa sổ và kết thúc phiên WebDriver.
        WebDriver currentDriver = getDriver();
        if (currentDriver != null) {
            currentDriver.quit();
        }

        // Bước 3: Xóa khỏi ThreadLocal để tránh rò rỉ bộ nhớ (memory leak)
        //         khi chạy trong môi trường thread pool.
        driver.remove();
    }

    // -------------------------------------------------------------------------
    // Phương thức nội bộ — Chụp ảnh màn hình
    // -------------------------------------------------------------------------

    /**
     * Chụp ảnh màn hình và lưu vào thư mục cấu hình sẵn.
     *
     * <p>Tên file ảnh bao gồm: tên phương thức kiểm thử + timestamp,
     * đảm bảo không bao giờ bị ghi đè giữa các lần chạy.
     *
     * <p>Ví dụ tên file: {@code loginWithInvalidPassword_20240715_143022_456.png}
     *
     * @param result Kết quả ca kiểm thử, dùng để lấy tên phương thức.
     */
    private void captureScreenshot(ITestResult result) {
        // Đảm bảo WebDriver còn hoạt động trước khi chụp.
        WebDriver currentDriver = getDriver();
        if (!(currentDriver instanceof TakesScreenshot)) {
            System.err.println("[Screenshot] WebDriver không hỗ trợ chụp ảnh màn hình.");
            return;
        }

        try {
            // Bước 1: Yêu cầu Selenium chụp ảnh màn hình dưới dạng File tạm.
            File tempScreenshot = ((TakesScreenshot) currentDriver)
                    .getScreenshotAs(OutputType.FILE);

            // Bước 2: Tạo thư mục đích nếu chưa tồn tại.
            Path screenshotDir = Paths.get(SCREENSHOT_DIR);
            Files.createDirectories(screenshotDir);

            // Bước 3: Đặt tên file theo dạng: <tênTest>_<timestamp>.png
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String fileName  = result.getMethod().getMethodName()
                               + "_" + timestamp + ".png";
            Path destination = screenshotDir.resolve(fileName);

            // Bước 4: Sao chép file ảnh từ vị trí tạm sang thư mục đích.
            Files.copy(tempScreenshot.toPath(), destination,
                       StandardCopyOption.REPLACE_EXISTING);

            System.out.println("[Screenshot] Đã lưu ảnh thất bại: "
                               + destination.toAbsolutePath());

        } catch (IOException e) {
            // Không ném exception để tránh che khuất lỗi gốc của ca kiểm thử.
            System.err.println("[Screenshot] Không thể lưu ảnh màn hình: "
                               + e.getMessage());
        }
    }
}