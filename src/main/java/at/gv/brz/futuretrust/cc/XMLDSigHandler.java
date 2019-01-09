package at.gv.brz.futuretrust.cc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.spec.XPathFilterParameterSpec;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xml.security.c14n.Canonicalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.base64.Base64;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
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
import com.helger.xml.transform.XMLTransformerFactory;
import com.helger.xmldsig.XMLDSigCreator;

public final class XMLDSigHandler
{
  public static final String NS_DSS2 = "urn:oasis:names:tc:dss:2.0:core:schema";
  public static final String NS_ETSIVAL = "http://uri.etsi.org/119442/v1.1.1#";
  public static final String NS_VR = "urn:oasis:names:tc:dss:1.0:profiles:verificationreport:schema#";

  private static final Logger LOGGER = LoggerFactory.getLogger (XMLDSigHandler.class);

  private XMLDSigHandler ()
  {}

  @Nonnull
  private static byte [] _getAsBytesTransformer (@Nonnull final Element e)
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

  @Nonnull
  private static byte [] _getAsBytesCanonicalized (@Nonnull final Node e)
  {
    try
    {
      final Canonicalizer canon = Canonicalizer.getInstance (Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
      final byte [] ret = canon.canonicalizeSubtree (e);
      return ret;
    }
    catch (final Exception ex)
    {
      throw new IllegalStateException (ex);
    }
  }

  private static XMLWriterSettings _getXWS ()
  {
    final MapBasedNamespaceContext aNsCtx = new MapBasedNamespaceContext ();
    aNsCtx.addMapping ("eb", "http://www.ebinterface.at/schema/4p3/");
    aNsCtx.addMapping ("ds", "http://www.w3.org/2000/09/xmldsig#");
    final XMLWriterSettings aXWS = XMLWriterSettings.createForCanonicalization ()
                                                    .setNamespaceContext (aNsCtx)
                                                    .setPutNamespaceContextPrefixesInRoot (false)
                                                    .setSerializeXMLDeclaration (EXMLSerializeXMLDeclaration.EMIT_NO_NEWLINE);
    return aXWS;
  }

  @Nonnull
  private static byte [] _getAsBytesMicro (@Nonnull final Node e)
  {
    final XMLWriterSettings aXWS = _getXWS ();
    return XMLWriter.getNodeAsBytes (XMLHelper.getOwnerDocument (e), aXWS);
  }

  @Nonnull
  public static Document sign (@Nonnull final Document aEbiDoc,
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

      protected Transform _createDefaultTransform () throws Exception
      {
        return getSignatureFactory ().newTransform (Transform.XPATH, new XPathFilterParameterSpec ("/Signature"));
      }

      protected List <Transform> _createTransformList () throws Exception
      {
        return new CommonsArrayList <> ();
      }

      @Override
      @Nonnull
      protected String getCanonicalizationMethod () throws Exception
      {
        return CanonicalizationMethod.EXCLUSIVE;
      }

      @Override
      protected String getDefaultReferenceURI ()
      {
        // "" means sign the whole document
        return "";
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

    // Create empty document that should contain the signature
    final Document aSignatureDoc = XMLFactory.newDocument ();
    final Node aSignatureParent = aSignatureDoc.appendChild (aSignatureDoc.createElement ("so-a-schas"));

    final DOMSignContext aDOMSignContext = new DOMSignContext (aPrivateKey, aSignatureParent);
    aDOMSignContext.setDefaultNamespacePrefix (XMLDSigCreator.DEFAULT_NS_PREFIX);

    // Marshal, generate, and sign the enveloped signature.
    aXMLSignature.sign (aDOMSignContext);

    if (true)
      LOGGER.info ("Created signature:\n" + XMLWriter.getNodeAsString (aSignatureDoc, _getXWS ()));

    if (false)
      LOGGER.info ("Created doc:\n" + XMLWriter.getNodeAsString (aEbiDoc, _getXWS ()));

    return aSignatureDoc;
  }

  @Nonnull
  public static IMicroDocument createVerifyRequest (@Nonnull final Element aInvoice,
                                                    @Nonnull final Document aSignature) throws IOException
  {
    ValueEnforcer.notNull (aInvoice, "Invoice");
    ValueEnforcer.notNull (aSignature, "Signature");

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
      eValue.appendText (Base64.encodeBytes (_getAsBytesCanonicalized (aInvoice), true ? 0 : Base64.DO_BREAK_LINES));
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
      eValue.appendText (Base64.encodeBytes (_getAsBytesMicro (aSignature), true ? 0 : Base64.DO_BREAK_LINES));
    }
    return aVerifyRequestDoc;
  }
}
