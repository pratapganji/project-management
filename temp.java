https://github.com/jasypt/jasypt/releases/download/jasypt-1.9.3/jasypt-1.9.3-dist.zip


java -cp jasypt-1.9.3.jar \
  org.jasypt.intf.cli.JasyptPBEStringEncryptionCLI \
  input="changeit" \
  password="d7+t6OIhoif3G10hWHXj0VZeo0Nbtsu7NB7MitaUEtw=" \
  algorithm=PBEWITHHMACSHA512ANDAES_256