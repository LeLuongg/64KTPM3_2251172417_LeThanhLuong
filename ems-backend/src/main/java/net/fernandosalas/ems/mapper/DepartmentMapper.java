package net.fernandosalas.ems.mapper;

import net.fernandosalas.ems.dto.DepartmentDto;
import net.fernandosalas.ems.entity.Department;

public class DepartmentMapper {
    private DepartmentMapper() {
        // Utility Class – không cho phép khởi tạo
        throw new UnsupportedOperationException(
            "DepartmentMapper là Utility Class, không thể khởi tạo đối tượng."
        );
    }

    public static DepartmentDto mapToDepartmentDto(Department department) {
        return new DepartmentDto(
                department.getId(),
                department.getDepartmentName(),
                department.getDepartmentDescription()
        );
    }

    public static Department mapToDepartment(DepartmentDto departmentDto) {
        return new Department(
                departmentDto.getId(),
                departmentDto.getDepartmentName(),
                departmentDto.getDepartmentDescription()
        );
    }
}
