package pl.pwr.miasi.equipmentrental.inventory.application.command;

public record RegisterEquipmentModelCommand(
        String name,
        String category,
        String manufacturer
) {
}