package co.edu.uniquindio.uqdelivery.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public enum AgrupacionPeriodo {

    DIA {
        @Override
        public LocalDate inicio(LocalDate fecha) {
            return fecha;
        }

        @Override
        public LocalDate siguiente(LocalDate inicioPeriodo) {
            return inicioPeriodo.plusDays(1);
        }
    },

    SEMANA {
        @Override
        public LocalDate inicio(LocalDate fecha) {
            return fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        }

        @Override
        public LocalDate siguiente(LocalDate inicioPeriodo) {
            return inicioPeriodo.plusWeeks(1);
        }
    },

    MES {
        @Override
        public LocalDate inicio(LocalDate fecha) {
            return fecha.withDayOfMonth(1);
        }

        @Override
        public LocalDate siguiente(LocalDate inicioPeriodo) {
            return inicioPeriodo.plusMonths(1);
        }
    };

    public abstract LocalDate inicio(LocalDate fecha);

    public abstract LocalDate siguiente(LocalDate inicioPeriodo);
}
