Recently, I was frustrated while trying to package a Java keystore file from my Maven project to a project archive (tar or zip) using assembly.xml. Somehow, when I used the command: keytool -keystore cacerts -list on the certificate keystore inside the tar/zip archive, it would just say that the keystore is invalid. After some search, I found that there is a little setting in the pom.xml which instructs maven not to do any filtering tasks on the files you specify when you execute a maven command.