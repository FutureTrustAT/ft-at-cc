
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.xml.security.c14n.Canonicalizer;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.helger.commons.base64.Base64;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.io.file.SimpleFileIO;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.mime.CMimeType;
import com.helger.httpclient.HttpClientFactory;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.HttpDebugger;
import com.helger.httpclient.response.ResponseHandlerMicroDom;
import com.helger.security.keystore.EKeyStoreType;
import com.helger.security.keystore.KeyStoreHelper;
import com.helger.security.keystore.LoadedKey;
import com.helger.xml.XMLHelper;
import com.helger.xml.microdom.IMicroDocument;
import com.helger.xml.microdom.IMicroElement;
import com.helger.xml.microdom.MicroDocument;
import com.helger.xml.microdom.serialize.MicroWriter;
import com.helger.xml.namespace.MapBasedNamespaceContext;
import com.helger.xml.serialize.read.DOMReader;
import com.helger.xml.serialize.write.EXMLSerializeXMLDeclaration;
import com.helger.xml.serialize.write.XMLWriter;
import com.helger.xml.serialize.write.XMLWriterSettings;
import com.helger.xml.transform.XMLTransformerFactory;
import com.helger.xmldsig.XMLDSigCreator;
import com.helger.xmldsig.XMLDSigValidationResult;
import com.helger.xmldsig.XMLDSigValidator;

