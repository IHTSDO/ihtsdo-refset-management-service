/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

/**
 * Implementation of an algorithm to export a refset definition.
 */
public class ExportRefsetRf2WithNameExcelHandler
    extends ExportRefsetRf2WithNameHandler {

  /**
   * Instantiates an empty {@link ExportRefsetRf2WithNameExcelHandler}.
   * @throws Exception if anything goes wrong
   */
  public ExportRefsetRf2WithNameExcelHandler() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public String getFileTypeFilter() {
    return ".xlsx";
  }

  /* see superclass */
  @Override
  public String getMimeType() {
    return "application/vnd.ms-excel";
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Export RF2 .xlsx with name";
  }

}
