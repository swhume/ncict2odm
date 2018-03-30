/*
* Copyright 2017 Sam Hume.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package ct2odm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Term represents a CDISC Controlled Terminology Code List Term
 * @author Sam Hume swhume@gmail.com
 */
class Term {
    private final XMLNamespace ns = new XMLNamespace();
    private final String itCode;
    private final String itSubVal;
    private final List<String> itSynonyms = new ArrayList<>();
    private final String itDefn;
    private final String itPrefTerm;
        
    /**
     * Codelist constructor takes a string array of data from the CT delimited text file 
     * @param row a String array containing the row of delimited text file CT
     */
    public Term(String[] row) {
        this.itCode = row[0];                      // Code
        this.itSubVal = row[4];                    // CDISC Submission Value
        if (row[5] != null && !row[5].isEmpty()) { //CDISC Synonym(s)
            this.itSynonyms.addAll(Arrays.asList(row[5].split(";")));
        }
        this.itDefn = row[6];        // CDISC Definition
        this.itPrefTerm = row[7];    // NCI Preferred Term
    }

    /**
     * generateEnumeratedItem takes a Term object and returns an EnumeratedItem element 
     * @param t Term a Term object used to create the EnumeratedItem content
     * @return it element returns an EnumeratedItem element
     */
    public Element generateEnumeratedItem(Term t) {
        Element it = new Element("EnumeratedItem", this.ns.getOdmNamespace());
        it.setAttribute(new Attribute("CodedValue", this.itSubVal));
        it.setAttribute(new Attribute("ExtCodeID", this.itCode, this.ns.getNciNamespace()));
        for (String s : this.itSynonyms) {
            it.addContent(generateSynonym(s));
        }
        it.addContent(generateCdiscDefn());
        it.addContent(generatePrefTerm());
        return it;
    }

    /* takes a synonym string and returns a synonym element */
    private Element generateSynonym(String synonym) {
        Element cdiscSynonym = new Element("CDISCSynonym", this.ns.getNciNamespace());
        cdiscSynonym.addContent(synonym);
        return cdiscSynonym;
    }

    /* returns the preferred term element created using an object property */
    private Element generatePrefTerm() {
        Element cdiscPrefTerm = new Element("PreferredTerm", this.ns.getNciNamespace());
        cdiscPrefTerm.addContent(this.itPrefTerm);
        return cdiscPrefTerm;
    }

    /* returns the cdisc definition element created using an object property */
    private Element generateCdiscDefn() {
        Element cdiscDefn = new Element("CDISCDefinition", this.ns.getNciNamespace());
        cdiscDefn.addContent(this.itDefn);
        return cdiscDefn;
    }
}
