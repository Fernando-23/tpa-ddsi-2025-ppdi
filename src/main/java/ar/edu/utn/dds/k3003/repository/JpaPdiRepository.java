package ar.edu.utn.dds.k3003.repository;

import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("!test")
public interface JpaPdiRepository extends JpaRepository<PiezaDeInformacion, Integer>, PdiRepository {

    @Override
    default Optional<PiezaDeInformacion> get(int id) {
        return findById(id);
    }

    @Query("SELECT p FROM PiezaDeInformacion p WHERE p.hechoId = :hechoId")
    List<PiezaDeInformacion> listByHechoId(@Param("hechoId") String hechoId);

    @Query("SELECT e.Texto FROM EtiquetaXPdi ep JOIN ep.etiqueta e WHERE ep.pdi.id = :pdiId")
    List<String> listEtiquetas(@Param("pdiId") int pdiId);

    @Transactional
    default void addEtiquetas(PiezaDeInformacion pdi, List<String> etiquetas){};

    @Query("SELECT DISTINCT p FROM PiezaDeInformacion p LEFT JOIN FETCH p.etiquetas ep LEFT JOIN FETCH ep.etiqueta")
    List<PiezaDeInformacion> findAllWithEtiquetas();
    //@Modifying
    //@Query(value = "ALTER TABLE pieza_de_informacion AUTO_INCREMENT = 1", nativeQuery = true)
    //void resetAutoIncrement();
}