package com.github.gv2011.tools.vcard;

import static com.github.gv2011.util.Verify.verify;

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.VCardVersion;
import ezvcard.ValidationWarnings;
import ezvcard.property.Address;
import ezvcard.property.Email;
import ezvcard.property.Note;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;

public class Main {

  public static void main(final String[] args) {
    final VCard vcard = new VCard();

    final StructuredName n = new StructuredName();
    n.setGiven("Eberhard");
    n.setFamily("Iglhaut");
    n.getPrefixes().add("Dipl.-Ing.");
    vcard.setStructuredName(n);

    vcard.setFormattedName("Dipl.-Ing. Eberhard Iglhaut");
    vcard.setProperty(new Email("eberhard@iglhaut.com"));
    vcard.setProperty(new Telephone("+49 1577 195 7622"));
    vcard.setProperty(new Note("PGP Key Fingerprint: D32D4C0A6BB2D76454452D391E2B955858D1D904"));
    final Address address = new Address();
    address.setLocality("Berlin");
    vcard.setProperty(address);

    final ValidationWarnings validate = vcard.validate(VCardVersion.V2_1);
    verify(validate.isEmpty());

    final String text = Ezvcard.write(vcard).prodId(false).version(VCardVersion.V2_1).go();
    System.out.println(text);
  }

}
