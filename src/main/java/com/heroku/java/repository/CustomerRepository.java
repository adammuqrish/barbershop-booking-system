package com.heroku.java.repository;

import com.heroku.java.model.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {
    Optional<Customer> findByCustEmail(String custEmail);

    // ✅ WAJIB TAMBAH @Query dengan JOIN
    // Kita join Appointment untuk cari customer yang pernah booking dengan barber
    // ini
    @Query("SELECT DISTINCT c FROM Customer c " +
            "JOIN Appointment a ON c.custId = a.custId " +
            "WHERE a.barberId = :staffId")
    List<Customer> findCustomersByStaffId(@Param("staffId") Long staffId);

    Customer findByCustPhoneNumber(String custPhoneNumber);
}