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

import java.security.KeyStore;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Locale LOCALE = Locale.US;

  public static void main (final String [] args)
  {
    LOGGER.info ("Started application");

    // Load configuration file
    final ConfigFile aCF = new ConfigFileBuilder ().addPathFromSystemProperty ("ft-at-cc-configuration-file")
                                                   .addPath ("config.properties")
                                                   .build ();
    if (!aCF.isRead ())
      throw new IllegalStateException ("Failed to resolve configuration file");
    LOGGER.info ("Loaded configuration file '" + aCF.getReadResource ().getPath () + '"');

    // Load keystore
    final EKeyStoreType eKSType = EKeyStoreType.getFromIDCaseInsensitiveOrDefault (aCF.getAsString ("keystore.type"),
                                                                                   EKeyStoreType.JKS);
    final LoadedKeyStore aLKS = KeyStoreHelper.loadKeyStore (eKSType,
                                                             aCF.getAsString ("keystore.path"),
                                                             aCF.getAsString ("keystore.password"));
    if (aLKS.isFailure ())
      throw new IllegalStateException ("Failed to load keystore: " + aLKS.getErrorText (LOCALE));
    LOGGER.info ("Successfully loaded keystore");

    // Load key from keystore
    final LoadedKey <KeyStore.PrivateKeyEntry> aLPK = KeyStoreHelper.loadPrivateKey (aLKS.getKeyStore (),
                                                                                     aCF.getAsString ("keystore.path"),
                                                                                     aCF.getAsString ("keystore.key.alias"),
                                                                                     aCF.getAsCharArray ("keystore.key.password"));
    if (aLPK.isFailure ())
      throw new IllegalStateException ("Failed to load key from keystore: " + aLPK.getErrorText (LOCALE));
    LOGGER.info ("Successfully loaded key from keystore");

    LOGGER.info ("Finished application");
  }
}
