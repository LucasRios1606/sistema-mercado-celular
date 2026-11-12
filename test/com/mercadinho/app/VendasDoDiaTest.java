package com.mercadinho.app;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.TimeZone;

public final class VendasDoDiaTest {
    private static void conferir(boolean condicao) {
        if (!condicao) throw new AssertionError();
    }

    private static void conferirDia(String data, String fuso, int horas) {
        LocalDate dia = LocalDate.parse(data);
        ZoneId zona = ZoneId.of(fuso);
        long inicio = dia.atStartOfDay(zona).toInstant().toEpochMilli();
        long fim = dia.plusDays(1).atStartOfDay(zona).toInstant().toEpochMilli();
        long meioDia = dia.atTime(12, 0).atZone(zona).toInstant().toEpochMilli();
        VendasDoDia resumo = new VendasDoDia(meioDia, TimeZone.getTimeZone(fuso));
        conferir(resumo.inicio == inicio && resumo.fim == fim);
        conferir(!resumo.contem(inicio - 1));
        conferir(resumo.contem(inicio));
        conferir(resumo.contem(fim - 1));
        conferir(!resumo.contem(fim));
        conferir(fim - inicio == horas * 60L * 60 * 1000);
    }

    public static void main(String[] args) {
        conferirDia("2026-11-12", "America/Sao_Paulo", 24);
        conferirDia("2026-12-31", "America/Sao_Paulo", 24);
        conferirDia("2028-02-29", "America/Sao_Paulo", 24);
        conferirDia("2026-03-08", "America/New_York", 23);
        conferirDia("2026-11-01", "America/New_York", 25);
        conferirDia("2018-11-04", "America/Sao_Paulo", 23);
        conferirDia("2026-11-12", "Asia/Tokyo", 24);

        VendasDoDia resumo = new VendasDoDia(0, TimeZone.getTimeZone("UTC"));
        conferir(resumo.total() == 0 && resumo.quantidade() == 0);
        resumo.adicionar(1948);
        resumo.adicionar(750);
        resumo.adicionar(0);
        conferir(resumo.total() == 2698 && resumo.quantidade() == 3);
        try {
            resumo.adicionar(-1);
            throw new AssertionError("Aceitou valor negativo");
        } catch (IllegalArgumentException esperado) { }
        try {
            resumo.adicionar(Long.MAX_VALUE);
            throw new AssertionError("Aceitou soma acima do limite");
        } catch (IllegalArgumentException esperado) { }
        conferir(resumo.total() == 2698 && resumo.quantidade() == 3);
        System.out.println("OK: filtro por dia, fusos, meia-noite, horario de verao, soma e limites.");
    }
}
