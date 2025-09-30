package ar.edu.utn.dds.k3003;

import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;
import ar.edu.utn.dds.k3003.repository.PdiRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final PdiRepository pdiRepository;

    public DataLoader(PdiRepository pdiRepository) {
        this.pdiRepository = pdiRepository;
    }

    @Override
    public void run(String... args) throws Exception {
         PiezaDeInformacion pdi1_asociado_hecho01 = new PiezaDeInformacion(
                "Hecho01",
                "Incendio en Sede Medrano",
                "CABA",
                LocalDateTime.of(2025, 9, 9, 15, 30),
                "Mucho humo en Av Medrano y Tucuman"
                ,"https://www.example.com/imagen1.jpg"
        );

        PiezaDeInformacion pdi2_asociado_hecho01 = new PiezaDeInformacion(
                "Hecho01",
                "Incendio en Sede Medrano",
                "CABA",
                LocalDateTime.of(2025, 9, 9, 16, 30),
                "Corte de calle en Av Cordoba"
                ,"https://www.example.com/imagen2.jpg"
        );

        PiezaDeInformacion pdi3_asociado_hecho02 = new PiezaDeInformacion(
                "Hecho02",
                "Corte de luz en Sede Lugano",
                "CABA",
                LocalDateTime.of(2025, 9, 1, 16, 30),
                "se corto la loooz"
                ,"https://www.example.com/imagen3.jpg"
        );

        pdiRepository.save(pdi1_asociado_hecho01);
        pdiRepository.save(pdi2_asociado_hecho01);
        pdiRepository.save(pdi3_asociado_hecho02);
        System.out.println("Datos de prueba cargados exitosamente");

         
    }
}
