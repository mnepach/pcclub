    package com.pcclub.repository;

    import org.springframework.data.jpa.repository.JpaRepository;
    import com.pcclub.model.WorkStation;

    import java.util.List;

    public interface WorkStationRepository extends JpaRepository<WorkStation, Long> {
        List<WorkStation> findByAvailable(boolean available);
    }