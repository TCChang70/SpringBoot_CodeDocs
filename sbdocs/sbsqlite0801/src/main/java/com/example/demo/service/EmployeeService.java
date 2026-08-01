package com.example.demo.service;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service  // 標記為服務元件，Spring 會自動管理此類別的實例
public class EmployeeService  implements CommandLineRunner{

    // 透過建構子注入（Constructor Injection），這是 Spring 官方推薦的注入方式
//    private final EmployeeRepository employeeRepository;
//
//    public EmployeeService(EmployeeRepository employeeRepository) {
//        this.employeeRepository = employeeRepository;
//    }
	
    @Autowired 
	private EmployeeRepository employeeRepository;
    // 查詢所有員工
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // 依 id 查詢單筆（回傳 Optional，讓呼叫者自行處理找不到的情況）
    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }
    public List<Employee> findByDepartment(String department) {
		return employeeRepository.findByDepartment(department);
	}
    
    public Employee findByEmail(String email) {
    			return employeeRepository.findByEmail(email);
    }
    // 新增員工
    public Employee create(Employee employee) {
        return employeeRepository.save(employee);
    }

    // 修改員工資料（先確認是否存在，再更新）
    public Optional<Employee> update(Long id, Employee updatedEmployee) {
        return employeeRepository.findById(id).map(existing -> {
            existing.setName(updatedEmployee.getName());
            existing.setEmail(updatedEmployee.getEmail());
            existing.setDepartment(updatedEmployee.getDepartment());
            existing.setSalary(updatedEmployee.getSalary());
            return employeeRepository.save(existing); // save 有 id → UPDATE
        });
    }

    // 刪除員工（回傳 boolean 告知呼叫者是否成功）
    public boolean delete(Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return true;
        }
        return false;
    }

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		if(employeeRepository.count()==0) {
			employeeRepository.save(new Employee("王瑪莉","mary@demo.com","HR",45000.0));
			employeeRepository.save(new Employee("李小華","lee@demo.com","Finance",65000.0));
			employeeRepository.save(new Employee("吳大力","wu@demo.com","HR",46000.0));
		}
	}
}
