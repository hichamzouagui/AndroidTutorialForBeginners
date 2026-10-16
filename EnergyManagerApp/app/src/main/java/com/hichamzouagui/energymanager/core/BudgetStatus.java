package com.hichamzouagui.energymanager.core;

/** Position de la facture projetée par rapport au budget fixé par l'utilisateur. */
public enum BudgetStatus {

    /** Pas de budget défini : rien à comparer. */
    NOT_SET,
    /** Moins de 90 % du budget consommé. */
    UNDER,
    /** Entre 90 % et 100 % du budget : alerte. */
    NEAR,
    /** Budget dépassé. */
    OVER
}
