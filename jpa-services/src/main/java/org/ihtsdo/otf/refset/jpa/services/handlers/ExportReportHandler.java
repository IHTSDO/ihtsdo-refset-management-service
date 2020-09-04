/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.refset.jpa.services.handlers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.ihtsdo.otf.refset.MemberDiffReport;
import org.ihtsdo.otf.refset.Refset.Type;
import org.ihtsdo.otf.refset.helpers.ConceptList;
import org.ihtsdo.otf.refset.helpers.LocalException;
import org.ihtsdo.otf.refset.jpa.services.RefsetServiceJpa;
import org.ihtsdo.otf.refset.rf2.Concept;
import org.ihtsdo.otf.refset.rf2.ConceptRefsetMember;

/**
 * A handler for exporting a {@link Report}.
 */
public class ExportReportHandler {

  /** The reason map. */
  private Map<String, Concept> reasonMap = new HashMap<>();

  /** The headers. */
  private Map<String, String> headers = new HashMap<>();

  /** The migration terminology. */
  private String migrationTerminology;

  /** The migration version. */
  private String migrationVersion;

  /** The members in common. */
  private List<ConceptRefsetMember> membersInCommon = new ArrayList<>();

  /** The refset service. */
  private RefsetServiceJpa refsetService;

  /**
   * Instantiates an empty {@link ExportReportHandler}.
   */
  public ExportReportHandler() {

  }

