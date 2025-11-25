import org.jasypt.util.text.AES256TextEncryptor;

public static void main(String[] args) {

    // TEMP ENCRYPTION
    AES256TextEncryptor encryptor = new AES256TextEncryptor();
    encryptor.setPassword("d7+t6OIhoif3G10hWHXj0VZeo0Nbtsu7NB7MitaUEtw="); // your key
    String encrypted = encryptor.encrypt("changeit"); // your real JKS password
    System.out.println("ENC(" + encrypted + ")");

    if (true) return; // stop app after printing encrypted text

    SpringApplication.run(CrmDataIngestToOlympusApplication.class, args);
}