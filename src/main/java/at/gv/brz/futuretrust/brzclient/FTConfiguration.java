/**
 * Copyright (C) 2018-2019 BRZ
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
package at.gv.brz.futuretrust.brzclient;

import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.exception.InitializationException;
import com.helger.security.keystore.EKeyStoreType;
import com.helger.security.keystore.KeyStoreHelper;
import com.helger.security.keystore.LoadedKey;
import com.helger.security.keystore.LoadedKeyStore;
import com.helger.settings.exchange.configfile.ConfigFile;
import com.helger.settings.exchange.configfile.ConfigFileBuilder;

public class FTConfiguration
{
  private static final Logger LOGGER = LoggerFactory.getLogger (FTConfiguration.class);
  private static final ConfigFile CONFIG_FILE;
  private static final KeyStore KEY_STORE;
  private static final PrivateKeyEntry KEY;

  static
  {
    // Load configuration file
    CONFIG_FILE = new ConfigFileBuilder ().addPathFromEnvVar ("FT_AT_CONFIG")
                                          .addPathFromSystemProperty ("ft-at-cc-configuration-file")
                                          .addPath ("private-config.properties")
                                          .addPath ("config.properties")
                                          .build ();
    if (!CONFIG_FILE.isRead ())
      throw new InitializationException ("Failed to resolve configuration file");
    LOGGER.info ("Loaded configuration file '" + CONFIG_FILE.getReadResource ().getPath () + "'");

    // Load keystore
    final EKeyStoreType eKSType = EKeyStoreType.getFromIDCaseInsensitiveOrDefault (CONFIG_FILE.getAsString ("keystore.type"),
                                                                                   EKeyStoreType.JKS);
    final String sKeystorePath = CONFIG_FILE.getAsString ("keystore.path");
    final LoadedKeyStore aLKS = KeyStoreHelper.loadKeyStore (eKSType,
                                                             sKeystorePath,
                                                             CONFIG_FILE.getAsString ("keystore.password"));
    if (aLKS.isFailure ())
      throw new InitializationException (aLKS.getErrorText (Locale.US));
    LOGGER.info ("Successfully loaded keystore file '" + sKeystorePath + "'");
    KEY_STORE = aLKS.getKeyStore ();

    // Load key from keystore
    final String sKeystoreKeyAlias = CONFIG_FILE.getAsString ("keystore.key.alias");
    final LoadedKey <KeyStore.PrivateKeyEntry> aLPK = KeyStoreHelper.loadPrivateKey (KEY_STORE,
                                                                                     sKeystorePath,
                                                                                     sKeystoreKeyAlias,
                                                                                     CONFIG_FILE.getAsCharArray ("keystore.key.password"));
    if (aLPK.isFailure ())
      throw new InitializationException ("Failed to load key '" +
                                         sKeystoreKeyAlias +
                                         "' from keystore '" +
                                         sKeystorePath +
                                         "': " +
                                         aLPK.getErrorText (Locale.US));
    LOGGER.info ("Successfully loaded key '" + sKeystoreKeyAlias + "' from keystore '" + sKeystorePath + "'");
    KEY = aLPK.getKeyEntry ();
  }

  @Nonnull
  public static ConfigFile getConfigFile ()
  {
    return CONFIG_FILE;
  }

  @Nonnull
  public static X509Certificate getCertificate ()
  {
    return (X509Certificate) KEY.getCertificate ();
  }

  @Nonnull
  public static PrivateKey getPrivateKey ()
  {
    return KEY.getPrivateKey ();
  }

  @Nullable
  public static String getGlobalDebug ()
  {
    return CONFIG_FILE.getAsString ("global.debug");
  }

  @Nullable
  public static String getGlobalProduction ()
  {
    return CONFIG_FILE.getAsString ("global.production");
  }

  @Nullable
  public static String getDataPath ()
  {
    return CONFIG_FILE.getAsString ("webapp.datapath");
  }
}
