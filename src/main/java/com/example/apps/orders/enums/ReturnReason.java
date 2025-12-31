package com.example.apps.orders.enums;

public enum ReturnReason {
    DEFECTIVE("Ayıplı/Kusurlu Ürün"),
    WRONG_ITEM("Yanlış Ürün"),
    CHANGED_MIND("Vazgeçtim/Beğenmedim"),
    SIZE_MISMATCH("Beden Uymadı"),
    OTHER("Diğer");

    private final String label;

    ReturnReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
