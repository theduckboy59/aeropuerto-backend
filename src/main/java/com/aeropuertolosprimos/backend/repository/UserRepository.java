package com.aeropuertolosprimos.backend.repository;

import com.aeropuertolosprimos.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // Indica que es un componente de acceso a datos (Spring lo detecta automáticamente)
public interface UserRepository extends JpaRepository<User, Integer> {
    // Extiende JpaRepository:
    // - User → entidad que maneja
    // - Integer → tipo de la clave primaria

    // Hereda métodos CRUD automáticamente:
    // save(), findById(), findAll(), deleteById(), etc.

    // Aquí puedes definir métodos personalizados si los necesitas
}