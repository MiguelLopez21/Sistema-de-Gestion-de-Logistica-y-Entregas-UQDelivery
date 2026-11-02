package co.edu.uniquindio.uqdelivery.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgrupacionPeriodoTest {

    @Test
    void diaInicioEsLaMismaFechaYSiguienteEsElDiaPosterior() {
        LocalDate fecha = LocalDate.of(2026, 10, 15);

        assertEquals(fecha, AgrupacionPeriodo.DIA.inicio(fecha));
        assertEquals(LocalDate.of(2026, 10, 16), AgrupacionPeriodo.DIA.siguiente(fecha));
    }

    @Test
    void semanaInicioEsElLunesAnterior() {
        assertEquals(LocalDate.of(2026, 9, 28), AgrupacionPeriodo.SEMANA.inicio(LocalDate.of(2026, 10, 1)));
        assertEquals(LocalDate.of(2026, 10, 5), AgrupacionPeriodo.SEMANA.inicio(LocalDate.of(2026, 10, 5)));
        assertEquals(LocalDate.of(2026, 10, 5), AgrupacionPeriodo.SEMANA.inicio(LocalDate.of(2026, 10, 11)));
    }

    @Test
    void semanaSiguienteEsSieteDiasDespues() {
        assertEquals(LocalDate.of(2026, 10, 12), AgrupacionPeriodo.SEMANA.siguiente(LocalDate.of(2026, 10, 5)));
    }

    @Test
    void mesInicioEsElPrimerDiaDelMes() {
        assertEquals(LocalDate.of(2026, 10, 1), AgrupacionPeriodo.MES.inicio(LocalDate.of(2026, 10, 31)));
    }

    @Test
    void mesSiguienteEsElPrimerDiaDelMesPosterior() {
        assertEquals(LocalDate.of(2026, 11, 1), AgrupacionPeriodo.MES.siguiente(LocalDate.of(2026, 10, 1)));
        assertEquals(LocalDate.of(2027, 1, 1), AgrupacionPeriodo.MES.siguiente(LocalDate.of(2026, 12, 1)));
    }
}
