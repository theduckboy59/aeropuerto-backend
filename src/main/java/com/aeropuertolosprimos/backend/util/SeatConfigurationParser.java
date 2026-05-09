package com.aeropuertolosprimos.backend.util;

import com.aeropuertolosprimos.backend.dto.ModeloAvionPreviewResponse;
import com.aeropuertolosprimos.backend.dto.SeatLevel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class SeatConfigurationParser {

    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public ModeloAvionPreviewResponse parse(
            String configuracion,
            Integer niveles,
            Integer totalColumnas,
            Integer pasillos
    ) {

        String[] blocks = configuracion.split("-");

        int calculatedColumns = Arrays.stream(blocks)
                .mapToInt(Integer::parseInt)
                .sum();

        if (calculatedColumns != totalColumnas) {

            throw new RuntimeException(
                    "La suma de configuración no coincide con totalColumnas"
            );
        }

        int calculatedPasillos = blocks.length - 1;

        if (calculatedPasillos != pasillos) {

            throw new RuntimeException(
                    "La cantidad de pasillos no coincide con la configuración"
            );
        }

        List<List<String>> bloques = new ArrayList<>();

        int currentIndex = 0;

        for (String block : blocks) {

            int size = Integer.parseInt(block);

            List<String> columns = new ArrayList<>();

            for (int i = 0; i < size; i++) {

                columns.add(
                        String.valueOf(
                                LETTERS.charAt(currentIndex++)
                        )
                );
            }

            bloques.add(columns);
        }

        List<SeatLevel> nivelesResponse = new ArrayList<>();

        for (int nivel = 1; nivel <= niveles; nivel++) {

            nivelesResponse.add(
                    SeatLevel.builder()
                            .nivel(nivel)
                            .bloques(bloques)
                            .build()
            );
        }

        return ModeloAvionPreviewResponse.builder()
                .niveles(nivelesResponse)
                .totalColumnas(totalColumnas)
                .pasillos(pasillos)
                .build();
    }
}