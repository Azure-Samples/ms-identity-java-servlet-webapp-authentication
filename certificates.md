# Use a certificate instead of a client secret

The Microsoft identity platform allows an application to use its own credentials for authentication anywhere a client secret could be used, for example, in confidential client apps, in the OAuth 2.0 client credentials grant flow, and the on-behalf-of (OBO) flow.

The certificate consists of a private key and a public key. The public key is uploaded to Azure AD, whereas the confidential client applications keep their private keys. There are various options for obtaining certificates. This readme will guide the developer to generate and use a self-signed certificate in confidential client apps.

## Use a self-signed certificate

In order to use certificates, you'll need to:

1. Generate a certificate and export it, if you don't have one already.
1. Register the certificate with your application registration  in the Azure AD portal.
1. Update your application code to utilize the certificate.

### 1. Generate a Self Signed certificate

If you already have valid certificate available, you may skip this step.

<details>
<summary>Click here to use Powershell</summary>

To generate a new self-signed certificate, we will use the [New-SelfSignedCertificate](https://docs.microsoft.com/powershell/module/pkiclient/new-selfsignedcertificate) Powershell command.

1. Open PowerShell and run `New-SelfSignedCertificate` command with the following parameters to create a new self-signed certificate that will be stored in the **current user** certificate store on your computer:

```PowerShell
$cert=New-SelfSignedCertificate -Subject "/CN=webapp" -CertStoreLocation "Cert:\CurrentUser\My"  -KeyExportPolicy Exportable -KeySpec Signature
```

1. Export this certificate using the "Manage User Certificate" MMC snap-in accessible from the Windows Control Panel. You can also add other options to generate the certificate in a different store such as the **Computer** or **service** store (See [How to: View Certificates with the MMC Snap-in](https://docs.microsoft.com/dotnet/framework/wcf/feature-details/how-to-view-certificates-with-the-mmc-snap-in)) for more details.

Export one with private key as webapp.pfx and another as webapp.cer without private key.
</details>

<details>
<summary>Click here to use OpenSSL</summary>

Type the following in a terminal.

```PowerShell
openssl req -x509 -newkey rsa:4096 -sha256 -days 365 -keyout webapp.key -out webapp.cer -nodes -batch

Generating a RSA private key
...........................................................................................................................................................................................................................................................++++
......................................................................................................++++
writing new private key to 'webapp.key'
----- 
```

Generate the webapp.pfx certificate with below command:

```console
openssl pkcs12 -export -out webapp.pfx -inkey webapp.key -in webapp.cer
```

Enter an export password when prompted and make a note of it.

The following files should be generated: `webapp.key`, `webapp.cer` and `webapp.pfx`.
</details>

### 2. Add the certificate to your app registration on Azure portal

1. Navigate back to the [Azure portal](https://portal.azure.com).
1. In the left-hand navigation pane, select the **Azure Active Directory** service, and then select **App registrations**.
1. Select the your application.
1. In the **Certificates & secrets** tab, go to **Certificates** section:
1. Select **Upload certificate** and, in select the browse button on the right to select the certificate you just exported, webapp.cer (or your existing certificate).
1. Select **Add**.

### 3. Update your application configuration and code

In the configuration file of your web app:

Add a `pfx_path` key and set its value to your pfx location on disk.
Add a `pfx_password` key and set its value to your pfx password.

1. Open your code editor and find the initialization of your confidential client. (In Java servlet app, this is in `src/main/java/.../msal4j/helpers/AuthHelper.java`)
2. Replace the `secret` variable assignment as follows:

    ```java
    final IClientSecret secret = ClientCredentialFactory.createFromSecret(Config.SECRET);
    confClientInstance = ConfidentialClientApplication.builder(Config.CLIENT_ID, secret)
                    .authority(Config.AUTHORITY).build();
    ```

    ```java
    final IClientCertificate secret = ClientCredentialFactory.createFromCertificate(ClassLoader.getResourceAsStream(Config.getProperty("pfx_path")), Config.getProperty("pfx_password"));
    confClientInstance = ConfidentialClientApplication.builder(Config.CLIENT_ID, secret)
                    .authority(Config.AUTHORITY).build();
    ```