package com.hichamzouagui.energymanager.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** Tests de la lecture des saisies numériques et de l'aller-retour avec l'affichage. */
public class NumbersTest {

    private static final double DELTA = 1e-9;

    @Test
    public void commaAndDotAreBothAccepted() {
        assertEquals(2.5, Numbers.parse("2,5", -1), DELTA);
        assertEquals(2.5, Numbers.parse("2.5", -1), DELTA);
    }

    @Test
    public void spacesIncludingNonBreakingOnesAreIgnored() {
        assertEquals(2000.0, Numbers.parse("2 000", -1), DELTA);
        assertEquals(2000.0, Numbers.parse("2 000", -1), DELTA);
        assertEquals(42.0, Numbers.parse("  42 ", -1), DELTA);
    }

    @Test
    public void unusableInputFallsBackToTheDefaultValue() {
        assertEquals(-1, Numbers.parse("", -1), DELTA);
        assertEquals(-1, Numbers.parse(null, -1), DELTA);
        assertEquals(-1, Numbers.parse("abc", -1), DELTA);
        assertEquals(0, Numbers.parse("   ", 0), DELTA);
    }

    /** Un champ prérempli doit pouvoir être relu sans perte : pas de séparateur de milliers. */
    @Test
    public void prefilledFieldsCanBeReadBack() {
        double[] values = {0.2516, 13.09, 2000, 1.5, 123456.78, 0};
        for (double value : values) {
            assertEquals(value, Numbers.parse(Formatters.input(value), -999), DELTA);
        }
    }
}
