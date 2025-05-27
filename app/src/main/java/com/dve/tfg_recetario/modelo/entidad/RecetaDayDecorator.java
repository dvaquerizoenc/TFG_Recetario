package com.dve.tfg_recetario.modelo.entidad;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.dve.tfg_recetario.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.HashSet;
import java.util.Set;

import lombok.Setter;

public class RecetaDayDecorator implements DayViewDecorator {

    @Setter
    private int color;
    @Setter
    private final HashSet<CalendarDay> fechasDecoradas;

    public RecetaDayDecorator(Set<CalendarDay> fechas, int color) {
        this.fechasDecoradas = new HashSet<>(fechas);
        this.color = color;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return fechasDecoradas.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(8, color));
    }
}
