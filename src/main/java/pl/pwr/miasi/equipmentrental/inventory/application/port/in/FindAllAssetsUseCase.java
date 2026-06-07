package pl.pwr.miasi.equipmentrental.inventory.application.port.in;

import pl.pwr.miasi.equipmentrental.inventory.application.result.AssetResult;

import java.util.List;

public interface FindAllAssetsUseCase {

    List<AssetResult> findAll();
}