/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.datasource;

import java.io.File;
import java.io.IOException;

import org.obiba.magma.MagmaEngine;
import org.obiba.magma.js.MagmaJsExtension;
import org.obiba.magma.xstream.MagmaXStreamExtension;

/**
 * Extract the drugs about human from the DPD files.
 * 
 * @see http://www.hc-sc.gc.ca/dhp-mps/prodpharma/databasdon/dpd_bdpp_data_extract-eng.php
 * @see http://www.hc-sc.gc.ca/dhp-mps/prodpharma/databasdon/dpd_read_me-bdpp_lisez-moi-eng.php
 */
public class Medications {

  public static void main(String[] args) throws IOException {
    if(args == null || args.length < 2) {
      System.err.println("Arguments required: (1) Directory were datasource file will be written (2) Name of the dataource file.");
    }
    new MagmaEngine().extend(new MagmaXStreamExtension()).extend(new MagmaJsExtension());

    for(int i = 1; i < args.length; i++) {
      System.out.println("===== " + args[i] + " =====");
      MedicationsBuilder builder = MedicationsBuilder.fromLatest()//
      .indications(new File("src/main/resources", "indications.csv"))//
      .noIndications(new File("src/main/resources", "indications-adjustments.csv"));

      if(i == 1) {
        builder.view(new File("src/main/resources", "Drugs.xml"), new File("target", "Drugs.csv"));
      }

      builder.to(new File(args[0], args[i]));
    }

  }

}
