package net.fernandosalas.ems;

import net.fernandosalas.ems.service.DepartmentService;
import net.fernandosalas.ems.service.StudentService;
import net.fernandosalas.ems.repository.StudentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class EmsBackendApplicationTests {

    // Inject ApplicationContext để kiểm tra trạng thái khởi động của Spring
    @Autowired
    private ApplicationContext applicationContext;

    // Inject các Bean nghiệp vụ cốt lõi để xác nhận đăng ký thành công
    @Autowired
    private StudentService studentService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private StudentRepository studentRepository;

    // ── Test 1: Xác nhận ApplicationContext khởi động thành công ──────────
    @Test
    void contextLoads() {
        Assertions.assertNotNull(
            applicationContext,
            "ApplicationContext phải được Spring khởi tạo thành công"
        );
    }

    // ── Test 2: Xác nhận các Bean Service được đăng ký đúng ───────────────
    @Test
    void serviceBeansShouldBeRegistered() {
        Assertions.assertAll(
            "Tất cả Service Bean phải tồn tại trong ApplicationContext",

            () -> Assertions.assertNotNull(
                    studentService,
                    "Bean StudentService phải được đăng ký"),

            () -> Assertions.assertNotNull(
                    departmentService,
                    "Bean DepartmentService phải được đăng ký")
        );
    }

    // ── Test 3: Xác nhận Repository Bean được đăng ký đúng ───────────────
    @Test
    void repositoryBeansShouldBeRegistered() {
        Assertions.assertNotNull(
            studentRepository,
            "Bean StudentRepository phải được đăng ký và kết nối datasource"
        );
    }

    // ── Test 4: Xác nhận số lượng Bean tối thiểu đã được nạp ─────────────
    @Test
    void applicationContextShouldContainExpectedBeans() {
        int beanCount = applicationContext.getBeanDefinitionCount();

        Assertions.assertTrue(
            beanCount > 10,
            "ApplicationContext phải chứa ít nhất 10 Bean — " +
            "hiện tại chỉ có: " + beanCount
        );
    }
}
