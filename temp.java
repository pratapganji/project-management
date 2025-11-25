https://github.com/ulisesbocchio/jasypt-spring-boot/releases/download/3.0.5/jasypt-3.0.5-dist.zip


java -cp jasypt-1.9.3.jar \
  org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
  input="changeit" \
  password="d7+t6OIhoif3G10hWHXj0VZeo0Nbtsu7NB7MitaUEtw=" \
  algorithm=PBEWITHHMACSHA512ANDAES_128