package jp.bitspace.salon.controller;

import jp.bitspace.salon.model.Customer;
import jp.bitspace.salon.model.Salon;
import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.service.CustomerService;
import jp.bitspace.salon.service.SalonService;
import jp.bitspace.salon.service.StaffService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DataController {
    private final SalonService salonService;
    private final StaffService staffService;
    private final CustomerService customerService;

    public DataController(SalonService salonService, StaffService staffService, CustomerService customerService) {
        this.salonService = salonService;
        this.staffService = staffService;
        this.customerService = customerService;
    }

    @GetMapping("/salons")
    public List<Salon> getAllSalons() {
        return salonService.findAll();
    }

    @GetMapping("/staffs")
    public List<Staff> getAllStaffs() {
        return staffService.findAll();
    }

    @GetMapping("/customers")
    public List<Customer> getAllCustomers() {
        return customerService.findAll();
    }
}
