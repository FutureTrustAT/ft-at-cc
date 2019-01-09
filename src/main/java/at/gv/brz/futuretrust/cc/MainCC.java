/**
 * Copyright (C) 2019 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.gv.brz.futuretrust.cc;

import java.io.File;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.file.FileOperations;
import com.helger.commons.string.StringHelper;
import com.helger.security.keystore.EKeyStoreType;
import com.helger.security.keystore.KeyStoreHelper;
import com.helger.security.keystore.LoadedKey;
import com.helger.security.keystore.LoadedKeyStore;
import com.helger.settings.exchange.configfile.ConfigFile;
import com.helger.settings.exchange.configfile.ConfigFileBuilder;

/**
 * The main class for the FutureTrust AT commandline client
 *
 * @author Philip Helger
 */
public final class MainCC
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainCC.class);
  private static final String APP_NAME = "ft-at-cc v0.1";
  private static final Locale LOCALE = Locale.US;

  private static void _fatalError (@Nonnull final String sMsg)
  {
    throw new IllegalStateException (sMsg);
  }

  @Nonnull
  private static File _checkExistingDir (@Nonnull final ConfigFile aCF, @Nullable final String sKey)
  {
    final String sDirName = aCF.getAsString (sKey);
    if (StringHelper.hasNoText (sDirName))
      _fatalError ("The configuration file entry '" + sKey + "' is missing or empty");

    final File ret = new File (sDirName).getAbsoluteFile ();
    if (!ret.isDirectory ())
    {
      // Create if necessary
      FileOperations.createDirRecursiveIfNotExisting (ret);

      // Check again
      if (!ret.isDirectory ())
      {
        _fatalError ("The configuration file entry '" +
                     sKey +
                     "' points to a non-directory: " +
                     ret.getAbsolutePath ());
      }
    }
    return ret;
  }

  private static void _signAndSendInvoices (@Nonnull final KeyStore aKeyStore,
                                            @Nonnull final PrivateKey aPrivateKey,
                                            @Nonnull final X509Certificate aCertificate,
                                            @Nonnull final File aDirOutgoing,
                                            @Nonnull final File aDirResponseSuccess,
                                            @Nonnull final File aDirResponseError)
  {
    // XMLDSigHandler.createVerifyRequest (aInvoice, aSignature)
  }

  public static void main (final String [] args)
  {
    LOGGER.info ("Starting " + APP_NAME);

    // Load configuration file
    final ConfigFile aCF = new ConfigFileBuilder ().addPathFromEnvVar ("FT_AT_CONFIG")
                                                   .addPathFromSystemProperty ("ft-at-cc-configuration-file")
                                                   .addPath ("private-config.properties")
                                                   .addPath ("config.properties")
                                                   .build ();
    if (!aCF.isRead ())
      _fatalError ("Failed to resolve configuration file");
    LOGGER.info ("Loaded configuration file '" + aCF.getReadResource ().getPath () + "'");

    // Load keystore
    final EKeyStoreType eKSType = EKeyStoreType.getFromIDCaseInsensitiveOrDefault (aCF.getAsString ("keystore.type"),
                                                                                   EKeyStoreType.JKS);
    final String sKeystorePath = aCF.getAsString ("keystore.path");
    final LoadedKeyStore aLKS = KeyStoreHelper.loadKeyStore (eKSType,
                                                             sKeystorePath,
                                                             aCF.getAsString ("keystore.password"));
    if (aLKS.isFailure ())
      _fatalError (aLKS.getErrorText (LOCALE));
    LOGGER.info ("Successfully loaded keystore file '" + sKeystorePath + "'");

    // Load key from keystore
    final String sKeystoreKeyAlias = aCF.getAsString ("keystore.key.alias");
    final LoadedKey <KeyStore.PrivateKeyEntry> aLPK = KeyStoreHelper.loadPrivateKey (aLKS.getKeyStore (),
                                                                                     sKeystorePath,
                                                                                     sKeystoreKeyAlias,
                                                                                     aCF.getAsCharArray ("keystore.key.password"));
    if (aLPK.isFailure ())
      _fatalError ("Failed to load key '" +
                   sKeystoreKeyAlias +
                   "' from keystore '" +
                   sKeystorePath +
                   "': " +
                   aLPK.getErrorText (LOCALE));
    LOGGER.info ("Successfully loaded key '" + sKeystoreKeyAlias + "' from keystore '" + sKeystorePath + "'");

    // Check directories
    final File aDirOutgoing = _checkExistingDir (aCF, "directory.outgoing");
    LOGGER.info ("Outgoing directory is '" + aDirOutgoing.getAbsolutePath () + "'");

    final File aDirResponseSuccess = _checkExistingDir (aCF, "directory.response.success");
    LOGGER.info ("Response success directory is '" + aDirResponseSuccess.getAbsolutePath () + "'");

    final File aDirResponseError = _checkExistingDir (aCF, "directory.response.error");
    LOGGER.info ("Response error directory is '" + aDirResponseError.getAbsolutePath () + "'");

    // Go go go
    _signAndSendInvoices (aLKS.getKeyStore (),
                          aLPK.getKeyEntry ().getPrivateKey (),
                          (X509Certificate) aLPK.getKeyEntry ().getCertificate (),
                          aDirOutgoing,
                          aDirResponseSuccess,
                          aDirResponseError);

    // Done
    LOGGER.info ("Finished " + APP_NAME);
  }
}
