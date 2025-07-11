package com.chillguy.tiny.blood.entity;

import java.util.List;

public enum BloodCompatibility {
    O_NEG("O-", List.of("O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+")),
    O_POS("O+", List.of("O+", "A+", "B+", "AB+")),
    A_NEG("A-", List.of("A-", "A+", "AB-", "AB+")),
    A_POS("A+", List.of("A+", "AB+")),
    B_NEG("B-", List.of("B-", "B+", "AB-", "AB+")),
    B_POS("B+", List.of("B+", "AB+")),
    AB_NEG("AB-", List.of("AB-", "AB+")),
    AB_POS("AB+", List.of("AB+"));

    private final String bloodCode;
    private final List<String> canDonateTo;

    BloodCompatibility(String bloodCode, List<String> canDonateTo) {
        this.bloodCode = bloodCode;
        this.canDonateTo = canDonateTo;
    }

    public List<String> getCanDonateTo() {
        return canDonateTo;
    }

    public static List<String> getCompatibleRecipients(String donorCode) {
        for (BloodCompatibility match : values()) {
            if (match.bloodCode.equalsIgnoreCase(donorCode)) {
                return match.getCanDonateTo();
            }
        }
        return List.of();
    }

    public static boolean canDonateTo(String donorCode, String recipientCode) {
        return getCompatibleRecipients(donorCode).contains(recipientCode);
    }
}
