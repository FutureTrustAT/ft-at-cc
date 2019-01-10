package at.gv.brz.futuretrust.cc;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMSignContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.base64.Base64;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.mime.CMimeType;
import com.helger.xml.XMLFactory;
import com.helger.xml.XMLHelper;
import com.helger.xml.microdom.IMicroDocument;
import com.helger.xml.microdom.IMicroElement;
import com.helger.xml.microdom.MicroDocument;
import com.helger.xml.namespace.MapBasedNamespaceContext;
import com.helger.xml.serialize.write.EXMLSerializeXMLDeclaration;
import com.helger.xml.serialize.write.XMLWriter;
import com.helger.xml.serialize.write.XMLWriterSettings;
import com.helger.xmldsig.XMLDSigCreator;

public final class XMLDSigHandler
{
  public static final String NS_DSS2 = "urn:oasis:names:tc:dss:2.0:core:schema";
  public static final String NS_ETSIVAL = "http://uri.etsi.org/119442/v1.1.1#";
  public static final String NS_VR = "urn:oasis:names:tc:dss:1.0:profiles:verificationreport:schema#";

  private static final MapBasedNamespaceContext NSCTX = new MapBasedNamespaceContext ();
  static
  {
    NSCTX.addMapping ("eb40", "http://www.ebinterface.at/schema/4p0/");
    NSCTX.addMapping ("eb41", "http://www.ebinterface.at/schema/4p1/");
    NSCTX.addMapping ("eb42", "http://www.ebinterface.at/schema/4p2/");
    NSCTX.addMapping ("eb43", "http://www.ebinterface.at/schema/4p3/");

    NSCTX.addMapping ("ds", "http://www.w3.org/2000/09/xmldsig#");
    NSCTX.addMapping ("xenc", "http://www.w3.org/2001/04/xmlenc#");

    NSCTX.addMapping ("dss1", "urn:oasis:names:tc:dss:1.0:core:schema");
    NSCTX.addMapping ("ades", "urn:oasis:names:tc:dss:1.0:profiles:AdES:schema#");
    NSCTX.addMapping ("vr", XMLDSigHandler.NS_VR);
    NSCTX.addMapping ("async", "urn:oasis:names:tc:dss:1.0:profiles:asynchronousprocessing:1.0");
    NSCTX.addMapping ("timestamping", "urn:oasis:names:tc:dss:1.0:profiles:TimeStamp:schema#");
    NSCTX.addMapping ("dss", XMLDSigHandler.NS_DSS2);
    NSCTX.addMapping ("saml1", "urn:oasis:names:tc:SAML:1.0:assertion");
    NSCTX.addMapping ("saml2", "urn:oasis:names:tc:SAML:2.0:assertion");

    NSCTX.addMapping ("xades132", "http://uri.etsi.org/01903/v1.3.2#");
    NSCTX.addMapping ("xades141", "http://uri.etsi.org/01903/v1.4.1#");
    NSCTX.addMapping ("etsival", XMLDSigHandler.NS_ETSIVAL);
    NSCTX.addMapping ("ts102231", "http://uri.etsi.org/102231/v2#");
    NSCTX.addMapping ("etsivr", "http://uri.etsi.org/1191022/v1.1.1#");

    NSCTX.addMapping ("policy", "http://www.arhs-group.com/spikeseed");
    NSCTX.addMapping ("vals", "http://futuretrust.eu/vals/v1.0.0#");
  }

  private static final Logger LOGGER = LoggerFactory.getLogger (XMLDSigHandler.class);

  private XMLDSigHandler ()
  {}

  public static MapBasedNamespaceContext getNSCtx ()
  {
    return NSCTX.getClone ();
  }

  @Nonnull
  private static XMLWriterSettings _getXWS ()
  {
    final XMLWriterSettings aXWS = XMLWriterSettings.createForCanonicalization ()
                                                    .setNamespaceContext (NSCTX)
                                                    .setPutNamespaceContextPrefixesInRoot (false)
                                                    .setSerializeXMLDeclaration (EXMLSerializeXMLDeclaration.EMIT_NO_NEWLINE);
    return aXWS;
  }