public final class FTValidateFuncTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (FTValidateFuncTest.class);
  private static final MapBasedNamespaceContext NSCTX = new MapBasedNamespaceContext ();

  private static final String NS_DSS2 = "urn:oasis:names:tc:dss:2.0:core:schema";
  private static final String NS_ETSIVAL = "http://uri.etsi.org/119442/v1.1.1#";
  private static final String NS_VR = "urn:oasis:names:tc:dss:1.0:profiles:verificationreport:schema#";
  private static final XMLWriterSettings XWS = new XMLWriterSettings ().setNamespaceContext (NSCTX)
                                                                       .setPutNamespaceContextPrefixesInRoot (true);
  private static final String VALS_URL = "https://futuretrust.brz.gv.at/vals-web/api/validation";

  static
  {
    NSCTX.addMapping ("ds", "http://www.w3.org/2000/09/xmldsig#");
    NSCTX.addMapping ("xenc", "http://www.w3.org/2001/04/xmlenc#");

    NSCTX.addMapping ("dss1", "urn:oasis:names:tc:dss:1.0:core:schema");
    NSCTX.addMapping ("ades", "urn:oasis:names:tc:dss:1.0:profiles:AdES:schema#");
    NSCTX.addMapping ("vr", NS_VR);
    NSCTX.addMapping ("async", "urn:oasis:names:tc:dss:1.0:profiles:asynchronousprocessing:1.0");
    NSCTX.addMapping ("timestamping", "urn:oasis:names:tc:dss:1.0:profiles:TimeStamp:schema#");
    NSCTX.addMapping ("dss", NS_DSS2);
    NSCTX.addMapping ("saml1", "urn:oasis:names:tc:SAML:1.0:assertion");
    NSCTX.addMapping ("saml2", "urn:oasis:names:tc:SAML:2.0:assertion");

    NSCTX.addMapping ("xades132", "http://uri.etsi.org/01903/v1.3.2#");
    NSCTX.addMapping ("xades141", "http://uri.etsi.org/01903/v1.4.1#");
    NSCTX.addMapping ("etsival", NS_ETSIVAL);
    NSCTX.addMapping ("ts102231", "http://uri.etsi.org/102231/v2#");
    NSCTX.addMapping ("etsivr", "http://uri.etsi.org/1191022/v1.1.1#");

    NSCTX.addMapping ("policy", "http://www.arhs-group.com/spikeseed");
    NSCTX.addMapping ("vals", "http://futuretrust.eu/vals/v1.0.0#");
  }

  @BeforeClass
  public static void beforeClass ()
  {
    HttpDebugger.setEnabled (false);
  }

  private static byte [] _getAsBytesT (@Nonnull final Element e)
  {
    try
    {
      final Transformer aTransformer = XMLTransformerFactory.newTransformer ();
      try (NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
      {
        final DOMSource aSrc = new DOMSource (e);
        final StreamResult aDst = new StreamResult (aBAOS);
        aTransformer.setOutputProperty (OutputKeys.INDENT, "yes");
        aTransformer.setOutputProperty ("{http://xml.apache.org/xslt}indent-amount", "2");
        aTransformer.setOutputProperty (OutputKeys.ENCODING, StandardCharsets.UTF_8.name ());
        aTransformer.transform (aSrc, aDst);
        return aBAOS.toByteArray ();
      }
    }
    catch (final TransformerException ex)
    {
      throw new IllegalStateException (ex);
    }
  }

  private static byte [] _getAsBytes (@Nonnull final Element e)
  {
    if (true)
    {
      if (false)
        return _getAsBytesT (e);

      try
      {
        final Canonicalizer canon = Canonicalizer.getInstance (Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        final byte canonXmlBytes[] = canon.canonicalizeSubtree (e);
        return canonXmlBytes;
      }
      catch (final Exception ex)
      {
        throw new IllegalStateException (ex);
      }
    }

    // Still not working
    final MapBasedNamespaceContext aNsCtx = new MapBasedNamespaceContext ();
    aNsCtx.addMapping ("eb", "http://www.ebinterface.at/schema/4p3/");
    aNsCtx.addMapping ("ds", "http://www.w3.org/2000/09/xmldsig#");
    final XMLWriterSettings aXWS = XMLWriterSettings.createForCanonicalization ()
                                                    .setNamespaceContext (aNsCtx)
                                                    .setPutNamespaceContextPrefixesInRoot (true)
                                                    .setSerializeXMLDeclaration (EXMLSerializeXMLDeclaration.EMIT_NO_NEWLINE);
    return XMLWriter.getNodeAsBytes (e.getOwnerDocument (), aXWS);
  }

  @Nonnull
  private static IMicroDocument createVerifyRequest (@Nonnull final Element aInvoice,
                                                     @Nonnull final Element aSignature) throws IOException
  {
    final IMicroDocument aVerifyRequestDoc = new MicroDocument ();
    final IMicroElement eRoot = aVerifyRequestDoc.appendElement (NS_ETSIVAL, "VerifyRequest");
    eRoot.setAttribute ("RequestID", UUID.randomUUID ().toString ());
    eRoot.appendElement (NS_DSS2, "Profile").appendText ("http://uri.etsi.org/19442/v1.1.1/validationprofile#");
    {
      final IMicroElement eInputDocs = eRoot.appendElement (NS_ETSIVAL, "InputDocuments");
      final IMicroElement eDoc = eInputDocs.appendElement (NS_DSS2, "Document");
      eDoc.setAttribute ("RefURI", "e-invoice.xml");
      final IMicroElement eBase64 = eDoc.appendElement (NS_DSS2, "Base64Data");
      final IMicroElement eValue = eBase64.appendElement (NS_DSS2, "Value");
      eValue.appendText (Base64.encodeBytes (_getAsBytes (aInvoice), Base64.DO_BREAK_LINES));
    }
    {
      final IMicroElement eOptionalInputs = eRoot.appendElement (NS_ETSIVAL, "OptionalInputs");
      eOptionalInputs.appendElement (NS_ETSIVAL, "ReturnVerificationTimeInfo").appendText ("true");
      final IMicroElement eRVR = eOptionalInputs.appendElement (NS_VR, "ReturnVerificationReport");
      eRVR.appendElement (NS_VR, "IncludeVerifier").appendText ("true");
      eRVR.appendElement (NS_VR, "IncludeCertificateValues").appendText ("false");
      eRVR.appendElement (NS_VR, "IncludeRevocationValues").appendText ("false");
      eRVR.appendElement (NS_VR, "ExpandBinaryValues").appendText ("false");
      String sReportDetailLevel = "urn:oasis:names:tc:dss:1.0:profiles:verificationreport:reportdetail:noDetails";
      if (true)
        sReportDetailLevel = "urn:oasis:names:tc:dss:1.0:profiles:verificationreport:reportdetail:allDetails";
      eRVR.appendElement (NS_VR, "ReportDetailLevel").appendText (sReportDetailLevel);
      if (true)
        eOptionalInputs.appendElement (NS_ETSIVAL, "VerifyManifests").appendText ("true");
      eOptionalInputs.appendElement (NS_ETSIVAL, "SignVerificationReport").appendText ("true");
    }

    {
      final IMicroElement eSignatureObject = eRoot.appendElement (NS_ETSIVAL, "SignatureObject");
      final IMicroElement eBase64Signature = eSignatureObject.appendElement (NS_DSS2, "Base64Signature");
      eBase64Signature.setAttribute ("MimeType", CMimeType.APPLICATION_XML.getAsString ());
      final IMicroElement eValue = eBase64Signature.appendElement (NS_DSS2, "Value");
      // Signature only
      eValue.appendText (Base64.encodeBytes (_getAsBytes (aSignature), Base64.DO_BREAK_LINES));
    }
    return aVerifyRequestDoc;
  }

  @Test
  @Ignore
  public void testCreateNewRequestWithPredefinedSignedXML () throws Exception
  {
    final Document aEbiSignedDoc = DOMReader.readXMLDOM (new File ("src/test/resources/ft/ebi43-signed-ft.xml"));
    assertNotNull (aEbiSignedDoc);

    final Element eSignatureElement = XMLHelper.getFirstChildElement (aEbiSignedDoc.getDocumentElement ());
    assertNotNull (eSignatureElement);
    assertEquals ("Signature", eSignatureElement.getLocalName ());

    // Create request
    final IMicroDocument aVerifyRequestDoc = createVerifyRequest (aEbiSignedDoc.getDocumentElement (),
                                                                  eSignatureElement);

    // Dump request
    SimpleFileIO.writeFile (new File ("request.xml"), MicroWriter.getNodeAsBytes (aVerifyRequestDoc, XWS));
    if (false)
      LOGGER.info ("Sending:\n" + MicroWriter.getNodeAsString (aVerifyRequestDoc, XWS));

    final HttpClientFactory aHCFactory = new HttpClientFactory ();
    try (HttpClientManager aMgr = new HttpClientManager (aHCFactory))
    {
      final HttpPost aPost = new HttpPost (VALS_URL);
      aPost.setEntity (new ByteArrayEntity (MicroWriter.getNodeAsBytes (aVerifyRequestDoc),
                                            ContentType.APPLICATION_XML));

      final ResponseHandlerMicroDom aRH = new ResponseHandlerMicroDom (false);
      final IMicroDocument aDoc = aMgr.execute (aPost, aRH);

      // Dump response
      LOGGER.info ("Received:\n" + MicroWriter.getNodeAsString (aDoc, XWS));
      SimpleFileIO.writeFile (new File ("response.xml"), MicroWriter.getNodeAsBytes (aDoc, XWS));
    }
  }

  @Test
  public void testCreateNewRequestFromUnsignedXML () throws Exception
  {
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

    final File fUnsigned = new File ("src/test/resources/ft/ebi43-unsigned.xml");
    final Document aEbiDoc = DOMReader.readXMLDOM (fUnsigned);
    assertNotNull (aEbiDoc);

    final Element aSignatureElement;
    {
      final XMLDSigCreator aCreator = new XMLDSigCreator ()
      {
        @Override
        protected String getDigestMethod () throws Exception
        {
          return DigestMethod.SHA512;
        }

        @Override
        @Nonnull
        protected String getDefaultTransform () throws Exception
        {
          return Transform.ENVELOPED;
        }

        @Override
        @Nonnull
        protected String getCanonicalizationMethod () throws Exception
        {
          return CanonicalizationMethod.EXCLUSIVE;
        }

        @Override
        protected String getSignatureMethod () throws Exception
        {
          return org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256;
        }

      };
      final XMLObject aObj = aCreator.getSignatureFactory ()
                                     .newXMLObject (new CommonsArrayList <> (new DOMStructure (aEbiDoc)),
                                                    null,
                                                    null,
                                                    null);
      final XMLSignature aXMLSignature = aCreator.createXMLSignature (aCertificate,
                                                                      new CommonsArrayList <> (aObj),
                                                                      null,
                                                                      null);

      final DOMSignContext aDOMSignContext = new DOMSignContext (aPrivateKey,
                                                                 aEbiDoc.getDocumentElement (),
                                                                 XMLHelper.getFirstChildElement (aEbiDoc.getDocumentElement ()));
      aDOMSignContext.setDefaultNamespacePrefix (XMLDSigCreator.DEFAULT_NS_PREFIX);

      // Marshal, generate, and sign the enveloped signature.
      aXMLSignature.sign (aDOMSignContext);

      aSignatureElement = XMLHelper.getFirstChildElement (aEbiDoc.getDocumentElement ());

      if (false)
        LOGGER.info ("Created signature:\n" + XMLWriter.getNodeAsString (aSignatureElement));

      if (true)
        LOGGER.info ("Created doc:\n" + XMLWriter.getNodeAsString (aEbiDoc));
    }
    {
      final XMLDSigValidationResult aResult = XMLDSigValidator.validateSignature (aEbiDoc,
                                                                                  KeySelector.singletonKeySelector (aCertificate.getPublicKey ()));
      assertTrue (aResult.isValid ());
    }

    // Create request
    final IMicroDocument aVerifyRequestDoc = createVerifyRequest (aEbiDoc.getDocumentElement (), aSignatureElement);

    // Dump request
    SimpleFileIO.writeFile (new File ("request.xml"), MicroWriter.getNodeAsBytes (aVerifyRequestDoc, XWS));
    if (true)
      LOGGER.info ("Sending:\n" + MicroWriter.getNodeAsString (aVerifyRequestDoc, XWS));

    final HttpClientFactory aHCFactory = new HttpClientFactory ();
    try (HttpClientManager aMgr = new HttpClientManager (aHCFactory))
    {
      final HttpPost aPost = new HttpPost (VALS_URL);
      aPost.setEntity (new ByteArrayEntity (MicroWriter.getNodeAsBytes (aVerifyRequestDoc),
                                            ContentType.APPLICATION_XML));

      final ResponseHandlerMicroDom aRH = new ResponseHandlerMicroDom (false);
      final IMicroDocument aDoc = aMgr.execute (aPost, aRH);

      // Dump response
      LOGGER.info ("Received:\n" + MicroWriter.getNodeAsString (aDoc, XWS));
      SimpleFileIO.writeFile (new File ("response.xml"), MicroWriter.getNodeAsBytes (aDoc, XWS));
    }
  }

  @Test
  @Ignore
  public void testSendPrebuild () throws Exception
  {
    final File f = new File ("src/test/resources/ft/eRechnung_01_verify-request_working.xml");

    final HttpClientFactory aHCFactory = new HttpClientFactory ();
    try (HttpClientManager aMgr = new HttpClientManager (aHCFactory))
    {
      final HttpPost aPost = new HttpPost (VALS_URL);
      aPost.setEntity (new FileEntity (f, ContentType.APPLICATION_XML));

      final ResponseHandlerMicroDom aRH = new ResponseHandlerMicroDom (false);
      final IMicroDocument aDoc = aMgr.execute (aPost, aRH);

      if (false)
        LOGGER.info ("Received:\n" + MicroWriter.getNodeAsString (aDoc, XWS));

      final IMicroElement aResult = aDoc.getDocumentElement ().getFirstChildElement (NS_DSS2, "Result");
      final IMicroElement aResultMajor = aResult.getFirstChildElement (NS_DSS2, "ResultMajor");
      LOGGER.info ("Result: " + aResultMajor.getTextContent ());
    }
  }
}