  /**
   * Export report.
   *
   * @param report the report
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportReport(MemberDiffReport report,
    RefsetServiceJpa refsetService, String migrationTerminology,
    String migrationVersion, Map<String, String> headers,
    List<ConceptRefsetMember> membersInCommon) throws Exception {

    // Create workbook
    Workbook wb = new HSSFWorkbook();

    this.headers = headers;
    this.migrationTerminology = migrationTerminology;
    this.migrationVersion = migrationVersion;
    this.membersInCommon = membersInCommon;
    this.refsetService = refsetService;

    // Export report
    handleExportReport(report, wb);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    wb.write(out);
    InputStream in = new ByteArrayInputStream(out.toByteArray());
    return in;

  }

  /**
   * Handle export report.
   *
   * @param report the report
   * @param wb the wb
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void handleExportReport(MemberDiffReport report, Workbook wb)
    throws Exception {
    Logger.getLogger(getClass()).info("Exporting report " + "..."); // TODO:
                                                                    // provide
                                                                    // report
                                                                    // name

    try {

      CreationHelper createHelper = wb.getCreationHelper();
      // Set font
      Font font = wb.createFont();
      font.setFontName("Calibri");
      font.setFontHeightInPoints((short) 11);

      // Fonts are set into a style
      CellStyle style = wb.createCellStyle();
      style.setFont(font);

      Sheet sheet = wb.createSheet("Report");
      for (int i = 0; i < 5; i++) {
        sheet.setColumnWidth(i, 50 * 256);
      }

      // Create header row and add cells
      int rownum = 0;
      int cellnum = 0;
      Row row = sheet.createRow(rownum++);
      Cell cell = null;

      cell = row.createCell(cellnum);
      cell.setCellStyle(style);
      cell.setCellValue(
          createHelper.createRichTextString("New Members").toString());
      if (report.getNewRegularMembers().size() > 0) {
        makeMemberHeaderHelper(wb, sheet, rownum++, cell.getCellStyle());
      } else {
        row = sheet.createRow(rownum++);
        cell = row.createCell(cellnum);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString("n/a").toString());
      }
      for (ConceptRefsetMember member : report.getNewRegularMembers()) {
        makeMemberRowHelper(member, wb, sheet, rownum++, cell.getCellStyle());
      }

      rownum++;
      row = sheet.createRow(rownum++);
      cellnum = 0;
      cell = row.createCell(cellnum);
      cell.setCellStyle(style);
      cell.setCellValue(
          createHelper.createRichTextString("Old Members").toString());
      if (report.getOldRegularMembers().size() > 0) {
        makeMemberHeaderHelper(wb, sheet, rownum++, cell.getCellStyle());
      } else {
        row = sheet.createRow(rownum++);
        cell = row.createCell(cellnum);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString("n/a").toString());
      }
      for (ConceptRefsetMember member : report.getOldRegularMembers()) {
        makeMemberRowHelper(member, wb, sheet, rownum++, cell.getCellStyle());

        /**
         * if (!member.isConceptActive() && report.getNewRefset().getType() ==
         * Type.INTENSIONAL) { ConceptList conceptList = refsetService
         * .getTerminologyHandler(member.getRefset().getProject(), headers)
         * .getReplacementConcepts(member.getConceptId(), migrationTerminology,
         * migrationVersion); for (Concept c : conceptList.getObjects()) {
         * appendReplacementConceptInfo(member, c, wb, sheet, rownum++,
         * cell.getCellStyle()); } }
         */

      }
      rownum++;

      if (report.getNewRefset().getType() == Type.INTENSIONAL) {
        // Valid inclusions
        row = sheet.createRow(rownum++);
        cellnum = 0;
        cell = row.createCell(cellnum);
        cell.setCellStyle(style);
        cell.setCellValue(
            createHelper.createRichTextString("Valid Inclusions").toString());

        if (report.getValidInclusions().size() > 0) {
          makeMemberHeaderHelper(wb, sheet, rownum++, cell.getCellStyle());
        } else {
          row = sheet.createRow(rownum++);
          cell = row.createCell(cellnum);
          cell.setCellStyle(style);
          cell.setCellValue(
              createHelper.createRichTextString("n/a").toString());
        }
        for (ConceptRefsetMember member : report.getValidInclusions()) {
          Logger.getLogger(getClass()).debug("  member = " + member);
          makeMemberRowHelper(member, wb, sheet, rownum++, cell.getCellStyle());
        }
        rownum++;

        // Valid exclusions
        row = sheet.createRow(rownum++);
        cellnum = 0;
        cell = row.createCell(cellnum);
        cell.setCellStyle(style);
        cell.setCellValue(
            createHelper.createRichTextString("Valid Exclusions").toString());

        if (report.getValidExclusions().size() > 0) {
          makeMemberHeaderHelper(wb, sheet, rownum++, cell.getCellStyle());
        } else {
          row = sheet.createRow(rownum++);
          cell = row.createCell(cellnum);
          cell.setCellStyle(style);
          cell.setCellValue(
              createHelper.createRichTextString("n/a").toString());
        }
        for (ConceptRefsetMember member : report.getValidExclusions()) {
          Logger.getLogger(getClass()).debug("  member = " + member);
          makeMemberRowHelper(member, wb, sheet, rownum++, cell.getCellStyle());
        }
        rownum++;

        // Staged inclusions
        row = sheet.createRow(rownum++);
        cellnum = 0;
        cell = row.createCell(cellnum);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper
            .createRichTextString("Migrated Inclusions").toString());

        if (report.getStagedInclusions().size() > 0) {
          makeMemberHeaderHelper(wb, sheet, rownum++, cell.getCellStyle());
        } else {
          row = sheet.createRow(rownum++);
          cell = row.createCell(cellnum);
          cell.setCellStyle(style);
          cell.setCellValue(
              createHelper.createRichTextString("n/a").toString());
        }
        for (ConceptRefsetMember member : report.getStagedInclusions()) {
          Logger.getLogger(getClass()).debug("  member = " + member);
          makeMemberRowHelper(member, wb, sheet, rownum++, cell.getCellStyle());
        }
        rownum++;

        // Staged exclusions
        row = sheet.createRow(rownum++);
        cellnum = 0;
        cell = row.createCell(cellnum);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper
            .createRichTextString("Migrated Exclusions").toString());

        if (report.getStagedExclusions().size() > 0) {
          makeMemberHeaderHelper(wb, sheet, rownum++, cell.getCellStyle());
        } else {
          row = sheet.createRow(rownum++);
          cell = row.createCell(cellnum);
          cell.setCellStyle(style);
          cell.setCellValue(
              createHelper.createRichTextString("n/a").toString());
        }
        for (ConceptRefsetMember member : report.getStagedExclusions()) {
          makeMemberRowHelper(member, wb, sheet, rownum++, cell.getCellStyle());
        }
        rownum++;

        // Invalid inclusions
        row = sheet.createRow(rownum++);
        cellnum = 0;
        cell = row.createCell(cellnum);
        cell.setCellStyle(style);
        cell.setCellValue(
            createHelper.createRichTextString("Invalid Inclusions").toString());

        if (report.getInvalidInclusions().size() > 0) {
          makeMemberHeaderHelper(wb, sheet, rownum++, cell.getCellStyle());
        } else {
          row = sheet.createRow(rownum++);
          cell = row.createCell(cellnum);
          cell.setCellStyle(style);
          cell.setCellValue(
              createHelper.createRichTextString("n/a").toString());
        }
        for (ConceptRefsetMember member : report.getInvalidInclusions()) {
          Logger.getLogger(getClass()).debug("  member = " + member);
          makeMemberRowHelper(member, wb, sheet, rownum++, cell.getCellStyle());
        }
        rownum++;

        // Invalid exclusions
        row = sheet.createRow(rownum++);
        cellnum = 0;
        cell = row.createCell(cellnum);
        cell.setCellStyle(style);
        cell.setCellValue(
            createHelper.createRichTextString("Invalid Exclusions").toString());

        if (report.getInvalidExclusions().size() > 0) {
          makeMemberHeaderHelper(wb, sheet, rownum++, cell.getCellStyle());
        } else {
          row = sheet.createRow(rownum++);
          cell = row.createCell(cellnum);
          cell.setCellStyle(style);
          cell.setCellValue(
              createHelper.createRichTextString("n/a").toString());
        }
        for (ConceptRefsetMember member : report.getInvalidExclusions()) {
          makeMemberRowHelper(member, wb, sheet, rownum++, cell.getCellStyle());
        }
        rownum++;

      }

      // Replacement Concepts
      row = sheet.createRow(rownum++);
      cellnum = 0;
      cell = row.createCell(cellnum);
      cell.setCellStyle(style);
      cell.setCellValue(createHelper
          .createRichTextString(
              "Inactive Concepts with their suggested Replacement Concepts")
          .toString());

      boolean inactiveHeaderWritten = false;
      for (ConceptRefsetMember member : membersInCommon) {
        Logger.getLogger(getClass()).debug("  member = " + member);

        if (!member.isConceptActive()) {
          // Create header rows first time only
          if (!inactiveHeaderWritten) {
            makeReplacementConceptHeaderHelper(wb, sheet, rownum++,
                cell.getCellStyle());
            inactiveHeaderWritten = true;
          }

          ConceptList conceptList = refsetService
              .getTerminologyHandler(member.getRefset().getProject(), headers)
              .getReplacementConcepts(member.getConceptId(),
                  migrationTerminology, migrationVersion);
          // If there are no suggestions
          if (conceptList.getObjects().size() == 0) {
            makeNoReplacementConceptAvailableHelper(member, wb, sheet, rownum++,
                cell.getCellStyle());

          }
          // If there are suggestions
          for (Concept c : conceptList.getObjects()) {
            appendReplacementConceptInfo(member, c, wb, sheet, rownum++,
                cell.getCellStyle());
          }
        }
      }
      for (ConceptRefsetMember member : report.getOldRegularMembers()) {

        if (!member.isConceptActive()
            && report.getNewRefset().getType() == Type.INTENSIONAL) {
          ConceptList conceptList = refsetService
              .getTerminologyHandler(member.getRefset().getProject(), headers)
              .getReplacementConcepts(member.getConceptId(),
                  migrationTerminology, migrationVersion);
          for (Concept c : conceptList.getObjects()) {
            if (!inactiveHeaderWritten) {
              makeReplacementConceptHeaderHelper(wb, sheet, rownum++,
                  cell.getCellStyle());
            }
            appendReplacementConceptInfo(member, c, wb, sheet, rownum++,
                cell.getCellStyle());
            inactiveHeaderWritten = true;
          }
        }
      }
      if (!inactiveHeaderWritten) {
        row = sheet.createRow(rownum++);
        cell = row.createCell(cellnum);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString("n/a").toString());
      }
      rownum++;

      // Members In Common
      row = sheet.createRow(rownum++);
      cellnum = 0;
      cell = row.createCell(cellnum);
      cell.setCellStyle(style);
      cell.setCellValue(
          createHelper.createRichTextString("Members in Common").toString());

      if (membersInCommon.size() > 0) {
        makeMemberHeaderHelper(wb, sheet, rownum++, cell.getCellStyle());
      } else {
        row = sheet.createRow(rownum++);
        cell = row.createCell(cellnum);
        cell.setCellStyle(style);
        cell.setCellValue(createHelper.createRichTextString("n/a").toString());
      }
      for (ConceptRefsetMember member : membersInCommon) {
        makeMemberRowHelper(member, wb, sheet, rownum++, cell.getCellStyle());
      }

      /*
       * for (int i = 0; i < 4; i++) { sheet.autoSizeColumn(i); }
       */
    } catch (Exception e) {
      throw new LocalException(e.getMessage(), e);
    }

  }

  private void appendReplacementConceptInfo(ConceptRefsetMember member,
    Concept c, Workbook wb, Sheet sheet, int rownum, CellStyle style)
    throws Exception {

    // given the reason code, look up the reason's name
    Concept reasonConcept = null;
    if (reasonMap.containsKey(c.getDefinitionStatusId())) {
      reasonConcept = reasonMap.get(c.getDefinitionStatusId());
    } else {
      reasonConcept = refsetService
          .getTerminologyHandler(member.getRefset().getProject(), headers)
          .findConceptsForQuery(
              c.getDefinitionStatusId().replace('_', ' ')
                  + " association reference set",
              migrationTerminology, migrationVersion, null)
          .getObjects().get(0);
      reasonMap.put(c.getDefinitionStatusId(), reasonConcept);
    }

    makeReplacementConceptHelper(member, c, reasonConcept, wb, sheet, rownum,
        style);

  }

  private void makeMemberRowHelper(ConceptRefsetMember member, Workbook wb,
    Sheet sheet, int rownum, CellStyle style) {
    CreationHelper createHelper = wb.getCreationHelper();

    Cell cell = null;
    int cellnum = 0;

    Row row = sheet.createRow(rownum);

    // UUID
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(
        createHelper.createRichTextString(member.getTerminologyId()));

    // Effective Time
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(
        createHelper.createRichTextString(member.getEffectiveTime() != null
            ? member.getEffectiveTime().toString() : ""));

    // Active
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(
        createHelper.createRichTextString(member.isActive() ? "1" : "0"));

    // Module Id
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString(member.getModuleId()));

    // Refset Id
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper
        .createRichTextString(member.getRefset().getTerminologyId()));

    // Referenced Component Id
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString(member.getConceptId()));

  }

  private void makeMemberHeaderHelper(Workbook wb, Sheet sheet, int rownum,
    CellStyle style) {
    CreationHelper createHelper = wb.getCreationHelper();

    Cell cell = null;
    int cellnum = 0;

    Row row = sheet.createRow(rownum);

    // UUID
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString("id"));

    // Effective Time
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString("effectiveTime"));

    // Active
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString("active"));

    // Module Id
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString("moduleId"));

    // Refset Id
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString("refsetId"));

    // Referenced Component Id
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(
        createHelper.createRichTextString("referencedComponentId"));

  }

  private void makeReplacementConceptHelper(ConceptRefsetMember member,
    Concept c, Concept reasonConcept, Workbook wb, Sheet sheet, int rownum,
    CellStyle style) {
    CreationHelper createHelper = wb.getCreationHelper();

    Cell cell = null;
    int cellnum = 0;

    Row row = sheet.createRow(rownum);

    // Inactive ConceptID
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString(member.getConceptId()));

    // Inactive Concept FSN
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(
        createHelper.createRichTextString(member.getConceptName()));

    // Reason
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(
        createHelper.createRichTextString(reasonConcept.getName()));

    // Suggested Replacement ConceptID(s)
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString(c.getTerminologyId()));

    // Suggested Replacement FSN(s)
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString(c.getName()));

  }

  private void makeNoReplacementConceptAvailableHelper(
    ConceptRefsetMember member, Workbook wb, Sheet sheet, int rownum,
    CellStyle style) {
    CreationHelper createHelper = wb.getCreationHelper();

    Cell cell = null;
    int cellnum = 0;

    Row row = sheet.createRow(rownum);

    // Inactive ConceptID
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString(member.getConceptId()));

    // Inactive Concept FSN
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(
        createHelper.createRichTextString(member.getConceptName()));

    // Reason
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue("NO VALID REPLACEMENTS FOUND");

    // Suggested Replacement ConceptID(s)
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue("");

    // Suggested Replacement FSN(s)
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue("");

  }

  private void makeReplacementConceptHeaderHelper(Workbook wb, Sheet sheet,
    int rownum, CellStyle style) {
    CreationHelper createHelper = wb.getCreationHelper();

    Cell cell = null;
    int cellnum = 0;

    Row row = sheet.createRow(rownum);

    // Inactive ConceptID
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString("Inactive ConceptID"));

    // Inactive Concept FSN
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(
        createHelper.createRichTextString("Inactive Concept FSN"));

    // Reason
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper.createRichTextString("Reason"));

    // Suggested Replacement ConceptID(s)
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(createHelper
        .createRichTextString("Suggested Replacement ConceptID(s)"));

    // Suggested Replacement FSN(s)
    cell = row.createCell(cellnum++);
    cell.setCellStyle(style);
    cell.setCellValue(
        createHelper.createRichTextString("Suggested Replacement FSN(s)"));

  }
}