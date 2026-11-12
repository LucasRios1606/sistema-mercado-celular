package com.mercadinho.app;

import java.util.Calendar;
import java.util.TimeZone;

/** Limites do dia no fuso do celular e soma exata dos valores em centavos. */
final class VendasDoDia {
    final long inicio;
    final long fim;
    private long total;
    private int quantidade;

    VendasDoDia(long agora, TimeZone fuso) {
        Calendar dia = Calendar.getInstance(fuso);
        dia.setTimeInMillis(agora);
        dia.set(Calendar.HOUR_OF_DAY, 0);
        dia.set(Calendar.MINUTE, 0);
        dia.set(Calendar.SECOND, 0);
        dia.set(Calendar.MILLISECOND, 0);
        inicio = dia.getTimeInMillis();
        dia.add(Calendar.DAY_OF_MONTH, 1);
        // Reajusta o horário após dias cuja meia-noite foi pulada pelo horário de verão.
        dia.set(Calendar.HOUR_OF_DAY, 0);
        fim = dia.getTimeInMillis();
    }

    boolean contem(long data) { return data >= inicio && data < fim; }

    void adicionar(long valor) {
        if (valor < 0 || total > Long.MAX_VALUE - valor) throw new IllegalArgumentException();
        total += valor;
        quantidade++;
    }

    long total() { return total; }
    int quantidade() { return quantidade; }
}
