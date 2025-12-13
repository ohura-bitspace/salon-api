package jp.bitspace.salon.service;

import jp.bitspace.salon.model.Salon;
import jp.bitspace.salon.repository.SalonRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SalonService {
    private final SalonRepository salonRepository;

    public SalonService(SalonRepository salonRepository) {
        this.salonRepository = salonRepository;
    }

    public List<Salon> findAll() {
        return salonRepository.findAll();
    }

    public Optional<Salon> findById(Long id) {
        return salonRepository.findById(id);
    }

    public Salon save(Salon salon) {
        return salonRepository.save(salon);
    }

    public void deleteById(Long id) {
        salonRepository.deleteById(id);
    }
}
