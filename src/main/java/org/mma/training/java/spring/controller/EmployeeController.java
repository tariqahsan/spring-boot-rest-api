package org.mma.training.java.spring.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.mma.training.java.spring.model.Employee;
import org.mma.training.java.spring.repository.EmployeeRepository;
import org.mma.training.java.spring.util.ResponseMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("api/v1/employees")
public class EmployeeController {
	
	@Autowired
	private EmployeeRepository employeeRepository;
	
	@GetMapping("/get")
	public List<Employee> getAllEmployees() {
		return employeeRepository.findAll();
	}
	
	@GetMapping("/get/{id}")
	public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") Long empId) {
		
		Optional<Employee> employee = employeeRepository.findById(empId);
		
		if(employee.isPresent()) {
			return new ResponseEntity<Employee>(employee.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		
	}
	
	@PostMapping("/add")
	public ResponseEntity<Employee> addEmployee(@RequestBody Employee employee) {
		return new ResponseEntity<Employee>(employeeRepository.save(employee), HttpStatus.CREATED);
		
	}
	
	@PostMapping("/add/file")
	public ResponseEntity<List<Employee>> uploadFileData(@RequestParam("file") MultipartFile file) {
		
		// Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        System.out.println("File Name : " + fileName);

		// ObjectMapper provides functionality to read/write JSON to/from POJO
		ObjectMapper mapper = new ObjectMapper();
		// At runtime TypeReference which will preserve data so Jackson can Deserialize the JSON String into the correct Employee class.
		TypeReference<List<Employee>> typeReference = new TypeReference<List<Employee>>(){};
		
		// Need to prefix "/" to the file name for getResourceAsStream to locate the physical file
		InputStream inputStream = TypeReference.class.getResourceAsStream("/" + fileName);
		
		try {
			List<Employee> employees = mapper.readValue(inputStream, typeReference);
			employeeRepository.saveAll(employees);
			
			System.out.println("Employee data list saved!");
			return new ResponseEntity<List<Employee>>(employees, HttpStatus.OK);
		    //return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
		} catch (IOException e){
			
			System.out.println("Unable to save employee data: " + e.getMessage());
			//message = "Could not upload the file: " + file.getOriginalFilename() + "!";
		    return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
		}
	}
	
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Employee> deleteEmployeeById(@PathVariable("id") Long empId) {
		
		Optional<Employee> employee = employeeRepository.findById(empId);
		
		if(employee.isPresent()) {
			employeeRepository.deleteById(empId);
			return new ResponseEntity<>(null, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		
	}
	
	@PutMapping("/update/{id}")
	public ResponseEntity<Employee> updatemployeeById(@PathVariable("id") Long empId, @RequestBody Employee employeeDetail) {
		
		Optional<Employee> employee = employeeRepository.findById(empId);
		
		if(employee.isPresent()) {
			employeeDetail.setId(employee.get().getId());
			BeanUtils.copyProperties(employee, employeeDetail  );
			employeeRepository.save(employeeDetail);
			return new ResponseEntity<>(null, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		
	}
}
