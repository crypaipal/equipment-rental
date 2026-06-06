package pl.pwr.miasi.equipmentrental.identity.application.port.out;

public interface PasswordHasher {

    String hash(String rawPassword);
}