  @Nonnull
  private static byte [] _getDocAsBytes (@Nonnull final Node e)
  {
    // Must be the document
    return XMLWriter.getNodeAsBytes (XMLHelper.getOwnerDocument (e), _getXWS ());
  }

  @Nonnull
  public static Element sign (@Nonnull final Document aSrcDoc,
                              @Nonnull final PrivateKey aPrivateKey,
                              @Nonnull final X509Certificate aCertificate) throws Exception
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
        // Exclusive, no comments
        return CanonicalizationMethod.EXCLUSIVE;
      }

      @Override
      protected String getSignatureMethod () throws Exception
      {
        return org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256;
      }
    };
    final XMLObject aObj = aCreator.getSignatureFactory ()
                                   .newXMLObject (new CommonsArrayList <> (new DOMStructure (aSrcDoc)),
                                                  null,
                                                  null,
                                                  null);
    final XMLSignature aXMLSignature = aCreator.createXMLSignature (aCertificate,
                                                                    new CommonsArrayList <> (aObj),
                                                                    null,
                                                                    null);

    // Clone source document into a new document
    final Document aDstDoc = XMLFactory.newDocument ();
    aDstDoc.appendChild (aDstDoc.adoptNode (aSrcDoc.getDocumentElement ().cloneNode (true)));

    final DOMSignContext aDOMSignContext = new DOMSignContext (aPrivateKey,
                                                               aDstDoc.getDocumentElement (),
                                                               aDstDoc.getDocumentElement ().getFirstChild ());
    aDOMSignContext.setDefaultNamespacePrefix (XMLDSigCreator.DEFAULT_NS_PREFIX);

    // Marshal, generate, and sign the enveloped signature.
    aXMLSignature.sign (aDOMSignContext);

    // Create empty document that should contain the signature
    final Element aSignatureElement = (Element) aDstDoc.getDocumentElement ().getFirstChild ();

    if (false)
      LOGGER.info ("Created signature:\n" + XMLWriter.getNodeAsString (aSignatureElement, _getXWS ()));

    if (false)
      LOGGER.info ("Created doc:\n" + XMLWriter.getNodeAsString (aDstDoc, _getXWS ()));

    return aSignatureElement;
  }

  @Nonnull
  public static IMicroDocument createVerifyRequest (@Nonnull final Node aSourceNode,
                                                    @Nonnull final Node aSignatureNode) throws IOException
  {
    ValueEnforcer.notNull (aSourceNode, "Invoice");
    ValueEnforcer.notNull (aSignatureNode, "Signature");

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
      // Works with line break and without
      eValue.appendText (Base64.encodeBytes (_getDocAsBytes (aSourceNode), Base64.DO_BREAK_LINES));
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
      if (false)
        sReportDetailLevel = "urn:oasis:names:tc:dss:1.0:profiles:verificationreport:reportdetail:allDetails";
      eRVR.appendElement (NS_VR, "ReportDetailLevel").appendText (sReportDetailLevel);
      // Manifest must be disabled - else ValS crashes
      eOptionalInputs.appendElement (NS_ETSIVAL, "VerifyManifests").appendText ("false");
      eOptionalInputs.appendElement (NS_ETSIVAL, "SignVerificationReport").appendText ("true");
    }

    {
      final IMicroElement eSignatureObject = eRoot.appendElement (NS_ETSIVAL, "SignatureObject");
      final IMicroElement eBase64Signature = eSignatureObject.appendElement (NS_DSS2, "Base64Signature");
      eBase64Signature.setAttribute ("MimeType", CMimeType.APPLICATION_XML.getAsString ());
      final IMicroElement eValue = eBase64Signature.appendElement (NS_DSS2, "Value");
      // Signature only
      eValue.appendText (Base64.encodeBytes (_getDocAsBytes (aSignatureNode), Base64.DO_BREAK_LINES));
    }
    return aVerifyRequestDoc;
  }
}
