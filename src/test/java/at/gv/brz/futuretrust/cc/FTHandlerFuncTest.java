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
package at.gv.brz.futuretrust.cc;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.xml.crypto.KeySelector;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.helger.commons.io.file.SimpleFileIO;
import com.helger.httpclient.HttpClientFactory;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.HttpDebugger;
import com.helger.httpclient.response.ResponseHandlerMicroDom;
import com.helger.security.keystore.EKeyStoreType;
import com.helger.security.keystore.KeyStoreHelper;
import com.helger.security.keystore.LoadedKey;
import com.helger.xml.microdom.IMicroDocument;
import com.helger.xml.microdom.IMicroElement;
import com.helger.xml.microdom.serialize.MicroWriter;
import com.helger.xml.serialize.read.DOMReader;
import com.helger.xml.serialize.write.XMLWriterSettings;
import com.helger.xmldsig.XMLDSigSetup;
import com.helger.xmldsig.XMLDSigValidationResult;
import com.helger.xmldsig.XMLDSigValidator;
import com.helger.xmldsig.keyselect.ContainedX509KeySelector;

public final class FTHandlerFuncTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (FTHandlerFuncTest.class);

  private static final XMLWriterSettings XWS = new XMLWriterSettings ().setNamespaceContext (FTHandler.getNSCtx ())
                                                                       .setPutNamespaceContextPrefixesInRoot (true);

  @BeforeClass
  public static void beforeClass ()
  {
    XMLDSigSetup.getXMLSignatureFactory ();
    HttpDebugger.setEnabled (false);
  }

  // Working fine
  @Test
  @Ignore
  public void testSendPrebuildVerifyRequest () throws Exception
  {
    final File f = new File ("src/test/resources/ft/eRechnung_01_verify-request_working.xml");

    final HttpClientFactory aHCFactory = new HttpClientFactory ();
    try (HttpClientManager aMgr = new HttpClientManager (aHCFactory))
    {
      final HttpPost aPost = new HttpPost (FTHandler.getValsURL ());
      aPost.setEntity (new FileEntity (f, ContentType.APPLICATION_XML));

      final ResponseHandlerMicroDom aRH = new ResponseHandlerMicroDom (false);
      final IMicroDocument aDoc = aMgr.execute (aPost, aRH);

      if (false)
        LOGGER.info ("Received:\n" + MicroWriter.getNodeAsString (aDoc, XWS));

      final IMicroElement aResult = aDoc.getDocumentElement ().getFirstChildElement (FTHandler.NS_DSS2, "Result");
      final IMicroElement aResultMajor = aResult.getFirstChildElement (FTHandler.NS_DSS2, "ResultMajor");
      LOGGER.info ("Result: " + aResultMajor.getTextContent ());
    }
  }

  @Test
  @Ignore
  public void testCreateNewRequestFromUnsignedXML () throws Exception
  {
    // Load keystore and key
    final String sKSPath = "futureTrust.cz.eRechnung.jks";
    final String sPW = "changeit";
    final KeyStore aKS = KeyStoreHelper.loadKeyStoreDirect (EKeyStoreType.JKS, sKSPath, sPW.toCharArray ());
    assertNotNull (aKS);
    final LoadedKey <PrivateKeyEntry> aLK = KeyStoreHelper.loadPrivateKey (aKS,
                                                                           sKSPath,
                                                                           "futuretrust.cz.erechnung",
                                                                           sPW.toCharArray ());
    assertTrue (aLK.isSuccess ());
    assertNotNull (aLK.getKeyEntry ());
    final X509Certificate aCertificate = (X509Certificate) aLK.getKeyEntry ().getCertificate ();
    final PrivateKey aPrivateKey = aLK.getKeyEntry ().getPrivateKey ();

    // Read unsigned invoice
    final File fUnsigned = new File ("src/test/resources/ft/ebi43-unsigned.xml");
    final Document aEbiDoc = DOMReader.readXMLDOM (fUnsigned);
    assertNotNull (aEbiDoc);

    // Sign
    final Element aSignatureElement = FTHandler.sign (aEbiDoc, aPrivateKey, aCertificate);

    // Self-test if signing worked
    if (true)
    {
      final XMLDSigValidationResult aResult = XMLDSigValidator.validateSignature (aEbiDoc,
                                                                                  aSignatureElement,
                                                                                  KeySelector.singletonKeySelector (aCertificate.getPublicKey ()));
      if (aResult.isInvalid ())
        throw new IllegalStateException ("Failed to validate created signature with constant provided key: " +
                                         aResult.toString ());
    }
    if (true)
    {
      final XMLDSigValidationResult aResult = XMLDSigValidator.validateSignature (aEbiDoc,
                                                                                  aSignatureElement,
                                                                                  new ContainedX509KeySelector ());
      if (aResult.isInvalid ())
        throw new IllegalStateException ("Failed to validate created signature with contained key: " +
                                         aResult.toString ());
    }

    // Create request
    final IMicroDocument aVerifyRequestDoc = FTHandler.createVerifyRequest (aEbiDoc.getDocumentElement (),
                                                                            aSignatureElement);

    // Dump request
    if (false)
      SimpleFileIO.writeFile (new File ("request.xml"), MicroWriter.getNodeAsBytes (aVerifyRequestDoc, XWS));
    if (false)
      LOGGER.info ("Sending:\n" + MicroWriter.getNodeAsString (aVerifyRequestDoc, XWS));

    final HttpClientFactory aHCFactory = new HttpClientFactory ();
    try (HttpClientManager aMgr = new HttpClientManager (aHCFactory))
    {
      final HttpPost aPost = new HttpPost (FTHandler.getValsURL ());
      aPost.setEntity (new ByteArrayEntity (MicroWriter.getNodeAsBytes (aVerifyRequestDoc),
                                            ContentType.APPLICATION_XML));

      final ResponseHandlerMicroDom aRH = new ResponseHandlerMicroDom (false);
      final IMicroDocument aDoc = aMgr.execute (aPost, aRH);

      // Dump response
      LOGGER.info ("Received:\n" + MicroWriter.getNodeAsString (aDoc, XWS));
      if (false)
        SimpleFileIO.writeFile (new File ("response.xml"), MicroWriter.getNodeAsBytes (aDoc, XWS));
    }
  }
}
