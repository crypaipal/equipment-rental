package pl.pwr.miasi.equipmentrental.inventory.domain;

public class AssetConditionRule {

    public boolean canBeAssignedToRent(AssetCondition condition) {
        return condition == AssetCondition.OPERATIONAL;
    }
}