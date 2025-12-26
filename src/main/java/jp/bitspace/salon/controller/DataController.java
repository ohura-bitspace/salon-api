package jp.bitspace.salon.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jp.bitspace.salon.dto.response.CustomerResponse;
import jp.bitspace.salon.model.Salon;
import jp.bitspace.salon.model.Staff;
import jp.bitspace.salon.service.CustomerService;
import jp.bitspace.salon.service.SalonService;
import jp.bitspace.salon.service.StaffService;

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
    
    /**
     * 顧客リスト取得.
     * @return 顧客リスト（最小限の情報のみ）
     */
    @GetMapping("/customers")
    public List<CustomerResponse> getAllCustomers() {
        return customerService.findAllAsResponse();
    }
}
