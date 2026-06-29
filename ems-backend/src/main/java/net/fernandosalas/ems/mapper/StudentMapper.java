package net.fernandosalas.ems.mapper;
import net.fernandosalas.ems.dto.StudentDto;
import net.fernandosalas.ems.entity.Student;

public class StudentMapper {
    private StudentMapper() {
        // Utility Class – không cho phép khởi tạo
        throw new UnsupportedOperationException(
            "StudentMapper là Utility Class, không thể khởi tạo đối tượng."
        );
    }

    public static StudentDto mapToStudentDto(Student student) {
        return new StudentDto(
            student.getId(),
            student.getFirstName(),
            student.getLastName(),
            student.getEmail(), 
            student.getDepartment() != null ? student.getDepartment().getId() : null
        );
    }

    public static Student mapToStudent(StudentDto studentDto) {
        return new Student(
            studentDto.getId(),
            studentDto.getFirstName(),
            studentDto.getLastName(),
            studentDto.getEmail(),
            null
        );
    }
}
